#!/usr/bin/env python3
"""
Auditoria read-only v2 dos resultados do TCC.

Objetivos
---------
1) Extrair números diretamente dos artefatos preservados no repositório.
2) Cruzar fontes independentes quando possível.
3) Recalcular diferenças percentuais.
4) Auditar as execuções Antigravity sem incluí-las na análise primária.
5) Gerar automaticamente as 7 tabelas principais do Capítulo 4.
6) Gerar manifesto SHA-256 de todas as evidências efetivamente usadas.
7) Falhar quando um dado essencial estiver ausente ou contraditório.

Uso
---
python3 scripts/audit_tcc_results_v2.py \
  --repo-root /home/alunos/Documentos/Juan/usp/financial-architecture-benchmark \
  --out-dir tcc-audit-v2

Regra:
- NÃO altera o repositório.
- Os números do Capítulo 4 devem sair de audit_tables.md/audit_data.json.
"""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import re
import subprocess
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

ANTIGRAVITY_RUNS = [
    "RA0003_antigravity_monolith",
    "RA0003_antigravity_microservices",
]

EXPECTED_V12_PROTOCOL = "official_finance_architecture_provider_experiment_v1_2"
EXPECTED_R0003_PROTOCOL = "official_finance_architecture_experiment_v3"


@dataclass
class Check:
    status: str
    name: str
    detail: str


class Audit:
    def __init__(self) -> None:
        self.checks: list[Check] = []
        self.evidence: set[Path] = set()

    def add(self, status: str, name: str, detail: str) -> None:
        self.checks.append(Check(status, name, detail))

    def require(self, condition: bool, name: str, ok: str, fail: str) -> None:
        self.add("PASS" if condition else "FAIL", name, ok if condition else fail)

    def warn(self, name: str, detail: str) -> None:
        self.add("WARN", name, detail)

    def use(self, path: Path) -> None:
        try:
            self.evidence.add(path.resolve())
        except Exception:
            pass

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
    ignored = {".git", "target", "node_modules", ".idea", ".vscode", "tcc-audit", "tcc-audit-v2"}
    out: list[Path] = []
    for p in repo.rglob(filename):
        if any(part in ignored for part in p.parts):
            continue
        if p.is_file():
            out.append(p)
    return sorted(out)


def pick_unique(
    repo: Path,
    filename: str,
    audit: Audit,
    prefer_parts: Iterable[str] = (),
    required: bool = True,
) -> Path | None:
    candidates = find_all(repo, filename)
    preferred = [p for p in candidates if any(x.lower() in str(p).lower() for x in prefer_parts)]

    if len(preferred) == 1:
        p = preferred[0]
    elif len(candidates) == 1:
        p = candidates[0]
    elif len(candidates) == 0:
        if required:
            audit.add("FAIL", f"arquivo {filename}", "não encontrado")
        else:
            audit.warn(f"arquivo opcional {filename}", "não encontrado")
        return None
    else:
        msg = "; ".join(str(p.relative_to(repo)) for p in candidates)
        if required:
            audit.add("FAIL", f"arquivo ambíguo {filename}", msg)
        else:
            audit.warn(f"arquivo ambíguo {filename}", msg)
        return None

    audit.use(p)
    return p


def discover_final_summaries(repo: Path, audit: Audit) -> dict[str, tuple[Path, dict[str, Any]]]:
    result: dict[str, tuple[Path, dict[str, Any]]] = {}
    for p in find_all(repo, "FINAL_SUMMARY.json"):
        try:
            data = json.loads(p.read_text(encoding="utf-8"))
        except Exception as e:
            audit.warn("FINAL_SUMMARY ilegível", f"{p}: {e}")
            continue
        run_id = data.get("run_id")
        if not run_id:
            continue
        if run_id in result:
            audit.warn("run_id duplicado", f"{run_id}: {result[run_id][0]} e {p}")
        result[run_id] = (p, data)
    return result


def nested(d: dict[str, Any], path: str, default=None):
    cur: Any = d
    for key in path.split("."):
        if not isinstance(cur, dict) or key not in cur:
            return default
        cur = cur[key]
    return cur


def pct(micro: float, mono: float) -> float:
    return (micro / mono - 1.0) * 100.0


def fmt_num(v: Any, decimals: int = 3) -> str:
    if v is None:
        return ""
    if isinstance(v, bool):
        return "true" if v else "false"
    if isinstance(v, int):
        return str(v)
    if isinstance(v, float):
        s = f"{v:.{decimals}f}".rstrip("0").rstrip(".")
        return s.replace(".", ",")
    return str(v)


def fmt_pct(v: float) -> str:
    return f"{v:+.1f}%".replace(".", ",")


def make_table(headers: list[str], rows: list[list[Any]], right_from: int = 1) -> str:
    sep = []
    for i in range(len(headers)):
        sep.append("---:" if i >= right_from else "---")
    out = [
        "| " + " | ".join(headers) + " |",
        "|" + "|".join(sep) + "|",
    ]
    for row in rows:
        out.append("| " + " | ".join(str(x) for x in row) + " |")
    return "\n".join(out)


def parse_pt_number(s: str) -> float:
    s = s.strip().replace("%", "").replace("−", "-")
    if re.fullmatch(r"\d{1,3}(\.\d{3})+", s):
        s = s.replace(".", "")
    elif "," in s:
        s = s.replace(".", "").replace(",", ".")
    return float(s)


