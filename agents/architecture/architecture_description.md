# Best Ori Gin. Описание архитектуры на feature 011

## Version baseline
На дату старта задачи 27.04.2026 используется совместимый baseline текущего монолита: Java 25 LTS baseline проекта при наличии Java 26 GA как latest stable, Spring Boot 4.0.6, Maven 3.9.13, React 19.2.0, TypeScript 5.9.3, Ant Design 6.0.0, Liquibase XML changelog policy, PostgreSQL-совместимая модель данных и S3/MinIO-compatible storage для файлов. В рамках feature #11 зависимости не обновляются, чтобы сохранить совместимость с уже реализованными feature #1-#10; upgrade follow-up: отдельно оценить переход runtime baseline с Java 25 LTS на Java 26 после проверки совместимости Spring Boot, Maven toolchain, CI и container images.

## Модули
- `frontend/public-web`: публичное React-приложение для маршрутов `/`, `/home`, `/community`, `/news`, `/content/:contentId`, `/offer/:offerId`, `/FAQ`, `/faq`, `/info/:section?`, `/documents/:documentType`, `/beauty-benefits`, `/beauty-benefits/:code`, `/business-benefits`, `/business-benefits/:code`, `/member-benefits`, `/vip-customer-benefits`, `/the-new-oriflame-app`, `/invite/beauty-partner-registration`, `/invite/business-partner-registration`, `/invite/partners-activation`, `/invite/sponsor-cabinet`, `/products/digital-catalogue-current`, `/products/digital-catalogue-next`, `/search`, `/product/:productCode`, `/cart`, `/cart/shopping-offers`, `/cart/supplementary`, `/cart/supplementary/shopping-offers`, `/order`, `/order/supplementary`.
- `backend/monolith/public-content`: Spring Boot модуль read-only API для публичной CMS-конфигурации, новостей, контентных страниц, офферов, FAQ, информационных разделов, документов, benefit-лендингов, referral statuses и conversion events.
- `backend/monolith/catalog`: Spring Boot модуль API для поиска товаров, фильтров, сортировки, карточек выдачи, детальной карточки товара, рекомендаций, цифровых выпусков каталога и PDF material actions.
- `backend/monolith/cart`: Spring Boot модуль API для основной корзины, shopping offers, supplementary order, support view и checkout validation.
- `backend/monolith/order`: Spring Boot модуль API для checkout draft, supplementary checkout, получателя, адреса, доставки, оплаты, выгод, validation, резервирования, order confirmation, чтения результата заказа, истории заказов, деталей заказа и repeat order.
- `backend/monolith/partner-onboarding`: Spring Boot модуль API для invite/referral validation, registration application, activation flow, partner profile activation, personal referral link, sponsor cabinet и registration lead events.
- `PostgreSQL public-content schema`: целевое хранение страниц, блоков, навигации, новостей, content pages, offers, FAQ, info sections, documents, archive versions, benefit landings, blocks, CTA, referral codes и conversion events.
- `PostgreSQL catalog schema`: целевое хранение категорий, товаров, детализации карточек, медиа, вложений, рекомендаций, тегов, промо-меток, цифровых выпусков, страниц, PDF-материалов, hotspots и visibility rules.
- `PostgreSQL cart schema`: целевое хранение `cart`, `cart_line`, `cart_applied_offer`, `cart_totals_snapshot`, `cart_audit_event`.
- `PostgreSQL order schema`: целевое хранение `order_checkout_draft`, `order_checkout_item`, `order_checkout_recipient`, `order_checkout_address`, `order_checkout_delivery`, `order_checkout_payment`, `order_checkout_benefit`, `order_reservation`, `order_order`, `order_audit_event`.
- `PostgreSQL partner-onboarding schema`: целевое хранение invite, registration applications, activation tokens, partner profiles, referral links и onboarding events.
- `S3/MinIO catalog PDF materials`: хранилище PDF и preview-материалов цифровых каталогов; module `catalog` выдает только разрешенные temporary URLs.
- `CRM / marketing systems`: внешний или внутренний интеграционный контур, который получает registration lead events и статусы onboarding funnel.
- `CMS admin контур`: будущий административный контур для управления публичным контентом и справкой.
- `Auth/Profile контур`: будущий контур входа, профиля и определения пользовательской аудитории.
- `WMS / 1C`: внешний контур проверки остатков и резервирования, вызываемый `order` при финальном подтверждении.
- `Payment provider`: внешний контур платежных сессий и статусов оплаты.
- `Delivery provider`: внешний или внутренний контур способов доставки, пунктов выдачи и ограничений.
- `Bonus/Partner finance контур`: будущий контур финального начисления бонусов после оплаченного заказа; feature #10 показывает только предварительные партнерские выгоды.
- `Partner контур`: будущий партнерский офис, отчеты, бонусы и логистика.

