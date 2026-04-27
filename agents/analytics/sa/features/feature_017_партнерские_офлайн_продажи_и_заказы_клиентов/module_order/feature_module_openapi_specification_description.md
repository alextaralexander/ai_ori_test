# Feature 017 module_order OpenAPI description

Модуль `order` публикует три endpoint для офлайн-заказов партнера:

- `GET /api/order/partner-offline-orders` возвращает страницу заказов с фильтрами campaignId, query, orderStatus, paymentStatus, deliveryStatus, customerSegment, partnerPersonNumber.
- `GET /api/order/partner-offline-orders/{orderNumber}` возвращает карточку заказа с items, delivery, payment, partner/customer links, bonusAccrualStatus, businessVolume, events и availableActions.
- `POST /api/order/partner-offline-orders/{orderNumber}/actions` выполняет `REPEAT_ORDER` или `SERVICE_ADJUSTMENT` и возвращает mnemonic result code.

Ошибки передаются mnemonic-кодами `STR_MNEMO_PARTNER_OFFLINE_ORDER_ACCESS_DENIED`, `STR_MNEMO_PARTNER_OFFLINE_ORDER_FILTER_INVALID`, `STR_MNEMO_PARTNER_OFFLINE_ORDER_NOT_FOUND`. Runtime Swagger должен попасть в `/v3/api-docs/order` автоматически через controller в `com.bestorigin.monolith.order.impl.controller`.