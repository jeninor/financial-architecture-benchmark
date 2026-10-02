#!/usr/bin/env python3
from __future__ import annotations

import argparse, hashlib, json, os, subprocess, sys, time
from datetime import datetime, timezone
from pathlib import Path

def now_iso():
    return datetime.now(timezone.utc).isoformat()

def sha256(path: Path):
    h=hashlib.sha256()
    with path.open("rb") as f:
        for c in iter(lambda:f.read(1024*1024),b""): h.update(c)
    return h.hexdigest()

def run(cmd, timeout=1800, cwd=None):
    return subprocess.run(
        cmd, cwd=str(cwd) if cwd else None, text=True,
        stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=timeout
    )

def image_info(ref):
    p=run(["docker","image","inspect",ref],60)
    if p.returncode:
        raise RuntimeError(f"Missing Docker image {ref}\n{p.stdout}\n{p.stderr}")
    row=json.loads(p.stdout)[0]
    return {"ref":ref,"image_id":row.get("Id"),"created":row.get("Created")}

def docker_context():
    p=run(["docker","context","show"],30)
    if p.returncode: raise RuntimeError(p.stdout+p.stderr)
    return p.stdout.strip()

def loadj(p): return json.loads(p.read_text(encoding="utf-8"))

def cache_manifest(root: Path):
    rows=[]
    if root.exists():
        for p in sorted(root.rglob("*")):
            if p.is_file():
                rows.append({
                    "path":str(p.relative_to(root)),
                    "bytes":p.stat().st_size,
                    "sha256":sha256(p)
                })
    return {"root":str(root),"files":rows}

def a2_scan(workspace, image, policy):
    t=policy["thresholds"]
    cmd=[
        "docker","run","--rm",
        "-v",f"{workspace.resolve()}:/workspace:ro",
        image,"/workspace",
        "--max-function-ccn",str(t["max_function_ccn"]),
        "--max-function-nloc",str(t["max_function_nloc"]),
        "--max-file-nloc",str(t["max_file_nloc"]),
        "--max-parameters",str(t["max_parameters"])
    ]
    started=time.perf_counter()
    p=run(cmd,600)
    elapsed=time.perf_counter()-started
    if p.returncode:
        raise RuntimeError("A2 failed:\n"+p.stdout[-5000:]+"\n"+p.stderr[-5000:])
    return json.loads(p.stdout), elapsed

def sanitize_trivy(raw):
    findings=[]
    counts={"vulnerabilities":0,"secrets":0,"misconfigurations":0,"HIGH":0,"CRITICAL":0}
    for result in raw.get("Results",[]) or []:
        target=result.get("Target")
        for v in result.get("Vulnerabilities",[]) or []:
            sev=(v.get("Severity") or "UNKNOWN").upper()
            counts["vulnerabilities"]+=1
            if sev in counts: counts[sev]+=1
            findings.append({
                "agent":"A4","category":"security","type":"VULNERABILITY",
                "severity":sev,"target":target,"id":v.get("VulnerabilityID"),
                "package":v.get("PkgName"),"installed_version":v.get("InstalledVersion"),
                "fixed_version":v.get("FixedVersion"),"title":v.get("Title"),
                "primary_url":v.get("PrimaryURL")
            })
        for s in result.get("Secrets",[]) or []:
            sev=(s.get("Severity") or "UNKNOWN").upper()
            counts["secrets"]+=1
            if sev in counts: counts[sev]+=1
            findings.append({
                "agent":"A4","category":"security","type":"SECRET",
                "severity":sev,"target":target,"rule_id":s.get("RuleID"),
                "category_name":s.get("Category"),"title":s.get("Title"),
                "start_line":s.get("StartLine"),"end_line":s.get("EndLine")
            })
        for m in result.get("Misconfigurations",[]) or []:
            sev=(m.get("Severity") or "UNKNOWN").upper()
            counts["misconfigurations"]+=1
            if sev in counts: counts[sev]+=1
            cm=m.get("CauseMetadata") or {}
            findings.append({
                "agent":"A4","category":"security","type":"MISCONFIGURATION",
                "severity":sev,"target":target,"id":m.get("ID") or m.get("AVDID"),
                "title":m.get("Title"),"message":m.get("Message"),
                "resolution":m.get("Resolution"),
                "start_line":cm.get("StartLine"),"end_line":cm.get("EndLine")
            })
    return {"schema_version":1,"agent":"A4","summary":counts,"findings":findings}

def a4_scan(workspace, image, cache_dir, policy):
    uid,gid=str(os.getuid()),str(os.getgid())
    cmd=[
        "docker","run","--rm",
        "--user",f"{uid}:{gid}","-e","HOME=/tmp",
        "-v",f"{workspace.resolve()}:/workspace:ro",
        "-v",f"{cache_dir.resolve()}:/cache",
        image,"fs","--cache-dir","/cache",
        "--format","json","--quiet",
        "--scanners",",".join(policy["scanners"]),
        "--severity",",".join(policy["severities"]),
        "--skip-db-update","--skip-java-db-update","--skip-check-update",
        "--skip-version-check","/workspace"
    ]
    started=time.perf_counter()
    p=run(cmd,1800)
    elapsed=time.perf_counter()-started
    if p.returncode:
        raise RuntimeError("A4 failed:\n"+p.stdout[-5000:]+"\n"+p.stderr[-5000:])
    raw=json.loads(p.stdout)
    return raw, sanitize_trivy(raw), elapsed

