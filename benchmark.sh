#!/usr/bin/env bash

NUM_RUNS=${1:-3}
TIMESTAMP=$(date +"%Y-%m-%d-%H%M%S")
OUTPUT_CSV="results-$TIMESTAMP.csv"
SECTIONS_LIST=("Uber JAR" "Extracted executable JAR" "CDS" "AOT Cache" "AOT Cache + Spring Boot AOT")
TOTAL_SECTIONS=${#SECTIONS_LIST[@]}
TOTAL_RUNS=$((NUM_RUNS * TOTAL_SECTIONS))

cleanup_extracted() {
  rm -rf extracted
}

cleanup_all() {
  rm -rf extracted extracted-spring-aot
}

check_port_8080() {
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
}

run_uber_jar() {
  cleanup_all
  echo "Building Uber JAR..."
  ./mvnw clean package --projects news-app
  export JAVA_JAR_COMMAND="java -jar news-app/target/news-app-1.0.0.jar"
  ./collect-rss-cputime-startup.sh
}

run_self_extracting() {
  cleanup_extracted
  echo "Building Uber JAR..."
  ./mvnw clean package --projects news-app
  echo "Extracting executable JAR..."
  java -Djarmode=tools -jar news-app/target/news-app-1.0.0.jar extract --destination extracted >/dev/null 2>&1
  export JAVA_JAR_COMMAND="java -jar extracted/news-app-1.0.0.jar"
  ./collect-rss-cputime-startup.sh
}

run_cds() {
  echo "Building CDS..."
  java -XX:ArchiveClassesAtExit=extracted/application.jsa -Dspring.context.exit=onRefresh -jar extracted/news-app-1.0.0.jar >/dev/null 2>&1
  export JAVA_JAR_COMMAND="java -XX:SharedArchiveFile=extracted/application.jsa -jar extracted/news-app-1.0.0.jar"
  ./collect-rss-cputime-startup.sh
}

run_aot_cache() {
  echo "Building AOT cache..."
  java -XX:AOTCacheOutput=extracted/app.aot -Dspring.context.exit=onRefresh -jar extracted/news-app-1.0.0.jar >/dev/null 2>&1
  export JAVA_JAR_COMMAND="java -XX:AOTCache=extracted/app.aot -jar extracted/news-app-1.0.0.jar"
  ./collect-rss-cputime-startup.sh
}

run_aot_cache_spring_aot() {
  cleanup_all
  echo "Building AOT cache + Spring AOT..."
  ./mvnw clean -Pnative package --projects news-app
  java -Djarmode=tools -jar news-app/target/news-app-1.0.0.jar extract --destination extracted-spring-aot >/dev/null 2>&1
  java -XX:AOTCacheOutput=extracted-spring-aot/app.aot -Dspring.context.exit=onRefresh -jar extracted-spring-aot/news-app-1.0.0.jar >/dev/null 2>&1
  export JAVA_JAR_COMMAND="java -XX:AOTCache=extracted-spring-aot/app.aot -Dspring.aot.enabled=true -jar extracted-spring-aot/news-app-1.0.0.jar"
  ./collect-rss-cputime-startup.sh
}

declare -A SECTIONS=(
  ["Uber JAR"]="run_uber_jar"
  ["Extracted executable JAR"]="run_self_extracting"
  ["CDS"]="run_cds"
  ["AOT Cache"]="run_aot_cache"
  ["AOT Cache + Spring Boot AOT"]="run_aot_cache_spring_aot"
)

echo "Starting benchmark: $NUM_RUNS runs x $TOTAL_SECTIONS sections = $TOTAL_RUNS total executions"
echo "---"
check_port_8080
echo "Section,Run,RSS,Time,Startup" > "$OUTPUT_CSV"

counter=0
for run in $(seq 1 $NUM_RUNS); do
  for section in "${SECTIONS_LIST[@]}"; do
    counter=$((counter + 1))
    echo "[$counter/$TOTAL_RUNS] Run $run/$NUM_RUNS: $section"
    result=$(${SECTIONS[$section]})
    RSS=$(echo "$result" | tail -n 1 | cut -d',' -f1)
    TIME=$(echo "$result" | tail -n 1 | cut -d',' -f2)
    STARTUP=$(echo "$result" | tail -n 1 | cut -d',' -f3)
    echo "$section,$run,$RSS,$TIME,$STARTUP" >> "$OUTPUT_CSV"
    echo "  --> RSS=$RSS Time=$TIME Startup=$STARTUP"
  done
  sleep 2
done

echo "---"
echo "Computing averages..."
echo "" >> "$OUTPUT_CSV"
echo "Averages:" >> "$OUTPUT_CSV"

for section in "${SECTIONS_LIST[@]}"; do
  avg_rss=$(grep "^$section," "$OUTPUT_CSV" | cut -d',' -f3 | awk '{sum+=$1} END {printf "%.0f", sum/NR}')
  avg_time=$(grep "^$section," "$OUTPUT_CSV" | cut -d',' -f4 | sed 's/.*://' | awk '{sum+=$1} END {printf "%.2f", sum/NR}')
  avg_startup=$(grep "^$section," "$OUTPUT_CSV" | cut -d',' -f5 | awk '{sum+=$1} END {printf "%.3f", sum/NR}')
  echo "$section,avg,$avg_rss,$avg_time,$avg_startup" >> "$OUTPUT_CSV"
  echo "Avg $section: RSS=$avg_rss Time=$avg_time Startup=$avg_startup"
done

cleanup_all
echo ""
echo "Results saved to $OUTPUT_CSV"