## Связи
- Пользователи открывают публичные маршруты через `frontend/public-web`.
- Frontend вызывает `backend/monolith/public-content` по REST для страниц, навигации, новостей, контента, офферов, FAQ, info, documents, benefit landing payload, referral status и conversion events.
- Frontend вызывает `backend/monolith/partner-onboarding` по REST для `/api/partner-onboarding/invites/validate`, `/api/partner-onboarding/registrations`, `/api/partner-onboarding/activations/{token}`, `/api/partner-onboarding/activations/{token}/confirm-contact`, `/api/partner-onboarding/activations/{token}/complete`, `/api/partner-onboarding/sponsor-cabinet/invites` и `/api/partner-onboarding/sponsor-cabinet/invites/{inviteId}/resend`.
- Frontend вызывает `backend/monolith/catalog` по REST для `/api/catalog/search`, `/api/catalog/products/{productCode}`, `/api/catalog/digital-catalogues/current`, `/api/catalog/digital-catalogues/next`, `/api/catalog/digital-catalogues/{issueCode}` и PDF material actions.
- Frontend вызывает `backend/monolith/cart` по REST для `/api/cart/current`, `/api/cart/items`, `/api/cart/items/{lineId}`, `/api/cart/shopping-offers`, `/api/cart/shopping-offers/{offerId}/apply`, `/api/cart/validate`, `/api/cart/supplementary/current`, `/api/cart/supplementary/items`, `/api/cart/supplementary/shopping-offers`, `/api/cart/support/users/{userId}/current`.
- Frontend вызывает `backend/monolith/order` по REST для `/api/order/checkouts`, `/api/order/checkouts/{checkoutId}`, `/api/order/checkouts/{checkoutId}/recipient`, `/address`, `/delivery`, `/payment`, `/benefits`, `/validation`, `/confirm`, `/api/order/orders/{orderNumber}`, `/api/order/order-history`, `/api/order/order-history/{orderNumber}` и `/api/order/order-history/{orderNumber}/repeat`.
- Backend возвращает DTO с i18n-ключами и mnemonic-кодами `STR_MNEMO_*`; пользовательские тексты локализуются на frontend.
- `public-content` ссылается на каталог через `productRef` и route references на `/search`; синхронная загрузка товаров выполняется frontend через `catalog`.
- `public-content` передает registration handoff через frontend route: `landingType`, `variant`, `code` и `campaignId` переходят в `partner-onboarding`, без синхронного backend-вызова между модулями.
- `partner-onboarding` взаимодействует с CRM/marketing systems по REST/event protocol для registration lead events; временная недоступность CRM переводит событие в retry/audit контур и не блокирует заявку.
- `partner-onboarding` передает activation/profile handoff в будущий `Auth/Profile контур` и `Partner контур` через internal boundary: feature #8 создает начальный partner profile/referral link, но не реализует MLM compensation plan.
- `cart` взаимодействует с `catalog` по internal REST/service contract для проверки productCode, цены, campaign status, availability и order limits.
- `order` взаимодействует с `cart` по REST/internal contract для финальной validation и получения cart snapshot.
- `order` взаимодействует с `catalog` по REST/internal contract для campaign/product snapshot, price/order limits и стабильных данных строк заказа.
- `order` взаимодействует с WMS/1C по REST adapter protocol для финальной проверки остатков и резервирования.
- `order` взаимодействует с payment provider по REST adapter protocol для создания платежной сессии и чтения статусов.
- `order` взаимодействует с delivery provider по REST adapter protocol для способов доставки, пунктов выдачи, стоимости и ограничений.
- `order` передает будущему `Bonus/Partner finance контуру` событие оплаченного заказа для финального начисления бонусов; feature #10 не реализует compensation plan.
- `catalog` взаимодействует с S3/MinIO-compatible storage по внутреннему adapter/client protocol для генерации временных download/share URL PDF-материалов.
- CMS admin в будущих фичах будет управлять теми же сущностями через модуль `public-content`.

