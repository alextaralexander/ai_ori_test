# Feature 017 sequence description

Пользовательский поток начинается во frontend `web-shell`: партнер открывает `/vip-orders` или управленческий маршрут `/business/tools/order-management/vip-orders/partner-orders`. Frontend вызывает `GET /api/order/partner-offline-orders`, передает фильтры кампании и статусов, затем показывает список заказов.

При открытии карточки frontend вызывает `GET /api/order/partner-offline-orders/{orderNumber}`. Модуль `order` возвращает состав, оплату, доставку, bonusAccrualStatus, businessVolume, partner/customer links, события и availableActions. Связи с `bonus-wallet`, `mlm-structure` и `partner-reporting` представлены через contract links и общие идентификаторы `partnerPersonNumber`, `customerId`, `campaignId`.

При сервисном действии frontend вызывает `POST /api/order/partner-offline-orders/{orderNumber}/actions`; backend проверяет роль, фиксирует событие и возвращает mnemonic result code. Все пользовательские predefined-сообщения передаются как `STR_MNEMO_*`.