# Acceptance criteria feature 017. Партнерские офлайн-продажи и заказы клиентов

## Функциональные критерии
1. `/vip-orders` доступен после логина ролям `partner`, `partner-leader` и `order-support` и показывает страницу клиентских офлайн-заказов с campaignId, orderNumber, customerId, partnerPersonNumber, orderStatus, paymentStatus, deliveryStatus, grandTotalAmount и bonusAccrualStatus.
2. `/business/tools/order-management/vip-orders/partner-orders` показывает тот же управленческий список с фильтрами `campaignId`, `query`, `orderStatus`, `paymentStatus`, `deliveryStatus`, `customerSegment`, `partnerPersonNumber`.
3. Некорректные фильтры возвращают mnemonic `STR_MNEMO_PARTNER_OFFLINE_ORDER_FILTER_INVALID`, frontend отображает локализованное сообщение из i18n.
4. `/business/tools/order-management/vip-orders/partner-orders/:orderId` открывает карточку заказа и показывает состав заказа, оплату, доставку, bonusAccrualStatus, businessVolume, linkedPartner, linkedCustomer, events и availableActions.
5. Карточка заказа поддерживает разрешенные действия: повторный заказ, сервисная корректировка и переход к карточке партнера; действия недоступны без соответствующей роли.
6. Для роли без доступа backend возвращает mnemonic `STR_MNEMO_PARTNER_OFFLINE_ORDER_ACCESS_DENIED`; hardcoded пользовательский текст из backend во frontend не передается.
7. Заказ `BOG-VIP-017-001` присутствует в тестовых данных и связан с партнером `BOG-016-002`, клиентом `VIP-017-001`, кампанией `CAT-2026-05`, оплаченной доставкой и начисленным businessVolume.
8. Swagger/OpenAPI runtime автоматически включает контроллеры owning module `order` через пакет `com.bestorigin.monolith.order.impl` и канонический путь `/v3/api-docs/order`.

## Data/API критерии
- Backend-модуль `order` соблюдает разделение `api`, `domain`, `db`, `impl`; новые runtime-классы размещаются в `impl/controller`, `impl/service`, `impl/config`.
- Liquibase changeset хранится отдельным XML-файлом `feature_017_partner_offline_orders.xml` в changelog-папке owning module.
- API не передает predefined пользовательский текст; все предопределенные сообщения представлены mnemonic-кодами `STR_MNEMO_*`.
- Тестовые данные покрывают минимум два офлайн-заказа: оплаченный заказ с доступным repeat action и заказ с сервисной корректировкой.

## UI критерии
- Desktop-страницы списка и карточки отображают фильтры, карточки заказов, timeline, payment/delivery/bonus блоки без перекрытий и со стабильными test id.
- Mobile-страницы перестраивают фильтры и карточки в одну колонку, длинные номера заказов и customerId не выходят за контейнеры.
- Все новые user-facing строки вынесены в `resources_ru.ts` и `resources_en.ts`; React-компоненты используют только `t(...)`.

## Тестовые критерии
- Managed API test начинается с логина роли, проверяет список `/api/order/partner-offline-orders`, детали `BOG-VIP-017-001`, фильтр по campaignId, repeat/service action и запрет доступа.
- Managed UI test начинается с `/test-login?role=partner-leader` и проходит маршруты `/vip-orders`, `/business/tools/order-management/vip-orders/partner-orders`, `/business/tools/order-management/vip-orders/partner-orders/BOG-VIP-017-001`.
- End-to-end managed tests агрегируют реальный feature #17 test вместе с предыдущими зелеными feature tests, а не содержат placeholder assertion.