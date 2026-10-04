#!/usr/bin/env python3
import argparse, json, subprocess, sys

def run(cmd):
    return subprocess.run(cmd, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument("--image", default="graphify-mcp:0.9.73")
    args=ap.parse_args()

    c=run(["docker","context","show"])
    current=c.stdout.strip()
    print("CURRENT_CONTEXT:", current)

    contexts=run(["docker","context","ls","--format","{{.Name}}"])
    names=[x.strip() for x in contexts.stdout.splitlines() if x.strip()]

    usable_current=False
    for ctx in names:
        print(f"\n=== {ctx} ===")
        print("tag inspect:")
        p=run(["docker","--context",ctx,"image","inspect",args.image,
               "--format","{{.Id}} {{.Created}}"])
        print(p.stdout.strip() or f"(failed rc={p.returncode})")

        print("matching image ls:")
        q=run(["docker","--context",ctx,"image","ls","--no-trunc",
               "--format","{{.Repository}}:{{.Tag}} {{.ID}}"])
        matches=[x for x in q.stdout.splitlines() if x.startswith(args.image+" ")]
        print("\n".join(matches) if matches else "(none)")

        if ctx==current:
            if p.returncode==0:
                usable_current=True
            else:
                for m in matches:
                    image_id=m.split(" ",1)[1].strip()
                    z=run(["docker","--context",ctx,"image","inspect",image_id,
                           "--format","{{.Id}} {{.Created}}"])
                    print("current-context ID fallback:")
                    print(z.stdout.strip() or f"(failed rc={z.returncode})")
                    if z.returncode==0:
                        usable_current=True

    print("\nRESULT:", "OK" if usable_current else "NOT_USABLE_IN_CURRENT_CONTEXT")
    return 0 if usable_current else 1

if __name__=="__main__":
    sys.exit(main())
