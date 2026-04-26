# Module catalog. Полное описание ER-модели на feature 005

## Назначение
`catalog` является owning module для публичного поиска товаров, фильтрации каталога текущей кампании, карточки товара, промо-меток, рекомендаций и добавления товара в корзину. На feature #5 модель расширяет поисковую модель feature #4 детальными данными продукта, медиа, вложениями и связями рекомендаций.

## Сущности и связи
- `catalog_category` хранит опубликованные категории и иерархию каталога.
- `catalog_product` хранит опубликованные товары кампаний, поисковые поля, цену, промо-цену, доступность, остаток, лимиты заказа и признаки сортировки.
- `catalog_product_detail` хранит подробное описание карточки, способ применения, состав и характеристики.
- `catalog_product_media` хранит галерею изображений карточки.
- `catalog_product_attachment` хранит PDF, инструкции, сертификаты и другие вложения.
- `catalog_product_recommendation` связывает товар с related, cross-sell и alternative товарами.
- `catalog_product_tag` связывает товары с тегами и атрибутами фильтрации.
- `catalog_product_promo` связывает товары с промо-метками.
- `catalog_cart_item` хранит строки корзины, созданные из выдачи или карточки.

`catalog_category` имеет self-reference через `parent_id`; одна категория содержит много товаров. Товар имеет один detail, много media, attachments, recommendations, tags, promo badges и cart items.

## Ключи, ограничения и индексы
- Все основные сущности используют `uuid` primary key.
- `catalog_category.slug`, `catalog_product.sku` и `catalog_product.slug` уникальны.
- `catalog_product.category_id` ссылается на `catalog_category(id)`.
- `catalog_product_detail.product_id` уникален и ссылается на `catalog_product(id)`.
- `catalog_product_media.product_id`, `catalog_product_attachment.product_id`, `catalog_product_recommendation.product_id` ссылаются на `catalog_product(id)`.
- `catalog_product_recommendation.recommended_product_id` ссылается на `catalog_product(id)` и не должен совпадать с `product_id`.
- Таблицы tag/promo имеют composite primary key `(product_id, code)` и cascade delete от товара.
- `catalog_cart_item.quantity` должен быть больше нуля.
- `catalog_product.min_order_quantity` и `max_order_quantity` должны быть больше нуля, а `min_order_quantity <= max_order_quantity`.
- Индексы поиска и карточки: `idx_catalog_product_search`, `idx_catalog_product_availability`, `idx_catalog_product_popular`, `idx_catalog_product_tag_code`, `idx_catalog_product_promo_code`, `idx_catalog_product_media_order`, `idx_catalog_product_attachment_order`, `idx_catalog_product_recommendation_order`, `idx_catalog_cart_user_context`.

## Правила данных
- Backend не передает hardcoded UI-сообщения во frontend: предопределенные причины ошибок и статусы идут как `STR_MNEMO_*`.
- Названия, описания, состав, alt text и вложения являются product/CMS data или i18n-key данными продукта.
- В поисковую выдачу и карточку включаются только опубликованные товары активной кампании и опубликованных категорий.
- `availability_status=OUT_OF_STOCK` или `DISCONTINUED` запрещает добавление в корзину и должен приводить к `STR_MNEMO_CATALOG_ITEM_UNAVAILABLE`.
- Превышение `max_order_quantity` или `available_quantity` приводит к `STR_MNEMO_CATALOG_QUANTITY_LIMIT_EXCEEDED`.
- `audience=PARTNER` в `catalog_cart_item` устанавливает `partner_context=true`, а `source=PRODUCT_CARD` фиксирует добавление из карточки.

## Backend package ownership
- `com.bestorigin.monolith.catalog.api`: REST DTO и enum.
- `com.bestorigin.monolith.catalog.domain`: repository interfaces и будущие JPA-сущности.
- `db/changelog/catalog`: Liquibase XML changelog.
- `com.bestorigin.monolith.catalog.impl.controller`: REST controller.
- `com.bestorigin.monolith.catalog.impl.service`: service interfaces, default service, in-memory repository и исключения.
- `com.bestorigin.monolith.catalog.impl.config`: module metadata.

## Версионная база
Liquibase changeset создается в XML. Новые библиотеки для persistence или search engine в feature #5 не добавляются; модель подготовлена к PostgreSQL, а runtime для текущей фичи использует совместимый monolith baseline.
