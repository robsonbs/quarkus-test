# Copilot Instructions for `quarkus-test`

## Project Snapshot

- Quarkus 3.6.4 on Java 17, packaged with Maven Wrapper (`./mvnw`).
- PostgreSQL 15 database; schema managed exclusively by Flyway scripts in `src/main/resources/db/migration`.
- Web layer built with JAX-RS controllers returning Qute templates or JSON DTOs.
- Security uses Elytron JDBC form auth; most routes require `@RolesAllowed`.

## Architectural Conventions

- **Controllers** only orchestrate HTTP concerns: read query/form params, call services, prepare DTOs, select Qute templates, and redirect with flash-style query params (`success`, `error`, form fields). Controllers accessing the database **must** use `@Blocking` annotation.
- **Services** hold validation, ownership checks (via `SecurityIdentity`), password hashing, and call `AuditLogService.recordDomainEvent` for CRUD events.
- **DAOs** extend Panache; prefer repository helpers (e.g., `find("email", value)`) and declarative filters over ad-hoc SQL.
- **DTOs** back every form/API payload; update DTOs, services, templates, and controllers together when changing data shape.
- **Views** live under `src/main/resources/templates`; breadcrumbs come from `BreadcrumbItem` helpers. Keep user-facing copy in Portuguese to match existing UI.
- **Error handling**: controllers catch `WebApplicationException`, sanitize the message (`sanitizeMessage`, `safeValue` helpers), and preserve form input via query params.

## Qute Templates & User-Defined Tags

Shared template fragments live in `templates/tags/` and are invoked as **user-defined tags**:

```html
{#navigation /}
<!-- renders tags/navigation.html -->
{#breadcrumb /}
<!-- renders tags/breadcrumb.html -->
{#flash /}
<!-- renders tags/flash.html -->
```

**Do NOT use** `{@include ...}` or `{#include path/file.html /}` — these won't work for partials. Always place reusable fragments in `templates/tags/` and call them by tag name.

Example page structure (`users.html`):

```html
<body class="bg-gray-100 min-h-screen">
  {#navigation /}
  <div class="container mx-auto px-4 py-8">
    <div class="max-w-6xl mx-auto">{#breadcrumb /} {#flash /}</div>
    <!-- page content -->
  </div>
</body>
```

## Domain Modules

- **User & UserProfile**: admin-only CRUD (`UserController`, `UserProfileController`), password hashing with `BcryptUtil`, uniqueness validation in services.
- **Notes**: user-scoped CRUD with ownership checks; business logic ensures trimmed fields and localized validation strings.
- **Tasks**: status workflow (`TaskStatus` enum), due-date validation rejecting past dates, audit entries for every mutation.
- **Audit Logs**: persisted through `AuditLogService.recordDomainEvent`; searchable via paginated criteria in `AuditController`.
- **Home/Docs/Login**: `HomeController` serves `/`, `DocsController` serves `/docs`, `LoginController` serves `/login`. All use `@Blocking` when accessing DB.

## Persistence & Migrations

- Never rely on `import.sql`; all schema/data updates go into a new Flyway migration (`V{N}__description.sql`).
- When you change entities: update the JPA model, create a matching migration, adjust DAOs/services/templates/tests.
- Keep `quarkus.hibernate-orm.database.generation=validate` untouched so Flyway stays authoritative.

## Security & Auditing

- Controllers that mutate data must stay protected with `@RolesAllowed`; ensure both HTML and JSON routes enforce roles.
- Call `auditLogService.recordDomainEvent(...)` with meaningful action names and sanitized identifiers.
- Services derive the actor email from `SecurityIdentity`; preserve that pattern when adding new services.

## UX Patterns

- Form handlers redirect with `Response.seeOther` and set `success`/`error` query params plus form re-population values.
- Breadcrumbs follow `List.of(BreadcrumbItem.link(...), BreadcrumbItem.current(...))` pattern.
- Keep validation messages concise, Portuguese, and aligned with current wording (`"Título é obrigatório"`, etc.).
- Use Tailwind CSS utility classes consistent with existing pages.

## Testing & Local Environment

```bash
docker compose up -d postgres     # start database
./mvnw quarkus:dev                # dev mode at http://localhost:8080
./mvnw test                       # run all tests (44 tests)
```

- Demo accounts: `admin@example.com` / `user@example.com`, password `123`
- Tests assume running PostgreSQL; use `@QuarkusTest` for new services/endpoints.
- Do not touch `target/` or generated sources.

## Quick Reference

| Pattern                   | Example                                                  |
| ------------------------- | -------------------------------------------------------- |
| Controller with DB access | `@Path("/") @Blocking public class HomeController`       |
| Inject Qute template      | `@Inject Template index;` → returns `index.instance()`   |
| User-defined tag          | `{#navigation /}` → `templates/tags/navigation.html`     |
| Audit domain event        | `auditLogService.recordDomainEvent("TASK_CREATED", ...)` |
| Form redirect             | `Response.seeOther(URI.create("/tasks?success=..."))`    |

