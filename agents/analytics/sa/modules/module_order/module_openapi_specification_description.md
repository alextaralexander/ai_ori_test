# Module order OpenAPI description after feature 017

Модуль `order` обслуживает checkout, историю заказов, претензии и партнерские офлайн-заказы. Feature 017 добавляет endpoints `/api/order/partner-offline-orders`, `/api/order/partner-offline-orders/{orderNumber}` и `/api/order/partner-offline-orders/{orderNumber}/actions`.

Все runtime controllers находятся в `com.bestorigin.monolith.order.impl.controller`; Swagger/OpenAPI попадает в группу `/v3/api-docs/order`. Предопределенные пользовательские сообщения передаются только mnemonic-кодами `STR_MNEMO_*` и локализуются во frontend.