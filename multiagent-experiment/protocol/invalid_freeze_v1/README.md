# Invalid official freeze v1

This environment freeze must NOT be used for official experimental results.

Reason:
The Trivy cache had been partially removed during Git history cleanup of
multiagent-experiment/tool-cache/.

The frozen cache contained:
- db/trivy.db
- java-db/trivy-java.db

but was missing at least:
- db/metadata.json
- java-db/metadata.json
- policy cache files

Consequently, the first attempted official run R0001_monolith completed A1
and acceptance 12/12, but A4 aborted with:

--skip-db-update cannot be specified on the first run

Classification:
ABORTED_INFRASTRUCTURE / INVALID_TRIVY_CACHE_SNAPSHOT

R0001_monolith is excluded from the primary official paired experiment.
