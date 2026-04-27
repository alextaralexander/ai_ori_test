# Module OpenAPI description. admin-identity

## Назначение
`module_openapi_specification.yml` описывает полный API module `admin-identity` после feature #35. Runtime Swagger monolith должен генерироваться из Spring MVC controllers автоматически через `springdoc-openapi`; ручные per-endpoint Swagger registrations не используются.

## Endpoints
- `GET /subjects` - поиск пользователей, партнеров, сотрудников и организаций с маскированием PII.
- `GET /subjects/{subjectId}` - сводная карточка субъекта.
- `POST /subjects/{subjectId}/status` - смена статуса с `reasonCode`.
- `POST /partners/{partnerSubjectId}/sponsor-relationships` - изменение sponsor relationship с проверкой MLM cycle.
- `PUT /employees/{employeeSubjectId}/bindings` - обновление ролей и operational scopes сотрудника.
- `GET /impersonation/policies` и `POST /impersonation/policies` - чтение и сохранение controlled impersonation policy.
- `POST /impersonation/sessions` - запуск session.
- `GET /audit-events` - поиск immutable audit events.

## DTOs и validation
State-changing requests требуют `reasonCode`. Status changes проверяют допустимый переход. Sponsor changes проверяют sponsor existence, отсутствие self-sponsor, отсутствие cycles и effective period. Employee bindings проверяют role conflict rules. Impersonation policy не должна разрешать платежи, вывод бонусов, смену пароля или PII update без elevated scope.

## Mnemonic contract
Все предопределенные backend-to-frontend сообщения используют `STR_MNEMO_ADMIN_IDENTITY_*`. Минимальный набор:
- `STR_MNEMO_ADMIN_IDENTITY_FORBIDDEN_ACTION`
- `STR_MNEMO_ADMIN_IDENTITY_INVALID_STATUS_TRANSITION`
- `STR_MNEMO_ADMIN_IDENTITY_ELIGIBILITY_CONFLICT`
- `STR_MNEMO_ADMIN_IDENTITY_ROLE_CONFLICT`
- `STR_MNEMO_ADMIN_IDENTITY_IMPERSONATION_ACTION_FORBIDDEN`
- `STR_MNEMO_ADMIN_IDENTITY_EXPORT_FORBIDDEN`

## Security
Backend проверяет RBAC, subject type, region/operational scope, PII access и impersonation mode. Frontend скрывает недоступные actions, но backend остается источником истины.

## Версионный baseline
Текущий baseline: Java 25, Spring Boot 4.0.6, Maven, Hibernate, MapStruct, Lombok, Liquibase XML, PostgreSQL, React, TypeScript, Vite, Ant Design.
