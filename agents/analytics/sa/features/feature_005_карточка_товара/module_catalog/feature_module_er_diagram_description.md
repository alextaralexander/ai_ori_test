# Feature 005. Module catalog. Описание ER-изменений

## Назначение
Feature #5 расширяет модель каталога данными, которые нужны для детальной карточки товара `/product/:productCode`: подробное описание, способ применения, состав, характеристики, медиа, вложения, рекомендации и покупательские ограничения. Основной владелец данных - backend module `catalog`.

## Сущности

### `catalog_product`
Базовая сущность опубликованного товара текущей кампании. Для карточки используются существующие поля поиска feature #4 и уточняются поля:
- `product_code varchar(64)` - бизнес-ключ товара, primary key.
- `name varchar(255)` - пользовательское название товара.
- `category_code varchar(64)` - категория товара для навигации и breadcrumbs.
- `brand varchar(128)` - бренд.
- `volume_label varchar(64)` - объем, размер или формат упаковки.
- `base_price numeric(12,2)` - публичная базовая цена.
- `promo_price numeric(12,2)` - промо-цена, nullable.
- `currency varchar(3)` - ISO-код валюты.
- `availability_status varchar(32)` - `IN_STOCK`, `LOW_STOCK`, `OUT_OF_STOCK`, `COMING_SOON`, `DISCONTINUED`.
- `available_quantity integer` - доступный остаток для расчета ограничений, nullable для публичного скрытия точного остатка.
- `min_order_quantity integer` - минимальное количество для покупки.
- `max_order_quantity integer` - максимальное количество в строке корзины.
- `is_published boolean` - признак публичной публикации.
- `campaign_code varchar(64)` - текущая кампания.
- `tags text` - поисковые и витринные теги.
- `promo_badges text` - промо-метки.

### `catalog_product_detail`
Детализация карточки с отношением один-к-одному к `catalog_product`.
- `product_code varchar(64)` - primary key и foreign key на `catalog_product.product_code`.
- `short_description text` - краткое описание для верхнего блока.
- `full_description text` - полное описание.
- `usage_instructions text` - способ применения.
- `ingredients text` - состав.
- `characteristics_json text` - структурированные характеристики в JSON-совместимом формате для frontend.

### `catalog_product_media`
Медиа карточки.
- `media_id varchar(64)` - primary key.
- `product_code varchar(64)` - foreign key на `catalog_product.product_code`.
- `media_type varchar(32)` - `IMAGE` или будущий расширяемый тип.
- `url varchar(1024)` - ссылка на изображение или S3/MinIO asset.
- `alt_text varchar(255)` - текст для доступности.
- `sort_order integer` - порядок показа.
- `is_primary boolean` - главное изображение.

### `catalog_product_attachment`
Вложения карточки: PDF, инструкции, сертификаты или материалы партнера.
- `attachment_id varchar(64)` - primary key.
- `product_code varchar(64)` - foreign key.
- `title varchar(255)` - название вложения как данные продукта.
- `document_type varchar(64)` - тип документа.
- `url varchar(1024)` - ссылка на файл.
- `sort_order integer` - порядок показа.

### `catalog_product_recommendation`
Связи рекомендаций.
- `recommendation_id varchar(64)` - primary key.
- `product_code varchar(64)` - исходный товар.
- `recommended_product_code varchar(64)` - рекомендуемый товар.
- `recommendation_type varchar(32)` - `RELATED`, `CROSS_SELL`, `ALTERNATIVE`.
- `sort_order integer` - порядок показа.

### `cart_line`
Для feature #5 используется существующий или временно in-memory контур корзины. Карточка передает `product_code`, `quantity`, `partner_context_id` и источник `PRODUCT_CARD`.

## Ограничения и индексы
- `catalog_product_detail.product_code` уникален и ссылается на опубликованный товар.
- Для `catalog_product_media` нужен индекс `(product_code, sort_order)` и ограничение максимум одного `is_primary=true` на товар.
- Для `catalog_product_attachment` нужен индекс `(product_code, sort_order)`.
- Для `catalog_product_recommendation` нужен уникальный индекс `(product_code, recommended_product_code, recommendation_type)`.
- Рекомендация не должна ссылаться на тот же `product_code`.
- Добавление в корзину проверяет `availability_status`, `min_order_quantity`, `max_order_quantity` и `available_quantity`.

## Версионная база
Фича использует текущий backend baseline monolith без новых runtime-технологий. Liquibase changeset должен быть отдельным XML-файлом модуля catalog.