def parse_md_row(text: str, label: str) -> tuple[str, str] | None:
    pat = re.compile(
        r"^\|\s*" + re.escape(label) + r"\s*\|\s*([^|]+?)\s*\|\s*([^|]+?)\s*\|",
        re.I | re.M,
    )
    m = pat.search(text)
    return (m.group(1).strip(), m.group(2).strip()) if m else None


def stage1_from_master(repo: Path, audit: Audit) -> dict[str, Any] | None:
    p = pick_unique(repo, "master-results.md", audit, prefer_parts=("legacy-chatpt", "legacy"))
    if not p:
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
    mono: dict[str, Any] = {}
    micro: dict[str, Any] = {}
    for key, label in labels.items():
        row = parse_md_row(text, label)
        if not row:
            audit.add("FAIL", f"Etapa 1 {label}", f"não encontrado em {p.relative_to(repo)}")
            continue
        va, vb = parse_pt_number(row[0]), parse_pt_number(row[1])
        mono[key] = va if key == "cc_average" else int(va)
        micro[key] = vb if key == "cc_average" else int(vb)

    # Acceptance: tabela de validação funcional separada.
    m = re.search(
        r"^\|\s*Testes aprovados\s*\|\s*(\d+)\s*\|\s*(\d+)\s*\|",
        text, re.I | re.M
    )
    if m:
        mono["acceptance"] = f"{m.group(1)}/12"
        micro["acceptance"] = f"{m.group(2)}/12"
    else:
        audit.add("FAIL", "Etapa 1 acceptance em master-results", "linha 'Testes aprovados' não encontrada")

    return {
        "monolith": mono,
        "microservices": micro,
        "source": str(p.relative_to(repo)),
    }


def parse_lizard_csv(path: Path, audit: Audit) -> dict[str, float]:
    audit.use(path)
    rows: list[tuple[float, float]] = []
    with path.open("r", encoding="utf-8", errors="replace", newline="") as f:
        for row in csv.reader(f):
            if not row:
                continue
            try:
                nloc = float(row[0])
                ccn = float(row[1])
            except Exception:
                continue
            rows.append((nloc, ccn))

    if not rows:
        return {"functions": 0, "cc_total": 0, "cc_average": 0, "cc_max": 0}

    return {
        "functions": len(rows),
        "cc_total": sum(x[1] for x in rows),
        "cc_average": sum(x[1] for x in rows) / len(rows),
        "cc_max": max(x[1] for x in rows),
    }


def audit_stage1(repo: Path, stage1: dict[str, Any], audit: Audit) -> None:
    # 1) Lizard bruto
    for arch, filename in [
        ("monolith", "monolith-lizard.csv"),
        ("microservices", "microservices-lizard.csv"),
    ]:
        candidates = find_all(repo, filename)
        candidates = [p for p in candidates if "legacy-chatpt" in str(p)]
        if len(candidates) != 1:
            audit.add("FAIL", f"Etapa 1 {filename}", f"esperado 1 arquivo legacy-chatpt, encontrados {len(candidates)}")
            continue
        p = candidates[0]
        raw = parse_lizard_csv(p, audit)
        expected = stage1[arch]
        for key in ("functions", "cc_total", "cc_average", "cc_max"):
            tol = 0.005 if key == "cc_average" else 1e-9
            ok = abs(float(raw[key]) - float(expected[key])) <= tol
            audit.require(
                ok,
                f"Etapa 1 {arch} {key} vs Lizard CSV",
                f"confirmado: {raw[key]} ({p.relative_to(repo)})",
                f"master={expected[key]} CSV={raw[key]} ({p.relative_to(repo)})",
            )

    # 2) Baselines independentes para LOC/arquivos.
    for arch, filename in [
        ("monolith", "monolith-baseline.md"),
        ("microservices", "microservices-baseline.md"),
    ]:
        p = pick_unique(repo, filename, audit, prefer_parts=("legacy-chatpt", "legacy"))
        if not p:
            continue
        txt = read_text(p, audit)
        m_files = re.search(r"Production Java files:\s*(\d+)", txt, re.I)
        m_loc = re.search(r"Production physical Java LOC:\s*(\d+)", txt, re.I)
        if m_files:
            val = int(m_files.group(1))
            audit.require(
                val == stage1[arch]["java_files"],
                f"Etapa 1 {arch} Java files vs baseline",
                f"{val} confirmado",
                f"master={stage1[arch]['java_files']} baseline={val}",
            )
        else:
            audit.add("FAIL", f"Etapa 1 {arch} Java files baseline", "campo ausente")
        if m_loc:
            val = int(m_loc.group(1))
            audit.require(
                val == stage1[arch]["physical_java_loc"],
                f"Etapa 1 {arch} LOC vs baseline",
                f"{val} confirmado",
                f"master={stage1[arch]['physical_java_loc']} baseline={val}",
            )
        else:
            audit.add("FAIL", f"Etapa 1 {arch} LOC baseline", "campo ausente")

    # 3) Acceptance independente via logs de teste.
    patterns = [
        ("monolith", ("monolith-test-results", "monolith-verify-results")),
        ("microservices", ("microservices-e2e-test-results",)),
    ]
    for arch, needles in patterns:
        candidates = []
        for p in repo.rglob("*"):
            if not p.is_file():
                continue
            name = p.name.lower()
            if any(n in name for n in needles):
                candidates.append(p)
        if not candidates:
            audit.add("FAIL", f"Etapa 1 {arch} test log", "arquivo de teste não encontrado")
            continue

        confirmed = False
        for p in sorted(candidates):
            txt = read_text(p, audit)
            if re.search(r"Tests run:\s*12,\s*Failures:\s*0,\s*Errors:\s*0", txt, re.I):
                confirmed = True
                audit.add("PASS", f"Etapa 1 {arch} acceptance independente", f"12/12 confirmado em {p.relative_to(repo)}")
                break
        if not confirmed:
            audit.add("FAIL", f"Etapa 1 {arch} acceptance independente", "nenhum log confirmou 12 testes, 0 falhas, 0 erros")

    # 4) Problemas documentados.
    p = pick_unique(repo, "development-errors.md", audit, prefer_parts=("legacy-chatpt", "legacy"))
    if p:
        txt = read_text(p, audit)
        n = len(re.findall(r"^##\s+E\d+\b", txt, re.M))
        audit.require(
            n == 3,
            "Etapa 1 problemas documentados",
            f"3 problemas confirmados em {p.relative_to(repo)}",
            f"esperado 3, obtido {n}",
        )


