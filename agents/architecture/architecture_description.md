# Best Ori Gin. Описание архитектуры на feature 001

## Version baseline
На дату старта задачи 26.04.2026 целевой baseline: Java 25, Spring Boot 4.x stable, Maven 4.x stable, Hibernate 7.x stable, Liquibase 5.x stable, PostgreSQL 18.x stable, TypeScript 5.x stable, React 19.x stable, Ant Design 6.x stable, Docker 29.x stable, Docker Compose 5.x stable.

## Модули
- `frontend/public-web`: публичное React-приложение для маршрутов `/`, `/home`, `/community` и навигационных переходов.
- `backend/monolith/public-content`: Spring Boot модуль, который отдает read-only API публичной CMS-конфигурации.
- `PostgreSQL public-content schema`: хранение страниц, блоков, навигации и entry points.
- `CMS admin контур`: будущий административный контур для управления публичным контентом.
- `Auth/Profile контур`: будущий контур входа, профиля и определения пользовательской аудитории.
- `Catalog контур`: будущий каталог косметики и кампаний.
- `Partner контур`: будущий партнерский офис, отчеты, бонусы и логистика.

## Связи
- Пользователи -> `frontend/public-web`: HTTPS, browser routes `/`, `/home`, `/community`.
- `frontend/public-web` -> `backend/monolith/public-content`: HTTPS REST `/api/public-content/*`.
- `backend/monolith/public-content` -> PostgreSQL: JDBC.
- `CMS admin контур` -> `backend/monolith/public-content`: HTTPS REST для будущего управления CMS-контентом.
- `frontend/public-web` -> `Auth/Profile контур`: HTTPS REST для входа, профиля и определения audience.
- `frontend/public-web` -> `Catalog контур`: HTTPS REST или route navigation к каталогу.
- `frontend/public-web` -> `Partner контур`: HTTPS REST или route navigation к партнерскому офису.

## Ownership
Backend module `public-content` должен соблюдать package policy:
- `api`: REST DTO и внешние контракты.
- `domain`: JPA entities и repository interfaces.
- `db`: Liquibase XML changelog files.
- `impl/controller`: REST controllers.
- `impl/service`: service interfaces и implementations.
- `impl/mapper`: MapStruct mappers.
- `impl/validator`: validators для audience, route targets и payload.
- `impl/config`: module config, OpenAPI group и seed data.

## Локализация и сообщения
Frontend user-facing строки хранятся в i18n dictionaries всех поддерживаемых языков. Backend возвращает только i18n keys или mnemonic codes `STR_MNEMO_*`, если сообщение заранее определено и может попасть во frontend.
