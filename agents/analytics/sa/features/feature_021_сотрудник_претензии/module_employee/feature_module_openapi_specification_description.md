# Feature 021. Module employee OpenAPI description

## Назначение
Feature #21 расширяет `Best Ori Gin Employee API` ресурсами employee-претензий для frontend-маршрутов `/employee/submit-claim`, `/employee/claims-history` и `/employee/claims-history/:claimId`. Контракт обслуживает операторское создание претензии от имени клиента или партнера, историю employee claims, детальную карточку, SLA, маршруты согласования, складские и финансовые задачи, customer support и audit trail.

## `POST /api/employee/submit-claim`
Назначение: создать претензию сотрудником по обращению клиента или партнера.

Заголовки:
- `Idempotency-Key` обязателен и защищает от дублей при повторной отправке.
- Auth/user context извлекается из backoffice-сессии или тестовых заголовков проекта.

Тело `EmployeeClaimCreateRequest` содержит `customerId` или `partnerId`, `orderId` или `orderNumber`, `sourceChannel`, обязательный `supportReasonCode`, `requestedResolution`, комментарий, вложения и список позиций. Каждая позиция содержит `sku`, `productCode`, `quantity`, `problemType`, `requestedResolution` и optional internal comment.

Успешный ответ:
- `201` для нового кейса.
- `200` для повторного idempotency key с возвратом уже созданной претензии.
- `EmployeeClaimDetailsResponse` содержит claimId, claimNumber, orderNumber, customer/partner context, status, slaState, slaDueAt, compensationAmount, publicReasonMnemonic, supervisorRequired, items, attachments, routeTasks, auditEvents и availableActions.

Ошибки:
- `400 STR_MNEMO_EMPLOYEE_CLAIM_VALIDATION_FAILED` для невалидного заказа, позиции, количества, решения, вложения или отсутствующего основания обращения.
- `403 STR_MNEMO_EMPLOYEE_ACCESS_DENIED` для пользователя без employee-прав.

Audit: успешное создание записывает `EMPLOYEE_CLAIM_CREATED` с actorUserId, actorRole, supportReasonCode, sourceRoute `/employee/submit-claim`, orderId, claimId и correlationId.

## `GET /api/employee/claims`
Назначение: получить постраничную операционную очередь employee claims.

Фильтры: `claimStatus`, `dateFrom`, `dateTo`, `slaState`, `responsibleRole`, `assigneeId`, `resolutionType`, `sourceChannel`, `warehouseCode`, `financeStatus`, `query`, `page`, `size`, `sort`.

Правила:
- Пустой запрос возвращает актуальную очередь за период по умолчанию.
- `query` поддерживает claimNumber, orderNumber, customerId, partnerId, телефон и email; персональные контакты возвращаются только как `maskedContact`.
- Невалидные даты, page/size или статусы возвращают `STR_MNEMO_EMPLOYEE_CLAIM_FILTER_INVALID`.

Ответ `EmployeeClaimPageResponse` содержит items, page, size, totalElements, auditRecorded и availableFilters. Каждый item содержит claimId, claimNumber, orderNumber, customerOrPartnerLabel, maskedContact, status, slaState, slaDueAt, resolutionType, compensationAmount, currencyCode, assignee, responsibleRole, updatedAt и availableActions.

Audit: успешный вызов записывает `EMPLOYEE_CLAIM_LIST_VIEWED`.

## `GET /api/employee/claims/{claimId}`
Назначение: получить детальную карточку employee-претензии.

Параметры:
- `claimId` обязателен.
- `supportReasonCode` обязателен для audit context.

Ответ `EmployeeClaimDetailsResponse` агрегирует:
- summary кейса, заказа, клиента или партнера;
- позиции и решения на уровне позиции;
- вложения metadata без приватных S3/MinIO paths;
- компенсацию, валюту, publicReasonMnemonic и supervisorRequired;
- routeTasks по складу, финансам, customer support и supervisor approval;
- auditEvents, объем которых зависит от роли сотрудника;
- availableActions как machine-readable transition/action codes.

Ошибки:
- `403 STR_MNEMO_EMPLOYEE_ACCESS_DENIED`.
- `404 STR_MNEMO_EMPLOYEE_CLAIM_NOT_FOUND` без раскрытия чужих идентификаторов.

