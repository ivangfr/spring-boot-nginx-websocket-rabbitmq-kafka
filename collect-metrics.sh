#!/usr/bin/env bash

URL="http://localhost:8080/actuator/health"
LOG_FILE=$(mktemp)

cleanup() {
  rm -f "$LOG_FILE"
}
trap cleanup EXIT

$JAVA_JAR_COMMAND > "$LOG_FILE" 2>&1 &
export MY_PID=$!

START_TIME=$EPOCHREALTIME

while [ "$(curl -s -o /dev/null -w '%{http_code}' "$URL")" != "200" ]; do
  sleep 0.001
done

END_TIME=$EPOCHREALTIME
TIME_TO_READY=$(echo "scale=3; $END_TIME - $START_TIME" | bc)

sleep 0.5

RSS=$(ps -p "$MY_PID" -o rss= | xargs)
CPUTIME=$(ps -p "$MY_PID" -o cputime= | xargs | awk -F: '
NF==3 {printf "%.2f", $1*3600+$2*60+$3}
NF==2 {printf "%.2f", $1*60+$2}
NF==1 {printf "%.2f", $1}
')
RSS_CPUTIME="$RSS,$CPUTIME"

STARTUP_TIME=$(grep "Started" "$LOG_FILE" | grep -oE 'in [0-9.]+ seconds' | grep -oE '[0-9.]+')

echo "RSS,CPUTIME,STARTUP_TIME,TIME_TO_READY"
echo "$RSS_CPUTIME,$STARTUP_TIME,$TIME_TO_READY"

kill -9 "$MY_PID" 2>/dev/null
wait "$MY_PID" 2>/dev/null