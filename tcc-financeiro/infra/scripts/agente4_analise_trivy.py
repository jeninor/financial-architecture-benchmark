#!/usr/bin/env python3
"""Agente 4 - camada de revisao por IA (pos-Trivy).

Consolida os dois JSON do Trivy (monolito x microsservicos) conforme
infra/scripts/AGENTE4_REVISAO_IA.md, usando apenas a stdlib (json).
Saida: resumo legivel em stdout e os dados completos em
metrics/agente4_analise_<timestamp>.json (para rastreabilidade).

Uso: python3 infra/scripts/agente4_analise_trivy.py [diretorio_metrics]
"""
import glob
import json
import os
import re
import sys
from collections import Counter, defaultdict

SEVERITIES = ["CRITICAL", "HIGH", "MEDIUM", "LOW", "UNKNOWN"]
RANK = {s: i for i, s in enumerate(SEVERITIES)}

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
METRICS = sys.argv[1] if len(sys.argv) > 1 else os.path.join(ROOT, "metrics")


def ts_of(path):
    return re.search(r"_(\d{8}_\d{6})\.json$", path).group(1)


def select_inputs():
    mono = sorted(glob.glob(os.path.join(METRICS, "trivy_monolito_*.json")), key=ts_of)
    micro = sorted(glob.glob(os.path.join(METRICS, "trivy_microservicos_*.json")), key=ts_of)
    if not mono or not micro:
        sys.exit("ERRO: faltam relatorios trivy_monolito_*.json ou trivy_microservicos_*.json")
    mono_latest = mono[-1]
    pair = [m for m in micro if ts_of(m) == ts_of(mono_latest)]
    if pair:
        return mono, mono_latest, pair[0], True
    return mono, mono_latest, micro[-1], False


def load(path):
    with open(path, encoding="utf-8") as fh:
        return json.load(fh)


def vulns(report):
    """Uma linha por ocorrencia (Target x pacote x CVE), como o Trivy reporta."""
    rows = []
    for res in report.get("Results") or []:
        for v in res.get("Vulnerabilities") or []:
            rows.append({
                "target": res["Target"],
                "id": v["VulnerabilityID"],
                "pkg": v["PkgName"],
                "pkg_id": v.get("PkgID", ""),
                "installed": v.get("InstalledVersion", ""),
                "fixed": v.get("FixedVersion", "") or "",
                "severity": v.get("Severity", "UNKNOWN"),
                "title": v.get("Title", ""),
            })
    return rows


def graph(report):
    """Por Target: (pacotes diretos, mapa pkgID -> DependsOn, mapa pkgID -> nome@versao)."""
    out = {}
    for res in report.get("Results") or []:
        pkgs = res.get("Packages") or []
        deps = {p["ID"]: p.get("DependsOn") or [] for p in pkgs}
        names = {p["ID"]: f'{p["Name"]}:{p.get("Version", "")}' for p in pkgs}
        direct = [p["ID"] for p in pkgs if p.get("Relationship") == "direct"]
        out[res["Target"]] = (direct, deps, names)
    return out


def origins(g, target, pkg_id):
    """Dependencias diretas do pom cujo fecho transitivo alcanca pkg_id."""
    if target not in g:
        return set()
    direct, deps, names = g[target]
    found = set()
    for d in direct:
        stack, seen = [d], set()
        while stack:
            cur = stack.pop()
            if cur == pkg_id:
                found.add(names[d].rsplit(":", 1)[0])
                break
            if cur in seen:
                continue
            seen.add(cur)
            stack.extend(deps.get(cur, []))
    return found


def all_pkg_versions(report):
    versions = defaultdict(set)
    for res in report.get("Results") or []:
        for p in res.get("Packages") or []:
            versions[p["Name"]].add(p.get("Version", ""))
    return versions


def sev_counts(rows):
    c = Counter(r["severity"] for r in rows)
    return {s: c.get(s, 0) for s in SEVERITIES}


def distinct_by_id(rows):
    """CVE distinta -> severidade mais alta observada e conjunto de pacotes."""
    out = {}
    for r in rows:
        cur = out.setdefault(r["id"], {"severity": r["severity"], "pkgs": set(), "rows": []})
        if RANK[r["severity"]] < RANK[cur["severity"]]:
            cur["severity"] = r["severity"]
        cur["pkgs"].add(r["pkg"])
        cur["rows"].append(r)
    return out


def vparse(v):
    return [int(x) for x in re.findall(r"\d+", v)[:3]] + [0] * (3 - len(re.findall(r"\d+", v)[:3]))


def upgrade_class(installed, fixed_field):
    """Escolhe a menor FixedVersion >= instalada e classifica o salto."""
    inst = vparse(installed)
    cands = [f.strip() for f in fixed_field.split(",") if f.strip()]
    cands = [c for c in cands if vparse(c) >= inst] or cands
    best = min(cands, key=vparse) if cands else ""
    if not best:
        return "", "sem correcao"
    b = vparse(best)
    if b[0] != inst[0]:
        kind = "major"
    elif b[1] != inst[1]:
        kind = "minor"
    else:
        kind = "patch"
    return best, kind


