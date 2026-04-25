# AGENTS

Primary process entrypoint for this repository (for a new feature, except `agents_app`): [agents/FEATURE_WORKFLOW__NEW.md](agents/FEATURE_WORKFLOW__NEW.md)
Все артефакты по аналитике в рамках workflow всегда создавай на русском языке!

## Operating Rule

- Any task that touches `agents_app/` must be executed directly from the user request.
- For `agents_app`, never use `agents/FEATURE_WORKFLOW__NEW.md` or `agents/FEATURE_WORKFLOW__REVISION.md`.
- Use `agents/FEATURE_WORKFLOW__NEW.md` as the entrypoint for every new task.
- Use `agents/FEATURE_WORKFLOW__REVISION.md` as the entrypoint for every existing task.
- Do not start implementation directly from an informal request when the task is expected to go through the managed delivery flow.
- Treat `agents/` as the process layer and the domain folders as the artifact layer.

## Repository Structure

- `agents/` - workflow for each feature implementation.
- `agents/analytics/` - analysis artifacts and supporting documents.
- `agents/architecture/` - architectural decisions, solution decomposition, and target designs.
- `agents/status/` - features statuses.
- `agents/tests/` - canonical API and UI test definitions plus synchronization metadata for executable test copies.
- `aisettings/` - AI-related project settings and support assets.
- `backend/` - Java microservices, shared platform libraries, archetypes, and backend build assets.
- `frontend/` - frontend applications and UI delivery layer.
- `infrastructure/` - Docker, Compose, Kubernetes manifests, ingress, and deployment scripts.

## Core Architectural Patterns

- Domain-oriented monolithic architecture. Each module represents a bounded business capability such as authorization, avatar, sparring, progress, or training engine.
- Multi-package service layout inside each backend module. A typical backend module is split into `api`, `domain`, `db`, and `impl` packages with fixed responsibilities.
- Environment-specific configuration. Service runtime configuration is usually split into `application.yml`, `application-local.yml`, `application-test.yml`, and `bootstrap.yml`.
- Database change management by module. Liquibase changelogs are isolated in the `db` package of the owning backend module.
- Container-first deployment model. Monolithic service is packaged as Spring Boot applications, containerized, and deployed through Docker and Kubernetes manifests.
- Centralized platform dependency management. Backend versions and common dependency/plugin configuration must be governed from a shared backend build baseline rather than duplicated per module.

## Liquibase Format Policy

- Liquibase changesets are always stored in XML format.
- New Liquibase changesets must be created as XML changelog files under the owning module `db` package.
- For every new feature, create a dedicated Liquibase changelog file for that feature instead of appending the feature changes to an existing shared module changelog.
- If a feature touches multiple backend modules, create a separate dedicated Liquibase changelog file in each affected module `db` package.
- Do not keep accumulating unrelated new feature changes in the same module changelog file over time.
- Do not introduce Liquibase changesets in SQL, YAML, JSON, or other formats unless an explicit compatibility exception is documented and approved.

## Technology Version Policy

- For every new task, architecture decision, and implementation stream, use the latest stable version of each technology available at the task start date unless an explicit compatibility constraint is documented and approved.
- Do not inherit legacy repository versions by default just because they already exist in older artifacts or code.
- Record the chosen version baseline in the task artifacts whenever the task introduces or upgrades technology-sensitive parts of the stack.
- If a task cannot use the latest stable version, document the blocking compatibility reason, the approved fallback version, and the upgrade follow-up task.
- This policy applies to backend, frontend, infrastructure, data, storage, and media/runtime tooling, including Java, Spring Boot, Maven, Hibernate, MapStruct, Lombok, TypeScript, React, Ant Design, Docker, Docker Compose, Helm, Kubernetes, PostgreSQL, S3 or MinIO, and Rive.

## Text Encoding Policy

