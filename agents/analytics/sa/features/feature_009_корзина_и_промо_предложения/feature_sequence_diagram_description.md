# Feature 009. Описание sequence diagram

## Назначение
Sequence diagram описывает runtime-взаимодействие feature 009 между frontend web-shell, module `cart`, module `catalog` и внутренними policy/audit компонентами. Диаграмма покрывает чтение корзины, изменение количества, получение и применение промо-предложений, partner supplementary order и validation перед checkout.

## Основной flow корзины
1. Пользователь открывает `/cart`.
2. Frontend вызывает `GET /api/cart/current`.
3. `CartController` передает запрос в `CartService`.
4. `CartService` обновляет цены, остатки, лимиты и publication status через `CatalogService`.
5. `PromotionPolicy` пересчитывает скидки, подарки, free-delivery progress, blocking reasons и итоговые суммы.
6. Backend возвращает `CartResponse` без hardcoded пользовательского текста; frontend переводит `STR_MNEMO_*` через i18n.

## Изменение количества
1. Frontend отправляет `PATCH /api/cart/items/{lineId}` с `Idempotency-Key` и `expectedVersion`.
2. `CartService` проверяет ownership корзины, optimistic version и допустимое количество.
3. `CatalogService` возвращает доступный остаток, `maxAllowedQuantity` и availability status.
4. После пересчета `CartService` фиксирует audit event и возвращает новую версию корзины.

## Shopping offers
1. Для `/cart/shopping-offers` frontend вызывает `GET /api/cart/shopping-offers`.
2. `PromotionPolicy` выбирает офферы по `campaignId`, `cartType=MAIN`, role segment, текущим строкам и остаткам.
3. Для применения frontend вызывает `POST /api/cart/shopping-offers/{offerId}/apply`.
4. `PromotionPolicy` проверяет применимость и добавляет строки или applied offer только в основную корзину.
5. Если offer недоступен, backend возвращает `STR_MNEMO_CART_OFFER_UNAVAILABLE` без добавления строк.

## Supplementary order
1. Партнер открывает `/cart/supplementary`.
2. Frontend вызывает `GET /api/cart/supplementary/current`.
3. `CartService` и `PromotionPolicy` проверяют partner status, campaign rules и доступность дозаказа.
4. Если дозаказ запрещен, возвращается `STR_MNEMO_SUPPLEMENTARY_ORDER_FORBIDDEN`.
5. `/cart/supplementary/shopping-offers` возвращает только офферы, разрешенные для `cartType=SUPPLEMENTARY`.

## Checkout validation
1. Перед переходом к checkout frontend вызывает `POST /api/cart/validate`.
2. `CartService` повторно проверяет campaign status, availability, role permissions и benefit rules.
3. При блокировках возвращается `CartValidationResponse.valid=false` и список `blockingReasons` с `STR_MNEMO_*`.
4. При успехе возвращается `valid=true` и стабильный `checkoutRoute`, совместимый с будущей feature оформления заказа.

## Ошибки и безопасность
- Все идентификаторы корзины, строк и офферов проверяются как untrusted input.
- Пользователь может читать и менять только свою корзину; support view требует отдельного permission и пишет audit event.
- Повторные операции изменения используют `Idempotency-Key`.
- Raw exception, stack trace и backend hardcoded user-facing text не возвращаются во frontend.

## Версионная база
Sequence не вводит новые технологии. Реализация должна использовать текущий Spring Boot monolith, Java/Maven baseline, React/TypeScript web-shell и Ant Design-compatible UI-подход проекта. Backend package ownership: DTO в `api`, JPA entities/repositories в `domain`, Liquibase XML в `db`, controllers/services/policies/audit в `impl` subpackages.