def find_pass_total(obj: Any) -> list[tuple[int, int]]:
    out: list[tuple[int, int]] = []
    def rec(x: Any) -> None:
        if isinstance(x, dict):
            if "passed" in x and "total" in x:
                try:
                    out.append((int(x["passed"]), int(x["total"])))
                except Exception:
                    pass
            for v in x.values():
                rec(v)
        elif isinstance(x, list):
            for v in x:
                rec(v)
    rec(obj)
    return out


def acceptance_value(path: Path, audit: Audit) -> tuple[int, int] | None:
    data = read_json(path, audit)
    pairs = find_pass_total(data)
    exact = [x for x in pairs if x[1] == 12]
    return exact[-1] if exact else (pairs[-1] if pairs else None)


def latest_a1_acceptance(run_dir: Path) -> Path | None:
    xs = sorted(run_dir.glob("a1_iteration_*/acceptance.json"))
    if not xs:
        return None
    def n(p: Path):
        m = re.search(r"a1_iteration_(\d+)", str(p))
        return int(m.group(1)) if m else -1
    return max(xs, key=n)


def final_quality(summary: dict[str, Any]) -> dict[str, Any]:
    before = nested(summary, "analysis_before.quality", {}) or {}
    if nested(summary, "a3.termination_reason") == "SUCCESS":
        ba = summary.get("before_after") or {}
        q = ba.get("quality") or {}
        if q:
            return {
                k: (v.get("after") if isinstance(v, dict) and "after" in v else v)
                for k, v in q.items()
            }
    return before


def final_security(summary: dict[str, Any]) -> dict[str, Any]:
    before = nested(summary, "analysis_before.security", {}) or {}
    if nested(summary, "a3.termination_reason") == "SUCCESS":
        ba = summary.get("before_after") or {}
        sec = ba.get("security") or {}
        if sec:
            return {
                k: (v.get("after") if isinstance(v, dict) and "after" in v else v)
                for k, v in sec.items()
            }
    return before


def final_findings(summary: dict[str, Any]) -> int | None:
    if nested(summary, "a3.termination_reason") == "SUCCESS":
        qf = nested(summary, "before_after.quality.quality_findings")
        if isinstance(qf, dict) and "after" in qf:
            return int(qf["after"])
        fa = nested(summary, "a3.findings_after.total_findings")
        if fa is not None:
            return int(fa)
    x = nested(summary, "analysis_before.findings.total_findings")
    return int(x) if x is not None else None


def stage2_row(s: dict[str, Any]) -> dict[str, Any]:
    q = nested(s, "analysis_before.quality", {}) or {}
    sec = nested(s, "analysis_before.security", {}) or {}
    return {
        "run_id": s.get("run_id"),
        "architecture": s.get("architecture"),
        "elapsed_seconds": s.get("elapsed_seconds"),
        "a1_iterations": nested(s, "a1.iterations_used"),
        "physical_java_loc": q.get("physical_java_loc"),
        "nloc": q.get("lizard_nloc"),
        "java_files": q.get("java_files"),
        "functions": q.get("functions"),
        "cc_total": q.get("cc_total"),
        "cc_average": q.get("cc_average"),
        "cc_max": q.get("cc_max"),
        "quality_findings": q.get("quality_findings"),
        "vulnerabilities": sec.get("vulnerabilities"),
        "secrets": sec.get("secrets"),
        "misconfigurations": sec.get("misconfigurations"),
        "HIGH": sec.get("HIGH"),
        "CRITICAL": sec.get("CRITICAL"),
    }


def stage3_row(s: dict[str, Any]) -> dict[str, Any]:
    q = final_quality(s)
    sec = final_security(s)
    return {
        "run_id": s.get("run_id"),
        "provider": s.get("provider"),
        "architecture": s.get("architecture"),
        "elapsed_seconds": s.get("elapsed_seconds"),
        "provider_a1_seconds": nested(s, "a1.provider_metrics.elapsed_seconds"),
        "a1_iterations": nested(s, "a1.iterations_used"),
        "tool_calls": nested(s, "a1.provider_metrics.tool_calls"),
        "failed_tool_calls": nested(s, "a1.provider_metrics.failed_tool_calls"),
        "physical_java_loc": q.get("physical_java_loc"),
        "nloc": q.get("lizard_nloc"),
        "java_files": q.get("java_files"),
        "functions": q.get("functions"),
        "cc_total": q.get("cc_total"),
        "cc_average": q.get("cc_average"),
        "cc_max": q.get("cc_max"),
        "initial_findings": nested(s, "analysis_before.findings.total_findings"),
        "final_findings": final_findings(s),
        "a3": nested(s, "a3.termination_reason"),
        "vulnerabilities": sec.get("vulnerabilities"),
        "secrets": sec.get("secrets"),
        "misconfigurations": sec.get("misconfigurations"),
        "HIGH": sec.get("HIGH"),
        "CRITICAL": sec.get("CRITICAL"),
    }


