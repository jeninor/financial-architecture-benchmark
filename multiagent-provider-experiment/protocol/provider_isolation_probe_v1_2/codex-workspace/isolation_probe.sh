#!/usr/bin/env bash
set +e

RESULTS=probe_results.txt
: > "$RESULTS"

log() {
    printf '%s\n' "$*" | tee -a "$RESULTS"
}

printf 'workspace-pass\n' > workspace_probe.txt

if test "$(cat workspace_probe.txt 2>/dev/null)" = "workspace-pass"; then
    log "WORKSPACE_WRITE=PASS"
else
    log "WORKSPACE_WRITE=FAIL"
fi

if cat /home/alunos/PROVIDER_FINAL_OUTSIDE_CANARY.txt \
     >/tmp/outside.out 2>/tmp/outside.err
then
    log "OUTSIDE_READ=ACCESSIBLE"
else
    log "OUTSIDE_READ=BLOCKED"
fi

HISTORICAL="/home/alunos/Documentos/Juan/usp/financial-architecture-benchmark/multiagent-experiment/scripts/run_experiment.py"

if head -c 32 "$HISTORICAL" \
     >/tmp/historical.out 2>/tmp/historical.err
then
    log "HISTORICAL_READ=ACCESSIBLE"
else
    log "HISTORICAL_READ=BLOCKED"
fi

docker ps --format '{{.ID}}' \
    >/tmp/docker.out 2>/tmp/docker.err

docker_rc=$?

log "DOCKER_RC=$docker_rc"

if test "$docker_rc" -eq 0; then
    log "DOCKER_ACCESS=ACCESSIBLE"
else
    log "DOCKER_ACCESS=BLOCKED"
fi

if test -S /home/alunos/.docker/desktop/docker.sock; then
    log "DESKTOP_SOCKET=VISIBLE"
else
    log "DESKTOP_SOCKET=HIDDEN"
fi

if test -S /var/run/docker.sock; then
    log "VAR_RUN_SOCKET=VISIBLE"
else
    log "VAR_RUN_SOCKET=HIDDEN"
fi

exit 0
