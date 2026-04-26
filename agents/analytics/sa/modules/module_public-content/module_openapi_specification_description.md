# Public-content module. Описание OpenAPI

Модуль `public-content` предоставляет публичные REST endpoints под префиксом `/api/public-content`. Он обслуживает главную, навигацию, новости, контентные страницы, офферы, FAQ, информационные страницы, документы и benefit-лендинги.

## Основные группы
- Главная и навигация: `/pages/home`, `/pages/community`, `/navigation`, `/entry-points`.
- Контент feature #2: `/news`, `/content/{contentId}`, `/offers/{offerId}`.
- Справка feature #3: `/faq`, `/info/{section}`, `/documents/{documentType}`.
- Benefit-лендинги feature #7: `/benefit-landings/{landingType}`, `/benefit-landings/conversions`.

## Benefit-лендинги
`GET /benefit-landings/{landingType}` возвращает структурированный payload для `BEAUTY`, `BUSINESS`, `MEMBER`, `VIP_CUSTOMER` и `APP`. Query-параметры `code`, `campaignId` и `variant` позволяют собрать персонализированный referral-context и A/B-вариант блоков. Ответ включает SEO metadata, referral status, ordered blocks и CTA.

`POST /benefit-landings/conversions` принимает обезличенное событие просмотра или клика CTA. Endpoint возвращает `202`, если событие принято, и не должен блокировать пользовательский переход при временной ошибке аналитики.

## Ошибки и i18n
Предопределенные пользовательские сообщения передаются только как mnemonic-коды `STR_MNEMO_*`. Frontend локализует их через `resources_ru.ts` и `resources_en.ts`.

Для feature #7 используются минимум `STR_MNEMO_PUBLIC_BENEFIT_LANDING_NOT_FOUND`, `STR_MNEMO_REFERRAL_CODE_INVALID`, `STR_MNEMO_REFERRAL_CODE_EXPIRED`, `STR_MNEMO_REFERRAL_CODE_DISABLED` и `STR_MNEMO_BENEFIT_CONVERSION_REJECTED`.

## Swagger
Для монолита OpenAPI должен генерироваться автоматически Spring MVC контроллерами. Ручные списки endpoints в коде не создаются; SA-спецификация является проектным артефактом workflow.
