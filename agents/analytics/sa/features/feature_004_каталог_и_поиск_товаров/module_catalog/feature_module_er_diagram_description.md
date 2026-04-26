# Feature 004. Module catalog. Описание ER-изменений

## Назначение модели
Модуль `catalog` хранит опубликованные товары текущих кампаний Best Ori Gin, категории, поисковые признаки, промо-метки и минимальный контур быстрого добавления товара в корзину из выдачи `/search`.

## Владение пакетами backend
- `api`: DTO запросов и ответов каталога, поиска и quick add в корзину.
- `domain`: JPA-сущности и repository-интерфейсы для категорий, товаров, тегов, промо и корзины.
- `db`: Liquibase XML changelog feature #4.
- `impl/controller`: REST-контроллеры модуля `catalog`.
- `impl/service`: бизнес-логика поиска, фильтрации, сортировки, рекомендаций и добавления в корзину.
- `impl/config`: регистрация module metadata и OpenAPI group metadata.

## Таблица catalog_category
- `id uuid not null primary key` - идентификатор категории.
- `slug varchar(120) not null unique` - человекочитаемый код категории для URL и фильтров.
- `name_i18n_key varchar(160) not null` - frontend i18n key названия категории.
- `parent_id uuid null references catalog_category(id)` - родительская категория.
- `display_order int not null default 0` - порядок отображения.
- `published boolean not null default false` - доступность категории в публичной выдаче.
- Индекс: `idx_catalog_category_parent` по `parent_id`.

## Таблица catalog_product
- `id uuid not null primary key` - идентификатор товара.
- `sku varchar(64) not null unique` - артикул товара.
- `slug varchar(160) not null unique` - slug карточки товара.
- `name_i18n_key varchar(180) not null` - frontend i18n key названия.
- `short_description_i18n_key varchar(220) not null` - frontend i18n key краткого описания.
- `category_id uuid not null references catalog_category(id)` - категория товара.
- `image_url varchar(500) not null` - публичный URL изображения.
- `campaign_code varchar(40) not null` - код трехнедельной кампании.
- `base_price decimal(12,2) not null` - базовая публичная цена.
- `currency char(3) not null` - валюта ISO 4217.
- `availability_status varchar(40) not null` - `IN_STOCK`, `LOW_STOCK`, `OUT_OF_STOCK`.
- `published boolean not null default false` - публикация в каталоге.
- `popular_rank int not null default 0` - вес сортировки по популярности.
- `created_at timestamptz not null` - дата заведения товара.
- Индексы: `idx_catalog_product_search` по `published, campaign_code, category_id`, `idx_catalog_product_availability` по `availability_status`, `idx_catalog_product_popular` по `popular_rank`.

## Таблица catalog_product_tag
- `product_id uuid not null references catalog_product(id) on delete cascade`.
- `tag_code varchar(80) not null` - код тега для фильтрации и поиска.
- Первичный ключ: `(product_id, tag_code)`.
- Индекс: `idx_catalog_product_tag_code` по `tag_code`.

## Таблица catalog_product_promo
- `product_id uuid not null references catalog_product(id) on delete cascade`.
- `promo_code varchar(80) not null` - код промо-метки.
- Первичный ключ: `(product_id, promo_code)`.
- Индекс: `idx_catalog_product_promo_code` по `promo_code`.

## Таблица catalog_cart_item
- `id uuid not null primary key` - идентификатор строки корзины.
- `user_context_id varchar(120) not null` - технический идентификатор пользовательского контекста или тестовой сессии.
- `audience varchar(40) not null` - `CUSTOMER` или `PARTNER`.
- `product_id uuid not null references catalog_product(id)` - добавленный товар.
- `quantity int not null` - количество, ограничение `quantity > 0`.
- `partner_context boolean not null default false` - признак партнерского контекста.
- `created_at timestamptz not null` - время добавления.
- Индексы: `idx_catalog_cart_user_context` по `user_context_id`, `idx_catalog_cart_product` по `product_id`.

## Ограничения видимости
В выдачу попадают только `catalog_product.published=true`, опубликованные категории и товары активной кампании. Для гостя возвращается публичная цена и public availability, для клиента и партнера - роль-специфичная интерпретация тех же данных без hardcoded backend UI-текстов.

## Версионная база
Новые технологии хранения не вводятся. Liquibase changeset должен быть XML-файлом в owning module `db` package. Реализация использует текущий Spring Boot monolith baseline репозитория для совместимости с предыдущими фичами.
