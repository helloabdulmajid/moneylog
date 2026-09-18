#!/usr/bin/env bash
# Dev launcher for the MoneyLog backend.
# Required env (SMTP + JWT) is set here so mail always works.
# Usage: ./start-backend.sh            (foreground, logs to terminal)
#        ./start-backend.sh --detach   (background, logs to /tmp/opencode/moneylog-backend.log)
set -euo pipefail
cd "$(dirname "$0")"

export JWT_SECRET="c2VjcmV0LWtleS1mb3ItZGV2LWVudmlyb25tZW50LTIwMjQ="
export JWT_ACCESS_TOKEN_EXPIRATION=3600000
export JWT_REFRESH_TOKEN_EXPIRATION=604800000
export SMTP_USERNAME="helloabdulmajid@gmail.com"
export SMTP_PASSWORD="jpsd omla nunv dkcd"
export SMTP_HOST="smtp.gmail.com"
export SMTP_PORT=587
export MAIL_FROM="helloabdulmajid@gmail.com"

if [ "${1:-}" = "--detach" ]; then
  nohup mvn -o spring-boot:run > /tmp/opencode/moneylog-backend.log 2>&1 &
  echo "started (pid $!) -> /tmp/opencode/moneylog-backend.log"
else
  exec mvn -o spring-boot:run
fi