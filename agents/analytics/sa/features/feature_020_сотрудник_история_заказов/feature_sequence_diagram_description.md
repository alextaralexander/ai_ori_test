# Feature 020. Sequence diagram description

## Список заказов
Сотрудник backoffice открывает `/employee/order-history` во frontend web-shell. Frontend вызывает `GET /api/employee/order-history` с фильтрами по партнеру, клиенту, периоду, статусам, `problemOnly`, query, page, size и sort. `EmployeeController` извлекает user context из заголовков и передает запрос в `EmployeeService`.

`EmployeeService` проверяет employee-доступ и валидность фильтров. При ошибке доступа возвращается `STR_MNEMO_EMPLOYEE_ACCESS_DENIED`, при ошибке фильтров - `STR_MNEMO_EMPLOYEE_ORDER_HISTORY_FILTER_INVALID`. При успешной проверке сервис читает `EmployeeOrderHistoryRepository`, который выбирает snapshot-ы из `employee_order_history_snapshot`. После чтения сервис записывает audit event `ORDER_HISTORY_LIST_VIEWED` с фильтрами и sourceRoute `/employee/order-history`.

## Детали заказа
При открытии `/employee/order-history/:orderId` frontend вызывает `GET /api/employee/order-history/{orderId}`. Сервис ищет snapshot, позиции и audit-события. Если snapshot устарел, сервис может сверить текущий статус со связанными источниками: order module, payment system, claims module и WMS/1C. Эти системы не становятся владельцами employee API; они остаются источниками доменных событий.

Ответ `EmployeeOrderHistoryDetailsResponse` содержит summary заказа, позиции, оплату, доставку, WMS, support cases, claims, payment events, flags `manualAdjustmentPresent` и `supervisorRequired`, а также доступный audit trail. После успешного чтения записывается `ORDER_DETAILS_VIEWED`.

## Переходы и audit
Когда сотрудник переходит из деталей в support, claim, payment event или partner card, frontend сохраняет order context в route/query state. Сервис фиксирует `ORDER_HISTORY_DEEP_LINK_OPENED` с target route и безопасными идентификаторами. Эти события не публикуются в клиентский timeline.

## Supervisor scope
Супервизор использует тот же endpoint деталей, но `EmployeeService` определяет роль `supervisor` и возвращает расширенный audit scope: actorUserId, actorRole, actionType, sourceRoute, supportReasonCode и occurredAt. Обычный сотрудник получает только разрешенный минимум audit-информации.

## Ошибки и локализация
Все предопределенные сообщения передаются как mnemonic-коды `STR_MNEMO_*`. Frontend преобразует их в пользовательский текст через `resources_ru.ts` и `resources_en.ts`. Backend не возвращает hardcoded user-facing текст в error payload, timeline или linked events.