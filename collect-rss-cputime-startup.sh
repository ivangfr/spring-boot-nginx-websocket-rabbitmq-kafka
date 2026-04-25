#!/usr/bin/env bash

URL="http://localhost:8080/actuator/health"
LOG_FILE=$(mktemp)

cleanup() {
  rm -f "$LOG_FILE"
}
trap cleanup EXIT

if [ -z "$JAVA_JAR_COMMAND" ]; then
  echo "Error: JAVA_JAR_COMMAND environment variable is not set"
  echo "Please set it before running this script, e.g.:"
  echo "  export JAVA_JAR_COMMAND='java -jar myapp.jar'"
  exit 1
fi

EXISTING_PID=$(lsof -i :8080 -sTCP:LISTEN -t 2>/dev/null)
if [ -n "$EXISTING_PID" ]; then
  echo "Port 8080 is in use by process $EXISTING_PID. Kill it? (y/n)"
  read -r answer
  if [ "$answer" = "y" ] || [ "$answer" = "Y" ]; then
    kill -9 "$EXISTING_PID" 2>/dev/null
    sleep 1
  else
    echo "Aborting."
    exit 1
  fi
fi

$JAVA_JAR_COMMAND > "$LOG_FILE" 2>&1 &
export MY_PID=$!

while [ "$(curl -s -o /dev/null -w '%{http_code}' "$URL")" != "200" ]; do
  sleep 0.001
done

sleep 0.5

RSS_CPUTIME=$(ps -p "$MY_PID" -o rss=,cputime= | xargs | tr ' ' ',')

STARTUP_TIME=$(grep "Started" "$LOG_FILE" | grep -oE 'in [0-9.]+ seconds' | grep -oE '[0-9.]+')

echo "RSS,CPUTIME,STARTUP_TIME"
echo "$RSS_CPUTIME,$STARTUP_TIME"

kill -9 "$MY_PID" 2>/dev/null
wait "$MY_PID" 2>/dev/null