# Acceptance criteria. Feature 031. Админ: цены, акции, предложения и бенефиты

## Общие критерии
1. Административный модуль pricing/promotions доступен только пользователям с RBAC scopes `ADMIN_PRICING_VIEW`, `ADMIN_PRICING_MANAGE`, `ADMIN_PROMOTION_MANAGE`, `ADMIN_PRICING_PUBLISH`, `ADMIN_PRICING_IMPORT_EXPORT`, `ADMIN_PRICING_AUDIT_VIEW`.
2. Все пользовательские строки frontend вынесены в `resources_ru.ts` и `resources_en.ts`; hardcoded user-facing text в React-компонентах отсутствует.
3. Backend не возвращает во frontend предопределенный пользовательский текст: ошибки, предупреждения и статусы передаются mnemonic-кодами `STR_MNEMO_ADMIN_PRICING_*`.
4. Все артефакты, исходный код, тесты, XML changelog и конфигурации сохраняются в UTF-8; русский текст читается без mojibake.
5. Backend package ownership соблюден: DTO и контракты в `api`, JPA/domain snapshots и repository interfaces в `domain`, Liquibase XML changelog в `db`, controller/service/config/exception/mapper/validator в role-specific подпакетах `impl`.
6. Monolith OpenAPI для модуля доступен через `/v3/api-docs/admin-pricing`, а Swagger UI через `/swagger-ui/admin-pricing`; endpoints попадают в группу автоматически по package prefix.

## Price lists и цены
1. Pricing manager может создать price list с `priceListId`, `priceListCode`, `name`, `campaignId`, `countryCode`, `currencyCode`, `channelCode`, `status`, `activeFrom`, `activeTo`, `version`.
2. `priceListCode` уникален среди неархивных прайс-листов; дубль возвращает `409` и `STR_MNEMO_ADMIN_PRICING_PRICE_LIST_CODE_CONFLICT`.
3. Базовая цена содержит `priceId`, `priceListId`, `productId`, `sku`, `basePrice`, `taxMode`, `activeFrom`, `activeTo`, `status`, `version`.
4. Нельзя активировать цену без опубликованного PIM-товара feature #29 и привязки к действующей кампании feature #030.
5. Пересекающиеся периоды активных цен для одной пары `priceListId + sku + segmentCode` блокируются с `409` и `STR_MNEMO_ADMIN_PRICING_PRICE_PERIOD_OVERLAP`.
6. Валюта цены должна совпадать с валютой price list; несовпадение возвращает `400` и field-level validation error.

## Promo prices и segment rules
1. Pricing manager может создать promo price с `promoPriceId`, `priceListId`, `sku`, `promoPrice`, `discountType`, `discountValue`, `activeFrom`, `activeTo`, `reasonCode`, `status`.
2. Promo price не может быть ниже допустимого min margin threshold без отдельного approval flag и audit reason.
3. Segment rule содержит `ruleId`, `campaignId`, `segmentCode`, `roleCode`, `partnerLevel`, `customerType`, `regionCode`, `priority`, `activeFrom`, `activeTo`, `status`.
4. Для одного сегмента приоритеты правил должны быть детерминированными; одинаковый priority для пересекающихся активных правил возвращает `409`.
5. Каталог, корзина и checkout получают только активные price rules, соответствующие роли, сегменту и текущему campaign window.

## Акции, shopping offers и bundles
1. Promotions manager может создать promotion с `promotionId`, `promotionCode`, `nameKey`, `campaignId`, `status`, `activeFrom`, `activeTo`, `audience`, `channelCode`.
2. `promotionCode` уникален среди неархивных акций; дубль возвращает `409` и `STR_MNEMO_ADMIN_PRICING_PROMOTION_CODE_CONFLICT`.
3. Shopping offer содержит `offerId`, `promotionId`, `offerCode`, `offerType`, `titleKey`, `descriptionKey`, `priority`, `stackable`, `mutuallyExclusiveGroup`, `status`.
4. Поддерживаются offer types `CROSS_SELL`, `UPSELL`, `BUNDLE`, `GIFT`, `WELCOME_BENEFIT`, `RETENTION_BENEFIT`, `THRESHOLD_BENEFIT`.
5. Bundle offer содержит обязательные и опциональные позиции с `sku`, `quantity`, `role`, `sortOrder`; нельзя активировать bundle с архивным или неопубликованным товаром.
6. Для offer можно задать eligibility conditions по сумме корзины, количеству SKU, категории, сегменту, partner level, campaignId и channelCode.
7. При конфликте stackability backend возвращает детерминированный список примененных и отклоненных offer codes с mnemonic-причинами.

