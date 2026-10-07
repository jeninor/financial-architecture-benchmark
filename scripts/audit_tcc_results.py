#!/usr/bin/env python3
"""
Auditoria read-only dos resultados do TCC.

Objetivo:
- ler os artefatos já existentes no repositório;
- extrair métricas sem digitá-las manualmente;
- recalcular diferenças percentuais;
- cruzar resumos com evidências auxiliares;
- gerar tabelas Markdown, JSON auditável e manifesto SHA-256;
- falhar/avisar quando um dado não puder ser comprovado.

Uso:
    python3 audit_tcc_results.py \
      --repo-root /home/alunos/Documentos/Juan/usp/financial-architecture-benchmark \
      --out-dir tcc-audit

O script NÃO altera o repositório.
"""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import re
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Iterable


PRIMARY_RUNS = [
    "RH0003_claude_monolith",
    "RH0003_claude_microservices",
    "RC0003_codex_monolith",
    "RC0003_codex_microservices",
]

R0003_RUNS = [
    "R0003_monolith",
    "R0003_microservices",
]

EXPECTED_V12_PROTOCOL = "official_finance_architecture_provider_experiment_v1_2"
EXPECTED_R0003_PROTOCOL = "official_finance_architecture_experiment_v3"


@dataclass
class Check:
    status: str   # PASS / WARN / FAIL
    name: str
    detail: str


class Audit:
    def __init__(self) -> None:
        self.checks: list[Check] = []
        self.evidence: set[Path] = set()

    def add(self, status: str, name: str, detail: str) -> None:
        self.checks.append(Check(status, name, detail))

    def require(self, condition: bool, name: str, detail_ok: str, detail_fail: str) -> None:
        self.add("PASS" if condition else "FAIL", name, detail_ok if condition else detail_fail)

    def warn(self, name: str, detail: str) -> None:
        self.add("WARN", name, detail)

    def use(self, path: Path) -> None:
        self.evidence.add(path.resolve())

    @property
    def failed(self) -> bool:
        return any(c.status == "FAIL" for c in self.checks)