## Ownership
Backend module `public-content` соблюдает package policy:
- `api`: REST DTO и внешние контракты.
- `domain`: repository interfaces и будущие JPA entities.
- `db`: Liquibase XML changelog files.
- `impl/controller`: REST controllers.
- `impl/service`: service interfaces, implementations, in-memory repository и исключения.
- `impl/config`: module config и seed/fallback configuration.

Backend module `catalog` соблюдает package policy:
- `api`: REST DTO, enum audience/availability/sort и request/response contracts.
- `domain`: repository interfaces и будущие JPA entities.
- `db`: Liquibase XML changelog files.
- `impl/controller`: REST controllers.
- `impl/service`: service interfaces, default service, in-memory repository и exceptions.
- `impl/config`: module metadata и OpenAPI group metadata.
- `impl/client`: storage adapter/client для PDF material URL, если общий S3/MinIO client отсутствует.

Backend module `partner-onboarding` соблюдает package policy:
- `api`: REST DTO, enums onboarding/invite/application statuses и request/response contracts.
- `domain`: JPA entities и repository interfaces для invite, registration application, activation token, partner profile, referral link и onboarding events.
- `db`: Liquibase XML changelog files, включая dedicated changelog feature #8.
- `impl/controller`: REST controllers для invite validation, registrations, activations и sponsor cabinet.
- `impl/service`: orchestration services регистрации, активации, invite lifecycle и idempotency.
- `impl/validator`: validation invite/referral code, contact, consent, activation token и sponsor permissions.
- `impl/mapper`: entity/DTO mapping.
- `impl/event`: audit events и CRM lead event publisher.
- `impl/client`: CRM/marketing integration adapter.
- `impl/config`: module metadata, OpenAPI group metadata и настройки scheduler/retry.

Backend module `cart` соблюдает package policy:
- `api`: REST DTO, enum cart/offer/availability/status и request/response contracts.
- `domain`: JPA entities и repository interfaces для cart, cart line, applied offer, totals snapshot и audit events.
- `db`: Liquibase XML changelog files, включая dedicated changelog feature #9.
- `impl/controller`: REST controllers для current cart, items, shopping offers, supplementary order, support view и validation.
- `impl/service`: service interfaces, orchestration services корзины, idempotency, пересчета и validation.
- `impl/policy`: promotion policy, supplementary access policy, checkout validation rules.
- `impl/mapper`: entity/DTO mapping.
- `impl/event`: audit event publisher.
- `impl/client`: catalog availability/price adapter, если прямой service dependency не используется.
- `impl/config`: module metadata и OpenAPI group metadata.

Backend module `order` соблюдает package policy:
- `api`: REST DTO, enum checkout/order/payment/delivery/status и request/response contracts.
- `domain`: JPA entities и repository interfaces для checkout draft, checkout item, recipient, address, delivery, payment, benefit, reservation, order и audit event.
- `db`: Liquibase XML changelog files, включая dedicated changelog feature #10.
- `impl/controller`: REST controllers для checkout lifecycle, supplementary checkout, validation, confirmation и order result.
- `impl/service`: orchestration services checkout, idempotency, delivery/payment/benefit update, reservation и order creation.
- `impl/validator`: validation contacts, address, pickup point, delivery, payment, benefit limits, partner supplementary access и stale version.
- `impl/mapper`: entity/DTO mapping.
- `impl/event`: audit events и downstream events для сборки, доставки и будущего bonus handoff.
- `impl/client`: cart, catalog, WMS/1C, delivery и payment adapters.
- `impl/config`: module metadata и OpenAPI group metadata.

