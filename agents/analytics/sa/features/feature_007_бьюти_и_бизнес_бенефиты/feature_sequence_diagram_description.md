# Feature 007. Описание sequence diagram

## Участники
- Пользователь: гость или потенциальный партнер, который открывает публичный benefit-лендинг.
- Frontend web-shell: React/TypeScript приложение, которое обрабатывает route, отображает лендинг, CTA и локализованные состояния.
- `public-content API`: backend module `public-content`, отдающий landing payload, referral status и принимающий conversion events.
- `public-content data`: данные лендингов, блоков, CTA, referral-кодов и conversion events.
- Registration flow: будущий или существующий сценарий регистрации, принимающий referral-context.
- Catalog flow: каталог, цифровой каталог или поиск, куда пользователь переходит из CTA.
- Analytics storage: хранилище обезличенных conversion events.

## Основной поток
1. Пользователь открывает базовый или персонализированный route benefit-лендинга.
2. Frontend вычисляет `landingType` из route и вызывает `GET /api/public-content/benefit-landings/{landingType}` с optional `code`, `campaignId` и `variant`.
3. Backend загружает опубликованный landing, A/B-вариант блоков, CTA и referral-код.
4. Если referral-код активен, backend возвращает статус `ACTIVE` и разрешенный публичный контекст спонсора.
5. Если код отсутствует, истек или отключен, backend возвращает контролируемый статус и mnemonic-код, например `STR_MNEMO_REFERRAL_CODE_INVALID`.
6. Frontend отображает лендинг через i18n-ключи, без hardcoded user-facing строк в компонентах.
7. Frontend отправляет conversion event `VIEW`. Ошибка отправки не блокирует отображение страницы.

## CTA-потоки
- Регистрация: frontend отправляет conversion event `REGISTER` или `REGISTER_PARTNER`, затем передает в registration flow `landingType`, `variant`, `code` и `campaignId`.
- Каталог: frontend отправляет event `OPEN_CATALOG`, затем открывает `/search` или цифровой каталог с campaign/referral context.
- Приложение: frontend отправляет event `INSTALL_APP`, затем открывает store-link или локализованное состояние недоступности ссылки.
- Контакт со спонсором: frontend отправляет event `CONTACT_SPONSOR`, затем открывает разрешенный публичный канал связи, если referral-код активен.

## Ошибки и устойчивость
- Неверный referral-код не блокирует базовый лендинг.
- Недоступность analytics endpoint не блокирует CTA.
- Недоступный landing payload приводит к локализованному состоянию и возможности перейти к базовой регистрации.
- Backend не передает hardcoded пользовательские сообщения; frontend разрешает mnemonic-коды через dictionaries.

## Версионная база
Новые технологии не вводятся. Взаимодействие реализуется в текущем Spring Boot monolith и React/TypeScript web-shell, OpenAPI формируется springdoc из контроллеров `public-content`, а пользовательские строки остаются в i18n-словарях frontend.
