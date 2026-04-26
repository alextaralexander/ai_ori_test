# Module catalog. Полное описание ER-модели на feature 004

## Назначение
`catalog` является owning module для публичного поиска товаров, фильтрации каталога текущей кампании, промо-меток и quick add в корзину. На feature #4 модель совпадает с feature-specific ER, потому что это первая реализуемая фича модуля.

## Сущности и связи
- `catalog_category` хранит опубликованные категории и иерархию каталога.
- `catalog_product` хранит опубликованные товары кампаний, поисковые поля, цену, доступность и признаки сортировки.
- `catalog_product_tag` связывает товары с тегами и атрибутами фильтрации.
- `catalog_product_promo` связывает товары с промо-метками.
- `catalog_cart_item` хранит минимальные строки корзины, созданные через quick add из выдачи.

`catalog_category` имеет self-reference через `parent_id`; одна категория содержит много товаров. Товар имеет много тегов, промо-меток и строк корзины.

## Ключи, ограничения и индексы
- Все основные сущности используют `uuid` primary key.
- `catalog_category.slug`, `catalog_product.sku` и `catalog_product.slug` уникальны.
- `catalog_product.category_id` ссылается на `catalog_category(id)`.
- Таблицы tag/promo имеют composite primary key `(product_id, code)` и cascade delete от товара.
- `catalog_cart_item.quantity` должен быть больше нуля.
- Индексы поиска: `idx_catalog_product_search`, `idx_catalog_product_availability`, `idx_catalog_product_popular`, `idx_catalog_product_tag_code`, `idx_catalog_product_promo_code`, `idx_catalog_cart_user_context`.

## Правила данных
- Backend не хранит пользовательские тексты карточек как готовые UI-строки: названия и описания передаются через i18n keys.
- В поисковую выдачу включаются только опубликованные товары активной кампании и опубликованных категорий.
- `availability_status=OUT_OF_STOCK` запрещает quick add и должен приводить к `STR_MNEMO_CATALOG_ITEM_UNAVAILABLE`.
- `audience=PARTNER` в `catalog_cart_item` устанавливает `partner_context=true`.

## Backend package ownership
- `com.bestorigin.monolith.catalog.api`: REST DTO и enum.
- `com.bestorigin.monolith.catalog.domain`: repository interfaces и будущие JPA-сущности.
- `db/changelog/catalog`: Liquibase XML changelog.
- `com.bestorigin.monolith.catalog.impl.controller`: REST controller.
- `com.bestorigin.monolith.catalog.impl.service`: service interfaces, default service, in-memory repository и исключения.
- `com.bestorigin.monolith.catalog.impl.config`: module metadata.

## Версионная база
Liquibase changeset создается в XML. Новые библиотеки для persistence или search engine в feature #4 не добавляются; модель подготовлена к PostgreSQL, а runtime для текущей фичи использует совместимый monolith baseline.
