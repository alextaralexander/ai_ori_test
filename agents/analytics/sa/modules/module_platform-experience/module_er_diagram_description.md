# Module ER description. platform-experience

## Назначение модуля
`platform-experience` является платформенным backend module для сквозного frontend experience: consent preferences, notification preferences, analytics diagnostics, i18n missing key diagnostics и runtime config snapshots. На момент создания module вводится feature #25, поэтому полный ER artifact совпадает с feature ER scope.

## Entity ownership
- `platform_consent_preference` - хранит consent state пользователя по policy version.
- `platform_notification_preference` - хранит notification/offline preferences по пользователю и локали.
- `platform_analytics_diagnostic_event` - хранит diagnostic metadata analytics adapters.
- `platform_i18n_missing_key_event` - хранит missing key diagnostics.
- `platform_runtime_config_snapshot` - фиксирует опубликованную runtime config для frontend providers и diagnostics.

## Constraints
- Все идентификаторы - UUID.
- Все временные поля - `timestamptz`.
- Все optimistic locking fields - `version integer`.
- Consent preferences имеют unique constraint `(subject_user_id, policy_version)`.
- Notification preferences имеют unique constraint `(subject_user_id, locale)`.
- Runtime config имеет unique constraint `(environment_code, config_version)`.
- Diagnostic tables индексируются по времени, каналу, correlationId и i18n key.

## Data protection
Модуль не хранит локализованные UI-тексты, токены, пароли, платежные данные, документы, полные адреса или marketing pixel payload. Diagnostic events принимают только техническую metadata: channel, eventCode, status, reasonCode, route, role, correlationId и timestamps.

## Liquibase
Changelog создается отдельным XML-файлом в package `com.bestorigin.monolith.platformexperience.db`. Изменения feature #25 не добавляются в changelog других modules.

## Package ownership
- `api` - REST DTO и публичные contracts.
- `domain` - JPA entities и repository interfaces.
- `db` - Liquibase XML changelog.
- `impl/controller` - Spring MVC controllers.
- `impl/service` - application services.
- `impl/validator` - request validation.
- `impl/config` - module config и Swagger group metadata.
- `impl/mapper` - DTO/domain mapping.

## Версионная база
Модуль рассчитан на технологическую базу 27.04.2026: Java/Spring Boot/Maven monolith, Hibernate, Liquibase XML, PostgreSQL, MapStruct, Lombok, Spring MVC, springdoc-openapi и frontend TypeScript/React/Ant Design. Новые технологические версии не вводятся.
