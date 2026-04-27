# Feature module OpenAPI description. Feature 023. Module employee

## Назначение контракта
OpenAPI контракт feature #23 описывает employee-facing API для собственных настроек сотрудника и контролируемого elevated mode. Все endpoint-ы принадлежат employee module, публикуются под `/api/employee` и должны попадать в runtime Swagger group employee автоматически через Spring MVC controllers без ручной регистрации списков endpoint-ов.

## Profile settings endpoints
- `GET /profile-settings` возвращает summary employee-профиля: employeeId, displayName, employeeStatus, доступные sections, readiness flags, activeElevatedSession, securityWarnings и auditContext.
- `GET /profile-settings/general` возвращает general-раздел с displayName, jobTitle, departmentCode, preferredLanguage, timezone, notificationChannel, employeeStatus, version, updatedAt и auditRecorded.
- `PUT /profile-settings/general` обновляет general-раздел. Обязательны displayName, preferredLanguage, timezone и version. Конфликт version возвращает HTTP 409.
- `GET /profile-settings/contacts` возвращает список контактов с maskedValue и verificationStatus.
- `POST /profile-settings/contacts` создает контакт. `PUT /profile-settings/contacts/{contactId}` обновляет контакт. Plain value принимается только в request, в response возвращается maskedValue.
- `GET /profile-settings/addresses` и `POST /profile-settings/addresses` управляют адресами office, pickup point, remote work и legal.
- `GET /profile-settings/documents` и `POST /profile-settings/documents` управляют metadata документов. Файл хранится вне API через существующий S3/MinIO слой; контракт принимает `fileReferenceId`.
- `GET /profile-settings/security` возвращает MFA state, lastPasswordChangedAt, activeSessionCount, riskFlags, recentEvents, allowedActions и auditRecorded без секретных значений.

## Super-user endpoints
- `GET /super-user` возвращает dashboard elevated mode: policies, activeSession, pendingRequests, history и auditRecorded.
- `POST /super-user/requests` создает elevated request. Обязательны policyCode, reasonCode, reasonText, targetScope и requestedDurationMinutes. linkedDocumentId используется, если policy требует подтверждающий документ.
- `POST /super-user/requests/{requestId}/approve` доступен супервайзеру и переводит request в approved state, создавая active elevated session.
- `POST /super-user/requests/{requestId}/reject` доступен супервайзеру и отклоняет request с decision comment.
- `POST /super-user/sessions/{sessionId}/close` завершает session сотрудником или отзывает session супервайзером; успешный ответ HTTP 204.

## DTO и валидация
- `EmployeeProfileSettingsSummaryResponse` агрегирует состояние employee-настроек и активного elevated mode.
- `EmployeeProfileGeneralUpdateRequest` требует непустые displayName, preferredLanguage, timezone и актуальный version.
- `EmployeeContactUpsertRequest` требует contactType, value и version. Backend валидирует тип и формат, но не возвращает исходный value.
- `EmployeeAddressUpsertRequest` требует addressType, regionCode, city, addressLine и version; validTo не может быть раньше validFrom.
- `EmployeeDocumentCreateRequest` требует documentType, maskedNumber и fileReferenceId.
- `EmployeeElevatedRequestCreateRequest` требует reasonText длиной от 3 до 1000 символов и requestedDurationMinutes от 1 до 480; фактический максимум policy может быть меньше.
- `EmployeeElevatedSessionResponse` возвращает remainingSeconds, allowedLinkedOperations и approvedBy, чтобы frontend показывал active/pending/expired state.

## Error contract и STR_MNEMO
Backend не передает predefined user-facing текст. Ошибки возвращаются в `EmployeeErrorResponse.code` как mnemonic-коды:
- `STR_MNEMO_EMPLOYEE_ACCESS_DENIED` - пользователь не является разрешенным employee actor.
- `STR_MNEMO_EMPLOYEE_SUPER_USER_FORBIDDEN` - нет доступа к `/employee/super-user`.
- `STR_MNEMO_EMPLOYEE_PROFILE_INVALID` - невалидный general-раздел.
- `STR_MNEMO_EMPLOYEE_CONTACT_INVALID` - невалидный контакт.
- `STR_MNEMO_EMPLOYEE_ADDRESS_INVALID` - невалидный адрес.
- `STR_MNEMO_EMPLOYEE_DOCUMENT_INVALID` - невалидная metadata документа.
- `STR_MNEMO_EMPLOYEE_ELEVATED_REQUEST_INVALID` - невалидный elevated request.
- `STR_MNEMO_EMPLOYEE_ELEVATED_POLICY_DENIED` - policy запрещает elevated mode.
- `STR_MNEMO_EMPLOYEE_ELEVATED_SESSION_EXPIRED` - session истекла, закрыта или отозвана.
- `STR_MNEMO_EMPLOYEE_VERSION_CONFLICT` - optimistic version conflict.

Каждый mnemonic-код, который может попасть во frontend, должен быть добавлен во все текущие frontend dictionaries `resources_*.ts` в рамках реализации.

## Security и audit context
Все endpoint-ы проверяют авторизованного employee actor, роль, scope, MFA state там, где это требуется, и active lock flags. Изменяющие операции пишут audit trail с actorUserId, targetEmployeeId, sourceRoute, actionCode, policyCode, elevatedRequestId/elevatedSessionId, correlationId и occurredAt. Связанные order, claim, partner, delivery и support flows получают elevatedSessionId и correlationId в audit context, но не получают hardcoded UI messages.

## Package ownership
- DTO request/response и enum-like API constants находятся в `backend/.../employee/api`.
- JPA entities и repository interfaces находятся в `backend/.../employee/domain`.
- Liquibase XML changelog feature #23 находится в `backend/.../employee/db` и подключается к module changelog.
- Controllers находятся в `impl/controller`, service contracts/implementations в `impl/service`, validators в `impl/validator`, mappers в `impl/mapper`, security/policy checks в `impl/security`, audit publishers в `impl/event` или `impl/audit`.

## Версионная база
Контракт рассчитан на baseline задачи 27.04.2026: Java/Spring Boot/Maven monolith, springdoc runtime Swagger group, PostgreSQL, Liquibase XML, frontend TypeScript/React/Ant Design, i18n dictionaries и backend-to-frontend mnemonic contract `STR_MNEMO_*`. Фича не требует отдельного технологического upgrade decision.