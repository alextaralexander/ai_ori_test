# Feature 002. OpenAPI описание изменений модуля public-content

## Ответственность API
Модуль `public-content` расширяется публичными read-only endpoints для новостей, динамических контентных страниц и промо-офферов. Все endpoints входят в monolith Swagger/OpenAPI group `public-content` и должны автоматически попадать в `/v3/api-docs/public-content` и `/swagger-ui/public-content`, так как контроллеры размещаются внутри package prefix модуля.

## Version baseline
На дату старта задачи 26.04.2026 целевой baseline: Java 25, Spring Boot 4.x stable, Spring Web MVC stable, springdoc-openapi stable для выбранного Spring Boot baseline, TypeScript latest stable, React latest stable, Ant Design latest stable.

## Endpoints

### GET `/api/public-content/news`
Возвращает ленту опубликованных новостей.
- Query `audience`: `GUEST`, `AUTHENTICATED`, `CUSTOMER`, `PARTNER`, `CONTENT_MANAGER`, `ANY`; default `GUEST`.
- Header `Accept-Language`: используется frontend и gateway-слоем для выбора словаря, backend возвращает i18n-ключи.
- `200 NewsFeedResponse`: массив `items`, опциональный `featured`, `emptyStateCode`.
- `503 ErrorResponse`: `STR_MNEMO_PUBLIC_CONTENT_UNAVAILABLE`.

### GET `/api/public-content/content/{contentId}`
Возвращает опубликованную динамическую контентную страницу.
- Path `contentId`: стабильный публичный идентификатор, длина 1..120.
- `200 ContentPageResponse`: meta, breadcrumbs, SEO, секции, PDF-вложения, товарные ссылки и CTA.
- `404 ErrorResponse`: `STR_MNEMO_PUBLIC_CONTENT_NOT_FOUND`, если материал отсутствует, не опубликован, еще не начался или снят с публикации.
- `503 ErrorResponse`: `STR_MNEMO_PUBLIC_CONTENT_UNAVAILABLE`.

### GET `/api/public-content/offers/{offerId}`
Возвращает опубликованное промо-предложение.
- Path `offerId`: стабильный публичный идентификатор, длина 1..120.
- `200 OfferResponse`: hero, условия, секции, вложения, связанные товары и CTA.
- `404 ErrorResponse`: `STR_MNEMO_PUBLIC_CONTENT_NOT_FOUND`.
- `503 ErrorResponse`: `STR_MNEMO_PUBLIC_CONTENT_UNAVAILABLE`.

## DTO
- `NewsFeedResponse`: `items`, `featured`, `emptyStateCode`.
- `NewsItem`: `newsKey`, `contentId`, `titleKey`, `summaryKey`, `categoryKey`, `imageUrl`, `publishedAt`, `targetRoute`.
- `ContentPageResponse`: `contentId`, `templateCode`, `titleKey`, `descriptionKey`, `breadcrumbs`, `seo`, `sections`, `attachments`, `productLinks`, `ctas`.
- `OfferResponse`: `offerId`, `titleKey`, `summaryKey`, `breadcrumbs`, `seo`, `hero`, `sections`, `attachments`, `productLinks`, `ctas`.
- `ContentSection`: `sectionKey`, `sectionType`, `sortOrder`, `payload`.
- `ContentAttachment`: `attachmentKey`, `fileType`, `titleKey`, `url`, `fileSizeBytes`.
- `ProductLink`: `productRef`, `labelKey`, `targetRoute`.
- `ContentCta`: `labelKey`, `targetType`, `targetValue`, `audience`.
- `ErrorResponse`: `code` только в формате `STR_MNEMO_*`.

## Валидация и публикация
Backend возвращает только материалы со статусом `PUBLISHED`, у которых `active_from <= now()` и `active_to` пустой или больше текущего времени. Для неопубликованных материалов response body не содержит пользовательский контент. CTA допускают только внутренние маршруты платформы или разрешенные внешние ссылки.

## Frontend contract
Frontend должен локализовать `titleKey`, `summaryKey`, `descriptionKey`, `breadcrumbKey`, CTA label keys, attachment title keys и mnemonic-коды ошибок через `resources_ru.ts` и `resources_en.ts`. Backend не передает hardcoded predefined user-facing тексты.
