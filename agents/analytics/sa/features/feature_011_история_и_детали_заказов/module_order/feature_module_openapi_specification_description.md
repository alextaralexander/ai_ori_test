# Feature 011. Module order OpenAPI specification description

## Назначение
OpenAPI-артефакт описывает публичный контракт module `order` для личной истории заказов. Контракт покрывает список заказов, детали заказа и repeat order action. Маршруты соответствуют продуктовым URL `/order/order-history` и `/order/order-history/:orderId`, но backend API располагается под `/api/order/order-history`.

## Контракты
- `GET /order/order-history` возвращает постраничный список заказов текущего пользователя с поиском и фильтрами по кампании, типу заказа, статусам оплаты и доставки.
- `GET /order/order-history/{orderNumber}` возвращает детали заказа с составом, totals, delivery/payment snapshots, timeline events, warnings и разрешенными actions.
- `POST /order/order-history/{orderNumber}/repeat` переносит доступные строки в основной cart или supplementary cart согласно `orderType` и текущим бизнес-правилам.

## Безопасность и локализация
Все endpoints требуют авторизации и ownership/support permission checks. DTO не содержат hardcoded user-facing text из backend: предопределенные сообщения, предупреждения и причины ограничений передаются только mnemonic-кодами `STR_MNEMO_*`, которые frontend локализует через i18n dictionaries.

## Версионная база
Новые технологии не вводятся. Используется текущий Spring Boot monolith, springdoc-openapi runtime grouping, Java/Maven baseline и frontend web-shell baseline. Контракт должен попасть в Swagger group module `order` автоматически через owning package prefix.
