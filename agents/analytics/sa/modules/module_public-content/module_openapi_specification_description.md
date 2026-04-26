# Public-content module. Описание OpenAPI

Модуль `public-content` предоставляет публичные REST endpoints под префиксом `/api/public-content`. Он обслуживает главную, навигацию, новости, контентные страницы, офферы, FAQ, информационные страницы и документы.

## Основные группы
- Главная и навигация: `/pages/home`, `/pages/community`, `/navigation`, `/entry-points`.
- Контент feature #2: `/news`, `/content/{contentId}`, `/offers/{offerId}`.
- Справка feature #3: `/faq`, `/info/{section}`, `/documents/{documentType}`.

## Ошибки и i18n
Предопределенные пользовательские сообщения передаются только как mnemonic-коды `STR_MNEMO_*`. Frontend локализует их через `resources_ru.ts` и `resources_en.ts`.

## Swagger
Для монолита OpenAPI должен генерироваться автоматически Spring MVC контроллерами. Ручные списки endpoints в коде не создаются; SA-спецификация является проектным артефактом workflow.
