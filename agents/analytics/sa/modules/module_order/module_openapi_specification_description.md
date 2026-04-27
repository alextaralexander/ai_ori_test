# Module order. Описание OpenAPI

## Назначение API
OpenAPI описывает внешний REST-контракт checkout для маршрутов frontend `/order` и `/order/supplementary`, а также историю и детали заказов для `/order/order-history` и `/order/order-history/:orderId`. Backend module `order` принимает валидированную корзину, создает или возвращает checkout draft, обновляет шаги оформления, применяет выгоды, выполняет validation, подтверждает заказ идемпотентно, отдает результат заказа, показывает список/детали собственных заказов и запускает repeat order.

## Endpoint groups

### `POST /api/order/checkouts`
Старт checkout из корзины.

Request:
- `cartId` — идентификатор валидированной корзины.
- `checkoutType` — `MAIN` или `SUPPLEMENTARY`.
- `vipMode`, `superOrderMode` — запрашиваемые режимы, которые backend обязан перепроверить.
- Header `Idempotency-Key` — рекомендуемый ключ повторов.

Behavior:
- Для `MAIN` проверяется валидность основной корзины.
- Для `SUPPLEMENTARY` проверяются partner status, право на дозаказ и отдельный supplementary cart.
- Если checkout уже создан для того же контекста, возвращается существующий draft.

Ошибки:
- `STR_MNEMO_ORDER_CHECKOUT_CART_INVALID`.
- `STR_MNEMO_SUPPLEMENTARY_ORDER_FORBIDDEN`.
- `STR_MNEMO_ORDER_CHECKOUT_FORBIDDEN`.

### `GET /api/order/checkouts/{checkoutId}`
Чтение checkout draft владельцем или сотрудником поддержки с разрешенными правами. Подмена `checkoutId` чужого пользователя возвращает controlled `403` без раскрытия данных.

### `PUT /api/order/checkouts/{checkoutId}/recipient`
Обновление получателя и контактов.

Валидации:
- `recipientType` только `SELF` или `OTHER`.
- `fullName` и `phone` обязательны.
- `email` валидируется, если передан.

Mnemonics:
- `STR_MNEMO_ORDER_CONTACT_INVALID`.
- `STR_MNEMO_ORDER_RECIPIENT_FORBIDDEN`.

### `PUT /api/order/checkouts/{checkoutId}/address`
Обновление адреса или пункта выдачи. После обновления backend пересчитывает доступные способы доставки.

Валидации:
- Для `ADDRESS` требуется адресный набор.
- Для `PICKUP_POINT` требуется `pickupPointId`.
- `addressId` должен принадлежать владельцу checkout или быть доступен support action.

Mnemonics:
- `STR_MNEMO_ORDER_ADDRESS_INVALID`.
- `STR_MNEMO_ORDER_PICKUP_POINT_UNAVAILABLE`.
- `STR_MNEMO_ORDER_ADDRESS_FORBIDDEN`.

### `PUT /api/order/checkouts/{checkoutId}/delivery`
Выбор способа доставки из рассчитанного списка `deliveryOptions`.

Валидации:
- Метод должен быть доступен для адреса, состава заказа, роли и типа checkout.
- Недоступный метод с `reasonMnemo` нельзя сохранить через подмену request body.

Mnemonics:
- `STR_MNEMO_ORDER_DELIVERY_UNAVAILABLE`.

### `PUT /api/order/checkouts/{checkoutId}/payment`
Выбор метода оплаты и, при необходимости, создание платежной сессии.

Валидации:
- Метод оплаты доступен для роли, типа заказа, региона и суммы.
- Header `Idempotency-Key` защищает создание платежной сессии от дублей.

Payment statuses:
- `PENDING`.
- `PAID`.
- `FAILED`.
- `EXPIRED`.
- `CANCELLED`.

Mnemonics:
- `STR_MNEMO_ORDER_PAYMENT_METHOD_UNAVAILABLE`.
- `STR_MNEMO_ORDER_PAYMENT_FAILED`.
- `STR_MNEMO_ORDER_PAYMENT_EXPIRED`.
- `STR_MNEMO_ORDER_PAYMENT_PENDING`.

### `PUT /api/order/checkouts/{checkoutId}/benefits`
Применение электронного кошелька, кешбэка и каталожных выгод.

Валидации:
- Сумма кошелька не превышает баланс, сумму заказа и campaign/role лимиты.
- Cashback и benefit-коды проверяются по campaignId, role segment, partner context, VIP/super order и retention-правилам.
- Повтор с тем же idempotency key не применяет выгоды повторно.