## Локализация и сообщения
Все новые frontend user-facing строки размещаются в `resources_ru.ts` и `resources_en.ts`. Backend не отправляет hardcoded пользовательские тексты в API responses; для предопределенных состояний используются `STR_MNEMO_PUBLIC_FAQ_EMPTY`, `STR_MNEMO_PUBLIC_INFO_NOT_FOUND`, `STR_MNEMO_PUBLIC_DOCUMENTS_NOT_FOUND`, `STR_MNEMO_PUBLIC_BENEFIT_LANDING_NOT_FOUND`, `STR_MNEMO_REFERRAL_CODE_INVALID`, `STR_MNEMO_REFERRAL_CODE_EXPIRED`, `STR_MNEMO_REFERRAL_CODE_DISABLED`, `STR_MNEMO_BENEFIT_CONVERSION_REJECTED`, `STR_MNEMO_INVITE_CODE_INVALID`, `STR_MNEMO_INVITE_CODE_EXPIRED`, `STR_MNEMO_INVITE_CODE_DISABLED`, `STR_MNEMO_INVITE_TYPE_MISMATCH`, `STR_MNEMO_REGISTRATION_APPLICATION_CREATED`, `STR_MNEMO_REGISTRATION_DUPLICATE_CONTACT`, `STR_MNEMO_ATTRIBUTION_CONFLICT`, `STR_MNEMO_ACTIVATION_READY`, `STR_MNEMO_ACTIVATION_TOKEN_EXPIRED`, `STR_MNEMO_CONTACT_CODE_INVALID`, `STR_MNEMO_CONTACT_CODE_EXPIRED`, `STR_MNEMO_PARTNER_ACTIVATED`, `STR_MNEMO_INVITE_CREATED`, `STR_MNEMO_INVITE_RESEND_UNAVAILABLE`, `STR_MNEMO_SPONSOR_CABINET_FORBIDDEN`, `STR_MNEMO_CATALOG_SEARCH_EMPTY`, `STR_MNEMO_CATALOG_PRODUCT_NOT_FOUND`, `STR_MNEMO_CATALOG_ITEM_UNAVAILABLE`, `STR_MNEMO_CATALOG_QUANTITY_LIMIT_EXCEEDED`, `STR_MNEMO_CATALOG_CART_ITEM_ADDED`, `STR_MNEMO_DIGITAL_CATALOGUE_NOT_FOUND`, `STR_MNEMO_DIGITAL_CATALOGUE_FORBIDDEN`, `STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_UNAVAILABLE`, `STR_MNEMO_DIGITAL_CATALOGUE_SHARE_NOT_ALLOWED`, `STR_MNEMO_DIGITAL_CATALOGUE_DOWNLOAD_NOT_ALLOWED`, `STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_READY`, `STR_MNEMO_CART_RECALCULATED`, `STR_MNEMO_CART_ITEM_ADDED`, `STR_MNEMO_CART_ITEM_REMOVED`, `STR_MNEMO_CART_ITEM_UNAVAILABLE`, `STR_MNEMO_CART_QUANTITY_LIMIT_EXCEEDED`, `STR_MNEMO_CART_OFFER_AVAILABLE`, `STR_MNEMO_CART_OFFER_APPLIED`, `STR_MNEMO_CART_OFFER_UNAVAILABLE`, `STR_MNEMO_SUPPLEMENTARY_ORDER_FORBIDDEN`, `STR_MNEMO_CART_CHECKOUT_VALIDATION_FAILED`, `STR_MNEMO_ORDER_CHECKOUT_CART_INVALID`, `STR_MNEMO_ORDER_CONTACT_INVALID`, `STR_MNEMO_ORDER_ADDRESS_INVALID`, `STR_MNEMO_ORDER_DELIVERY_UNAVAILABLE`, `STR_MNEMO_ORDER_PAYMENT_METHOD_UNAVAILABLE`, `STR_MNEMO_ORDER_BENEFIT_LIMIT_EXCEEDED`, `STR_MNEMO_ORDER_PARTIAL_RESERVE`, `STR_MNEMO_ORDER_PAYMENT_FAILED`, `STR_MNEMO_ORDER_PAYMENT_EXPIRED`, `STR_MNEMO_ORDER_CHECKOUT_VERSION_CONFLICT`, `STR_MNEMO_ORDER_CONFIRMED`, `STR_MNEMO_AUTH_REQUIRED` и существующие коды публичного контента.

