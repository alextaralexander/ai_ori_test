# Sequence diagram description. Feature 035

## Назначение
Диаграмма описывает основной runtime flow feature #35 для `/admin/identity`: поиск субъектов, открытие сводной карточки, изменение sponsor relationship и controlled impersonation session. Flow показывает, где выполняются RBAC checks, business validation, запись в базу и audit publishing.

## Участники
- `web-shell /admin/identity` - frontend workspace, использующий i18n dictionaries и masked response data.
- `AdminIdentityController` - Spring MVC controller owning module `admin-identity`.
- `AdminIdentityService` - orchestration layer для поиска, карточек, статусов, sponsor changes и impersonation.
- `PermissionGuard` - проверка role, permission scopes, PII access и impersonation mode.
- `SponsorPolicyService` - проверка MLM cycle, self-sponsor и impact preview.
- `ImpersonationPolicyService` - подбор policy, duration, allowed/forbidden actions и approval requirement.
- `admin-identity DB` - storage feature #35.
- `AuditPublisher` - публикация immutable audit events.

## Ключевые проверки
- Search и card endpoints возвращают только маскированные данные согласно permission scopes.
- Sponsor update запрещает циклы MLM, самоспонсорство и несогласованные effective dates.
- Impersonation session запускается только при policy match и reasonCode.
- Forbidden actions в impersonation mode отклоняются backend-ом с `STR_MNEMO_ADMIN_IDENTITY_IMPERSONATION_ACTION_FORBIDDEN`.
- Все state-changing операции пишут audit event с actor, target, reasonCode, old/new value и correlationId.

## Версионный baseline
Flow ориентирован на текущий monolith stack Best Ori Gin: Java 25, Spring Boot 4.0.6, Maven, Hibernate, MapStruct, Lombok, Liquibase XML, PostgreSQL, React, TypeScript, Vite и Ant Design. Backend package ownership соответствует политике `api/domain/db/impl`.