- All repository text files must be stored in UTF-8 encoding.
- This rule is mandatory for source code, configuration, documentation, scripts, templates, SQL, YAML, JSON, Markdown, CSV, and Gherkin files.
- Russian text should be easy to read.
- Russian text must be written and stored only in UTF-8 so it remains readable across editors, terminals, Git diffs, CI logs, generated artifacts, and published documents.
- Any change that adds or edits Russian text must preserve human-readable Cyrillic text. Mojibake such as `Р`, `С`, `Ð`, `Ñ`, replacement glyphs, or similar broken output is not allowed in committed content.
- After updating files with Russian text, verify the affected lines are still readable as proper Russian text in UTF-8 before finishing the task.
- When creating or updating files, do not save them in legacy encodings such as Windows-1251, KOI8-R, or UTF-16 unless a task explicitly documents and approves a compatibility exception.
- If a file is found in a non-UTF-8 encoding, the task that touches that file should normalize it to UTF-8 unless an approved exception says otherwise.
- Automated normalization may change only byte-level encoding markers such as UTF-8 BOM unless the user explicitly approves content recovery for a specific file set.
- Heuristic or bulk auto-recovery of broken text content is not allowed as a default repository-wide operation. Restore from version control first; apply manual or file-scoped repair only after verification.

## Test Naming Policy

- All new automated test classes in this repository must have names ending with the `Test` suffix.
- Do not create new test classes with suffixes such as `Tests`, `IT`, `ITCase`, or any other alternative naming pattern unless an explicit repository exception is documented and approved.
- When an existing task touches a test class that does not end with `Test`, rename it to the `*Test` convention as part of that change unless an approved exception says otherwise.

## Test Synchronization Policy

- `agents/tests/` is the source of truth for managed API and UI tests created by the feature workflow.
- Executable test files inside `backend/` and `frontend/` are derived copies synchronized from `agents/tests/`.
- Synchronization targets and path mapping must be declared explicitly in `agents/tests/targets.yml`.
- Agents must create and edit managed tests in `agents/tests/` first, then synchronize them into runtime test locations defined in `agents/tests/targets.yml`.
- Managed test copies inside `backend/` and `frontend/` must include a marker comment that they are synchronized artifacts and must not be edited manually.
- If a synchronized target file diverges from its canonical source, the agent must either overwrite it from `agents/tests/` or stop and report the drift explicitly.
- Managed end-to-end tests in `agents/tests/api/end_to_end` and `agents/tests/ui/end_to_end` must execute or aggregate the real per-feature managed tests for every implemented feature included in the green path.
- Placeholder assertions that only mention feature ids, filenames, or hardcoded strings instead of invoking the underlying feature tests are forbidden for managed end-to-end coverage.

## BA End-To-End Artifact Policy

- `agents/analytics/ba/end_to_end/` stores product-level end-to-end artifacts for the E-Trainer platform itself, not the feature delivery workflow.
- `end_to_end_process_bpmn.plantuml` must describe the business and user flow through the platform across the implemented feature set, using platform actors such as student, parent, administrator, trainer configuration, onboarding, plan generation, training, progress, sparring, and override flows where applicable.
- `end_to_end_process_description.md` must describe the same platform business flow in textual form and must stay aligned with the BPMN artifact.
- `end_to_end_bdd_scenarios.gherkin` must contain green-path end-to-end BDD scenarios for platform behavior and user outcomes, not for feature authoring, artifact generation, status tracking, or agent orchestration.
- Artifacts in `agents/analytics/ba/end_to_end/` must be updated whenever a feature changes the actual platform journey, actors, transitions, or cross-feature integration points.
- It is forbidden to describe in these files the process of creating features, running agent workflows, managing `feature_status_*.md`, invoking `START` or `RESTART`, or any other delivery-process mechanics.

## Frontend I18N Policy

- This policy is mandatory for every frontend module under `frontend/`.
- All new user-facing frontend strings must be defined through the project i18n dictionaries and resolved from there at runtime.
- Do not hardcode new user-facing text in React components, route manifests, form rules, alerts, placeholders, labels, buttons, page metadata, or any other frontend runtime artifact.
- Every new i18n key must be added for all currently supported frontend languages in the same task.
- A frontend change that introduces or updates user-facing text is not complete until the corresponding dictionary entries exist for every supported language.
- When replacing or deleting text, keep the i18n dictionaries aligned in all supported languages and remove obsolete keys when they are no longer used.

## Backend To Frontend Message Contract Policy

