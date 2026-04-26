# Public-content module. ER-описание

Модуль `public-content` владеет публичной витриной, навигацией, новостями, контентными страницами, офферами, FAQ, информационными разделами и документами. Текущая реализация использует in-memory репозиторий, а ER-модель фиксирует целевую структуру для Postgres.

## Состав модели
- `public_page`, `public_content_block`, `public_navigation_item`: главная, community, header, footer и entry points.
- `public_content_page`: новости, статьи, промо-материалы, rich text, вложения и CTA.
- `public_faq_item`: опубликованные вопросы с категорией, аудиторией, тегами и связями с инфо или документами.
- `public_info_section`: справочные страницы `/info/:section`.
- `public_document`: документы `/documents/:documentType`, актуальные версии и архив.

## Package ownership
- `api`: внешние DTO и REST response records.
- `domain`: доменные контракты репозиториев без сервисной логики.
- `impl/controller`: Spring MVC controllers модуля.
- `impl/service`: сервисы, in-memory репозиторий, фильтрация, fallback-логика и исключения.
- `db`: XML changelog-файлы feature #1, #2 и #3.

## Версионный baseline
Для текущего потока используется baseline монолита: Java 25, Spring Boot 4.0.6, Maven 3.9.13, React 19.2.0, TypeScript 5.9.3, Ant Design 6.0.0. При внедрении физической БД модель должна быть оформлена отдельными Liquibase XML changelog-файлами по feature.
