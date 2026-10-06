# java-spring-clean-events (English summary)

> **A sample / reference code project — NOT a production-ready product.** No authentication, no rate limiting, no hardening.

A reference implementation of **Clean Architecture** on **Java 21 + Spring Boot 3.5** for an events system. The full
documentation is in Portuguese ([README.md](README.md), [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), [docs/adr](docs/adr)).

## What you get

- Domain: **Events** (`DRAFT -> PUBLISHED -> CANCELLED/FINISHED`, venue value object), **Participants** (unique e-mail) and
  **Registrations** (only in published events, never above capacity, no duplicate active registration per event,
  cancelling frees the seat). Waitlist is intentionally omitted (YAGNI, listed as next step).
- **Layers as Maven modules**, so the compiler enforces the dependency rule: `domain` (zero dependencies) <- `application`
  (use cases + ports, no Spring/JPA) <- `infrastructure` (Spring Data JPA, MySQL, Flyway) and `web` (REST + composition root).
- **ArchUnit** tests that fail the build when the rule is broken.
- **Unit tests** (JUnit 5 + AssertJ, in-memory fakes, no mocking framework): **100 % line and branch coverage** in `domain`
  and `application`, enforced as a JaCoCo gate. Integration/API tests run against a real MySQL; their coverage is reported only.
- REST with Bean Validation, RFC 7807 `ProblemDetail` errors, springdoc OpenAPI and Actuator health.
- A concurrency test proves that simultaneous registrations never exceed an event's capacity (row lock).

## Run it

```bash
make up          # MySQL 8.4 + API (Docker only) -> http://localhost:8090/swagger-ui.html
make test-unit   # unit tests only, no database
make test        # unit + integration tests (starts MySQL by itself), enforces the 100 % gate
make coverage    # same + per-module coverage table
make down
```

`make` uses your local `mvn` if present, otherwise the official Maven + JDK 21 container. Host ports are 8090 (API) and 3316
(MySQL) to avoid clashing with a local MySQL on 3306. Dev passwords in `docker-compose.yml` / `.env.example` are
**development-only**.

## Using it as a template

See "Como adicionar um novo caso de uso" in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md): domain rule (test first) ->
use case + port -> adapter -> endpoint -> tests.

License: [MIT](LICENSE) © André Coura
