#!/usr/bin/env bash

set -euo pipefail

if [ -z "${SENTINEL_BASE_DIR:-}" ]; then SENTINEL_BASE_DIR=/sentinel; fi
cd $SENTINEL_BASE_DIR

addToSystemProperties() {
  SYSTEM_PROPERTIES+=" -D$1=$2"
}

test -n "${K8S_ENVIRONMENT}"
if [ -z "${SENTINEL_ENV:-}" ]; then SENTINEL_ENV=$K8S_ENVIRONMENT; fi
if [ -z "${INSURANCE_URL:-}" ]; then INSURANCE_URL=https://$K8S_LOAD_BALANCER; fi

SYSTEM_PROPERTIES="-Dsentinel=$SENTINEL_ENV -Dsentinel.insurance.url=${INSURANCE_URL}"

if [ -n "${INSURANCE_API_PATH:-}" ]; then  addToSystemProperties "sentinel.insurance.api-path" "$INSURANCE_API_PATH"; fi

java-tests \
  --module-name "insurance-tests" \
  --regression-test-pattern ".*IT\$" \
  --smoke-test-pattern ".*IT\$" \
  $SYSTEM_PROPERTIES \
  $@

exit $?