- This policy is mandatory for every backend module that serves data to `frontend/`.
- Backend must not send hardcoded user-facing text to frontend through API responses, error payloads, validation payloads, or any other runtime transport contract.
- If backend needs to communicate a predefined user-facing message to frontend, it must send a mnemonic code in upper case with the `STR_MNEMO_` prefix.
- The frontend must resolve such mnemonic codes to localized text from its `resources_*.ts` dictionaries.
- Every new backend mnemonic code that can reach frontend must be added to all currently supported frontend languages in the same task.
- Strings loaded from the database or formed dynamically from business data may be sent to frontend as data when they are not predefined hardcoded backend UI messages.

## Frontend React Type Policy

- This policy is mandatory for every frontend module under `frontend/`.
- Do not type React component return values as `JSX.Element`.
- For explicit component return types, use `ReactElement` imported from `react`, or rely on TypeScript return type inference when explicit annotation is unnecessary.
- This rule prevents namespace-resolution problems across React and TypeScript version changes.

## Backend Package Policy

- This policy is mandatory for every backend module under `backend/`.
- `api` contains external contracts, REST request/response DTOs, and module-facing API types.
- `domain` contains JPA entities and repository interfaces only.
- `db` contains Liquibase changelogs only.
- `impl` contains all service interfaces, service implementations, controllers, security wiring, validators, mappers, event publishers, configuration, and runtime orchestration logic.
- Do not keep concrete runtime classes directly in the root of `impl` when they belong to a specific technical role.
- Inside `impl`, place classes in dedicated technical subpackages such as `controller`, `service`, `config`, `security`, `exception`, `mapper`, `event`, `validator`, `scheduler`, `client`, or another role-specific package when that better matches the responsibility.
- New backend runtime code must be added to the matching `impl/<role>` package, not to `impl` root.
- Do not place JPA entities or repositories in `db`.
- Do not place service interfaces or service implementations in `domain`.
- New analytics, architecture, implementation, and handoff artifacts must describe backend package ownership using this policy.

## Monolith Swagger/OpenAPI Policy

- This policy is mandatory for every backend module under `backend/monolith/`.
- Swagger/OpenAPI for the monolith is generated automatically from Spring MVC controllers at runtime through `springdoc-openapi`.
- Each monolith module must have its own dedicated OpenAPI group and Swagger UI entrypoint.
- The canonical URLs are:
  - OpenAPI JSON: `/v3/api-docs/<module-key>`
  - Swagger UI: `/swagger-ui/<module-key>`
- `<module-key>` must match the module folder name and the `MonolithModule.moduleKey()` value, for example `common-auth` or `chess-trainer`.
- Controllers for a monolith module must stay inside the owning module package prefix declared in `MonolithModule.packagePrefix()` and inside the module `impl` package.
- Any new endpoint added under the owning module package prefix is required to appear in that module Swagger automatically without manual Swagger registration.
- Do not create manual per-endpoint Swagger routing, hardcoded endpoint lists, or separate ad hoc documentation registries for monolith modules.
- When adding a new monolith module, update the central monolith Swagger grouping configuration together with `MonolithModule` so the module gets its dedicated `/v3/api-docs/<module-key>` and `/swagger-ui/<module-key>` URLs immediately.

## Guidance For Agents

- When creating or evolving a backend module, follow the mandatory `api/domain/db/impl` package policy and keep those responsibilities separated.
- Prefer reusing `library-*` modules for cross-cutting behavior before introducing duplicate local implementations.
- Keep service contracts, deployment descriptors, and configuration files aligned with code changes.
- If a task affects delivery flow, statuses, or handoffs, update artifacts under `agents/` in addition to product code.
- In analysis, architecture, implementation, and DevOps artifacts, explicitly state the version baseline or point to the artifact section where the latest-stable baseline for the task is recorded.

## Основной домен для этого приложения origin.com
- соответственно java пакеты и все остальное что нужно строить от этого домена

## Продуктовая идея этого приложения/продукта/платформы

Платформа OriGin предназначена для реализации онлайн площадки продажи косметики.
Основные функции:
- Онлайн маркетплэйс для косметики
- Multilevel marketing партеров покупающих косметику через этот маркетплэйс и продающих потом оффлайн
- Смена каталогов с косметикой каждые 3 недели
- Интеграция с WMS системой для получения остатков (с 1С)
- Интеграция системой сборки заказов (конвеер с косметикой)
- Интеграция системой доставки заказов (в том числе функции для владельцев точек выдачи)
- Админские функции для всего что есть на сайте и для управленияинтеграциями