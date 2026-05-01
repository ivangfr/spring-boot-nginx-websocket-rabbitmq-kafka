# AGENTS.md

## Project Structure
- Parent POM (Java 25, Spring Boot 4.0.6) with single `news-app` module
- Event-driven architecture: WebSocket + RabbitMQ (broadcast) + Kafka (reactions by news ID) + PostgreSQL
- Nginx load balances across 3 news-app instances (see `docker-compose.yml`)

## Commands
```bash
./mvnw verify --projects news-app    # integration tests (uses Testcontainers)
./mvnw spotless:check                # formatting check (Google Java Format + Prettier)
./mvnw spotless:apply                # auto-fix formatting
./build-docker-images.sh             # build news-app Docker image
podman compose up -d                 # start all infra (not docker compose)
```

## Formatting
- Enforced during `verify` phase via Spotless
- Java: 2-space indent, max 100 chars, Google style
- Import order: `java,jakarta,org,com` (not default)
- HTML in `src/main/resources/static/**/*.html` formatted with Prettier

## Testing
- Integration tests use `maven-failsafe-plugin` with `reuseForks=false`
- Flaky tests rerun up to 2 times (`rerunFailingTestsCount=2`)
- Testcontainers manages Kafka, PostgreSQL, RabbitMQ — no external services needed for `verify`
- Playwright + Awaitility for async/browser tests

## Prerequisites
- Java 25+ (unusual—verify with `java -version`)
- Podman (project uses `podman compose`, not `docker compose`)
- Bash 4.0+ (macOS ships 3.2; `brew install bash`)

## Benchmarks
- `./benchmark.sh [runs]` — automated benchmark of 5 configurations
- Requires `podman compose up -d zookeeper kafka rabbitmq postgres` first
- See `BENCHMARKING.md` and `manual-steps.md` for details

## Infra Notes
- Kafdrop UI: http://localhost:9000
- RabbitMQ UI: http://localhost:15672 (guest/guest)
- Simulate: add `127.0.0.1 news-app.lb` to `/etc/hosts`, then access http://news-app.lb
