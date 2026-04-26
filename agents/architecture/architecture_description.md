# Best Ori Gin. Описание архитектуры на feature 003

## Version baseline
На дату старта задачи 26.04.2026 используется совместимый baseline текущего монолита: Java 25, Spring Boot 4.0.6, Maven 3.9.13, React 19.2.0, TypeScript 5.9.3, Ant Design 6.0.0, Liquibase XML changelog policy и PostgreSQL-совместимая модель данных. В рамках feature #3 зависимости не обновляются, чтобы сохранить совместимость с уже реализованными feature #1 и #2.

## Модули
- `frontend/public-web`: публичное React-приложение для маршрутов `/`, `/home`, `/community`, `/news`, `/content/:contentId`, `/offer/:offerId`, `/FAQ`, `/faq`, `/info/:section?`, `/documents/:documentType`.
- `backend/monolith/public-content`: Spring Boot модуль read-only API для публичной CMS-конфигурации, новостей, контентных страниц, офферов, FAQ, информационных разделов и документов.
- `PostgreSQL public-content schema`: целевое хранение страниц, блоков, навигации, новостей, content pages, offers, FAQ, info sections, documents и archive versions.
- `CMS admin контур`: будущий административный контур для управления публичным контентом и справкой.
- `Auth/Profile контур`: будущий контур входа, профиля и определения пользовательской аудитории.
- `Catalog контур`: будущий каталог косметики и кампаний.
- `Partner контур`: будущий партнерский офис, отчеты, бонусы и логистика.

## Связи
- Пользователи открывают публичные маршруты через `frontend/public-web`.
- Frontend вызывает `backend/monolith/public-content` по REST для страниц, навигации, новостей, контента, офферов, FAQ, info и documents.
- Backend возвращает DTO с i18n-ключами и mnemonic-кодами `STR_MNEMO_*`; пользовательские тексты локализуются на frontend.
- `public-content` ссылается на каталог через `productRef` и route references без синхронного backend-вызова в рамках feature #3.
- CMS admin в будущих фичах будет управлять теми же сущностями через модуль `public-content`.

## Ownership
Backend module `public-content` соблюдает package policy:
- `api`: REST DTO и внешние контракты.
- `domain`: repository interfaces и будущие JPA entities.
- `db`: Liquibase XML changelog files.
- `impl/controller`: REST controllers.
- `impl/service`: service interfaces, implementations, in-memory repository и исключения.
- `impl/config`: module config и seed/fallback configuration.

## Локализация и сообщения
Все новые frontend user-facing строки размещаются в `resources_ru.ts` и `resources_en.ts`. Backend не отправляет hardcoded пользовательские тексты в API responses; для предопределенных состояний используются `STR_MNEMO_PUBLIC_FAQ_EMPTY`, `STR_MNEMO_PUBLIC_INFO_NOT_FOUND`, `STR_MNEMO_PUBLIC_DOCUMENTS_NOT_FOUND` и существующие коды публичного контента.

## Feature #3
Feature #3 расширяет публичный контур справочным самообслуживанием:
- `GET /api/public-content/faq` для FAQ с category/query/audience.
- `GET /api/public-content/info/{section}` для информационных страниц.
- `GET /api/public-content/documents/{documentType}` для документов, PDF viewer, скачивания и архива версий.

Реализация остается в модуле `public-content`, потому что FAQ, info и documents являются частью публичного CMS-контента и используют те же правила i18n, публикации, аудитории и безопасного fallback.