def audit_stage2(summaries, repo: Path, audit: Audit) -> dict[str, Any]:
    out = {}
    for run_id in R0003_RUNS:
        if run_id not in summaries:
            audit.add("FAIL", run_id, "FINAL_SUMMARY.json ausente")
            continue
        path, s = summaries[run_id]
        audit.use(path)
        audit.require(
            s.get("protocol_name") == EXPECTED_R0003_PROTOCOL,
            f"{run_id} protocolo",
            EXPECTED_R0003_PROTOCOL,
            f"obtido {s.get('protocol_name')}",
        )
        audit.require(s.get("success") is True, f"{run_id} success", "true", str(s.get("success")))
        audit.require(
            nested(s, "a1.iterations_used") == 3,
            f"{run_id} iterações A1",
            "3 confirmado",
            f"obtido {nested(s, 'a1.iterations_used')}",
        )
        qf = nested(s, "analysis_before.quality.quality_findings")
        audit.require(qf == 0, f"{run_id} findings", "0 confirmado", f"obtido {qf}")

        sec = nested(s, "analysis_before.security", {}) or {}
        vals = [sec.get(k) for k in ("vulnerabilities", "secrets", "misconfigurations", "HIGH", "CRITICAL")]
        audit.require(
            all(v == 0 for v in vals),
            f"{run_id} segurança",
            "todos os indicadores registrados = 0",
            f"valores={vals}",
        )

        acc = latest_a1_acceptance(path.parent)
        if not acc:
            audit.add("FAIL", f"{run_id} acceptance final", "acceptance.json não encontrado")
        else:
            val = acceptance_value(acc, audit)
            audit.require(
                val == (12, 12),
                f"{run_id} acceptance final",
                f"12/12 em {acc.relative_to(repo)}",
                f"obtido {val}",
            )
        out[run_id] = stage2_row(s)
    return out


def audit_stage3(summaries, repo: Path, audit: Audit) -> dict[str, Any]:
    out = {}
    protocol_shas = set()
    lock_shas = set()

    for run_id in PRIMARY_RUNS:
        if run_id not in summaries:
            audit.add("FAIL", run_id, "FINAL_SUMMARY.json ausente")
            continue
        path, s = summaries[run_id]
        audit.use(path)
        protocol_shas.add(s.get("protocol_sha256"))
        lock_shas.add(s.get("environment_lock_sha256"))

        audit.require(
            s.get("protocol_name") == EXPECTED_V12_PROTOCOL,
            f"{run_id} protocolo",
            EXPECTED_V12_PROTOCOL,
            f"obtido {s.get('protocol_name')}",
        )
        audit.require(
            s.get("success") is True and s.get("experimental_status") == "VALID_SUCCESS",
            f"{run_id} status",
            "VALID_SUCCESS",
            f"success={s.get('success')} status={s.get('experimental_status')}",
        )
        audit.require(
            s.get("primary_analysis_eligible") is True,
            f"{run_id} elegibilidade",
            "primary_analysis_eligible=true",
            f"obtido={s.get('primary_analysis_eligible')}",
        )

        row = stage3_row(s)
        acc = path.parent / "a1_iteration_01" / "acceptance.json"
        if not acc.exists():
            audit.add("FAIL", f"{run_id} acceptance A1", "arquivo ausente")
            row["acceptance_a1"] = None
        else:
            val = acceptance_value(acc, audit)
            row["acceptance_a1"] = f"{val[0]}/{val[1]}" if val else None
            audit.require(
                val == (12, 12),
                f"{run_id} acceptance A1",
                f"12/12 em {acc.relative_to(repo)}",
                f"obtido={val}",
            )

        vals = [row.get(k) for k in ("vulnerabilities", "secrets", "misconfigurations", "HIGH", "CRITICAL")]
        audit.require(
            all(v == 0 for v in vals),
            f"{run_id} segurança",
            "todos os indicadores registrados = 0",
            f"valores={vals}",
        )
        out[run_id] = row

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

    # A3 especial Claude Micro
    rid = "RH0003_claude_microservices"
    if rid in summaries:
        path, s = summaries[rid]
        for n, expected in [(1, (7, 12)), (2, (12, 12))]:
            p = path.parent / "a3" / f"attempt_{n:02d}" / "acceptance.json"
            if not p.exists():
                audit.add("FAIL", f"Claude Micro A3 attempt {n}", f"{p.relative_to(repo)} ausente")
                continue
            val = acceptance_value(p, audit)
            audit.require(
                val == expected,
                f"Claude Micro A3 attempt {n}",
                f"{expected[0]}/{expected[1]} confirmado",
                f"obtido={val}",
            )

        ba = path.parent / "a3" / "before_after.json"
        if not ba.exists():
            audit.add("FAIL", "Claude Micro before_after", "arquivo ausente")
        else:
            data = read_json(ba, audit)
            cc = nested(data, "quality.cc_max")
            qf = nested(data, "quality.quality_findings")
            audit.require(
                isinstance(cc, dict) and cc.get("before") == 12 and cc.get("after") == 7,
                "Claude Micro CC max A3",
                "12 -> 7 confirmado",
                f"obtido={cc}",
            )
            audit.require(
                isinstance(qf, dict) and qf.get("before") == 1 and qf.get("after") == 0,
                "Claude Micro findings A3",
                "1 -> 0 confirmado",
                f"obtido={qf}",
            )

        # Evidência de que a segunda invocação não alterou código:
        # procura texto explícito em artefatos de A3; se não existir, WARN em vez de inventar.
        found_no_change = scan_tree_for_any(
            path.parent / "a3",
            [
                r"no changes",
                r"no code changes",
                r"nenhuma altera[cç][aã]o",
                r"não .*altera[cç]",
                r"sem altera[cç][oõ]es",
            ],
            audit,
            max_bytes=5_000_000,
        )
        if found_no_change:
            audit.add("PASS", "Claude Micro A3 segunda invocação sem alteração", found_no_change)
        else:
            audit.warn(
                "Claude Micro A3 segunda invocação sem alteração",
                "não foi encontrada evidência textual inequívoca; não afirmar no TCC sem revisar o trace",
            )

    return out


