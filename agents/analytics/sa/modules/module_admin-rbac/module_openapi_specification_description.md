# OpenAPI description. Feature 026. Module admin-rbac

## Назначение API
`admin-rbac` API обслуживает административный UI `/admin/rbac` и интеграционные проверки доступа. Все endpoint-ы находятся под `/api/admin/rbac`, входят в monolith OpenAPI group `admin-rbac` и должны появляться в runtime Swagger автоматически через Spring MVC controllers внутри `com.bestorigin.monolith.adminrbac.impl.controller`.

## Endpoint groups

### Accounts
- `GET /accounts` ищет внутренние учетные записи по query, status и department.
- `POST /accounts` создает учетную запись сотрудника. Требует `X-Correlation-Id`; для рискованных операций может требовать `X-Elevated-Session-Id`.
- `GET /accounts/{accountId}` возвращает карточку учетной записи.
- `PATCH /accounts/{accountId}` обновляет профильные поля и status metadata с optimistic version.
- `PUT /accounts/{accountId}/roles` заменяет роли, permission sets и responsibility scopes сотрудника.
- `POST /accounts/{accountId}/permission-preview` возвращает effective permission matrix без сохранения.
- `POST /accounts/{accountId}/block` блокирует учетную запись и завершает активные сессии.
- `POST /accounts/{accountId}/deactivate` деактивирует учетную запись без удаления audit trail.

### Roles and permission sets
- `GET /roles` возвращает role catalog и permission sets для UI assignment controls.

### Security policies
- `GET /security-policies` возвращает active password, MFA, session и emergency policies.
- `PUT /security-policies` обновляет policy и возвращает новую версию с `auditRecorded=true`.

### Service accounts
- `GET /service-accounts` ищет технические учетные записи интеграций.
- `POST /service-accounts` создает service account и возвращает one-time secret.
- `POST /service-accounts/{serviceAccountId}/rotate-secret` ротирует secret и показывает новый secret один раз.

### Emergency deactivation
- `POST /emergency-deactivations` отключает account, role или permission set в аварийном режиме и прекращает затронутый доступ.

### Audit
- `GET /audit-events` ищет immutable audit trail по actorUserId, targetUserId, actionCode, period и correlationId.

## DTO и validation
- `InternalAccountCreateRequest` требует `fullName`, `email`, `department`, `positionTitle`, `accountType`; email валидируется как email и должен быть уникальным.
- `InternalAccountUpdateRequest` наследует поля create request и требует `version` для optimistic locking.
- `AccountAccessUpdateRequest` содержит `roleCodes`, `permissionSetCodes`, `responsibilityScopes`.
- `ResponsibilityScopeRequest` поддерживает region, warehouse, pickup point, catalog, product category, department и partner structure segment.
- `EffectivePermissionPreviewResponse` содержит `effectivePermissions`, `conflicts`, `requiredMfa`, `affectedModules`, `auditPreview`.
- `SecurityPolicyUpdateRequest` требует `policyType`, `policyCode`, `settings`, `version`.
- `ServiceAccountCreateRequest` требует `code`, `ownerUserId`, `integrationType`, `permissionScopes`, `allowedIpRanges`; `expiresAt` optional.
- `ServiceAccountSecretResponse` возвращает `oneTimeSecret` только при create/rotate.
- `AuditEventResponse` возвращает immutable diff без секретов.

## HTTP-коды и mnemonic-коды
- `200` для чтения, update и preview.
- `201` для создания account и service account.
- `204` для block, deactivate и emergency deactivation без response body.
- `400` для validation errors с `STR_MNEMO_ADMIN_RBAC_POLICY_INVALID` или другим `STR_MNEMO_ADMIN_RBAC_*`.
- `401` для отсутствующей или истекшей сессии.
- `403` для запрета доступа: `STR_MNEMO_ADMIN_RBAC_ACCESS_DENIED`, `STR_MNEMO_ADMIN_RBAC_SCOPE_DENIED`, `STR_MNEMO_ADMIN_RBAC_SERVICE_ACCOUNT_DISABLED`.
- `404` для разрешенного пользователя при отсутствии target.
- `409` для конфликтов: `STR_MNEMO_ADMIN_RBAC_ACCOUNT_ALREADY_EXISTS`, `STR_MNEMO_ADMIN_RBAC_PERMISSION_CONFLICT`.

Backend не возвращает predefined user-facing текст. Frontend обязан разрешать mnemonic-коды через `resources_*.ts`.

## Security и audit
Каждый endpoint проверяет session context, role, permission set, responsibility scope и elevated session/MFA policy. Все state-changing endpoint-ы пишут audit event с actor, target, actionCode, oldValue, newValue, sourceIp, correlationId и occurredAt. Secret values, passwords, MFA seed и session tokens не попадают в response и audit.

## Package ownership
- `api`: DTO и request/response contracts.
- `domain`: JPA entities и repository interfaces.
- `db`: отдельный Liquibase XML changelog feature #26.
- `impl/controller`: Spring MVC controllers.
- `impl/service`: orchestration, effective permission calculation, service account rotation.
- `impl/security`: RBAC/elevated/MFA policy checks.
- `impl/validator`: request и conflict validation.
- `impl/mapper`: MapStruct mappers.
- `impl/audit`: audit event publisher/serializer.
- `impl/config`: module config и Swagger grouping metadata.

## Версионная база
Спецификация соответствует backend baseline Best Ori Gin на 27.04.2026: Spring MVC generated OpenAPI, Java records/DTOs, PostgreSQL jsonb, Liquibase XML, Hibernate/JPA, MapStruct и Lombok. Module key `admin-rbac`, package prefix `com.bestorigin.monolith.adminrbac`.