Mnemonics:
- `STR_MNEMO_ORDER_BENEFIT_LIMIT_EXCEEDED`.
- `STR_MNEMO_ORDER_WALLET_BALANCE_INSUFFICIENT`.
- `STR_MNEMO_ORDER_BENEFIT_UNAVAILABLE`.

### `POST /api/order/checkouts/{checkoutId}/validation`
Явная validation перед подтверждением. Ответ содержит `valid` и массив `reasons`, где каждый predefined reason представлен `STR_MNEMO_*`.

Проверки:
- checkout owner and permissions.
- non-empty checkout items.
- campaign status.
- contact/address/delivery/payment completeness.
- availability and reserve precheck.
- benefit limits.
- stale version.

### `POST /api/order/checkouts/{checkoutId}/confirm`
Финальное подтверждение заказа.

Request:
- `checkoutVersion` — версия draft, которую видел пользователь.
- Header `Idempotency-Key` — обязательный в реализации, даже если OpenAPI оставляет его optional для совместимости клиентов.

Оркестрация:
1. Проверка владельца и version.
2. Финальная validation.
3. Применение выгод.
4. Резервирование WMS/1C.
5. Создание `order_order`.
6. Создание или проверка платежной сессии.
7. Audit events и downstream events для сборки/доставки.

Mnemonics:
- `STR_MNEMO_ORDER_CHECKOUT_VERSION_CONFLICT`.
- `STR_MNEMO_ORDER_PARTIAL_RESERVE`.
- `STR_MNEMO_ORDER_CONFIRMATION_FAILED`.

### `GET /api/order/orders/{orderNumber}`
Чтение результата созданного заказа. Используется для success page и повторного открытия результата после оплаты.

### `GET /api/order/order-history`
Постраничный список заказов текущего пользователя с поиском по номеру, названию товара или SKU и фильтрами по кампании, типу заказа и статусам.

Mnemonics:
- `STR_MNEMO_ORDER_HISTORY_EMPTY`.
- `STR_MNEMO_ORDER_HISTORY_FILTER_INVALID`.

### `GET /api/order/order-history/{orderNumber}`
Детали заказа владельца или сотрудника поддержки с permission context. Ответ содержит состав, gifts, repeat/claim eligibility, totals, payment/delivery snapshots, timeline events, warnings и actions.

Mnemonics:
- `STR_MNEMO_ORDER_HISTORY_ACCESS_DENIED`.
- `STR_MNEMO_ORDER_REPEAT_PARTIAL`.
- `STR_MNEMO_ORDER_PAYMENT_PENDING`.

### `POST /api/order/order-history/{orderNumber}/repeat`
Запуск repeat order для доступных строк заказа. Для `MAIN` переносит доступные строки в основную корзину. Для `SUPPLEMENTARY` проверяет partner status и переносит строки в supplementary cart. Header `Idempotency-Key` защищает от повторного добавления.

## DTO policy
- DTO находятся в backend package `api`.
- DTO не содержат hardcoded user-facing текст.
- Все predefined user-facing причины передаются в `code`, `reasonMnemo`, `blockingReasonMnemo` или `details[].code` как `STR_MNEMO_*`.
- Строки, пришедшие из пользовательских данных или справочников, передаются как business data, а не predefined backend UI message.

## Security
- Все endpoints требуют авторизации.
- `checkoutId`, `cartId`, `addressId`, `pickupPointId`, `orderNumber`, `paymentSessionId` считаются untrusted input.
- Доступ к чужому checkout/order/order-history разрешен только support role с audit trail.
- Логи не должны содержать полные платежные реквизиты, token, session identifiers и лишние персональные данные.

## Runtime Swagger
Для monolith module `order` Swagger должен генерироваться автоматически из Spring MVC controllers через `springdoc-openapi`. Module key должен быть `order`; canonical URLs:
- `/v3/api-docs/order`
- `/swagger-ui/order`

Ручная регистрация endpoint list запрещена. Controller должен находиться под owning package prefix `com.bestorigin.monolith.order.impl.controller`.

## Версионная база
Новые технологии не вводятся. Контракт рассчитан на текущий Spring Boot monolith, springdoc-openapi runtime generation, Java/Maven baseline, React/TypeScript frontend и Ant Design-compatible UI. Backend-to-frontend message contract остается mnemonic-based через `STR_MNEMO_*`.
