# Описание sequence diagram feature 001

## Участники
- `Frontend public-web`: React-приложение публичной витрины.
- `Auth context`: локальный контекст сессии frontend, определяющий аудиторию пользователя без передачи приватных данных в публичный API.
- `public-content API`: backend-модуль, отдающий CMS-конфигурацию страниц, навигацию и entry points.
- `public-content DB`: PostgreSQL-схема модуля с публичными страницами, блоками и навигацией.
- `Frontend i18n`: словари локализации frontend.

## Основной поток
1. Пользователь открывает `/`, `/home` или `/community`.
2. Frontend определяет аудиторию пользователя: `GUEST`, `CUSTOMER`, `PARTNER` или `AUTHENTICATED`.
3. Для главной frontend вызывает `GET /api/public-content/pages/home?audience=...`.
4. Для community frontend вызывает `GET /api/public-content/pages/community?audience=...`.
5. Backend выбирает опубликованную page config, активные блоки, навигацию и entry points с учетом аудитории.
6. Backend возвращает DTO только с i18n keys, route targets, block metadata и payload без hardcoded predefined UI text.
7. Frontend разрешает i18n keys через словари и отображает header, hero, промо, быстрые переходы и footer.
8. Пользователь выбирает CTA или пункт навигации и переходит в каталог, community, benefits, вход, профиль, заказы или партнерский офис.

## Ошибки и fallback
- При ошибке `public-content` backend возвращает mnemonic `STR_MNEMO_PUBLIC_CONTENT_UNAVAILABLE`.
- Frontend локализует mnemonic через i18n dictionaries и показывает безопасный fallback.
- При недоступности профиля или auth context frontend использует аудиторию `GUEST`, не раскрывает приватные данные и сохраняет доступ к публичной навигации.

## Контракт локализации
Backend возвращает:
- `titleKey`;
- `labelKey`;
- `descriptionKey`;
- `STR_MNEMO_*` для предопределенных сообщений.

Frontend обязан хранить значения этих ключей во всех поддерживаемых языках и не hardcode user-facing строки в React runtime.