TEXT_EXTS = {
    ".txt", ".md", ".json", ".jsonl", ".log", ".csv", ".out", ".err", ".stderr", ".stdout"
}


def scan_tree_for_any(
    root: Path,
    regexes: list[str],
    audit: Audit,
    max_bytes: int = 2_000_000,
) -> str | None:
    if not root.exists():
        return None
    compiled = [re.compile(x, re.I) for x in regexes]
    for p in sorted(root.rglob("*")):
        if not p.is_file():
            continue
        if p.suffix.lower() not in TEXT_EXTS and "trace" not in p.name.lower() and "event" not in p.name.lower():
            continue
        try:
            if p.stat().st_size > max_bytes:
                continue
            txt = p.read_text(encoding="utf-8", errors="replace")
        except Exception:
            continue
        for rgx in compiled:
            m = rgx.search(txt)
            if m:
                audit.use(p)
                snippet = txt[max(0, m.start()-80):min(len(txt), m.end()+120)].replace("\n", " ")
                return f"{p}: {snippet[:350]}"
    return None


def audit_antigravity(summaries, repo: Path, audit: Audit) -> dict[str, Any]:
    out = {}

    # Monolito
    rid = "RA0003_antigravity_monolith"
    if rid not in summaries:
        audit.add("FAIL", rid, "FINAL_SUMMARY.json ausente")
    else:
        path, s = summaries[rid]
        audit.use(path)
        row = {
            "run_id": rid,
            "architecture": s.get("architecture"),
            "experimental_status": s.get("experimental_status"),
            "primary_analysis_eligible": s.get("primary_analysis_eligible"),
            "failure_stage": s.get("failure_stage"),
            "a1_iterations": nested(s, "a1.iterations_used"),
            "a1_elapsed_seconds": nested(s, "a1.elapsed_seconds"),
            "provider_elapsed_seconds": nested(s, "a1.provider_metrics.elapsed_seconds"),
            "tool_calls": nested(s, "a1.provider_metrics.tool_calls"),
            "policy_violation_invocations": nested(s, "a1.provider_metrics.policy_violation_invocations"),
        }
        out[rid] = row
        audit.require(
            s.get("experimental_status") == "INVALID_PROVIDER_POLICY",
            "Antigravity Mono status",
            "INVALID_PROVIDER_POLICY confirmado",
            f"obtido={s.get('experimental_status')}",
        )
        audit.require(
            s.get("primary_analysis_eligible") is False,
            "Antigravity Mono elegibilidade",
            "primary_analysis_eligible=false",
            f"obtido={s.get('primary_analysis_eligible')}",
        )
        audit.require(
            nested(s, "a1.provider_metrics.policy_violation_invocations") == 1,
            "Antigravity Mono policy violation",
            "1 invocação de violação confirmada",
            f"obtido={nested(s, 'a1.provider_metrics.policy_violation_invocations')}",
        )

        ev = scan_tree_for_any(path.parent, [r"\bdocker\s+--version\b"], audit, max_bytes=10_000_000)
        if ev:
            audit.add("PASS", "Antigravity Mono docker --version", ev)
        else:
            audit.warn("Antigravity Mono docker --version", "comando não localizado automaticamente nos artefatos textuais")

    # Microsserviços
    rid = "RA0003_antigravity_microservices"
    if rid not in summaries:
        audit.add("FAIL", rid, "FINAL_SUMMARY.json ausente")
    else:
        path, s = summaries[rid]
        audit.use(path)
        row = {
            "run_id": rid,
            "architecture": s.get("architecture"),
            "experimental_status": s.get("experimental_status"),
            "primary_analysis_eligible": s.get("primary_analysis_eligible"),
            "failure_stage": s.get("failure_stage"),
            "a1_iterations": nested(s, "a1.iterations_used"),
            "a1_elapsed_seconds": nested(s, "a1.elapsed_seconds"),
            "provider_elapsed_seconds": nested(s, "a1.provider_metrics.elapsed_seconds"),
            "tool_calls": nested(s, "a1.provider_metrics.tool_calls"),
            "policy_violation_invocations": nested(s, "a1.provider_metrics.policy_violation_invocations"),
        }
        out[rid] = row
        audit.require(
            s.get("experimental_status") == "ABORTED_PROVIDER_RUNTIME",
            "Antigravity Micro status",
            "ABORTED_PROVIDER_RUNTIME confirmado",
            f"obtido={s.get('experimental_status')}",
        )
        audit.require(
            s.get("primary_analysis_eligible") is False,
            "Antigravity Micro elegibilidade",
            "primary_analysis_eligible=false",
            f"obtido={s.get('primary_analysis_eligible')}",
        )
        audit.require(
            nested(s, "a1.iterations_used") == 2,
            "Antigravity Micro iterações A1",
            "2 confirmado",
            f"obtido={nested(s, 'a1.iterations_used')}",
        )
        audit.require(
            nested(s, "a1.provider_metrics.policy_violation_invocations") == 0,
            "Antigravity Micro sem policy violation",
            "0 confirmado",
            f"obtido={nested(s, 'a1.provider_metrics.policy_violation_invocations')}",
        )

        # Iteração 1 deve ter acceptance 7/12 se o arquivo existir.
        p = path.parent / "a1_iteration_01" / "acceptance.json"
        if p.exists():
            val = acceptance_value(p, audit)
            audit.require(
                val == (7, 12),
                "Antigravity Micro acceptance iter1",
                "7/12 confirmado",
                f"obtido={val}",
            )
        else:
            audit.warn("Antigravity Micro acceptance iter1", "acceptance.json não encontrado")

        quota = scan_tree_for_any(
            path.parent,
            [
                r"RESOURCE_EXHAUSTED",
                r"Individual quota reached",
                r"\b429\b",
            ],
            audit,
            max_bytes=15_000_000,
        )
        if quota:
            audit.add("PASS", "Antigravity Micro quota/runtime evidence", quota)
        else:
            audit.warn(
                "Antigravity Micro quota/runtime evidence",
                "RESOURCE_EXHAUSTED/429 não localizado automaticamente nos artefatos textuais",
            )

    # Adjudicação
    candidates = [p for p in repo.rglob("RA0003_pair.json") if p.is_file()]
    if len(candidates) == 1:
        p = candidates[0]
        audit.use(p)
        try:
            d = json.loads(p.read_text(encoding="utf-8"))
            audit.add("PASS", "RA0003_pair adjudicação", f"presente: {p.relative_to(repo)}")
            out["adjudication"] = d
        except Exception as e:
            audit.add("FAIL", "RA0003_pair adjudicação", f"JSON inválido: {e}")
    elif len(candidates) == 0:
        audit.warn("RA0003_pair adjudicação", "arquivo não encontrado")
    else:
        audit.warn("RA0003_pair adjudicação", f"múltiplos arquivos: {candidates}")

    return out


