# Module OpenAPI specification description. Employee

## Назначение
Employee API обслуживает backoffice/call-center контуры Best Ori Gin: поиск рабочего контекста, операторский заказ, поддержку проблемного заказа, супервизорские эскалации, операторскую историю заказов feature #20, employee-претензии feature #21, карточку партнера и отчеты feature #22, employee profile settings и контролируемый elevated mode feature #23.

## Feature 019 endpoints
- `GET /api/employee/workspace` ищет клиента, партнера или заказ по рабочему запросу сотрудника.
- `POST /api/employee/operator-orders` создает операторский заказ.
- `POST /api/employee/operator-orders/{operatorOrderId}/confirm` подтверждает операторский заказ.
- `GET /api/employee/order-support/{orderNumber}` открывает поддержку проблемного заказа.
- `POST /api/employee/order-support/{orderNumber}/notes`, `/adjustments`, `/escalations` сохраняют служебные действия.
- `GET /api/employee/supervisor/escalations` возвращает эскалации супервизору.

## Feature 020 endpoints
- `GET /api/employee/order-history` возвращает страницу заказов с фильтрами `partnerId`, `customerId`, `dateFrom`, `dateTo`, `orderStatus`, `paymentStatus`, `deliveryStatus`, `problemOnly`, `query`, `page`, `size`, `sort`.
- `GET /api/employee/order-history/{orderId}` возвращает расширенные детали заказа: позиции, totals, связанные payment/delivery/WMS/support/claim события, flags и audit trail.

## Feature 021 endpoints
- `POST /api/employee/submit-claim` создает претензию сотрудником от имени клиента или партнера. Запрос принимает `Idempotency-Key`, customer/partner context, order context, `supportReasonCode`, выбранные позиции, проблему, ожидаемое решение, вложения и комментарий.
- `GET /api/employee/claims` возвращает страницу employee claims с фильтрами `claimStatus`, `dateFrom`, `dateTo`, `slaState`, `responsibleRole`, `assigneeId`, `resolutionType`, `sourceChannel`, `warehouseCode`, `financeStatus`, `query`, `page`, `size`, `sort`.
- `GET /api/employee/claims/{claimId}` возвращает детальную карточку employee-претензии.
- `POST /api/employee/claims/{claimId}/transitions` выполняет допустимый transition: склад, финансы, customer support, supervisor approval, approve/reject/complete.

## Feature 022 endpoints
- `GET /api/employee/partner-card` ищет и открывает карточку партнера по query, supportReasonCode и optional regionCode.
- `GET /api/employee/partner-card/{partnerId}` открывает карточку партнера по partnerId.
- `GET /api/employee/report/order-history` возвращает отчет истории заказов партнера с фильтрами partner/person/campaign/status/problem/page/sort.

## Feature 023 endpoints
- `GET /api/employee/profile-settings` возвращает summary настроек employee-профиля, sections, readiness flags, active elevated session, securityWarnings и auditContext.
- `GET /api/employee/profile-settings/general` и `PUT /api/employee/profile-settings/general` читают и обновляют general-раздел. Request требует displayName, preferredLanguage, timezone и version.
- `GET /api/employee/profile-settings/contacts` и `POST /api/employee/profile-settings/contacts` читают и создают контакты. Контактное значение принимается в request, но response возвращает только maskedValue.
- `GET /api/employee/profile-settings/addresses` и `POST /api/employee/profile-settings/addresses` управляют адресами office, pickup point, remote work и legal.
- `GET /api/employee/profile-settings/documents` и `POST /api/employee/profile-settings/documents` управляют metadata документов с fileReferenceId из S3/MinIO abstraction.
- `GET /api/employee/profile-settings/security` возвращает MFA state, active sessions, risk flags и recent events без секретов.
- `GET /api/employee/super-user` возвращает dashboard elevated mode: policies, activeSession, pendingRequests и audit history.
- `POST /api/employee/super-user/requests` создает elevated request с policyCode, reasonCode, reasonText, targetScope, requestedDurationMinutes и optional linkedDocumentId.
- `POST /api/employee/super-user/requests/{requestId}/approve` и `/reject` используются супервайзером для решения pending request.
- `POST /api/employee/super-user/sessions/{sessionId}/close` завершает или отзывает elevated session и возвращает HTTP 204.

## DTO, validation and message contract
DTO находятся в `api` employee module. Backend возвращает только mnemonic-коды `STR_MNEMO_*`, включая уже существующие коды employee features #19-#22 и новые коды feature #23: `STR_MNEMO_EMPLOYEE_SUPER_USER_FORBIDDEN`, `STR_MNEMO_EMPLOYEE_PROFILE_INVALID`, `STR_MNEMO_EMPLOYEE_CONTACT_INVALID`, `STR_MNEMO_EMPLOYEE_ADDRESS_INVALID`, `STR_MNEMO_EMPLOYEE_DOCUMENT_INVALID`, `STR_MNEMO_EMPLOYEE_ELEVATED_REQUEST_INVALID`, `STR_MNEMO_EMPLOYEE_ELEVATED_POLICY_DENIED`, `STR_MNEMO_EMPLOYEE_ELEVATED_SESSION_EXPIRED`, `STR_MNEMO_EMPLOYEE_VERSION_CONFLICT`.

Валидации feature #23 проверяют employee-доступ, scope, locks, MFA state, allowed policies, максимальный срок elevated session, document requirement, optimistic version и состояние request/session. Ошибки не раскрывают секреты, чужие идентификаторы и не содержат hardcoded user-facing текстов.

## Swagger
Модуль employee публикуется в `/v3/api-docs/employee` и `/swagger-ui/employee` через monolith module metadata. Ручные endpoint registry не требуются; новые Spring MVC controllers в package prefix employee должны автоматически попадать в module group.

## Package ownership
- `api` - DTO request/response, module-facing API types и enum-like constants.
- `domain` - JPA entities и repository interfaces.
- `db` - Liquibase XML changelogs, включая отдельный changelog feature #23.
- `impl/controller` - Spring MVC controllers.
- `impl/service` - service contracts и implementations.
- `impl/validator`, `impl/security`, `impl/mapper`, `impl/event`, `impl/exception` - role-specific runtime code.

## Version baseline
Baseline на 27.04.2026: Java/Spring Boot/Maven monolith, springdoc runtime Swagger group, PostgreSQL, Liquibase XML, TypeScript/React/Ant Design frontend, i18n dictionaries и backend-to-frontend mnemonic contract `STR_MNEMO_*`. Feature #23 не требует downgrade или отдельного runtime framework.