## Feature #10
Feature #10 добавляет модуль `order` и frontend routes оформления:
- `POST /api/order/checkouts` создает или возвращает checkout draft для `MAIN` или `SUPPLEMENTARY` на основе валидированной корзины.
- `GET /api/order/checkouts/{checkoutId}` возвращает draft владельцу или сотруднику поддержки с audit trail.
- `PUT /recipient`, `/address`, `/delivery`, `/payment`, `/benefits` обновляют шаги checkout, пересчитывают delivery/payment/benefit state и возвращают только structured DTO и `STR_MNEMO_*`.
- `POST /validation` выполняет server-side validation перед подтверждением.
- `POST /confirm` идемпотентно выполняет final validation, резерв WMS/1C, создание order, платежную сессию и audit/downstream events.
- `GET /api/order/orders/{orderNumber}` возвращает результат созданного заказа для success/retry страниц.
- Frontend открывает `/order` и `/order/supplementary`, локализует все checkout labels/status/errors через i18n и не содержит hardcoded user-facing строк.

## Feature #11
Feature #11 расширяет модуль `order` и frontend routes истории:
- `GET /api/order/order-history` возвращает список собственных заказов с фильтрами, поиском, пагинацией и summary statuses.
- `GET /api/order/order-history/{orderNumber}` возвращает детали заказа: строки, подарки, totals, delivery/payment snapshots, timeline events, warnings и actions.
- `POST /api/order/order-history/{orderNumber}/repeat` переносит доступные строки в основную корзину или supplementary cart согласно типу заказа.
- Frontend открывает `/order/order-history` и `/order/order-history/:orderId`, локализует все labels/status/errors через i18n и не содержит hardcoded user-facing строк.
- Доступ к чужому заказу запрещен ownership checks; support-просмотр требует permission context и audit event.

## Feature #9
Feature #9 добавляет модуль `cart` и frontend routes корзины:
- `GET /api/cart/current`, `POST /api/cart/items`, `PATCH/DELETE /api/cart/items/{lineId}` поддерживают чтение, добавление, изменение количества и удаление строк основной корзины.
- `GET /api/cart/shopping-offers` и `POST /api/cart/shopping-offers/{offerId}/apply` поддерживают наборы, подарки, cross-sell, upsell, retention offers и условия бесплатной доставки.
- `GET /api/cart/supplementary/current`, `POST /api/cart/supplementary/items`, `GET /api/cart/supplementary/shopping-offers` поддерживают отдельный партнерский дозаказ без смешивания с основной корзиной.
- `POST /api/cart/validate` выполняет server-side validation перед checkout.
- `GET /api/cart/support/users/{userId}/current` предоставляет support view с audit event и permission checks.
- Frontend открывает `/cart`, `/cart/shopping-offers`, `/cart/supplementary`, `/cart/supplementary/shopping-offers`, локализует все статусы через i18n и не содержит hardcoded user-facing строк.
- Module `cart` использует `catalog` для проверки productCode, цен, доступности, campaign status и order limits; WMS/payment/order остаются будущими integration boundaries.

## Feature #8
Feature #8 добавляет модуль `partner-onboarding` и frontend routes регистрации/активации:
- `GET /api/partner-onboarding/invites/validate` проверяет invite/referral-код, campaign и onboarding type, возвращая только public sponsor context.
- `POST /api/partner-onboarding/registrations` создает registration application с idempotency, consent snapshot, source attribution и CRM lead event.
- `GET /api/partner-onboarding/activations/{token}`, `POST /confirm-contact` и `POST /complete` поддерживают activation flow, подтверждение контакта, принятие условий, создание partner profile и personal referral link.
- `GET/POST /api/partner-onboarding/sponsor-cabinet/invites` и `POST /resend` поддерживают sponsor cabinet, создание invite и повторную отправку без дублей.
- Frontend открывает `/invite/beauty-partner-registration`, `/invite/business-partner-registration`, `/invite/partners-activation`, `/invite/sponsor-cabinet`, сохраняет referral/campaign context, локализует все статусы через i18n и не содержит hardcoded user-facing строк.
- CRM получает registration lead events, но недоступность CRM не блокирует заявку и фиксируется в retry/audit контуре.

