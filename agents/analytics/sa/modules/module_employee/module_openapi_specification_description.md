# Module OpenAPI specification description. Employee

## Назначение
Employee API обслуживает backoffice/call-center контуры Best Ori Gin: поиск рабочего контекста, операторский заказ, поддержку проблемного заказа, супервизорские эскалации и операторскую историю заказов feature 020.

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

## DTO, validation and message contract
DTO находятся в `com.bestorigin.monolith.employee.api.EmployeeDtos`. Backend возвращает mnemonic-коды `STR_MNEMO_EMPLOYEE_ACCESS_DENIED`, `STR_MNEMO_EMPLOYEE_QUERY_INVALID`, `STR_MNEMO_EMPLOYEE_ORDER_NOT_FOUND`, `STR_MNEMO_EMPLOYEE_ORDER_HISTORY_FILTER_INVALID`, `STR_MNEMO_EMPLOYEE_AUDIT_RECORDED`. Пользовательские тексты локализуются во frontend dictionaries.

## Swagger
Модуль employee публикуется в `/v3/api-docs/employee` и `/swagger-ui/employee` через monolith module metadata. Ручные endpoint registry не требуются.

## Version baseline
Baseline на 27.04.2026: Java 25, Spring Boot 4.0.6, Maven 4-compatible build, TypeScript/React/Ant Design из текущего web-shell без понижения версий.