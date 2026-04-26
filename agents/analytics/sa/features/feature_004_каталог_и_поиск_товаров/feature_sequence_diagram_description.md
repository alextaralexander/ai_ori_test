# Feature 004. Описание sequence-диаграммы

## Основной поиск
1. Пользователь открывает `/search` как гость, покупатель или партнер.
2. Frontend web-shell восстанавливает `q`, `category`, `priceMin`, `priceMax`, `availability`, `tags`, `promo`, `sort` и `page` из URL.
3. Frontend вызывает `GET /api/catalog/search` с текущей аудиторией и фильтрами.
4. `catalog controller` передает запрос в `catalog service`.
5. `catalog service` получает опубликованные товары активной кампании из `catalog repository`.
6. Сервис применяет ролевую видимость, фильтры, сортировку, пагинацию и расчет рекомендаций.
7. Backend возвращает `CatalogSearchResponse` с карточками товаров или пустым списком и `STR_MNEMO_CATALOG_SEARCH_EMPTY`.
8. Frontend локализует i18n keys товаров, категорий, тегов, промо-меток и messageCode.

## Добавление в корзину
1. Авторизованный customer или partner нажимает кнопку добавления на доступном товаре.
2. Frontend вызывает `POST /api/catalog/cart/items`, передавая `productId`, `quantity`, `audience`, `userContextId` и текущий `searchUrl`.
3. `catalog service` проверяет публикацию товара, кампанию, доступность и роль.
4. Для partner сервис сохраняет `partnerContext=true`.
5. Cart summary возвращает количество позиций, суммарное количество и `STR_MNEMO_CATALOG_CART_ITEM_ADDED`.
6. Frontend обновляет счетчик корзины и показывает локализованное подтверждение.

## Альтернативы
- Гость не вызывает backend quick add: frontend переводит его к входу или регистрации, сохраняя URL поиска.
- Если товар недоступен или снят с кампании, backend возвращает 409 и `STR_MNEMO_CATALOG_ITEM_UNAVAILABLE`.
- Если фильтры невалидны, backend нормализует безопасные значения и не раскрывает технических ошибок.

## Backend package ownership
- DTO находятся в `catalog/api`.
- Репозитории и будущие JPA-сущности находятся в `catalog/domain`.
- Liquibase XML changelog находится в `db/changelog/catalog`.
- Controller, service, config и runtime orchestration находятся в `catalog/impl/<role>`.

## Версионная база
Sequence рассчитан на текущую monolith архитектуру репозитория. Новые runtime-технологии не вводятся; совместимость с feature #1-#3 важнее точечного обновления dependency baseline в рамках этой фичи.
