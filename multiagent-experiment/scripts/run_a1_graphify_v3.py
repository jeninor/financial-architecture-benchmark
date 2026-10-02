#!/usr/bin/env python3
from __future__ import annotations

import argparse, hashlib, json, shutil, socket, subprocess, sys, time
import urllib.error, urllib.request
from datetime import datetime, timezone
from pathlib import Path

from graphify_runtime_v2 import ensure_graphify_image, build_code_graph, query_graph

def ts(): return datetime.now().strftime("%H:%M:%S")
def log(msg=""): print(f"[{ts()}] {msg}", flush=True)
def now_iso(): return datetime.now(timezone.utc).isoformat()

def sha256(path: Path):
    h=hashlib.sha256()
    with path.open("rb") as f:
        for c in iter(lambda:f.read(1024*1024), b""): h.update(c)
    return h.hexdigest()

def run(cmd, cwd: Path, timeout=None):
    return subprocess.run(cmd, cwd=str(cwd), text=True,
                          stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                          timeout=timeout)

def save_json(path: Path, obj):
    path.write_text(json.dumps(obj, indent=2, ensure_ascii=False), encoding="utf-8")

def port_open(host, port):
    try:
        with socket.create_connection((host,port), timeout=.5): return True
    except OSError: return False

def wait_http(timeout_seconds=180):
    deadline=time.time()+timeout_seconds
    while time.time()<deadline:
        try:
            with urllib.request.urlopen("http://localhost:8080/api/quotes/AAPL",timeout=3):
                return True
        except urllib.error.HTTPError: return True
        except Exception: time.sleep(2)
    return False

def compose(project, workspace, args, timeout=900):
    return run(["docker","compose","-p",project]+list(args), workspace, timeout)

def compose_services(workspace):
    p=run(["docker","compose","config","--services"],workspace,60)
    if p.returncode: raise RuntimeError(p.stdout)
    return [x.strip() for x in p.stdout.splitlines() if x.strip()]

def claude_stream(workspace, prompt, model, max_turns, timeout_seconds, trace, resume=None):
    cmd=["claude","-p",prompt,"--output-format","stream-json","--verbose",
         "--max-turns",str(max_turns),"--model",model,
         "--allowedTools","Read,Write,Edit,Glob,Grep,Bash"]
    if resume: cmd += ["--resume",resume]
    log(f"CLAUDE start model={model} requested_max_turns={max_turns}")
    proc=subprocess.Popen(cmd,cwd=str(workspace),text=True,
                          stdout=subprocess.PIPE,stderr=subprocess.STDOUT,bufsize=1)
    final=None; deadline=time.time()+timeout_seconds
    with trace.open("w",encoding="utf-8") as out:
        for line in proc.stdout:
            if time.time()>deadline:
                proc.kill(); raise TimeoutError("Claude timeout")
            out.write(line); out.flush()
            try: ev=json.loads(line)
            except Exception: continue
            if ev.get("type")=="assistant":
                for b in (ev.get("message") or {}).get("content",[]) or []:
                    if b.get("type")=="tool_use": log("CLAUDE tool -> "+b.get("name","?"))
            if ev.get("type")=="result":
                final=ev
                log(f"CLAUDE result turns={ev.get('num_turns')} cumulative_cost=${ev.get('total_cost_usd')}")
    rc=proc.wait()
    if rc: raise RuntimeError(f"Claude exit {rc}")
    if not final: raise RuntimeError("No Claude result event")
    return final

def usage(r):
    u=r.get("usage") or {}
    return {
      "session_id":r.get("session_id"),"total_cost_usd":r.get("total_cost_usd"),
      "num_turns":r.get("num_turns"),"input_tokens":u.get("input_tokens"),
      "output_tokens":u.get("output_tokens"),
      "cache_creation_input_tokens":u.get("cache_creation_input_tokens"),
      "cache_read_input_tokens":u.get("cache_read_input_tokens"),
      "canonical_models": {
          k:v.get("canonicalModel") for k,v in (r.get("modelUsage") or {}).items()
      }
    }

def run_acceptance(fw, run_id, outfile):
    p=run(["python3",str(fw/"acceptance-tests"/"run_acceptance.py"),
           "--base-url","http://localhost:8080","--run-id",run_id,
           "--output",str(outfile)],fw,240)
    data=json.loads(outfile.read_text()) if outfile.exists() else {}
    return p.returncode,data,p.stdout

def functional_question(acc):
    failed=[t for t in acc.get("tests",[]) if not t.get("passed")]
    parts=[]
    for t in failed:
        parts.append(f"{t.get('id')} {t.get('name')}: {t.get('message')}")
    return (
      "Locate the Java classes and methods most relevant to these failed acceptance "
      "behaviors and show their structural relationships: " + " | ".join(parts)
    )[:5000]

