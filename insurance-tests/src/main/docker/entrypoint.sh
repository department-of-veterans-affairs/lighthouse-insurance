#!/usr/bin/env bash

set -euo pipefail

test -n "${K8S_ENVIRONMENT}"
test -n "${K8S_LOAD_BALANCER}"

if [ -z "${SENTINEL_BASE_DIR:-}" ]; then SENTINEL_BASE_DIR=/sentinel; fi
cd $SENTINEL_BASE_DIR

# =~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=

main() {
  if [ -z "${SENTINEL_ENV:-}" ]; then SENTINEL_ENV="${K8S_ENVIRONMENT}"; fi
  if [ -z "${INSURANCE_URL:-}" ]; then INSURANCE_URL="https://${K8S_LOAD_BALANCER}"; fi

  SYSTEM_PROPERTIES="-Dsentinel=${SENTINEL_ENV} -Dsentinel.insurance.url=${INSURANCE_URL}"

  if [ -n "${INSURANCE_API_PATH:-}" ]; then  addToSystemProperties "sentinel.insurance.api-path" "${INSURANCE_API_PATH}"; fi
  if [ -n "${INSURANCE_PORT:-}" ]; then addToSystemProperties "sentinel.insurance.port" "${INSURANCE_PORT}"; fi
  if [ -n "${MAGIC_ACCESS_TOKEN:-}" ]; then addToSystemProperties "access-token" "${MAGIC_ACCESS_TOKEN}"; fi

  # Skip adding these unless lab env
  if [[ "${SENTINEL_ENV^^}" =~ .*LAB$ ]]; then setUpOauthTest; fi

  java-tests \
    --module-name "insurance-tests" \
    --regression-test-pattern ".*IT\$" \
    --smoke-test-pattern ".*SearchIT\$" \
    $SYSTEM_PROPERTIES \
    $@

  exit $?
}

# =~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=

addToSystemProperties() {
  SYSTEM_PROPERTIES+=" -D$1=$2"
}

setUpOauthTest() {
  if [ -n "${OAUTH_AUD:-}" ]; then addToSystemProperties "system-oauth-robot.aud" "${OAUTH_AUD}"; fi
  if [ -n "${OAUTH_TOKEN_URL:-}" ]; then addToSystemProperties "system-oauth-robot.token-url" "${OAUTH_TOKEN_URL}"; fi
  if [ -n "${OAUTH_SCOPES:-}" ]; then addToSystemProperties "system-oauth-robot.scopes-csv" "${OAUTH_SCOPES}"; fi
  addToSystemProperties "system-oauth-robot.client-id" "${OAUTH_CLIENT_ID}"
  addToSystemProperties "system-oauth-robot.client-secret" "${OAUTH_CLIENT_SECRET}"
}

# =~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=

main $@
