#!/usr/bin/env python3
import argparse, hashlib, json
from datetime import datetime, timezone
from pathlib import Path

def sha(path):
    h=hashlib.sha256()
    with path.open("rb") as f:
        for c in iter(lambda:f.read(1024*1024),b""): h.update(c)
    return h.hexdigest()

ap=argparse.ArgumentParser()
ap.add_argument("--cache",type=Path,required=True)
ap.add_argument("--output",type=Path,required=True)
a=ap.parse_args()
rows=[]
for p in sorted(a.cache.rglob("*")):
    if p.is_file():
        rows.append({"path":str(p.relative_to(a.cache)),"bytes":p.stat().st_size,"sha256":sha(p)})
data={"generated_at_utc":datetime.now(timezone.utc).isoformat(),
      "cache_root":str(a.cache.resolve()),"files":rows}
a.output.parent.mkdir(parents=True,exist_ok=True)
a.output.write_text(json.dumps(data,indent=2),encoding="utf-8")
print(a.output)
