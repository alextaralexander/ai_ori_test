# Module OpenAPI specification description. Employee

## Назначение
Employee API обслуживает backoffice/call-center контуры Best Ori Gin: поиск рабочего контекста, операторский заказ, поддержку проблемного заказа, супервизорские эскалации, операторскую историю заказов feature #20 и employee-претензии feature #21.

## Feature 019 endpoints
- `GET /api/employee/workspace` ищет клиента, партнера или заказ.
- `POST /api/employee/operator-orders` создает операторский заказ.
- `POST /api/employee/operator-orders/{operatorOrderId}/confirm` подтверждает операторский заказ.
- `GET /api/employee/order-support/{orderNumber}` открывает поддержку проблемного заказа.
- `POST /api/employee/order-support/{orderNumber}/notes`, `/adjustments`, `/escalations` сохраняют служебные действия.
- `GET /api/employee/supervisor/escalations` возвращает эскалации супервизору.

## Feature 020 endpoints
- `GET /api/employee/order-history` возвращает страницу заказов с фильтрами `partnerId`, `customerId`, `dateFrom`, `dateTo`, `orderStatus`, `paymentStatus`, `deliveryStatus`, `problemOnly`, `query`, `page`, `size`, `sort`.
- `GET /api/employee/order-history/{orderId}` возвращает расширенные детали заказа: позиции, totals, связанные payment/delivery/WMS/support/claim события, flags и audit trail.

## Feature 021 endpoints
- `POST /api/employee/submit-claim` создает претензию сотрудником от имени клиента или партнера. Запрос принимает `Idempotency-Key`, customer/partner context, order context, `supportReasonCode`, выбранные позиции, проблему, ожидаемое решение, вложения и комментарий. Успешный ответ возвращает `EmployeeClaimDetailsResponse` с claimNumber, SLA, компенсацией, route tasks, audit и availableActions.
- `GET /api/employee/claims` возвращает страницу employee claims с фильтрами `claimStatus`, `dateFrom`, `dateTo`, `slaState`, `responsibleRole`, `assigneeId`, `resolutionType`, `sourceChannel`, `warehouseCode`, `financeStatus`, `query`, `page`, `size`, `sort`. Контакты возвращаются только маскированными.
- `GET /api/employee/claims/{claimId}` возвращает детальную карточку employee-претензии: заказ, клиента или партнера, позиции, вложения metadata, компенсацию, route tasks склада/финансов/support/supervisor, timeline и audit trail.
- `POST /api/employee/claims/{claimId}/transitions` выполняет допустимый transition: `SEND_TO_WAREHOUSE_REVIEW`, `SEND_TO_FINANCE_REFUND`, `SEND_TO_CUSTOMER_SUPPORT`, `REQUEST_SUPERVISOR_APPROVAL`, `APPROVE_COMPENSATION`, `REJECT_CLAIM`, `COMPLETE_CLAIM`.

## DTO, validation and message contract
DTO находятся в `com.bestorigin.monolith.employee.api.EmployeeDtos` или выделенных api record-файлах employee module. Backend возвращает mnemonic-коды `STR_MNEMO_EMPLOYEE_ACCESS_DENIED`, `STR_MNEMO_EMPLOYEE_QUERY_INVALID`, `STR_MNEMO_EMPLOYEE_ORDER_NOT_FOUND`, `STR_MNEMO_EMPLOYEE_ORDER_HISTORY_FILTER_INVALID`, `STR_MNEMO_EMPLOYEE_AUDIT_RECORDED`, `STR_MNEMO_EMPLOYEE_CLAIM_VALIDATION_FAILED`, `STR_MNEMO_EMPLOYEE_CLAIM_FILTER_INVALID`, `STR_MNEMO_EMPLOYEE_CLAIM_NOT_FOUND`, `STR_MNEMO_EMPLOYEE_CLAIM_TRANSITION_INVALID`, `STR_MNEMO_EMPLOYEE_CLAIM_CREATED`, `STR_MNEMO_EMPLOYEE_CLAIM_SENT_TO_WAREHOUSE`, `STR_MNEMO_EMPLOYEE_CLAIM_SENT_TO_FINANCE`, `STR_MNEMO_EMPLOYEE_CLAIM_SUPERVISOR_REQUIRED`, `STR_MNEMO_EMPLOYEE_CLAIM_COMPLETED`. Пользовательские тексты локализуются во frontend dictionaries.

Валидации feature #21 проверяют employee-доступ, обязательный `supportReasonCode`, tenant scope, доступность заказа, доступность claim по позиции, `quantity > 0`, лимиты компенсации, допустимость transition и idempotency. Ошибки не раскрывают чужие идентификаторы и не содержат hardcoded user-facing текстов.

## Swagger
Модуль employee публикуется в `/v3/api-docs/employee` и `/swagger-ui/employee` через monolith module metadata. Ручные endpoint registry не требуются.

## Version baseline
Baseline на 27.04.2026: Java 25, Spring Boot 4.0.6, Maven 4-compatible build, TypeScript/React/Ant Design из текущего web-shell без понижения версий.
