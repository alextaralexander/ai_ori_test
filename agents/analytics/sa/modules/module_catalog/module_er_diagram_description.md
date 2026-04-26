# Module catalog. Полное описание ER-модели на feature 006

## Назначение модуля
`module_catalog` отвечает за публичный каталог товаров Best Ori Gin, поиск, карточку товара, добавление товара в корзину из catalog context и цифровые каталоги текущего/следующего периода с PDF-материалами.

## Package ownership
- `api`: REST DTO и внешние контракты каталога.
- `domain`: JPA entities и repository interfaces для товаров, карточки, корзины и цифровых каталогов.
- `db`: Liquibase XML changelogs owning module catalog.
- `impl/controller`: Spring MVC controllers.
- `impl/service`: service interfaces и implementations.
- `impl/mapper`: MapStruct mappers.
- `impl/validator`: validators для поиска, карточки, корзины и цифровых каталогов.
- `impl/exception`: exceptions и handlers с `STR_MNEMO_*`.
- `impl/client`: adapters к S3/MinIO для PDF material URL, если общий storage client недоступен.

## Основные сущности товаров
- `catalog_category`: категории с иерархией, slug, i18n-ключом названия, порядком и флагом публикации.
- `catalog_product`: товар текущей кампании с SKU, slug, i18n-ключами, категорией, брендом, объемом, campaign code, ценой, промо-ценой, валютой, доступностью, лимитами заказа и публикацией.
- `catalog_product_detail`: расширенные описания карточки: full description, usage instructions, ingredients и characteristics JSON.
- `catalog_product_media`: галерея карточки товара.
- `catalog_product_attachment`: вложения карточки товара.
- `catalog_product_recommendation`: related, cross-sell и alternative recommendations.
- `catalog_product_tag`: теги фильтрации и поиска.
- `catalog_product_promo`: промо-метки.
- `catalog_cart_item`: строка корзины, добавленная из поиска или карточки.

## Сущности цифровых каталогов

### catalog_digital_issue
Полный выпуск цифрового каталога.
- Primary key: `id uuid`.
- Unique: `issue_code`.
- Период: `period_type`, `period_start`, `period_end`.
- Публикация: `publication_status`, `preview_allowed_from`.
- Сортировка и аудит: `display_order`, `created_at`, `updated_at`.
- Ограничения: период окончания не раньше периода начала; enum для period/publication status.

### catalog_digital_page
Страница выпуска.
- Primary key: `id uuid`.
- FK: `issue_id -> catalog_digital_issue.id`.
- Unique: `(issue_id, page_number)`.
- URL preview и thumbnail используются frontend viewer.
- `width_px` и `height_px` нужны для корректного позиционирования hotspots.

### catalog_digital_material
PDF или связанный материал выпуска.
- Primary key: `id uuid`.
- FK: `issue_id -> catalog_digital_issue.id`.
- `material_type`: `MAIN_CATALOG`, `BROCHURE`, `PROMO_LEAFLET`, `DOCUMENT`.
- `file_url` хранит ссылку/key на S3/MinIO или CDN.
- `allow_download` и `allow_share` управляют viewer actions.

### catalog_digital_page_hotspot
Интерактивная зона страницы, ведущая в карточку товара.
- Primary key: `id uuid`.
- FK: `page_id -> catalog_digital_page.id`.
- FK: `product_id -> catalog_product.id`.
- Координаты `x_percent`, `y_percent`, `width_percent`, `height_percent` хранят проценты от размеров страницы.
- `display_order` задает порядок клавиатурного обхода.

### catalog_digital_visibility
Правила видимости выпуска или материала.
- Primary key: `id uuid`.
- FK: `issue_id -> catalog_digital_issue.id`.
- Optional FK: `material_id -> catalog_digital_material.id`.
- `role_code`: `GUEST`, `CUSTOMER`, `PARTNER`, `CONTENT_MANAGER`, `CATALOG_MANAGER`.
- `can_preview`, `can_download`, `can_share` задают доступные действия.
- Unique: `(issue_id, material_id, role_code)`.

## Индексы
- `catalog_product`: category, campaign, published, availability, popular rank.
- `catalog_product_tag`: `(tag_code, product_id)`.
- `catalog_product_promo`: `(promo_code, product_id)`.
- `catalog_product_recommendation`: `(product_id, recommendation_type, display_order)`.
- `catalog_digital_issue`: `(period_type, publication_status, period_start, period_end)`.
- `catalog_digital_page`: `(issue_id, page_number)`.
- `catalog_digital_material`: `(issue_id, publication_status, display_order)`.
- `catalog_digital_page_hotspot`: `(page_id, display_order)` and `(product_id)`.
- `catalog_digital_visibility`: `(role_code, can_preview)` and `(material_id, role_code)`.

## Связи
- Category 1:N Product.
- Product 1:1 ProductDetail.
- Product 1:N Media, Attachment, Recommendation, Tag, Promo, CartItem.
- DigitalIssue 1:N DigitalPage, DigitalMaterial, DigitalVisibility.
- DigitalMaterial 1:N DigitalVisibility.
- DigitalPage 1:N DigitalPageHotspot.
- Product 1:N DigitalPageHotspot.

## Mnemonic-коды модуля
Модуль catalog должен возвращать predefined user-facing состояния только как mnemonic-коды:
- `STR_MNEMO_CATALOG_SEARCH_EMPTY`.
- `STR_MNEMO_CATALOG_PRODUCT_NOT_FOUND`.
- `STR_MNEMO_CATALOG_ITEM_UNAVAILABLE`.
- `STR_MNEMO_CATALOG_QUANTITY_LIMIT_EXCEEDED`.
- `STR_MNEMO_CATALOG_CART_ITEM_ADDED`.
- `STR_MNEMO_DIGITAL_CATALOGUE_NOT_FOUND`.
- `STR_MNEMO_DIGITAL_CATALOGUE_FORBIDDEN`.
- `STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_UNAVAILABLE`.
- `STR_MNEMO_DIGITAL_CATALOGUE_SHARE_NOT_ALLOWED`.
- `STR_MNEMO_DIGITAL_CATALOGUE_DOWNLOAD_NOT_ALLOWED`.
- `STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_READY`.

## Версионная база
Новые runtime-технологии не вводятся. Полная ER-модель рассчитана на текущий PostgreSQL, Hibernate/JPA и Liquibase XML baseline monolith. Любые новые Liquibase changesets для feature #6 должны быть отдельным XML changelog файлом в module catalog, а не добавлением unrelated изменений в существующий общий changelog.