def group_exclusive(ids, dist, g, other_versions, side_rows):
    groups = defaultdict(lambda: {"sev": Counter(), "ids": set(), "installed": set(),
                                  "origins": set(), "targets": set()})
    for cid in ids:
        for r in dist[cid]["rows"]:
            grp = groups[r["pkg"]]
            if cid not in grp["ids"]:
                grp["ids"].add(cid)
                grp["sev"][dist[cid]["severity"]] += 1
            grp["installed"].add(r["installed"])
            grp["targets"].add(r["target"].split("/")[0])
            grp["origins"] |= origins(g, r["target"], r["pkg_id"])
    result = []
    for pkg, grp in sorted(groups.items(), key=lambda kv: (-len(kv[1]["ids"]), kv[0])):
        result.append({
            "pkg": pkg,
            "distinct_cves": len(grp["ids"]),
            "by_severity": {s: grp["sev"].get(s, 0) for s in SEVERITIES},
            "installed": sorted(grp["installed"]),
            "present_in_other_arch": sorted(other_versions.get(pkg, [])),
            "modules": sorted(grp["targets"]),
            "direct_origins": sorted(grp["origins"]),
        })
    return result


def actionable(ids, dist, g):
    items = defaultdict(lambda: {"ids": set(), "sev": set(), "fixed": set(), "origins": set(), "modules": set()})
    for cid in ids:
        if dist[cid]["severity"] not in ("CRITICAL", "HIGH"):
            continue
        for r in dist[cid]["rows"]:
            if not r["fixed"] or r["severity"] not in ("CRITICAL", "HIGH"):
                continue
            it = items[(r["pkg"], r["installed"])]
            it["ids"].add(cid)
            it["sev"].add(r["severity"])
            it["fixed"].add(r["fixed"])
            it["modules"].add(r["target"].split("/")[0])
            it["origins"] |= origins(g, r["target"], r["pkg_id"])
    out = []
    for (pkg, inst), it in sorted(items.items()):
        # a versao alvo precisa corrigir TODAS as CVEs do pacote: pega a maior das minimas
        targets = [upgrade_class(inst, f) for f in it["fixed"]]
        best = max(targets, key=lambda t: vparse(t[0]))
        out.append({
            "pkg": pkg, "installed": inst, "target_version": best[0], "jump": best[1],
            "severities": sorted(it["sev"], key=RANK.get), "cves": sorted(it["ids"]),
            "fixed_fields": sorted(it["fixed"]), "modules": sorted(it["modules"]),
            "direct_origins": sorted(it["origins"]),
        })
    return out


def main():
    mono_all, mono_path, micro_path, same_ts = select_inputs()
    mono, micro = load(mono_path), load(micro_path)
    mrows, srows = vulns(mono), vulns(micro)
    mdist, sdist = distinct_by_id(mrows), distinct_by_id(srows)
    mids, sids = set(mdist), set(sdist)
    shared, only_micro, only_mono = mids & sids, sids - mids, mids - sids

    # determinismo: outras execucoes do monolito com a mesma contagem
    mono_counts = {os.path.basename(p): sev_counts(vulns(load(p))) for p in mono_all}

    def dsev(dist, ids):
        c = Counter(dist[i]["severity"] for i in ids)
        return {s: c.get(s, 0) for s in SEVERITIES}

    sample = sorted(shared, key=lambda i: (RANK[mdist[i]["severity"]], i))[:5]
    shared_pkgs = Counter(p for i in shared for p in mdist[i]["pkgs"])

    data = {
        "inputs": {"monolito": os.path.basename(mono_path), "microservicos": os.path.basename(micro_path),
                   "same_timestamp": same_ts, "trivy_version": micro.get("Trivy", {}).get("Version"),
                   "created_at": {"monolito": mono.get("CreatedAt"), "microservicos": micro.get("CreatedAt")}},
        "monolito_runs": mono_counts,
        "targets": {"monolito": sorted({r["target"] for r in mrows}),
                    "microservicos": dict(Counter(r["target"] for r in srows))},
        "raw_by_severity": {"monolito": sev_counts(mrows), "microservicos": sev_counts(srows)},
        "distinct_by_severity": {"monolito": dsev(mdist, mids), "microservicos": dsev(sdist, sids)},
        "distinct_total": {"monolito": len(mids), "microservicos": len(sids)},
        "shared": {"count": len(shared), "by_severity": dsev(mdist, shared),
                   "top_pkgs": shared_pkgs.most_common(10),
                   "sample": [{"id": i, "severity": mdist[i]["severity"], "pkgs": sorted(mdist[i]["pkgs"]),
                               "installed_mono": sorted({r["installed"] for r in mdist[i]["rows"]}),
                               "installed_micro": sorted({r["installed"] for r in sdist[i]["rows"]}),
                               "title": mdist[i]["rows"][0]["title"]} for i in sample]},
        "exclusive_micro": {"count": len(only_micro), "by_severity": dsev(sdist, only_micro),
                            "groups": group_exclusive(only_micro, sdist, graph(micro), all_pkg_versions(mono), srows)},
        "exclusive_mono": {"count": len(only_mono), "by_severity": dsev(mdist, only_mono),
                           "groups": group_exclusive(only_mono, mdist, graph(mono), all_pkg_versions(micro), mrows)},
        "actionable_exclusive_micro": actionable(only_micro, sdist, graph(micro)),
        "actionable_shared": actionable(shared, sdist, graph(micro)),
    }

    out = os.path.join(METRICS, f"agente4_analise_{ts_of(micro_path)}.json")
    with open(out, "w", encoding="utf-8") as fh:
        json.dump(data, fh, indent=2, ensure_ascii=False)

    print(json.dumps(data, indent=2, ensure_ascii=False))
    print(f"\nDados salvos em {out}", file=sys.stderr)


if __name__ == "__main__":
    main()