## Feature #7
Feature #7 расширяет модуль `public-content` и frontend routes benefit-лендингов:
- `GET /api/public-content/benefit-landings/{landingType}` возвращает payload для `BEAUTY`, `BUSINESS`, `MEMBER`, `VIP_CUSTOMER` и `APP` с SEO, referral context, ordered blocks и CTA.
- `POST /api/public-content/benefit-landings/conversions` принимает обезличенные события просмотра и CTA-кликов.
- Frontend открывает `/beauty-benefits`, `/beauty-benefits/:code`, `/business-benefits`, `/business-benefits/:code`, `/member-benefits`, `/vip-customer-benefits`, `/the-new-oriflame-app`, отображает blocks через i18n и передает `landingType`, `variant`, `code`, `campaignId` в регистрацию, каталог, store-link или контакт со спонсором.
- Referral-код обрабатывается как untrusted input; публичный sponsor payload не содержит email, телефон и internal id.
- Недоступность analytics endpoint не блокирует CTA и переходы пользователя.

## Feature #6
Feature #6 расширяет модуль `catalog` и frontend routes `/products/digital-catalogue-current`, `/products/digital-catalogue-next`:
- `GET /api/catalog/digital-catalogues/current` возвращает текущий опубликованный цифровой выпуск.
- `GET /api/catalog/digital-catalogues/next` возвращает следующий выпуск с учетом роли и preview window.
- `GET /api/catalog/digital-catalogues/{issueCode}` поддерживает прямую ссылку или preview выпуска.
- `POST /api/catalog/digital-catalogues/materials/{materialId}/download` и `/share` создают временные URL только для разрешенных PDF-материалов.
- Frontend показывает PDF viewer, навигацию по страницам, zoom/download/share actions, локализованные состояния и hotspots перехода в `/product/:productCode`.
- S3/MinIO хранит PDF и preview assets, а публичный frontend не получает приватные ссылки без проверки module `catalog`.

## Feature #5
Feature #5 расширяет модуль `catalog` и frontend route `/product/:productCode`:
- `GET /api/catalog/products/{productCode}` для детальной карточки товара, медиа, описания, состава, вложений, доступности, ограничений заказа и рекомендаций.
- `POST /api/catalog/cart/items` принимает `source=PRODUCT_CARD`, `partnerContextId` и проверяет лимиты карточки перед добавлением в корзину.
- Frontend показывает desktop/mobile карточку товара, локализует статические подписи и `STR_MNEMO_*`, а после успешного добавления открывает checkout handoff.
- Партнер получает partner-specific контекст покупки из карточки для будущей бонусной и комиссионной логики.

## Feature #4
Feature #4 добавляет модуль `catalog` и route `/search`:
- `GET /api/catalog/search` для поиска, фильтров, сортировки, пагинации и рекомендаций.
- `POST /api/catalog/cart/items` для добавления доступного товара из выдачи в корзину.
- Frontend сохраняет параметры поиска в URL и локализует товары, теги, промо-метки и messageCode через i18n.
- Партнер получает partner-specific контекст добавления в корзину.

## Feature #3
Feature #3 расширяет публичный контур справочным самообслуживанием:
- `GET /api/public-content/faq` для FAQ с category/query/audience.
- `GET /api/public-content/info/{section}` для информационных страниц.
- `GET /api/public-content/documents/{documentType}` для документов, PDF viewer, скачивания и архива версий.

Реализация остается в модуле `public-content`, потому что FAQ, info и documents являются частью публичного CMS-контента и используют те же правила i18n, публикации, аудитории и безопасного fallback.
