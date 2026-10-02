#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
from pathlib import Path
import lizard

def g(obj, name, default=None):
    try:
        return getattr(obj, name)
    except Exception:
        return default

def physical(path: Path):
    lines = path.read_text(encoding="utf-8", errors="replace").splitlines()
    return len(lines), sum(1 for x in lines if x.strip())

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("root", type=Path)
    ap.add_argument("--max-function-ccn", type=int, default=10)
    ap.add_argument("--max-function-nloc", type=int, default=60)
    ap.add_argument("--max-file-nloc", type=int, default=300)
    ap.add_argument("--max-parameters", type=int, default=6)
    args = ap.parse_args()

    root = args.root.resolve()
    files = sorted(
        p for p in root.rglob("*.java")
        if "/src/main/java/" in p.as_posix() and "/target/" not in p.as_posix()
    )

    file_rows, funcs, findings = [], [], []
    physical_total = nonblank_total = lizard_nloc = cc_total = cc_max = 0

    for path in files:
        ploc, nonblank = physical(path)
        physical_total += ploc
        nonblank_total += nonblank

        result = lizard.analyze_file(str(path))
        fn_list = g(result, "function_list", []) or []
        fnloc = int(g(result, "nloc", 0) or 0)
        lizard_nloc += fnloc
        rel = str(path.relative_to(root))

        file_rows.append({
            "file": rel,
            "physical_loc": ploc,
            "nonblank_loc": nonblank,
            "lizard_nloc": fnloc,
            "functions": len(fn_list)
        })

        if fnloc > args.max_file_nloc:
            findings.append({
                "agent": "A2", "category": "quality",
                "type": "QUALITY_LARGE_FILE", "severity": "MEDIUM",
                "file": rel, "metric": "lizard_nloc",
                "value": fnloc, "threshold": args.max_file_nloc
            })

        for fn in fn_list:
            ccn = int(g(fn, "cyclomatic_complexity", 0) or 0)
            nloc = int(g(fn, "nloc", 0) or 0)
            params = g(fn, "parameters", []) or []
            try:
                pcount = len(params)
            except Exception:
                pcount = int(g(fn, "parameter_count", 0) or 0)

            cc_total += ccn
            cc_max = max(cc_max, ccn)
            row = {
                "file": rel,
                "name": g(fn, "name", ""),
                "long_name": g(fn, "long_name", g(fn, "name", "")),
                "start_line": int(g(fn, "start_line", 0) or 0),
                "end_line": int(g(fn, "end_line", 0) or 0),
                "nloc": nloc,
                "ccn": ccn,
                "parameter_count": pcount
            }
            funcs.append(row)

            if ccn > args.max_function_ccn:
                findings.append({
                    "agent": "A2", "category": "quality",
                    "type": "QUALITY_HIGH_CCN", "severity": "HIGH",
                    "file": rel, "symbol": row["long_name"],
                    "start_line": row["start_line"], "metric": "ccn",
                    "value": ccn, "threshold": args.max_function_ccn
                })
            if nloc > args.max_function_nloc:
                findings.append({
                    "agent": "A2", "category": "quality",
                    "type": "QUALITY_LONG_FUNCTION", "severity": "MEDIUM",
                    "file": rel, "symbol": row["long_name"],
                    "start_line": row["start_line"], "metric": "function_nloc",
                    "value": nloc, "threshold": args.max_function_nloc
                })
            if pcount > args.max_parameters:
                findings.append({
                    "agent": "A2", "category": "quality",
                    "type": "QUALITY_TOO_MANY_PARAMETERS", "severity": "MEDIUM",
                    "file": rel, "symbol": row["long_name"],
                    "start_line": row["start_line"], "metric": "parameter_count",
                    "value": pcount, "threshold": args.max_parameters
                })

    nfunc = len(funcs)
    out = {
        "schema_version": 1,
        "agent": "A2",
        "tool": {"name": "lizard", "version": getattr(lizard, "__version__", "unknown")},
        "scope": {"root": str(root), "production_only": True},
        "summary": {
            "physical_java_loc": physical_total,
            "nonblank_java_loc": nonblank_total,
            "java_files": len(files),
            "lizard_nloc": lizard_nloc,
            "functions": nfunc,
            "cc_total": cc_total,
            "cc_average": round(cc_total / nfunc, 6) if nfunc else 0.0,
            "cc_max": cc_max,
            "quality_findings": len(findings)
        },
        "thresholds": {
            "max_function_ccn": args.max_function_ccn,
            "max_function_nloc": args.max_function_nloc,
            "max_file_nloc": args.max_file_nloc,
            "max_parameters": args.max_parameters
        },
        "top_complex_functions": sorted(
            funcs, key=lambda x: (x["ccn"], x["nloc"]), reverse=True
        )[:20],
        "files": file_rows,
        "findings": findings
    }
    print(json.dumps(out, indent=2, ensure_ascii=False))

if __name__ == "__main__":
    main()
