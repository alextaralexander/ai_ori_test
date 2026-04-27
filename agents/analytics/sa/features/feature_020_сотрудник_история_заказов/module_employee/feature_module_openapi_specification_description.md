# Feature 020. Module employee OpenAPI description

## Назначение API
Feature 020 расширяет `Best Ori Gin Employee API` двумя endpoint-ами операторской истории заказов. Контракт обслуживает frontend-маршруты `/employee/order-history` и `/employee/order-history/:orderId`, сохраняет employee audit и возвращает только коды/данные, а не hardcoded пользовательские тексты.

## `GET /api/employee/order-history`
Назначение: получить постраничную employee-выборку заказов для backoffice-оператора.

Параметры:
- `partnerId`, `customerId` - точные фильтры по участникам заказа.
- `dateFrom`, `dateTo` - период создания/обновления заказа; `dateFrom > dateTo` запрещен.
- `orderStatus`, `paymentStatus`, `deliveryStatus` - фильтры статусов.
- `problemOnly` - включает только заказы с проблемными признаками.
- `query` - поиск по order number, customer id, partner id, телефону или email.
- `page`, `size`, `sort` - постраничная выдача; `size` ограничен 100.

Успешный ответ `EmployeeOrderHistoryPageResponse` содержит `items`, `page`, `size`, `totalElements`, `auditRecorded` и доступные problem-фильтры. Каждый item содержит маскированные контакты, статусы, сумму, problem flags и `linkedRoutes` для деталей, поддержки, claim, payment events и partner card.

Ошибки:
- 400 `STR_MNEMO_EMPLOYEE_ORDER_HISTORY_FILTER_INVALID` для невалидного периода, размера страницы или статуса.
- 403 `STR_MNEMO_EMPLOYEE_ACCESS_DENIED` для пользователя без employee-прав.

Audit: успешный вызов записывает `ORDER_HISTORY_LIST_VIEWED` с actorUserId, actorRole, фильтрами и sourceRoute `/employee/order-history`.

## `GET /api/employee/order-history/{orderId}`
Назначение: получить расширенную карточку заказа для сотрудника или супервизора.

Path parameter `orderId` принимает внутренний `orderId` или номер заказа. Ответ `EmployeeOrderHistoryDetailsResponse` расширяет summary и содержит:
- `items` с SKU, названием, количеством, ценой, promoCode, bonusPoints и reserveStatus.
- `paymentEvents`, `deliveryEvents`, `wmsEvents` как связанные события с `messageCode`.
- `supportCaseIds`, `claimIds`, `paymentEventIds`, `wmsBatchId`, `deliveryTrackingId`.
- `manualAdjustmentPresent`, `supervisorRequired`, `sourceChannel`.
- `auditEvents`, объем которых зависит от роли: supervisor получает расширенные события, обычный employee - разрешенный минимум.

Ошибки:
- 403 `STR_MNEMO_EMPLOYEE_ACCESS_DENIED`.
- 404 `STR_MNEMO_EMPLOYEE_ORDER_NOT_FOUND` без раскрытия внутренних идентификаторов.

Audit: успешный вызов записывает `ORDER_DETAILS_VIEWED`. Переходы из карточки в support, claims и payment events должны записывать `ORDER_HISTORY_DEEP_LINK_OPENED` в сервисном слое или при последующем endpoint-вызове.

## DTO и mnemonic-коды
Новые DTO размещаются в `com.bestorigin.monolith.employee.api.EmployeeDtos` как record-ы. Backend возвращает только коды: `STR_MNEMO_EMPLOYEE_ACCESS_DENIED`, `STR_MNEMO_EMPLOYEE_ORDER_NOT_FOUND`, `STR_MNEMO_EMPLOYEE_ORDER_HISTORY_FILTER_INVALID`, `STR_MNEMO_EMPLOYEE_AUDIT_RECORDED`. Frontend обязан локализовать их через `resources_ru.ts` и `resources_en.ts`.

## Swagger
Контроллеры остаются в `com.bestorigin.monolith.employee.impl.controller`; module metadata employee уже должен попадать в `/v3/api-docs/employee` и `/swagger-ui/employee` без ручной регистрации endpoint-ов.