Audit: успешный вызов записывает `EMPLOYEE_CLAIM_DETAILS_VIEWED`.

## `POST /api/employee/claims/{claimId}/transitions`
Назначение: провести претензию по маршруту обработки.

Тело `EmployeeClaimTransitionRequest` содержит `transitionCode`, `supportReasonCode`, optional `assigneeId`, `resultCode`, `comment` и `approvedCompensationAmount`.

Поддерживаемые transition-коды:
- `SEND_TO_WAREHOUSE_REVIEW`;
- `SEND_TO_FINANCE_REFUND`;
- `SEND_TO_CUSTOMER_SUPPORT`;
- `REQUEST_SUPERVISOR_APPROVAL`;
- `APPROVE_COMPENSATION`;
- `REJECT_CLAIM`;
- `COMPLETE_CLAIM`.

Успешный ответ возвращает обновленный `EmployeeClaimDetailsResponse`. Backend сам рассчитывает итоговый статус исполнения по результатам складской, финансовой и support задач; frontend не должен выводить итог из разрозненных полей.

Ошибки:
- `400 STR_MNEMO_EMPLOYEE_CLAIM_TRANSITION_INVALID` для недопустимого перехода или суммы.
- `403 STR_MNEMO_EMPLOYEE_ACCESS_DENIED`.
- `404 STR_MNEMO_EMPLOYEE_CLAIM_NOT_FOUND`.

Audit: успешный transition записывает `EMPLOYEE_CLAIM_TRANSITION_APPLIED`, а supervisor approval дополнительно фиксирует `EMPLOYEE_CLAIM_SUPERVISOR_APPROVED`.

## Mnemonic-коды
Backend не отправляет hardcoded user-facing тексты во frontend. Для feature #21 используются:
- `STR_MNEMO_EMPLOYEE_ACCESS_DENIED`;
- `STR_MNEMO_EMPLOYEE_CLAIM_VALIDATION_FAILED`;
- `STR_MNEMO_EMPLOYEE_CLAIM_FILTER_INVALID`;
- `STR_MNEMO_EMPLOYEE_CLAIM_NOT_FOUND`;
- `STR_MNEMO_EMPLOYEE_CLAIM_TRANSITION_INVALID`;
- `STR_MNEMO_EMPLOYEE_CLAIM_CREATED`;
- `STR_MNEMO_EMPLOYEE_CLAIM_SENT_TO_WAREHOUSE`;
- `STR_MNEMO_EMPLOYEE_CLAIM_SENT_TO_FINANCE`;
- `STR_MNEMO_EMPLOYEE_CLAIM_SUPERVISOR_REQUIRED`;
- `STR_MNEMO_EMPLOYEE_CLAIM_COMPLETED`.

Frontend обязан добавить эти ключи во все поддерживаемые dictionaries и использовать локализацию для заголовков, фильтров, статусов, кнопок, ошибок и empty states.

## Package ownership и Swagger
DTO должны быть размещены в `com.bestorigin.monolith.employee.api.EmployeeDtos` или выделенных api record-файлах. Controller остается в `com.bestorigin.monolith.employee.impl.controller`, service interfaces и implementation - в `com.bestorigin.monolith.employee.impl.service`, validators - в `impl.validator`, mapper - в `impl.mapper`, security checks - в `impl.security`. Domain snapshots/repositories размещаются только в `com.bestorigin.monolith.employee.domain`, Liquibase marker - в `com.bestorigin.monolith.employee.db`.

Endpoint-ы должны попадать в dedicated Swagger group employee по каноническим URL `/v3/api-docs/employee` и `/swagger-ui/employee` через module metadata, без ручной регистрации отдельных endpoint-ов.

## Тестовые требования
Managed API test должен покрывать login сотрудника, создание претензии, idempotency, историю с фильтрами, SLA state, детали, transition в склад/финансы/support, supervisor approval, forbidden и validation error. Managed UI test должен покрывать три employee-маршрута, фильтры, открытие деталей, действие решения, empty и forbidden states. Runtime-копии тестов синхронизируются только из `agents/tests/`.
