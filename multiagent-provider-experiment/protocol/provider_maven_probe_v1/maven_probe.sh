#!/usr/bin/env bash
set +e

RESULTS=maven_probe_results.txt
: > "$RESULTS"

log() {
    printf '%s\n' "$*" | tee -a "$RESULTS"
}

log "===== JAVA ====="
java -version >>"$RESULTS" 2>&1
java_rc=$?
log "JAVA_RC=$java_rc"

log ""
log "===== MAVEN ====="
mvn -version >>"$RESULTS" 2>&1
mvn_rc=$?
log "MAVEN_VERSION_RC=$mvn_rc"

log ""
log "===== POMS ====="

mapfile -t POMS < <(
    find . \
      -maxdepth 2 \
      -type f \
      -name pom.xml \
      -not -path '*/target/*' \
      | sort
)

log "POM_COUNT=${#POMS[@]}"

fail=0

for pom in "${POMS[@]}"; do
    dir="$(dirname "$pom")"

    log ""
    log "BUILD=$pom"

    (
        cd "$dir" || exit 98

        mvn \
          -o \
          -B \
          -q \
          -DskipTests \
          package
    ) >>"$RESULTS" 2>&1

    rc=$?

    log "BUILD_RC=$rc"

    if [ "$rc" -ne 0 ]; then
        fail=1
    fi
done

log ""
log "OVERALL_RC=$fail"

exit "$fail"