def sha256_file(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def read_json(path: Path, audit: Audit) -> dict[str, Any]:
    audit.use(path)
    with path.open("r", encoding="utf-8") as f:
        return json.load(f)


def read_text(path: Path, audit: Audit) -> str:
    audit.use(path)
    return path.read_text(encoding="utf-8", errors="replace")


def find_all(repo: Path, filename: str) -> list[Path]:
    ignore_parts = {".git", "target", "node_modules", ".idea", ".vscode"}
    out = []
    for p in repo.rglob(filename):
        if any(part in ignore_parts for part in p.parts):
            continue
        if p.is_file():
            out.append(p)
    return sorted(out)


def find_preferred(repo: Path, filename: str, audit: Audit, prefer_parts: Iterable[str] = ()) -> Path | None:
    candidates = find_all(repo, filename)
    if not candidates:
        audit.add("FAIL", f"arquivo {filename}", "não encontrado")
        return None

    preferred = [p for p in candidates if any(part.lower() in str(p).lower() for part in prefer_parts)]
    if len(preferred) == 1:
        p = preferred[0]
    elif len(candidates) == 1:
        p = candidates[0]
    else:
        audit.warn(
            f"arquivo ambíguo {filename}",
            "candidatos: " + "; ".join(str(p.relative_to(repo)) for p in candidates)
        )
        return None

    audit.use(p)
    return p


def discover_final_summaries(repo: Path, audit: Audit) -> dict[str, tuple[Path, dict[str, Any]]]:
    result = {}
    for p in find_all(repo, "FINAL_SUMMARY.json"):
        try:
            data = json.loads(p.read_text(encoding="utf-8"))
        except Exception as e:
            audit.warn("FINAL_SUMMARY ilegível", f"{p}: {e}")
            continue
        run_id = data.get("run_id")
        if run_id:
            if run_id in result:
                audit.warn("run_id duplicado", f"{run_id}: {result[run_id][0]} e {p}")
            result[run_id] = (p, data)
    return result


def num_pt(value: Any, decimals: int = 3) -> str:
    if isinstance(value, int):
        return str(value)
    if isinstance(value, float):
        s = f"{value:.{decimals}f}".rstrip("0").rstrip(".")
        return s.replace(".", ",")
    return str(value)


def pct(micro: float, mono: float) -> float:
    return (micro / mono - 1.0) * 100.0


def get_nested(d: dict[str, Any], path: str, default=None):
    cur: Any = d
    for key in path.split("."):
        if not isinstance(cur, dict) or key not in cur:
            return default
        cur = cur[key]
    return cur


def find_pass_total(obj: Any) -> list[tuple[int, int, str]]:
    """Busca recursivamente pares passed/total em qualquer acceptance.json."""
    out: list[tuple[int, int, str]] = []

    def rec(x: Any, path: str) -> None:
        if isinstance(x, dict):
            if "passed" in x and "total" in x:
                try:
                    out.append((int(x["passed"]), int(x["total"]), path or "$"))
                except Exception:
                    pass
            for k, v in x.items():
                rec(v, f"{path}.{k}" if path else str(k))
        elif isinstance(x, list):
            for i, v in enumerate(x):
                rec(v, f"{path}[{i}]")

    rec(obj, "")
    return out


def acceptance_from_file(path: Path, audit: Audit) -> tuple[int, int] | None:
    try:
        data = read_json(path, audit)
    except Exception as e:
        audit.add("FAIL", "acceptance JSON", f"{path}: {e}")
        return None
    pairs = find_pass_total(data)
    # Preferimos o contrato 12/12 quando houver mais de um resumo interno.
    exact = [(p, t, loc) for p, t, loc in pairs if t == 12]
    if exact:
        p, t, _ = exact[-1]
        return p, t
    if pairs:
        p, t, _ = pairs[-1]
        return p, t
    audit.add("FAIL", "acceptance JSON", f"não encontrei passed/total em {path}")
    return None


def find_latest_a1_acceptance(run_dir: Path) -> Path | None:
    candidates = sorted(run_dir.glob("a1_iteration_*/acceptance.json"))
    if not candidates:
        return None
    def n(p: Path) -> int:
        m = re.search(r"a1_iteration_(\d+)", str(p))
        return int(m.group(1)) if m else -1
    return max(candidates, key=n)


def find_a1_iter1_acceptance(run_dir: Path) -> Path | None:
    p = run_dir / "a1_iteration_01" / "acceptance.json"
    return p if p.exists() else None


def final_quality(summary: dict[str, Any]) -> dict[str, Any]:
    before = get_nested(summary, "analysis_before.quality", {}) or {}
    if get_nested(summary, "a3.termination_reason") == "SUCCESS":
        ba = summary.get("before_after") or {}
        q = ba.get("quality") or {}
        if q:
            out = {}
            for k, v in q.items():
                out[k] = v.get("after") if isinstance(v, dict) and "after" in v else v
            return out
    return before


def final_security(summary: dict[str, Any]) -> dict[str, Any]:
    before = get_nested(summary, "analysis_before.security", {}) or {}
    if get_nested(summary, "a3.termination_reason") == "SUCCESS":
        ba = summary.get("before_after") or {}
        sec = ba.get("security") or {}
        if sec:
            out = {}
            for k, v in sec.items():
                out[k] = v.get("after") if isinstance(v, dict) and "after" in v else v
            return out
    return before


def final_findings(summary: dict[str, Any]) -> int | None:
    if get_nested(summary, "a3.termination_reason") == "SUCCESS":
        ba = summary.get("before_after") or {}
        qf = (ba.get("quality") or {}).get("quality_findings")
        if isinstance(qf, dict) and "after" in qf:
            return int(qf["after"])
        fa = get_nested(summary, "a3.findings_after.total_findings")
        if fa is not None:
            return int(fa)
    v = get_nested(summary, "analysis_before.findings.total_findings")
    return int(v) if v is not None else None


def stage3_row(summary: dict[str, Any]) -> dict[str, Any]:
    q = final_quality(summary)
    s = final_security(summary)
    return {
        "run_id": summary.get("run_id"),
        "provider": summary.get("provider"),
        "architecture": summary.get("architecture"),
        "elapsed_seconds": summary.get("elapsed_seconds"),
        "provider_a1_seconds": get_nested(summary, "a1.provider_metrics.elapsed_seconds"),
        "a1_iterations": get_nested(summary, "a1.iterations_used"),
        "tool_calls": get_nested(summary, "a1.provider_metrics.tool_calls"),
        "failed_tool_calls": get_nested(summary, "a1.provider_metrics.failed_tool_calls"),
        "physical_java_loc": q.get("physical_java_loc"),
        "nloc": q.get("lizard_nloc"),
        "java_files": q.get("java_files"),
        "functions": q.get("functions"),
        "cc_total": q.get("cc_total"),
        "cc_average": q.get("cc_average"),
        "cc_max": q.get("cc_max"),
        "initial_findings": get_nested(summary, "analysis_before.findings.total_findings"),
        "final_findings": final_findings(summary),
        "a3": get_nested(summary, "a3.termination_reason"),
        "vulnerabilities": s.get("vulnerabilities"),
        "secrets": s.get("secrets"),
        "misconfigurations": s.get("misconfigurations"),
        "HIGH": s.get("HIGH"),
        "CRITICAL": s.get("CRITICAL"),
    }


def stage2_row(summary: dict[str, Any]) -> dict[str, Any]:
    q = get_nested(summary, "analysis_before.quality", {}) or {}
    s = get_nested(summary, "analysis_before.security", {}) or {}
    return {
        "run_id": summary.get("run_id"),
        "architecture": summary.get("architecture"),
        "elapsed_seconds": summary.get("elapsed_seconds"),
        "a1_iterations": get_nested(summary, "a1.iterations_used"),
        "physical_java_loc": q.get("physical_java_loc"),
        "nloc": q.get("lizard_nloc"),
        "java_files": q.get("java_files"),
        "functions": q.get("functions"),
        "cc_total": q.get("cc_total"),
        "cc_average": q.get("cc_average"),
        "cc_max": q.get("cc_max"),
        "quality_findings": q.get("quality_findings"),
        "vulnerabilities": s.get("vulnerabilities"),
        "secrets": s.get("secrets"),
        "misconfigurations": s.get("misconfigurations"),
        "HIGH": s.get("HIGH"),
        "CRITICAL": s.get("CRITICAL"),
    }


def parse_md_row(text: str, label: str) -> tuple[str, str] | None:
    # Ex.: | Physical Java LOC | 1.274 | 2.206 | +73,2% |
    pat = re.compile(
        r"^\|\s*" + re.escape(label) + r"\s*\|\s*([^|]+?)\s*\|\s*([^|]+?)\s*\|",
        re.I | re.M
    )
    m = pat.search(text)
    if not m:
        return None
    return m.group(1).strip(), m.group(2).strip()


def parse_pt_number(s: str) -> float:
    s = s.strip().replace("%", "").replace("−", "-")
    # 1.274 significa milhar no material do TCC; 1,61 decimal.
    if re.fullmatch(r"\d{1,3}(\.\d{3})+", s):
        s = s.replace(".", "")
    else:
        s = s.replace(".", "").replace(",", ".") if "," in s else s
    return float(s)


def stage1_from_master(repo: Path, audit: Audit) -> dict[str, dict[str, Any]] | None:
    p = find_preferred(repo, "master-results.md", audit, prefer_parts=("legacy-chatpt", "legacy"))
    if p is None:
        return None
    text = read_text(p, audit)

    labels = {
        "physical_java_loc": "Java LOC físico",
        "java_files": "Arquivos Java",
        "nloc": "Lizard NLOC",
        "functions": "Métodos/funções",
        "cc_total": "CC total",
        "cc_average": "CC médio",
        "cc_max": "CC máximo",
    }
    mono, micro = {}, {}
    for key, label in labels.items():
        row = parse_md_row(text, label)
        if not row:
            audit.add("FAIL", f"Etapa 1 {label}", f"linha não encontrada em {p}")
            continue
        a, b = row
        va, vb = parse_pt_number(a), parse_pt_number(b)
        mono[key] = int(va) if key not in ("cc_average",) else va
        micro[key] = int(vb) if key not in ("cc_average",) else vb

    # Validação funcional da mesma tabela.
    row = parse_md_row(text, "Testes aprovados")
    if row:
        mono["acceptance"] = row[0]
        micro["acceptance"] = row[1]
    else:
        # master-results pode ter tabela separada de validação funcional
        m = re.search(r"\|\s*Testes aprovados\s*\|\s*(\d+)\s*\|\s*(\d+)\s*\|", text, re.I)
        if m:
            mono["acceptance"] = f"{m.group(1)}/12"
            micro["acceptance"] = f"{m.group(2)}/12"

    return {"monolith": mono, "microservices": micro, "source": str(p.relative_to(repo))}


def verify_stage1_lizard_csv(repo: Path, stage1: dict[str, Any], audit: Audit) -> None:
    """
    O CSV do Lizard é por função.
    Podemos auditar diretamente:
      - número de funções = número de linhas;
      - CC total = soma da coluna CCN;
      - CC média = média da coluna CCN;
      - CC máxima = máximo da coluna CCN.
    NÃO usamos soma de NLOC por função como NLOC do projeto.
    """
    for arch, stem in [("monolith", "monolith-lizard.csv"), ("microservices", "microservices-lizard.csv")]:
        candidates = find_all(repo, stem)
        if not candidates:
            audit.warn("Lizard bruto etapa 1", f"{stem} não encontrado; validação cruzada omitida")
            continue
        preferred = [p for p in candidates if "legacy" in str(p).lower()]
        p = preferred[0] if len(preferred) == 1 else (candidates[0] if len(candidates) == 1 else None)
        if p is None:
            audit.warn("Lizard bruto etapa 1", f"{stem} ambíguo: " + "; ".join(map(str, candidates)))
            continue

        audit.use(p)
        rows = []
        with p.open("r", encoding="utf-8", errors="replace", newline="") as f:
            for row in csv.reader(f):
                if not row:
                    continue
                try:
                    nloc_func = float(row[0])
                    ccn = float(row[1])
                except Exception:
                    continue
                rows.append((nloc_func, ccn))

        expected = stage1[arch]
        checks = {
            "functions": len(rows),
            "cc_total": int(sum(cc for _, cc in rows)),
            "cc_average": (sum(cc for _, cc in rows) / len(rows)) if rows else None,
            "cc_max": int(max((cc for _, cc in rows), default=0)),
        }
        for k, actual in checks.items():
            exp = expected.get(k)
            if exp is None:
                continue
            ok = abs(float(actual) - float(exp)) < (0.005 if k == "cc_average" else 1e-9)
            audit.require(
                ok,
                f"Etapa 1 {arch} {k} vs Lizard CSV",
                f"confirmado: {actual} ({p.relative_to(repo)})",
                f"divergência: tabela={exp}, CSV={actual} ({p.relative_to(repo)})",
            )


def make_markdown_table(headers: list[str], rows: list[list[Any]]) -> str:
    lines = [
        "| " + " | ".join(headers) + " |",
        "|" + "|".join(["---"] + ["---:"] * (len(headers)-1)) + "|",
    ]
    for row in rows:
        lines.append("| " + " | ".join(str(x) for x in row) + " |")
    return "\n".join(lines)


def git_info(repo: Path) -> dict[str, Any]:
    def run(*args: str) -> str | None:
        try:
            cp = subprocess.run(
                ["git", "-C", str(repo), *args],
                check=True, capture_output=True, text=True
            )
            return cp.stdout.strip()
        except Exception:
            return None

    return {
        "head": run("rev-parse", "HEAD"),
        "branch": run("rev-parse", "--abbrev-ref", "HEAD"),
        "status_porcelain": run("status", "--porcelain"),
    }


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--repo-root", required=True, type=Path)
    ap.add_argument("--out-dir", default="tcc-audit", type=Path)
    args = ap.parse_args()

    repo = args.repo_root.resolve()
    out = args.out_dir
    if not out.is_absolute():
        out = (repo / out).resolve()
    out.mkdir(parents=True, exist_ok=True)

    audit = Audit()
    summaries = discover_final_summaries(repo, audit)

    # ---------- Etapa 1 ----------
    stage1 = stage1_from_master(repo, audit)
    if stage1:
        verify_stage1_lizard_csv(repo, stage1, audit)

        # Evidência independente dos 3 problemas documentados.
        dev = find_preferred(repo, "development-errors.md", audit, prefer_parts=("legacy-chatpt", "legacy"))
        if dev:
            txt = read_text(dev, audit)
            errors = len(re.findall(r"^##\s+E\d+\b", txt, re.M))
            audit.require(
                errors == 3,
                "Etapa 1 problemas documentados",
                f"3 problemas confirmados em {dev.relative_to(repo)}",
                f"esperado 3 no capítulo; arquivo contém {errors}",
            )

    # ---------- Etapa 2 ----------
    stage2 = {}
    for run_id in R0003_RUNS:
        if run_id not in summaries:
            audit.add("FAIL", run_id, "FINAL_SUMMARY.json não encontrado")
            continue
        path, data = summaries[run_id]
        audit.use(path)
        audit.require(
            data.get("protocol_name") == EXPECTED_R0003_PROTOCOL,
            f"{run_id} protocolo",
            f"{EXPECTED_R0003_PROTOCOL}",
            f"obtido: {data.get('protocol_name')}",
        )
        audit.require(
            data.get("success") is True,
            f"{run_id} success",
            "true",
            f"obtido: {data.get('success')}",
        )
        stage2[run_id] = stage2_row(data)

        acc = find_latest_a1_acceptance(path.parent)
        if acc:
            val = acceptance_from_file(acc, audit)
            if val:
                audit.require(
                    val == (12, 12),
                    f"{run_id} acceptance final",
                    f"12/12 em {acc.relative_to(repo)}",
                    f"{val[0]}/{val[1]} em {acc.relative_to(repo)}",
                )
        else:
            audit.warn(f"{run_id} acceptance final", "acceptance.json não encontrado")

    # ---------- Etapa 3 ----------
    stage3 = {}
    protocol_shas, lock_shas = set(), set()
    for run_id in PRIMARY_RUNS:
        if run_id not in summaries:
            audit.add("FAIL", run_id, "FINAL_SUMMARY.json não encontrado")
            continue

        path, data = summaries[run_id]
        audit.use(path)
        protocol_shas.add(data.get("protocol_sha256"))
        lock_shas.add(data.get("environment_lock_sha256"))

        audit.require(
            data.get("protocol_name") == EXPECTED_V12_PROTOCOL,
            f"{run_id} protocolo",
            EXPECTED_V12_PROTOCOL,
            f"obtido: {data.get('protocol_name')}",
        )
        audit.require(
            data.get("success") is True and data.get("experimental_status") == "VALID_SUCCESS",
            f"{run_id} status",
            "VALID_SUCCESS",
            f"success={data.get('success')} status={data.get('experimental_status')}",
        )
        audit.require(
            data.get("primary_analysis_eligible") is True,
            f"{run_id} elegibilidade",
            "primary_analysis_eligible=true",
            f"obtido: {data.get('primary_analysis_eligible')}",
        )

        stage3[run_id] = stage3_row(data)

        acc = find_a1_iter1_acceptance(path.parent)
        if acc:
            val = acceptance_from_file(acc, audit)
            if val:
                stage3[run_id]["acceptance_a1"] = f"{val[0]}/{val[1]}"
                audit.require(
                    val == (12, 12),
                    f"{run_id} acceptance A1",
                    f"12/12 em {acc.relative_to(repo)}",
                    f"{val[0]}/{val[1]} em {acc.relative_to(repo)}",
                )
        else:
            stage3[run_id]["acceptance_a1"] = None
            audit.add("FAIL", f"{run_id} acceptance A1", "a1_iteration_01/acceptance.json não encontrado")

    audit.require(
        len({x for x in protocol_shas if x}) == 1,
        "v1.2 protocol SHA comum",
        f"SHA único: {next(iter(protocol_shas)) if protocol_shas else None}",
        f"SHAs divergentes: {protocol_shas}",
    )
    audit.require(
        len({x for x in lock_shas if x}) == 1,
        "v1.2 environment lock SHA comum",
        f"SHA único: {next(iter(lock_shas)) if lock_shas else None}",
        f"SHAs divergentes: {lock_shas}",
    )

    # Claude Micro A3: 7/12 -> 12/12 e before/after.
    cm_id = "RH0003_claude_microservices"
    if cm_id in summaries:
        cm_path, cm = summaries[cm_id]
        for attempt, expected in [(1, (7, 12)), (2, (12, 12))]:
            p = cm_path.parent / "a3" / f"attempt_{attempt:02d}" / "acceptance.json"
            if p.exists():
                val = acceptance_from_file(p, audit)
                if val:
                    audit.require(
                        val == expected,
                        f"Claude Micro A3 attempt {attempt}",
                        f"{expected[0]}/{expected[1]} confirmado",
                        f"esperado {expected[0]}/{expected[1]}, obtido {val[0]}/{val[1]}",
                    )
            else:
                audit.add("FAIL", f"Claude Micro A3 attempt {attempt}", f"{p.relative_to(repo)} ausente")

        ba_path = cm_path.parent / "a3" / "before_after.json"
        if ba_path.exists():
            ba = read_json(ba_path, audit)
            ccmax = get_nested(ba, "quality.cc_max")
            findings = get_nested(ba, "quality.quality_findings")
            if isinstance(ccmax, dict):
                audit.require(
                    ccmax.get("before") == 12 and ccmax.get("after") == 7,
                    "Claude Micro CC max A3",
                    "12 -> 7 confirmado",
                    f"obtido: {ccmax}",
                )
            if isinstance(findings, dict):
                audit.require(
                    findings.get("before") == 1 and findings.get("after") == 0,
                    "Claude Micro findings A3",
                    "1 -> 0 confirmado",
                    f"obtido: {findings}",
                )
        else:
            audit.add("FAIL", "Claude Micro before_after.json", "arquivo ausente")

    # Segurança primária: zeros no escopo registrado.
    for run_id, row in stage3.items():
        vals = [row.get(k) for k in ("vulnerabilities", "secrets", "misconfigurations", "HIGH", "CRITICAL")]
        audit.require(
            all(v == 0 for v in vals),
            f"{run_id} segurança",
            "todos os indicadores registrados = 0",
            f"valores: {vals}",
        )

    # ---------- Tabelas derivadas ----------
    # Etapa 3
    order = PRIMARY_RUNS
    display = {
        "RH0003_claude_monolith": "Claude Mono",
        "RH0003_claude_microservices": "Claude Micro",
        "RC0003_codex_monolith": "Codex Mono",
        "RC0003_codex_microservices": "Codex Micro",
    }

    metrics = [
        ("Acceptance A1", "acceptance_a1", None),
        ("Tempo total (s)", "elapsed_seconds", 3),
        ("Tempo provider A1 (s)", "provider_a1_seconds", 3),
        ("Iterações A1", "a1_iterations", None),
        ("Tool calls A1", "tool_calls", None),
        ("Tool calls com falha", "failed_tool_calls", None),
        ("Physical Java LOC final", "physical_java_loc", None),
        ("NLOC final", "nloc", None),
        ("Arquivos Java", "java_files", None),
        ("Funções/métodos", "functions", None),
        ("CC total", "cc_total", None),
        ("CC média", "cc_average", 3),
        ("CC máxima", "cc_max", None),
        ("Findings A2 iniciais", "initial_findings", None),
        ("Findings finais", "final_findings", None),
        ("A3", "a3", None),
    ]

    table3_rows = []
    for label, key, dec in metrics:
        row = [label]
        for rid in order:
            v = stage3.get(rid, {}).get(key)
            if isinstance(v, float) and dec is not None:
                row.append(num_pt(v, dec))
            else:
                row.append("" if v is None else str(v))
        table3_rows.append(row)

    table3 = make_markdown_table(
        ["Métrica"] + [display[rid] for rid in order],
        table3_rows
    )

    # Comparação transversal
    conditions = []
    if stage1:
        conditions.append(("ChatGPT direto", stage1["monolith"], stage1["microservices"]))
    if "R0003_monolith" in stage2 and "R0003_microservices" in stage2:
        conditions.append(("Claude R0003", stage2["R0003_monolith"], stage2["R0003_microservices"]))
    if "RH0003_claude_monolith" in stage3 and "RH0003_claude_microservices" in stage3:
        conditions.append(("Claude v1.2", stage3["RH0003_claude_monolith"], stage3["RH0003_claude_microservices"]))
    if "RC0003_codex_monolith" in stage3 and "RC0003_codex_microservices" in stage3:
        conditions.append(("Codex v1.2", stage3["RC0003_codex_monolith"], stage3["RC0003_codex_microservices"]))

    delta_rows = []
    ccavg_rows = []
    for name, mono, micro in conditions:
        delta_rows.append([
            name,
            f"{pct(float(micro['physical_java_loc']), float(mono['physical_java_loc'])):+.1f}%".replace(".", ","),
            f"{pct(float(micro['nloc']), float(mono['nloc'])):+.1f}%".replace(".", ","),
            f"{pct(float(micro['java_files']), float(mono['java_files'])):+.1f}%".replace(".", ","),
            f"{pct(float(micro['functions']), float(mono['functions'])):+.1f}%".replace(".", ","),
            f"{pct(float(micro['cc_total']), float(mono['cc_total'])):+.1f}%".replace(".", ","),
        ])
        ccavg_rows.append([
            name,
            num_pt(float(mono["cc_average"]), 3),
            num_pt(float(micro["cc_average"]), 3),
        ])

    delta_table = make_markdown_table(
        ["Condição", "Δ LOC Micro", "Δ NLOC Micro", "Δ arquivos", "Δ funções", "Δ CC total"],
        delta_rows,
    )
    ccavg_table = make_markdown_table(
        ["Condição", "CC média Mono", "CC média Micro"],
        ccavg_rows,
    )

    # ---------- Outputs ----------
    audit_data = {
        "git": git_info(repo),
        "stage1": stage1,
        "stage2": stage2,
        "stage3": stage3,
        "checks": [c.__dict__ for c in audit.checks],
    }
    (out / "audit_data.json").write_text(
        json.dumps(audit_data, indent=2, ensure_ascii=False),
        encoding="utf-8"
    )

    tables_md = (
        "# Tabelas geradas automaticamente\n\n"
        "## Protocolo v1.2 — execuções elegíveis\n\n"
        + table3
        + "\n\n## Comparação transversal — diferenças percentuais\n\n"
        + delta_table
        + "\n\n## Complexidade ciclomática média\n\n"
        + ccavg_table
        + "\n"
    )
    (out / "audit_tables.md").write_text(tables_md, encoding="utf-8")

    # Manifesto de evidências
    manifest_rows = []
    for p in sorted(audit.evidence):
        if not p.exists():
            continue
        try:
            rel = str(p.relative_to(repo))
        except ValueError:
            rel = str(p)
        manifest_rows.append({
            "path": rel,
            "bytes": p.stat().st_size,
            "sha256": sha256_file(p),
        })
    with (out / "evidence_manifest.csv").open("w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=["path", "bytes", "sha256"])
        w.writeheader()
        w.writerows(manifest_rows)

    pass_n = sum(c.status == "PASS" for c in audit.checks)
    warn_n = sum(c.status == "WARN" for c in audit.checks)
    fail_n = sum(c.status == "FAIL" for c in audit.checks)

    report = [
        "# Relatório de auditoria dos resultados do TCC",
        "",
        f"- PASS: {pass_n}",
        f"- WARN: {warn_n}",
        f"- FAIL: {fail_n}",
        f"- Git HEAD: `{audit_data['git'].get('head')}`",
        f"- Git branch: `{audit_data['git'].get('branch')}`",
        "",
        "## Checks",
        "",
        "| Status | Verificação | Detalhe |",
        "|---|---|---|",
    ]
    for c in audit.checks:
        detail = c.detail.replace("|", r"\|").replace("\n", " ")
        name = c.name.replace("|", r"\|")
        report.append(f"| {c.status} | {name} | {detail} |")

    report += [
        "",
        "## Regra de uso",
        "",
        "Os números do capítulo de Resultados devem ser copiados de `audit_tables.md` "
        "ou de `audit_data.json`, e não digitados a partir de memória/conversa.",
        "",
        "Se houver qualquer `FAIL`, o conjunto não deve ser considerado auditado até "
        "que a divergência seja explicada.",
    ]
    (out / "audit_report.md").write_text("\n".join(report) + "\n", encoding="utf-8")

    print(f"Audit dir : {out}")
    print(f"PASS      : {pass_n}")
    print(f"WARN      : {warn_n}")
    print(f"FAIL      : {fail_n}")
    print(f"Tables    : {out / 'audit_tables.md'}")
    print(f"Data      : {out / 'audit_data.json'}")
    print(f"Manifest  : {out / 'evidence_manifest.csv'}")
    print(f"Report    : {out / 'audit_report.md'}")

    return 2 if audit.failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
