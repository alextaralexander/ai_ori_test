# Sequence description. Feature 026. Админ: RBAC и учетные записи

## Участники
- `Frontend /admin/rbac` - административный React UI, который строит доступность действий по permission matrix и i18n dictionaries.
- `Auth module` - источник session context, ролей, elevated state и termination affected sessions.
- `admin-rbac controller` - Spring MVC layer для `/api/admin/rbac`.
- `admin-rbac service` - orchestration создания учетных записей, расчета effective permissions, service account rotation и emergency deactivation.
- `admin-rbac security policy` - проверка роли, permission set, responsibility scope, MFA/elevated session и emergency policies.
- `admin-rbac DB` - persisted state модуля.
- `Audit publisher` - immutable audit trail для всех state-changing операций.
- `Integration client` - WMS/1C, сборка, доставка, платежи, бонусы или другая система, использующая service account credentials.

## Поток создания учетной записи и выдачи доступа
Суперадмин открывает `/admin/rbac`. Frontend запрашивает текущую auth-сессию и получает roles, permission matrix и elevated state. При создании внутренней учетной записи frontend вызывает `POST /api/admin/rbac/accounts`. Controller проверяет actor-а через `admin-rbac security policy`, service создает `admin_rbac_internal_account`, audit publisher пишет `ADMIN_ACCOUNT_CREATED`, а response возвращает DTO без паролей, MFA secrets или session secrets.

Перед сохранением ролей frontend вызывает `POST /accounts/{id}/permission-preview`. Service читает роли, permission sets и responsibility scopes, рассчитывает `effectivePermissions`, `conflicts`, `requiredMfa`, `affectedModules` и `auditPreview`. Preview не меняет persisted state.

После подтверждения frontend вызывает `PUT /accounts/{id}/roles` с `X-Elevated-Session-Id`, если requiredMfa=true или действие high risk. Controller проверяет elevated/MFA и scope, service обновляет `admin_rbac_account_role` и `admin_rbac_responsibility_scope`, audit publisher пишет `ADMIN_ROLE_ASSIGNED` и `ADMIN_PERMISSION_SET_CHANGED`, response возвращает обновленную effective permission matrix.

## Поток отказа вне scope
HR/операционный администратор может создавать и сопровождать сотрудников только в своем responsibility scope. Если он пытается назначить admin permission set или scope вне подразделения, security policy возвращает `STR_MNEMO_ADMIN_RBAC_SCOPE_DENIED`. Controller отвечает HTTP 403, данные не сохраняются, audit фиксирует отказ доступа без секретов.

## Security policies и service accounts
Администратор безопасности обновляет password, MFA или session policy через `PUT /security-policies`. Endpoint требует elevated session для high-risk изменений. Service увеличивает version policy, сохраняет `settings_json` и пишет `ADMIN_POLICY_CHANGED`.

Service accounts создаются и ротируются только администратором безопасности или суперадмином с elevated session. При rotation service помечает старый secret как `EXPIRING`, сохраняет hash нового secret, masked hint и возвращает `oneTimeSecret` только в текущем response. Frontend показывает secret один раз. Дальнейшие API-ответы содержат только masked hint, status и lastUsedAt.

Integration client использует service credentials для технических запросов. Admin-rbac service валидирует hash, status, expiresAt, allowedIpRanges и permission scopes. Disabled или expired service account получает `STR_MNEMO_ADMIN_RBAC_SERVICE_ACCOUNT_DISABLED`.

## Emergency deactivation и audit
Суперадмин запускает `POST /emergency-deactivations` с elevated session. Service деактивирует target account, role или permission set, запрашивает auth module завершить затронутые сессии и пишет `ADMIN_EMERGENCY_DEACTIVATED`. Новые запросы с отключенным доступом получают `STR_MNEMO_ADMIN_RBAC_ACCESS_DENIED`.

Аудитор вызывает `GET /audit-events` с фильтрами. Service возвращает immutable events с actor, target, diff, sourceIp, correlationId и occurredAt. Response не содержит passwords, MFA seed, service account secret или session token.

## Контракты сообщений и локализации
Backend возвращает только structured data и mnemonic-коды `STR_MNEMO_ADMIN_RBAC_*`. Frontend локализует все user-facing strings через `resources_*.ts`; компоненты не должны hardcode-ить тексты ошибок, статусов, кнопок, подсказок и empty states.

## Версионная база
Последовательность соответствует baseline Best Ori Gin на 27.04.2026: Spring MVC, Java/Spring Boot/Maven, Liquibase XML, Hibernate/JPA, MapStruct, Lombok, frontend TypeScript/React/Ant Design и обязательный i18n/runtime message contract.
