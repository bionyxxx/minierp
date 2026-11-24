# Spring Boot 4 Project Guidance

## Stack

- Spring Boot 4.x, Spring Framework 7, Java 21
- Maven wrapper: `./mvnw`
- Jakarta EE 11, Jackson 3, and Boot 4 modular starters
- Use dedicated technology test starters rather than the classic test starter

## Project notes (MiniERP)

- Local JDK: `~/.jdks/openjdk-27` — prefix builds with `JAVA_HOME=~/.jdks/openjdk-27`.
  Target bytecode stays at Java 21 (`<java.version>21</java.version>`).
- Spring Boot parent version is `4.2.0` (the Initializr id `4.2.0.RELEASE` maps to the
  Maven artifact `4.2.0`; the `.RELEASE` id does not resolve from Maven Central).
- PostgreSQL 17 via `docker-compose.yml`; tests use Testcontainers (`postgres:17-alpine`).
- Schema lives in Flyway migrations `src/main/resources/db/migration` and is the source
  of truth; `spring.jpa.hibernate.ddl-auto: validate` checks mappings against it.
- Security is stateless JWT (HS256, `spring-boot-starter-oauth2-resource-server`).
  Login issues tokens; authority comes from the `role` claim.
- Seed users (dev): admin/Admin123!, manager/Manager123!, staff/Staff123!.
- Business rules are enforced in services (`ApprovalPolicy`, `StockService`), not in
  controllers or security rules. See README.md for the domain workflow.
- Hibernate snapshots entity state at persist time: always set every field (including
  document numbers) BEFORE `repository.save()`. Document numbers come from dedicated
  number sequences (`*NumberRepository#nextNumberValue()`), never from post-save updates.

## Commands

- Build: `./mvnw verify`
- Unit tests: `./mvnw test`
- Single test: `./mvnw -Dtest=ClassName test`
- Native tests: use the configured native profile; do not invent one

## Engineering Rules

- Inspect neighboring code and the dependency tree before choosing an API or starter.
- Keep controllers as adapters and transactions in application services.
- Use DTOs; do not expose Hibernate entities as external contracts.
- Use `tools.jackson` APIs for Jackson customization.
- Use `@MockitoBean`/`@MockitoSpyBean` and explicit test auto-configuration.
- Add Flyway migrations for schema changes and never edit applied migrations.
- Preserve error, security, pagination, nullability, and observability contracts.

## Skills

Load relevant skills from `.opencode/skills/`. Typical combinations:

- Upgrade: `spring-boot-migration`, `testing-pyramid`
- Endpoint: `rest-api-conventions`, `api-versioning`, `testing-pyramid`
- Persistence: `spring-data-jpa`, `transactional-patterns`, `flyway-migrations`
- Production: `production-observability`, `container-native-deployment`

## Verification

Run focused tests and then `./mvnw verify`. For native changes, test the produced native executable.
Report any verification that could not run.
