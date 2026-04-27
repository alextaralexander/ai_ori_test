# OpenAPI description. Feature 035 / module_admin-identity

## Назначение API
OpenAPI спецификация описывает административный REST-контур `/api/admin/identity`, который обслуживает workspace `/admin/identity` и связанные действия feature #35. Контур объединяет поиск субъектов, карточки пользователей/партнеров/сотрудников, изменение статусов, eligibility rules, sponsor relationships, employee bindings, impersonation policy/session и audit search.

## Группы endpoints
- `AdminIdentitySubjects` - единый поиск, чтение карточки, смена статуса, eligibility rules.
- `AdminIdentityPartners` - sponsor relationship и partner-specific changes.
- `AdminIdentityEmployees` - employee role bindings, operational scopes и role conflict checks.
- `AdminIdentityImpersonation` - policy management, start/finish session.
- `AdminIdentityAudit` - поиск audit events по subject, actor, action, reason и периоду.

## Контракт сообщений
Backend не возвращает hardcoded пользовательский текст для предопределенных ошибок. Все управляемые отказы передаются в `MnemonicError.code` с префиксом `STR_MNEMO_ADMIN_IDENTITY_*`. Frontend обязан отображать эти коды через `resources_ru.ts` и `resources_en.ts`.

Ключевые mnemonic-коды:
- `STR_MNEMO_ADMIN_IDENTITY_FORBIDDEN_ACTION`
- `STR_MNEMO_ADMIN_IDENTITY_INVALID_STATUS_TRANSITION`
- `STR_MNEMO_ADMIN_IDENTITY_ELIGIBILITY_CONFLICT`
- `STR_MNEMO_ADMIN_IDENTITY_INVALID_OFFICE_BINDING`
- `STR_MNEMO_ADMIN_IDENTITY_ROLE_CONFLICT`
- `STR_MNEMO_ADMIN_IDENTITY_IMPERSONATION_ACTION_FORBIDDEN`
- `STR_MNEMO_ADMIN_IDENTITY_EXPORT_FORBIDDEN`

## RBAC и security
Каждый endpoint проверяет:
- роль и permission scopes actor-а;
- subject type и региональный/операционный scope;
- PII access для чтения и экспорта персональных данных;
- impersonation mode и запрещенные действия;
- reasonCode для всех state-changing операций.

Frontend скрывает недоступные actions, но backend остается источником истины. При отказе backend возвращает `403` или `409` с mnemonic-кодом.

## Data flow
1. Frontend вызывает search endpoint и получает только маскированные данные, разрешенные текущими scopes.
2. При открытии карточки backend агрегирует subject, profile attributes, partner summary, employee bindings, eligibility rules и audit preview.
3. State-changing endpoint выполняет validation, RBAC, business constraints, сохраняет изменение и пишет audit event.
4. Impersonation session добавляет actor/target context и блокирует forbidden actions на backend.
5. Audit endpoint возвращает неизменяемую историю без раскрытия секретов и лишних PII.

## Версионный baseline
Спецификация ориентирована на текущий monolith stack Best Ori Gin: Java 25, Spring Boot 4.0.6, Maven, Hibernate, MapStruct, Lombok, Liquibase XML, PostgreSQL, React, TypeScript, Vite и Ant Design. OpenAPI для monolith должен генерироваться автоматически через `springdoc-openapi` из controllers module `admin-identity`; ручные per-endpoint Swagger routes не используются.
