#!/usr/bin/env bash

set -euo pipefail

if [ -z "${SENTINEL_BASE_DIR:-}" ]; then SENTINEL_BASE_DIR=/sentinel; fi
cd $SENTINEL_BASE_DIR

test -n "${K8S_ENVIRONMENT}"
if [ -z "${SENTINEL_ENV:-}" ]; then SENTINEL_ENV=$K8S_ENVIRONMENT; fi
if [ -z "${INSURANCE_URL:-}" ]; then INSURANCE_URL=https://$K8S_LOAD_BALANCER; fi

java-tests \
  --module-name "insurance-tests" \
  --regression-test-pattern ".*IT\$" \
  --smoke-test-pattern ".*IT\$" \
  -Dsentinel="${SENTINEL_ENV}" \
  -Dsentinel.insurance.url="${INSURANCE_URL}" \
  $@

exit $?
