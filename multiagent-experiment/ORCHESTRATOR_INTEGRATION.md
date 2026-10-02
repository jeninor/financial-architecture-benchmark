# Integração com orchestrator.py

Depois de iniciar o workspace e esperar o healthcheck, chame a suíte EXTERNA:

```python
from framework.acceptance import run_acceptance_suite

acceptance = run_acceptance_suite(
    project_root=ROOT,
    run_id=run_id,
    base_url="http://localhost:8080",
    output_path=run_dir / "acceptance_before_refactor.json",
)

if not acceptance["success"]:
    # A1 ainda precisa corrigir a implementação.
    ...
```

Depois do A3:

```python
acceptance_after = run_acceptance_suite(
    project_root=ROOT,
    run_id=run_id + "_after",
    base_url="http://localhost:8080",
    output_path=run_dir / "acceptance_after_refactor.json",
)
```

A suíte deve permanecer em `multiagent-experiment/acceptance-tests`,
fora de `runs/<run>/workspace`.