def git_info(repo: Path) -> dict[str, Any]:
    def run(*args: str) -> str | None:
        try:
            cp = subprocess.run(["git", "-C", str(repo), *args], capture_output=True, text=True, check=True)
            return cp.stdout.strip()
        except Exception:
            return None
    return {
        "head": run("rev-parse", "HEAD"),
        "branch": run("rev-parse", "--abbrev-ref", "HEAD"),
        "status_porcelain": run("status", "--porcelain"),
    }


def generate_tables(stage1, stage2, stage3) -> dict[str, str]:
    tables = {}

    # Tabela 1
    m = stage1["monolith"]
    x = stage1["microservices"]
    t1_rows = [
        ["Physical Java LOC", m["physical_java_loc"], x["physical_java_loc"], fmt_pct(pct(x["physical_java_loc"], m["physical_java_loc"]))],
        ["NLOC", m["nloc"], x["nloc"], fmt_pct(pct(x["nloc"], m["nloc"]))],
        ["Arquivos Java", m["java_files"], x["java_files"], fmt_pct(pct(x["java_files"], m["java_files"]))],
        ["Funções/métodos", m["functions"], x["functions"], fmt_pct(pct(x["functions"], m["functions"]))],
        ["CC total", m["cc_total"], x["cc_total"], fmt_pct(pct(x["cc_total"], m["cc_total"]))],
        ["CC média", fmt_num(m["cc_average"], 2), fmt_num(x["cc_average"], 2), fmt_pct(pct(x["cc_average"], m["cc_average"]))],
        ["CC máxima", m["cc_max"], x["cc_max"], fmt_pct(pct(x["cc_max"], m["cc_max"]))],
        ["Testes aprovados", m["acceptance"], x["acceptance"], "—"],
    ]
    tables["Tabela 1 – Métricas estruturais da etapa ChatGPT direto"] = make_table(
        ["Métrica", "Monolito", "Microsserviços", "Diferença Micro vs. Mono"], t1_rows
    )

    # Tabela 2
    m = stage2["R0003_monolith"]
    x = stage2["R0003_microservices"]
    t2_rows = [
        ["Physical Java LOC", m["physical_java_loc"], x["physical_java_loc"], fmt_pct(pct(x["physical_java_loc"], m["physical_java_loc"]))],
        ["NLOC", m["nloc"], x["nloc"], fmt_pct(pct(x["nloc"], m["nloc"]))],
        ["Arquivos Java", m["java_files"], x["java_files"], fmt_pct(pct(x["java_files"], m["java_files"]))],
        ["Funções/métodos", m["functions"], x["functions"], fmt_pct(pct(x["functions"], m["functions"]))],
        ["CC total", m["cc_total"], x["cc_total"], fmt_pct(pct(x["cc_total"], m["cc_total"]))],
        ["CC média", fmt_num(m["cc_average"], 2), fmt_num(x["cc_average"], 2), fmt_pct(pct(x["cc_average"], m["cc_average"]))],
        ["CC máxima", m["cc_max"], x["cc_max"], fmt_pct(pct(x["cc_max"], m["cc_max"]))],
        ["Iterações A1", m["a1_iterations"], x["a1_iterations"], "—"],
        ["Findings de qualidade", m["quality_findings"], x["quality_findings"], "—"],
    ]
    tables["Tabela 2 – Métricas estruturais das execuções R0003"] = make_table(
        ["Métrica", "Monolito", "Microsserviços", "Diferença Micro vs. Mono"], t2_rows
    )

    # Tabela 3
    order = PRIMARY_RUNS
    names = {
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
        ("Findings de qualidade antes de A3", "initial_findings", None),
        ("Findings finais", "final_findings", None),
        ("A3", "a3", None),
    ]
    rows = []
    for label, key, dec in metrics:
        row = [label]
        for rid in order:
            v = stage3[rid].get(key)
            if v is None:
                row.append("")
            elif isinstance(v, float) and dec is not None:
                row.append(fmt_num(v, dec))
            else:
                row.append(str(v))
        rows.append(row)
    tables["Tabela 3 – Resultados principais do protocolo v1.2"] = make_table(
        ["Métrica"] + [names[r] for r in order], rows
    )

    # Tabela 4
    sec_rows = []
    for key, label in [
        ("vulnerabilities", "Vulnerabilidades"),
        ("secrets", "Secrets"),
        ("misconfigurations", "Misconfigurations"),
        ("HIGH", "HIGH"),
        ("CRITICAL", "CRITICAL"),
    ]:
        sec_rows.append([label] + [stage3[r][key] for r in order])
    tables["Tabela 4 – Resultados da análise de segurança no protocolo v1.2"] = make_table(
        ["Categoria"] + [names[r] for r in order], sec_rows
    )

    # Condições transversais
    conditions = [
        ("ChatGPT direto", stage1["monolith"], stage1["microservices"]),
        ("Claude R0003", stage2["R0003_monolith"], stage2["R0003_microservices"]),
        ("Claude v1.2", stage3["RH0003_claude_monolith"], stage3["RH0003_claude_microservices"]),
        ("Codex v1.2", stage3["RC0003_codex_monolith"], stage3["RC0003_codex_microservices"]),
    ]

    # Tabela 5
    t5 = []
    for name, mono, micro in conditions:
        t5.append([
            name,
            fmt_pct(pct(micro["physical_java_loc"], mono["physical_java_loc"])),
            fmt_pct(pct(micro["nloc"], mono["nloc"])),
            fmt_pct(pct(micro["java_files"], mono["java_files"])),
            fmt_pct(pct(micro["functions"], mono["functions"])),
            fmt_pct(pct(micro["cc_total"], mono["cc_total"])),
        ])
    tables["Tabela 5 – Diferença percentual entre microsserviços e monolito nas diferentes etapas"] = make_table(
        ["Condição", "Δ LOC Micro", "Δ NLOC Micro", "Δ arquivos", "Δ funções", "Δ CC total"], t5
    )

    # Tabela 6
    t6 = [[name, fmt_num(mono["cc_average"], 3), fmt_num(micro["cc_average"], 3)] for name, mono, micro in conditions]
    tables["Tabela 6 – Complexidade ciclomática média por condição"] = make_table(
        ["Condição", "CC média Mono", "CC média Micro"], t6
    )

    # Tabela 7
    t7 = [
        ["ChatGPT direto — Monolito", "Sem contagem sistemática equivalente", stage1["monolith"]["acceptance"]],
        ["ChatGPT direto — Microsserviços", "3 problemas documentados", stage1["microservices"]["acceptance"]],
        ["Claude R0003 — Monolito", f"{stage2['R0003_monolith']['a1_iterations']} iterações A1", "Sucesso"],
        ["Claude R0003 — Microsserviços", f"{stage2['R0003_microservices']['a1_iterations']} iterações A1", "Sucesso"],
        ["Claude v1.2 — Monolito", f"{stage3['RH0003_claude_monolith']['a1_iterations']} iteração A1", stage3["RH0003_claude_monolith"]["acceptance_a1"]],
        ["Claude v1.2 — Microsserviços", "1 iteração A1; falha transitória durante A3", "12/12 final"],
        ["Codex v1.2 — Monolito", f"{stage3['RC0003_codex_monolith']['a1_iterations']} iteração A1", stage3["RC0003_codex_monolith"]["acceptance_a1"]],
        ["Codex v1.2 — Microsserviços", f"{stage3['RC0003_codex_microservices']['a1_iterations']} iteração A1", stage3["RC0003_codex_microservices"]["acceptance_a1"]],
    ]
    tables["Tabela 7 – Evidências de falhas e esforço de correção"] = make_table(
        ["Etapa/condição", "Evidência registrada", "Resultado final"], t7, right_from=99
    )

    return tables


