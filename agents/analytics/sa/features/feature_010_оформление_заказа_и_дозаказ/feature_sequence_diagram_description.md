# Feature 010. Описание sequence diagram

## Назначение
Диаграмма описывает runtime-взаимодействие checkout-флоу для `/order` и `/order/supplementary`. Основной владелец orchestration — backend `module_order`; frontend не принимает финальных решений о доступности дозаказа, delivery, payment, benefit-лимитах и резервировании.

## Участники
- Пользователь: покупатель или партнер.
- Frontend web-shell: страницы `/order` и `/order/supplementary`, i18n отображение, формы и CTA.
- `module_order OrderController`: REST endpoints `/api/order/**`.
- `module_order CheckoutService`: orchestration checkout draft, validation, benefits, reservation, payment and order creation.
- `module_cart`: validation источника корзины и cart snapshot.
- `module_catalog`: campaign/product snapshot для стабильных строк заказа.
- Benefit rules: расчет wallet, cashback, retention, VIP/super order и partner benefit constraints.
- Delivery integration: расчет способов доставки, пунктов выдачи, стоимости и причин недоступности.
- WMS/1C inventory: финальная проверка и резервирование остатков.
- Payment provider: платежная сессия и статусы оплаты.
- Audit/Event publisher: audit trail и события для сборки/доставки.

## Основной checkout
1. Пользователь открывает `/order`.
2. Frontend вызывает `POST /api/order/checkouts` с `cartId`, `checkoutType=MAIN` и idempotency key.
3. `module_order` запрашивает validation корзины у `module_cart`, получает snapshot строк, цен, доступности и предварительных итогов.
4. `module_order` уточняет campaign/product snapshot через `module_catalog`, чтобы checkout не зависел от последующих изменений карточки товара.
5. Benefit rules возвращает разрешенные выгоды и предупреждения.
6. `module_order` создает checkout draft и audit event `CHECKOUT_STARTED`.
7. Frontend отображает draft и все predefined причины через frontend i18n по `STR_MNEMO_*`.

## Обновление шагов
- Получатель и контакты обновляются через `PUT /recipient`; ошибки контактов возвращаются structured validation errors.
- Адрес или пункт выдачи обновляются через `PUT /address`; после этого `module_order` пересчитывает delivery options.
- Доставка выбирается через `PUT /delivery`; backend повторно проверяет доступность выбранного метода.
- Кошелек, кешбэк и benefit-коды применяются через `PUT /benefits`; операция идемпотентна и не применяет выгоду дважды.
- Оплата выбирается через `PUT /payment`; payment session создается или переиспользуется по idempotency key.

## Confirmation
1. Frontend отправляет `POST /confirm` с `checkoutVersion` и idempotency key.
2. `module_order` проверяет owner, permission и stale version.
3. Выполняется финальная validation корзины и benefit-правил.
4. WMS/1C резервирует позиции.
5. При полном резерве создается order, проверяется payment session, публикуются audit/downstream events.
6. Frontend получает `OrderConfirmationResponse` с номером заказа, статусом, суммой и next action.

## Supplementary order
Для `/order/supplementary` используется тот же контракт, но `checkoutType=SUPPLEMENTARY`. Отличия:
- `module_cart` возвращает supplementary cart snapshot.
- `module_order` обязательно проверяет partner status, право на дозаказ, partner context, VIP/super order, retention и benefit-лимиты.
- Основной checkout и основная корзина не изменяются.
- Заказ создается с типом `SUPPLEMENTARY`.

## Ошибки и альтернативы
- Partial reserve: WMS/1C возвращает доступные количества, `module_order` создает audit event `RESERVATION_FAILED` и возвращает `STR_MNEMO_ORDER_PARTIAL_RESERVE`.
- Payment failed/expired: payment provider возвращает `FAILED` или `EXPIRED`; frontend показывает `STR_MNEMO_ORDER_PAYMENT_FAILED` или `STR_MNEMO_ORDER_PAYMENT_EXPIRED` и не создает дубль.
- Delivery unavailable: выбранная доставка отклоняется кодом `STR_MNEMO_ORDER_DELIVERY_UNAVAILABLE`.
- Benefit limits: превышение кошелька, кешбэка или partner benefit-лимитов возвращает `STR_MNEMO_ORDER_BENEFIT_LIMIT_EXCEEDED`.
- Version conflict: устаревший `checkoutVersion` возвращает `STR_MNEMO_ORDER_CHECKOUT_VERSION_CONFLICT`.

## Package ownership
- `api`: DTO и enum-контракты sequence.
- `impl/controller`: REST endpoints.
- `impl/service`: orchestration.
- `impl/client`: cart/catalog/benefit/delivery/WMS/payment adapters.
- `impl/validator`: validation commands.
- `impl/event`: audit and downstream events.
- `domain`: persisted aggregate entities.
- `db`: Liquibase XML.

## Версионная база
Новые технологии не вводятся. Реализация остается в текущем Spring Boot monolith и React/TypeScript web-shell baseline, с runtime Swagger generation через springdoc-openapi.
