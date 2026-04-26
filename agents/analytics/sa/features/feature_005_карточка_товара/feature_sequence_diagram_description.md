# Feature 005. Описание sequence diagram

## Назначение
Диаграмма описывает взаимодействие пользователя, web-shell frontend, backend module `catalog`, repository layer, корзины и frontend i18n при открытии карточки товара, обработке отсутствующего товара и добавлении товара в корзину из карточки.

## Основной поток открытия карточки
1. Пользователь открывает маршрут `/product/:productCode`.
2. `ProductCardView` запрашивает `GET /api/catalog/products/{productCode}` с параметрами аудитории и кампании.
3. `CatalogController` передает запрос в `CatalogService`.
4. `CatalogService` читает опубликованный товар, детализацию, медиа, вложения и рекомендации через repository.
5. Если товар найден и доступен аудитории, backend возвращает `CatalogProductCardResponse`.
6. Frontend локализует статические подписи через i18n и показывает галерею, цену, описание, состав, рекомендации и покупательский блок.

## Альтернативный поток отсутствующего товара
Если товар неизвестен, снят с публикации или недоступен аудитории, backend возвращает `CatalogErrorResponse` с `messageCode=STR_MNEMO_CATALOG_PRODUCT_NOT_FOUND`. Frontend локализует mnemonic-код и показывает безопасное состояние без stack trace или технического JSON.

## Поток добавления в корзину
1. Пользователь выбирает количество и нажимает добавление в корзину.
2. Frontend отправляет `POST /api/catalog/cart/items` с `productCode`, `quantity`, `partnerContextId` и `source=PRODUCT_CARD`.
3. Backend проверяет доступность, лимиты и остаток.
4. При успехе cart context создает или обновляет строку корзины и возвращает `CartSummaryResponse` с `STR_MNEMO_CATALOG_CART_ITEM_ADDED`.
5. Frontend обновляет краткое состояние корзины и показывает переход к checkout.

## Ошибки покупки
- Недоступный товар возвращает `STR_MNEMO_CATALOG_ITEM_UNAVAILABLE`.
- Превышение лимита количества возвращает `STR_MNEMO_CATALOG_QUANTITY_LIMIT_EXCEEDED`.
- Гость получает `STR_MNEMO_AUTH_REQUIRED` и route возврата на карточку.

## Версионная база
Фича не вводит новые runtime-технологии. Взаимодействия реализуются в текущем Spring Boot monolith и React web-shell; все user-facing сообщения frontend получает из i18n-словарей.