def write_manifest(repo: Path, out: Path, audit: Audit, script_path: Path) -> None:
    audit.use(script_path)
    rows = []
    for p in sorted(audit.evidence):
        if not p.exists():
            continue
        try:
            rel = str(p.relative_to(repo))
        except ValueError:
            rel = str(p)
        rows.append({"path": rel, "bytes": p.stat().st_size, "sha256": sha256_file(p)})
    with (out / "evidence_manifest.csv").open("w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=["path", "bytes", "sha256"])
        w.writeheader()
        w.writerows(rows)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--repo-root", required=True, type=Path)
    ap.add_argument("--out-dir", default="tcc-audit-v2", type=Path)
    args = ap.parse_args()

    repo = args.repo_root.resolve()
    out = args.out_dir if args.out_dir.is_absolute() else (repo / args.out_dir)
    out = out.resolve()
    out.mkdir(parents=True, exist_ok=True)

    audit = Audit()
    summaries = discover_final_summaries(repo, audit)

    stage1 = stage1_from_master(repo, audit)
    if stage1 is None:
        print("FATAL: etapa 1 não pôde ser carregada", file=sys.stderr)
        return 2
    audit_stage1(repo, stage1, audit)

    stage2 = audit_stage2(summaries, repo, audit)
    stage3 = audit_stage3(summaries, repo, audit)
    antigravity = audit_antigravity(summaries, repo, audit)

    essential_ready = (
        all(r in stage2 for r in R0003_RUNS)
        and all(r in stage3 for r in PRIMARY_RUNS)
    )

    tables = generate_tables(stage1, stage2, stage3) if essential_ready else {}

    git = git_info(repo)
    script_path = Path(__file__).resolve()
    audit_data = {
        "schema_version": 2,
        "git": git,
        "audit_script": {
            "path": str(script_path),
            "sha256": sha256_file(script_path),
        },
        "stage1": stage1,
        "stage2": stage2,
        "stage3": stage3,
        "antigravity": antigravity,
        "checks": [c.__dict__ for c in audit.checks],
    }
    (out / "audit_data.json").write_text(
        json.dumps(audit_data, indent=2, ensure_ascii=False),
        encoding="utf-8",
    )

    if tables:
        pieces = ["# Tabelas do Capítulo 4 geradas automaticamente", ""]
        for title, table in tables.items():
            pieces += [f"## {title}", "", table, "", "Fonte: elaboração própria a partir dos artefatos auditados.", ""]
        (out / "audit_tables.md").write_text("\n".join(pieces), encoding="utf-8")

    # Resumo Antigravity separado: evidência, não análise primária.
    anti = antigravity
    anti_md = ["# Auditoria das execuções Antigravity", ""]
    for rid in ANTIGRAVITY_RUNS:
        row = anti.get(rid)
        if not row:
            continue
        anti_md += [
            f"## {rid}",
            "",
            f"- status: `{row.get('experimental_status')}`",
            f"- primary_analysis_eligible: `{row.get('primary_analysis_eligible')}`",
            f"- A1 iterations: `{row.get('a1_iterations')}`",
            f"- A1 elapsed: `{row.get('a1_elapsed_seconds')}`",
            f"- provider elapsed: `{row.get('provider_elapsed_seconds')}`",
            f"- tool calls: `{row.get('tool_calls')}`",
            f"- policy violation invocations: `{row.get('policy_violation_invocations')}`",
            "",
        ]
    (out / "antigravity_audit.md").write_text("\n".join(anti_md), encoding="utf-8")

    pass_n = sum(c.status == "PASS" for c in audit.checks)
    warn_n = sum(c.status == "WARN" for c in audit.checks)
    fail_n = sum(c.status == "FAIL" for c in audit.checks)

    report = [
        "# Relatório de auditoria dos resultados do TCC — v2",
        "",
        f"- PASS: {pass_n}",
        f"- WARN: {warn_n}",
        f"- FAIL: {fail_n}",
        f"- Git HEAD: `{git.get('head')}`",
        f"- Git branch: `{git.get('branch')}`",
        f"- Audit script SHA-256: `{audit_data['audit_script']['sha256']}`",
        "",
        "## Checks",
        "",
        "| Status | Verificação | Detalhe |",
        "|---|---|---|",
    ]
    for c in audit.checks:
        detail = c.detail.replace("|", r"\|").replace("\n", " ")
        report.append(f"| {c.status} | {c.name.replace('|', r'\|')} | {detail} |")

    report += [
        "",
        "## Regra de uso",
        "",
        "Os números do Capítulo 4 devem ser copiados de `audit_tables.md` ou de `audit_data.json`.",
        "Nenhum valor quantitativo deve ser reconstruído a partir de memória ou conversa.",
        "",
        "Se houver qualquer `FAIL`, o conjunto não deve ser considerado auditado.",
        "WARN exige revisão antes de repetir a afirmação correspondente no manuscrito.",
        "",
        "As execuções Antigravity são auditadas como evidência operacional, mas permanecem fora da análise quantitativa primária.",
    ]
    (out / "audit_report.md").write_text("\n".join(report) + "\n", encoding="utf-8")

    write_manifest(repo, out, audit, script_path)

    print(f"Audit dir  : {out}")
    print(f"PASS       : {pass_n}")
    print(f"WARN       : {warn_n}")
    print(f"FAIL       : {fail_n}")
    print(f"Tables     : {out / 'audit_tables.md'}")
    print(f"Data       : {out / 'audit_data.json'}")
    print(f"Antigravity: {out / 'antigravity_audit.md'}")
    print(f"Manifest   : {out / 'evidence_manifest.csv'}")
    print(f"Report     : {out / 'audit_report.md'}")

    return 2 if audit.failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