## Подарки и пороговые бенефиты
1. Gift rule содержит `giftRuleId`, `promotionId`, `giftSku`, `triggerType`, `thresholdAmount`, `triggerSku`, `maxGiftQuantity`, `activeFrom`, `activeTo`, `status`.
2. Gift rule нельзя активировать, если gift SKU отсутствует в PIM, не опубликован, недоступен для текущего campaignId или не имеет базовой цены.
3. Threshold benefit содержит `benefitId`, `benefitType`, `thresholdAmount`, `thresholdQuantity`, `progressStrategy`, `rewardCode`, `status`.
4. Корзина feature #009 получает progress до threshold benefit и localized mnemonic reason, если порог не выполнен.
5. Checkout feature #010 обязан повторно валидировать примененные gifts и benefits перед созданием заказа.

## Импорт, экспорт и массовые операции
1. Pricing manager может создать import job для CSV/XLSX цен, promo prices, segment rules и offers с `jobId`, `sourceFileName`, `idempotencyKey`, `dryRun`, `status`.
2. Dry-run import валидирует строки, возвращает row-level ошибки, summary и не меняет бизнес-данные.
3. Apply import выполняется атомарно по valid batch; critical errors не создают частично примененные цены или offers.
4. Повтор import job с тем же idempotency key возвращает существующий результат и не создает дубли.
5. Export endpoint возвращает отфильтрованные price lists, prices, promotions, offers, gifts и audit summary с checksum выгрузки.

## Публикация и интеграция с пользовательскими фичами
1. Публикация price list или promotion выполняет preflight validation по PIM, campaign calendar, конфликтам периодов, сегментам, min margin и offer compatibility.
2. Published price list становится доступен публичному каталогу feature #004/#005, цифровому каталогу feature #006/#030, корзине feature #009, checkout feature #010 и партнерским офлайн-продажам feature #017.
3. Нельзя удалить опубликованный price list, promotion или offer; допускается только архивирование или pause с reason code.
4. Emergency pause немедленно исключает offer из apply-расчета, но сохраняет audit trail и readonly history.
5. Все apply/calculation endpoints возвращают correlationId для расследования расхождений цены.

## Audit и наблюдаемость
1. Все изменения price lists, prices, promo prices, segment rules, promotions, offers, gifts, imports, exports, publish/pause/archive пишутся в audit trail с `actorUserId`, `actionCode`, `entityType`, `entityId`, `oldValue`, `newValue`, `correlationId`, `occurredAt`.
2. Audit search поддерживает фильтры `priceListId`, `promotionId`, `offerId`, `sku`, `campaignId`, `actionCode`, `actorUserId`, `dateFrom`, `dateTo`, `correlationId`.
3. Ошибки публикации, импорта и конфликтов возвращают stable mnemonic-коды и не раскрывают секреты, внутренние stack traces или персональные данные.

## UI criteria
1. В админке есть workspace "Цены и промо" с вкладками: price lists, цены, promo prices, segment rules, promotions, shopping offers, gifts/threshold benefits, import/export, audit.
2. Формы используют Ant Design-compatible controls: tables, filters, segmented status controls, date range picker, upload controls, drawers/modal, switches, selects, numeric inputs, comparison drawer и icon buttons.
3. UI показывает loading, empty, validation, forbidden, conflict, overlap, import progress, dry-run errors, publish preflight, emergency pause и retry states.
4. Длинные `priceListCode`, `promotionCode`, `offerCode`, SKU, segment codes и correlationId не ломают layout на desktop и mobile.
5. Все статусы и ошибки отображаются через i18n dictionaries по mnemonic-кодам.

## Testing criteria
1. Managed BDD scenarios начинаются с логина пользователя с соответствующей ролью.
2. Managed API test покрывает login, создание price list, добавление базовой цены, отказ overlap, создание promotion, настройку shopping offer, gift rule, dry-run import, публикацию, emergency pause, audit и forbidden access.
3. Managed UI test покрывает вход в workspace, создание price list, добавление цены, создание promotion/offer/gift, dry-run import, публикацию, проверку conflict state и локализованные сообщения.
4. Managed end-to-end API/UI tests агрегируют реальные per-feature tests для green path и не содержат placeholder assertions.
5. Runtime-копии tests синхронизируются из `agents/tests/` по `agents/tests/targets.yml` и содержат marker comment о generated source.

## Версионная база
Критерии приемки рассчитаны на runtime baseline 27.04.2026: Java 25, Spring Boot 4.0.6, Maven, Liquibase XML, React, TypeScript, Vite и Ant Design через зависимости frontend-проекта. Новые technology-sensitive отклонения от latest-stable baseline не допускаются без отдельного compatibility decision.