def feedback(acc, logs, graph_context):
    failed=[{
      "id":t.get("id"),"name":t.get("name"),"message":t.get("message"),
      "http_statuses":t.get("http_statuses")
    } for t in acc.get("tests",[]) if not t.get("passed")]
    return f"""The immutable external acceptance gate failed.

FAILED TESTS:
{json.dumps(failed,indent=2,ensure_ascii=False)}

GRAPHIFY RETRIEVED CONTEXT (read-only, source files remain authoritative):
{graph_context}

RECENT APPLICATION LOGS:
{logs[-8000:]}

Fix only the implementation. Read exact source only for the symbols you need.
Do NOT run docker compose up/down/start/stop/restart. The orchestrator owns the
container lifecycle. You may use:
docker compose run --rm --no-deps app mvn -q -B package -DskipTests
for compilation/build checks. Preserve the frozen architecture.
"""


ERROR_MARKERS = (
    "APPLICATION FAILED",
    "Exception",
    "Caused by:",
    "BeanCreation",
    "PSQLException",
    "SQLState",
    "Failed to start",
    "ERROR",
)

def startup_has_error_evidence(app_logs: str) -> bool:
    return any(marker.lower() in app_logs.lower() for marker in ERROR_MARKERS)

def startup_question(app_logs: str) -> str:
    interesting = []
    for line in app_logs.splitlines():
        low = line.lower()
        if any(marker.lower() in low for marker in ERROR_MARKERS):
            interesting.append(line)
    excerpt = "\n".join(interesting[-40:]) or app_logs[-5000:]
    return (
        "Locate the Java classes, configuration, entities, repositories, and methods "
        "most relevant to this Spring Boot startup/runtime failure. Show structural "
        "relationships that can help fix it. Failure evidence:\n" + excerpt
    )[:7000]

