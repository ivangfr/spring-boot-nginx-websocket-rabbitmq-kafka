# Manual Steps

Reference:

- [Optimizing Microservices Startup - Spring Boot Project Leyden](https://javadevtech.com/2025/06/11/optimizing-microservices-startup-spring-boot-project-leyden/)
- [YouTube Video](https://www.youtube.com/watch?v=h16ngaxx0zo)

## Uber JAR

```bash
./mvnw clean package --projects news-app
```

```bash
export JAVA_JAR_COMMAND="java -jar news-app/target/news-app-1.0.0.jar"
./collect-metrics.sh
```

## Extracted executable JAR

```bash
java -Djarmode=tools -jar news-app/target/news-app-1.0.0.jar extract --destination extracted
```

```bash
export JAVA_JAR_COMMAND="java -jar extracted/news-app-1.0.0.jar"
./collect-metrics.sh
```

## CDS

```bash
java -XX:ArchiveClassesAtExit=extracted/application.jsa -Dspring.context.exit=onRefresh -jar extracted/news-app-1.0.0.jar
```

```bash
export JAVA_JAR_COMMAND="java -XX:SharedArchiveFile=extracted/application.jsa -jar extracted/news-app-1.0.0.jar"
./collect-metrics.sh
```

## AOT Cache

```bash
java -XX:AOTCacheOutput=extracted/app.aot -Dspring.context.exit=onRefresh -jar extracted/news-app-1.0.0.jar
```

```bash
export JAVA_JAR_COMMAND="java -XX:AOTCache=extracted/app.aot -jar extracted/news-app-1.0.0.jar"
./collect-metrics.sh
```

## AOT Cache + Spring Boot AOT

```bash
./mvnw clean -Pnative package --projects news-app
```

```bash
java -Djarmode=tools -jar news-app/target/news-app-1.0.0.jar extract --destination extracted-spring-aot
```

```bash
java -XX:AOTCacheOutput=extracted-spring-aot/app.aot -Dspring.context.exit=onRefresh -jar extracted-spring-aot/news-app-1.0.0.jar
```

```bash
export JAVA_JAR_COMMAND="java -XX:AOTCache=extracted-spring-aot/app.aot -Dspring.aot.enabled=true -jar extracted-spring-aot/news-app-1.0.0.jar"
./collect-metrics.sh
```