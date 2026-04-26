# Feature 003. ER-изменения модуля public-content

## Назначение
Feature #3 расширяет модуль `public-content` справочными сущностями: FAQ, информационные разделы и документы. В текущей реализации данные хранятся in-memory, но модель фиксирует целевую структуру для последующего перехода к Postgres и Liquibase XML.

## Сущности
- `public_faq_category`: категория FAQ с i18n-ключом названия, аудиторией и порядком сортировки.
- `public_faq_item`: опубликованный вопрос с i18n-ключами вопроса и ответа, тегами, аудиторией и ссылками на информационный раздел или тип документа.
- `public_info_section`: информационный раздел для маршрута `/info/:section` с SEO-ключами, аудиторией и статусом публикации.
- `public_document`: документ для маршрута `/documents/:documentType` с версией, датой публикации, PDF viewer URL, download URL, признаком обязательности, признаком актуальности и аудиторией.

## Владение пакетами
- `api`: DTO ответов FAQ, информационных разделов и документов.
- `domain`: контракт `PublicContentRepository`, который расширяется методами чтения справочных данных.
- `impl/service`: выборка, фильтрация по аудитории, поиск FAQ и fallback-данные.
- `impl/controller`: REST endpoints `/api/public-content/faq`, `/api/public-content/info/{section}` и `/api/public-content/documents/{documentType}`.
- `db`: отдельный XML changelog feature #3 для будущей физической схемы.

## Версионный baseline
На дату старта задачи 26.04.2026 используется совместимый baseline текущего монолита: Java 25, Spring Boot 4.0.6, Maven 3.9.13, React 19.2.0, TypeScript 5.9.3, Ant Design 6.0.0 и PostgreSQL-совместимая модель данных. Совместимость с уже созданным монолитом важнее точечного обновления зависимостей в рамках этой фичи.