def combine(q,s):
    fs=(q.get("findings") or [])+(s.get("findings") or [])
    by_agent={"A2":len(q.get("findings") or []),"A4":len(s.get("findings") or [])}
    by_sev={}; by_type={}
    for f in fs:
        sev=f.get("severity","UNKNOWN"); typ=f.get("type","UNKNOWN")
        by_sev[sev]=by_sev.get(sev,0)+1
        by_type[typ]=by_type.get(typ,0)+1
    return {
        "schema_version":1,"generated_at_utc":now_iso(),"agents":["A2","A4"],
        "summary":{"total_findings":len(fs),"by_agent":by_agent,
                   "by_severity":by_sev,"by_type":by_type},
        "quality_summary":q.get("summary"),"security_summary":s.get("summary"),
        "findings":fs
    }

def write_report(path, workspace, q, s, f, tq, ts, meta):
    qs=q["summary"]; ss=s["summary"]
    lines=[
        "# A2 Quality + A4 Security report","",
        f"- Workspace: `{workspace}`",
        f"- Docker context: `{meta['docker_context']}`",
        f"- Lizard image: `{meta['lizard']['ref']}`",
        f"- Trivy image: `{meta['trivy']['ref']}`","",
        "## A2 — Quality","",
        f"- Physical Java LOC: **{qs['physical_java_loc']}**",
        f"- Java files: **{qs['java_files']}**",
        f"- Lizard NLOC: **{qs['lizard_nloc']}**",
        f"- Functions/methods: **{qs['functions']}**",
        f"- CC total: **{qs['cc_total']}**",
        f"- CC average: **{qs['cc_average']}**",
        f"- CC max: **{qs['cc_max']}**",
        f"- Actionable quality findings: **{qs['quality_findings']}**",
        f"- Analysis time: **{tq:.3f} s**","",
        "## A4 — Security","",
        f"- Vulnerabilities: **{ss['vulnerabilities']}**",
        f"- Secrets: **{ss['secrets']}**",
        f"- Misconfigurations: **{ss['misconfigurations']}**",
        f"- HIGH findings: **{ss['HIGH']}**",
        f"- CRITICAL findings: **{ss['CRITICAL']}**",
        f"- Analysis time: **{ts:.3f} s**","",
        f"## Combined findings\n\n- Total actionable findings: **{f['summary']['total_findings']}**","",
        "| Type | Severity | Target | Detail |",
        "|---|---|---|---|"
    ]
    for x in f["findings"][:100]:
        target=str(x.get("file") or x.get("target") or "").replace("|","\\|")
        detail=str(x.get("symbol") or x.get("id") or x.get("package") or x.get("title") or "").replace("|","\\|").replace("\n"," ")
        lines.append(f"| {x.get('type','')} | {x.get('severity','')} | {target} | {detail} |")
    path.write_text("\n".join(lines)+"\n",encoding="utf-8")

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument("--workspace",type=Path,required=True)
    ap.add_argument("--output-dir",type=Path,required=True)
    ap.add_argument("--lizard-image",default="multiagent-lizard:1.24.0")
    ap.add_argument("--trivy-image",default="aquasec/trivy:0.74.0")
    ap.add_argument("--trivy-cache",type=Path,required=True)
    ap.add_argument("--quality-policy",type=Path,required=True)
    ap.add_argument("--security-policy",type=Path,required=True)
    args=ap.parse_args()

    workspace=args.workspace.resolve()
    out=args.output_dir.resolve()
    cache=args.trivy_cache.resolve()
    if not workspace.exists(): raise SystemExit(f"Missing workspace: {workspace}")
    if out.exists() and any(out.iterdir()): raise SystemExit(f"Output not empty: {out}")
    out.mkdir(parents=True,exist_ok=True)

    qpol=loadj(args.quality_policy); spol=loadj(args.security_policy)
    meta={
        "generated_at_utc":now_iso(),
        "docker_context":docker_context(),
        "lizard":image_info(args.lizard_image),
        "trivy":image_info(args.trivy_image),
        "quality_policy_sha256":sha256(args.quality_policy),
        "security_policy_sha256":sha256(args.security_policy),
        "trivy_cache_before":cache_manifest(cache)
    }

    print("===== A2 QUALITY =====",flush=True)
    q,tq=a2_scan(workspace,args.lizard_image,qpol)
    (out/"quality_before.json").write_text(json.dumps(q,indent=2,ensure_ascii=False),encoding="utf-8")
    print(json.dumps(q["summary"],indent=2),flush=True)

    print("\n===== A4 SECURITY =====",flush=True)
    raw,s,ts=a4_scan(workspace,args.trivy_image,cache,spol)
    (out/"trivy_raw.json").write_text(json.dumps(raw,indent=2,ensure_ascii=False),encoding="utf-8")
    (out/"security_before.json").write_text(json.dumps(s,indent=2,ensure_ascii=False),encoding="utf-8")
    print(json.dumps(s["summary"],indent=2),flush=True)

    f=combine(q,s)
    (out/"findings.json").write_text(json.dumps(f,indent=2,ensure_ascii=False),encoding="utf-8")
    meta["trivy_cache_after"]=cache_manifest(cache)
    meta["timings_seconds"]={"A2_quality":round(tq,3),"A4_security":round(ts,3),"total":round(tq+ts,3)}
    (out/"analysis_manifest.json").write_text(json.dumps(meta,indent=2,ensure_ascii=False),encoding="utf-8")
    write_report(out/"A2_A4_REPORT.md",workspace,q,s,f,tq,ts,meta)

    print("\n===== COMPLETE =====")
    print("Quality :",out/"quality_before.json")
    print("Security:",out/"security_before.json")
    print("Findings:",out/"findings.json")
    print("Report  :",out/"A2_A4_REPORT.md")
    print("Total findings:",f["summary"]["total_findings"])
    return 0

if __name__=="__main__":
    sys.exit(main())
