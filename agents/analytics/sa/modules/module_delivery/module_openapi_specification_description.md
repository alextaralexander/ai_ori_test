# module_delivery OpenAPI specification description

## Назначение API
module_delivery предоставляет runtime API для выбора доставки в checkout, поиска точек выдачи, создания shipment после подтверждения заказа, показа tracking timeline, операций владельца ПВЗ и приема статусов от внешнего delivery contour.

Swagger/OpenAPI для monolith должен генерироваться автоматически через springdoc-openapi. Для module_delivery требуется отдельная группа:
- OpenAPI JSON: `/v3/api-docs/delivery`
- Swagger UI: `/swagger-ui/delivery`
- `MonolithModule.moduleKey()` = `delivery`
- `MonolithModule.packagePrefix()` = `com.bestorigin.monolith.delivery`

## Endpoint ownership

### `GET /api/delivery/options`
Возвращает доступные способы доставки для checkout по `orderDraftId` и опциональному городу. Endpoint используется покупателем, партнером и сотрудником, который оформляет заказ по обращению клиента.

Ответ содержит `DeliveryOptionDto[]` с `method`, `available`, `priceAmount`, `currency`, `expectedReceiveAt` и `unavailableReasonCode`. Предопределенная причина недоступности передается только mnemonic-кодом, например `STR_MNEMO_DELIVERY_PICKUP_POINT_UNAVAILABLE`.

### `GET /api/delivery/pickup-points`
Ищет активные точки выдачи по городу и региону. Возвращает справочные бизнес-данные ПВЗ: код, название, адрес, график, контакты, срок хранения, емкость и статус. Эти данные являются доменными справочными данными, а не hardcoded UI message.

### `POST /api/delivery/shipments`
Создает shipment после подтверждения заказа. Требует `Idempotency-Key` и `X-Correlation-Id`. Для `PICKUP_POINT` обязателен `pickupPointId`, для домашней и курьерской доставки обязателен `addressLine`. Повтор с тем же idempotency key возвращает уже созданный shipment.

### `GET /api/delivery/shipments/{shipmentId}/tracking`
Возвращает tracking timeline shipment для владельца заказа, партнера в разрешенном scope, сотрудника поддержки, оператора доставки или администратора с нужным permission scope. Пользователь без доступа получает mnemonic-код `STR_MNEMO_DELIVERY_ACCESS_DENIED`.

### `GET /api/delivery/pickup-owner/shipments`
Возвращает список отправлений для ПВЗ текущего владельца или оператора. Фильтр по статусу используется для очередей: ожидается, принято, готово к выдаче, проблемное, невыкуп.

### `POST /api/delivery/pickup-owner/shipments/{shipmentId}/accept`
Фиксирует приемку отправления в ПВЗ по коду отправления. Команда идемпотентна по `Idempotency-Key`; повторная приемка не создает дубль операции.

### `POST /api/delivery/pickup-owner/shipments/{shipmentId}/deliver`
Фиксирует полную выдачу заказа по проверочному коду получателя. Backend хранит только хэш проверочного кода и audit-событие, исходный код не сохраняется.

### `POST /api/delivery/pickup-owner/shipments/{shipmentId}/partial-deliver`
Фиксирует частичную выдачу и список позиций, которые возвращаются в логистическую цепочку. Для каждой позиции передается `orderItemId`, `sku`, `quantity`, `itemResult`. Команда публикует события для order, WMS/1C, service, payment и bonus-контуров.

### `POST /api/delivery/integration/status-events`
Принимает внешнее событие доставки от delivery provider, WMS/1C, сборочного контура или внутреннего оператора. Повторное событие определяется по `(sourceSystem, externalEventId)` и не создает дубль tracking event. Некорректная последовательность статусов отклоняется кодом `STR_MNEMO_DELIVERY_INVALID_STATUS_TRANSITION`.

## DTO и валидации
- `DeliveryMethod`: `HOME_DELIVERY`, `COURIER_DELIVERY`, `PICKUP_POINT`.
- `ShipmentStatus`: `ORDER_CONFIRMED`, `ASSEMBLY_STARTED`, `ASSEMBLY_COMPLETED`, `SHIPPED`, `IN_TRANSIT`, `ARRIVED_AT_PICKUP_POINT`, `READY_FOR_PICKUP`, `DELIVERED`, `PARTIALLY_DELIVERED`, `NOT_COLLECTED`, `RETURNED_TO_LOGISTICS`, `DELIVERY_PROBLEM`.
- `CreateShipmentRequest` проверяет наличие orderId, customerId, deliveryMethod и согласованность pickupPointId/addressLine.
- `PickupDeliverRequest` и `PickupPartialDeliverRequest` требуют verificationCode, но код не возвращается в ответах.
- `ExternalDeliveryStatusEvent` проверяет наличие externalShipmentId, status, sourceSystem и occurredAt.

## STR_MNEMO codes
Backend может возвращать во frontend только mnemonic-коды:
- `STR_MNEMO_DELIVERY_ACCESS_DENIED`
- `STR_MNEMO_DELIVERY_PICKUP_POINT_UNAVAILABLE`
- `STR_MNEMO_DELIVERY_INVALID_METHOD`
- `STR_MNEMO_DELIVERY_INVALID_STATUS_TRANSITION`
- `STR_MNEMO_DELIVERY_SHIPMENT_NOT_FOUND`
- `STR_MNEMO_DELIVERY_VERIFICATION_CODE_INVALID`
- `STR_MNEMO_DELIVERY_READY_FOR_PICKUP`
- `STR_MNEMO_DELIVERY_PARTIAL_DELIVERY_RECORDED`
- `STR_MNEMO_DELIVERY_NOT_COLLECTED_RECORDED`
- `STR_MNEMO_DELIVERY_PROBLEM_RECORDED`

Все эти коды должны быть добавлены в `resources_ru.ts` и `resources_en.ts` при реализации frontend.

## Security и audit
Доступы:
- customer видит только свои shipment и tracking timeline.
- partner видит shipments своего activePartner scope.
- pickup-owner работает только с ПВЗ, к которому он привязан.
- delivery-operator видит интеграционный журнал и может расследовать problem statuses.
- employee/admin роли получают доступ только через permission scopes.

Состояние меняют только команды с `Idempotency-Key` и `X-Correlation-Id`. Все операции приемки, выдачи, частичной выдачи, невыкупа, проблемных статусов и внешних integration events создают audit trail с actorRole, actionCode, reasonCode, subjectRef, idempotencyKey и correlationId.