def startup_feedback(app_logs: str, compose_ps: str, graph_context: str) -> str:
    return f"""The orchestrator started the Compose stack but the application did not
become reachable on port 8080.

APP LOGS AFTER THE READINESS WAIT:
{app_logs[-12000:]}

COMPOSE STATE AFTER THE READINESS WAIT:
{compose_ps[-4000:]}

GRAPHIFY RETRIEVED CONTEXT:
{graph_context}

Fix only source/configuration problems supported by the evidence above.
Do NOT run docker compose up/down/start/stop/restart. The orchestrator owns
container lifecycle. For build-only verification you may run:
docker compose run --rm --no-deps app mvn -q -B package -DskipTests
Preserve the frozen architecture.
"""

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument("--repo-root",type=Path,default=Path("."))
    ap.add_argument("--framework-root",type=Path,default=Path("multiagent-experiment"))
    ap.add_argument("--seq",type=int,required=True)
    ap.add_argument("--prefix",default="P")
    ap.add_argument("--model",required=True)
    ap.add_argument("--graphify-image",default="graphify-mcp:0.9.73")
    ap.add_argument("--graph-budget",type=int,default=1200)
    ap.add_argument("--max-iterations",type=int,default=3)
    ap.add_argument("--max-turns-first",type=int,default=15)
    ap.add_argument("--max-turns-fix",type=int,default=8)
    ap.add_argument("--startup-timeout",type=int,default=360)
    args=ap.parse_args()

    repo=args.repo_root.resolve(); fw=(repo/args.framework_root).resolve()
    baseline=fw/"baselines"/"monolith"
    files={
      "acceptance":fw/"acceptance-tests"/"run_acceptance.py",
      "contract":fw/"acceptance-tests"/"acceptance_contract.json",
      "requirements":fw/"contracts"/"requirements.md",
      "architecture":fw/"contracts"/"architecture_monolith.md",
      "api":fw/"contracts"/"public_api_v1.md",
      "prompt":fw/"prompts"/"builder.md",
    }
    missing=[str(p) for p in [baseline/"docker-compose.yml",baseline/"financial-monolith"/"pom.xml",*files.values()] if not p.exists()]
    if missing:
        print("Missing:",*missing,sep="\n - "); return 2

    image_meta=ensure_graphify_image(args.graphify_image)
    run_id=f"{args.prefix}{args.seq:04d}_monolith"; run_dir=fw/"runs"/run_id
    workspace=run_dir/"workspace"
    if run_dir.exists(): print("ABORT existing run",run_dir); return 2
    if port_open("127.0.0.1",8080): print("ABORT port 8080 in use"); return 2

    run_dir.mkdir(parents=True); shutil.copytree(baseline,workspace)
    compose_hash=sha256(workspace/"docker-compose.yml")
    services=compose_services(workspace)

    meta={
      "run_id":run_id,"architecture":"monolith","provider":"claude-code",
      "model_requested":args.model,"claude_version":run(["claude","--version"],workspace,30).stdout.strip(),
      "graphify":{**image_meta,"mode":"orchestrator-side-retrieval","budget":args.graph_budget},
      "started_at_utc":now_iso(),"acceptance_contract_sha256":sha256(files["contract"]),
      "acceptance_runner_sha256":sha256(files["acceptance"]),
      "docker_compose_sha256":compose_hash,"compose_services":services,
      "startup_timeout_seconds":args.startup_timeout
    }
    save_json(run_dir/"run_config.json",meta)

    static=(
      files["prompt"].read_text()+"\n\n# ARCHITECTURE\n"+files["architecture"].read_text()
      +"\n\n# PUBLIC API\n"+files["api"].read_text()
      +"\n\n# REQUIREMENTS\n"+files["requirements"].read_text()
      +"""
# ORCHESTRATION RULES
The orchestrator owns Docker lifecycle and external acceptance tests.
Do NOT run docker compose up/down/start/stop/restart.
For build-only verification you may run:
docker compose run --rm --no-deps app mvn -q -B package -DskipTests
Do not access files outside this workspace.
"""
    )

    project=run_id.lower().replace("_","-")
    session=None; records=[]; success=False; current_feedback=None
    session_costs={}; started=time.perf_counter()

    try:
      for i in range(1,args.max_iterations+1):
        idir=run_dir/f"iteration_{i:02d}"; idir.mkdir()
        log("="*64); log(f"ITERATION {i}/{args.max_iterations}")

        # Graph extraction is recorded every iteration, but retrieval is only useful
        # when there is concrete failure information from a previous attempt.
        log("GRAPHIFY extract")
        gm=build_code_graph(workspace,idir/"graphify",args.graphify_image)
        save_json(idir/"graphify_metrics.json",gm)

        prompt=static if i==1 else current_feedback
        cr=claude_stream(workspace,prompt,args.model,
                         args.max_turns_first if i==1 else args.max_turns_fix,
                         1800,idir/"claude_trace.jsonl",session)
        us=usage(cr); save_json(idir/"claude_usage.json",us)
        session=us.get("session_id") or session

        sid=us.get("session_id") or f"iteration-{i}"
        curr=float(us.get("total_cost_usd") or 0)
        prev=session_costs.get(sid,0.0)
        incremental=max(0.0,curr-prev)
        session_costs[sid]=curr
        us["reported_cost_usd_incremental_derived"]=round(incremental,8)
        save_json(idir/"claude_usage.json",us)

        if sha256(workspace/"docker-compose.yml")!=compose_hash:
            raise RuntimeError("ARCHITECTURE_VIOLATION compose changed")
        if set(compose_services(workspace))!=set(services):
            raise RuntimeError("ARCHITECTURE_VIOLATION services changed")

        # Important: only destroy volumes on the first iteration of a run.
        # Between repair iterations, preserve volumes so a Maven cache volume (if
        # present in the frozen Compose file) is not repeatedly cold-started.
        if i == 1:
            compose(project,workspace,["down","-v","--remove-orphans"],180)
        else:
            compose(project,workspace,["down","--remove-orphans"],180)

        up=compose(project,workspace,["up","-d","--build"],900)
        (idir/"compose_up.log").write_text(up.stdout)

        if up.returncode!=0:
            logs=compose(project,workspace,["logs","--no-color","--tail=500"],120).stdout
            app_logs=compose(project,workspace,["logs","--no-color","--tail=500","app"],120).stdout
            ps=compose(project,workspace,["ps","-a"],120).stdout
            (idir/"compose_logs_after_wait.txt").write_text(logs)
            (idir/"app_logs_after_wait.txt").write_text(app_logs)
            (idir/"compose_ps_after_wait.txt").write_text(ps)

            current_feedback=f"""The orchestrator could not build/start the stack.

DOCKER OUTPUT:
{up.stdout[-10000:]}

APP LOGS:
{app_logs[-8000:]}

COMPOSE STATE:
{ps[-4000:]}

Do not start/stop Docker yourself. Fix source/build configuration only."""
            records.append({
                "iteration":i,"graphify":gm,"claude":us,
                "compose_up_ok":False,"startup_class":"BUILD_OR_START_FAILURE"
            })
            continue

        # Wait first; capture logs only AFTER the readiness decision so the evidence
        # is not a stale snapshot from immediately after docker compose up.
        ready=wait_http(args.startup_timeout)

        logs=compose(project,workspace,["logs","--no-color","--tail=500"],120).stdout
        app_logs=compose(project,workspace,["logs","--no-color","--tail=500","app"],120).stdout
        ps=compose(project,workspace,["ps","-a"],120).stdout
        (idir/"compose_logs_after_wait.txt").write_text(logs)
        (idir/"app_logs_after_wait.txt").write_text(app_logs)
        (idir/"compose_ps_after_wait.txt").write_text(ps)

        if not ready:
            if startup_has_error_evidence(app_logs):
                q=startup_question(app_logs)
                (idir/"graph_query_startup.txt").write_text(q)
                log(f"GRAPHIFY startup query budget={args.graph_budget}")
                ctx=query_graph(idir/"graphify",args.graphify_image,q,args.graph_budget)
                (idir/"graph_context_startup.txt").write_text(ctx)
                current_feedback=startup_feedback(app_logs,ps,ctx)
                records.append({
                    "iteration":i,"graphify":gm,"claude":us,
                    "compose_up_ok":True,"http_ready":False,
                    "startup_class":"STARTUP_ERROR_WITH_EVIDENCE"
                })
                continue

            # No stack trace/error evidence after a long fixed timeout: do not spend
            # more Claude tokens guessing. Classify it as an infrastructure/readiness
            # timeout and stop this run.
            records.append({
                "iteration":i,"graphify":gm,"claude":us,
                "compose_up_ok":True,"http_ready":False,
                "startup_class":"STARTUP_TIMEOUT_NO_ERROR_EVIDENCE"
            })
            log("STARTUP timeout without error evidence; stopping run without another Claude call")
            break

        afile=idir/"acceptance.json"
        rc,acc,aout=run_acceptance(fw,f"{run_id}_i{i:02d}",afile)
        (idir/"acceptance_stdout.txt").write_text(aout)
        print(aout,flush=True)
        gate=rc==0 and acc.get("passed")==12 and acc.get("total")==12
        records.append({"iteration":i,"graphify":gm,"claude":us,
                        "compose_up_ok":True,"http_ready":True,
                        "acceptance":{"passed":acc.get("passed"),"failed":acc.get("failed"),
                                      "total":acc.get("total"),"success":gate}})
        if gate:
            success=True; break

        q=functional_question(acc)
        (idir/"graph_query.txt").write_text(q)
        log(f"GRAPHIFY query budget={args.graph_budget}")
        ctx=query_graph(idir/"graphify",args.graphify_image,q,args.graph_budget)
        (idir/"graph_context.txt").write_text(ctx)
        current_feedback=feedback(acc,logs,ctx)

    finally:
      try: compose(project,workspace,["down","-v","--remove-orphans"],180)
      except Exception: pass

    final_cost=sum(session_costs.values())
    termination_reason = "SUCCESS" if success else (
      records[-1].get("startup_class")
      if records and records[-1].get("startup_class")
      else "MAX_ITERATIONS_OR_OTHER_FAILURE"
    )
    summary={
      **meta,"finished_at_utc":now_iso(),"success":success,
      "termination_reason":termination_reason,
      "iterations_used":len(records),"elapsed_seconds":round(time.perf_counter()-started,3),
      "reported_cost_usd_final_by_session":round(final_cost,8),
      "session_costs":session_costs,"iterations":records
    }
    save_json(run_dir/"builder_summary.json",summary)

    lines=[
      f"# A1 Graphify retrieval pilot — {run_id}","",
      f"- Success: **{success}**",
      f"- Model: `{args.model}`",
      f"- Iterations: **{len(records)}**",
      f"- Termination: **{termination_reason}**",
      f"- Startup timeout: **{args.startup_timeout} s**",
      f"- Final CLI-reported session cost: **${round(final_cost,8)}**","",
      "| Iter | Graph nodes | Graph edges | Turns | Incremental cost* | Tests |",
      "|---:|---:|---:|---:|---:|---:|"
    ]
    for r in records:
      a=r.get("acceptance") or {}
      lines.append(
        f"| {r['iteration']} | {r['graphify'].get('nodes')} | {r['graphify'].get('edges')} "
        f"| {r['claude'].get('num_turns')} "
        f"| {r['claude'].get('reported_cost_usd_incremental_derived')} "
        f"| {a.get('passed','-')}/{a.get('total','-')} |"
      )
    lines += ["","*Derived from successive cumulative `total_cost_usd` values within the same resumed session."]
    (run_dir/"RUN_REPORT.md").write_text("\n".join(lines))
    print(f"\nSuccess={success} final_reported_cost=${round(final_cost,8)}")
    print(run_dir/"RUN_REPORT.md")
    return 0 if success else 1

if __name__=="__main__":
    sys.exit(main())
