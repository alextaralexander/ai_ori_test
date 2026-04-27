# Acceptance criteria. Feature 028. Админ: лендинги, инвайты и рефералы

## Общие критерии
1. Административная функциональность доступна только пользователям с разрешенными RBAC scopes feature #26 для marketing landing, referral code, registration funnel, attribution policy, conversion analytics и audit view.
2. Все пользовательские строки frontend вынесены в поддерживаемые i18n dictionaries `resources_ru.ts` и `resources_en.ts`; hardcoded user-facing text в React-компонентах отсутствует.
3. Backend не возвращает во frontend предопределенный пользовательский текст: для ошибок, предупреждений и статусов используются mnemonic-коды `STR_MNEMO_ADMIN_REFERRAL_*`.
4. Все артефакты, исходный код, тесты и конфигурации сохраняются в UTF-8; русский текст читается без mojibake.
5. Backend package ownership соблюден: DTO и контракты в `api`, сущности и repository interfaces в `domain`, Liquibase XML changelog в `db`, controller/service/mapper/validator/config/audit в role-specific подпакетах `impl`.
6. Monolith OpenAPI для модуля доступен через `/v3/api-docs/admin-referral`, а Swagger UI через `/swagger-ui/admin-referral`; endpoints попадают в группу автоматически по package prefix.

## Landing variants
1. Администратор может создать landing variant с типом `BEAUTY`, `BUSINESS` или `CUSTOMER_REFERRAL`, статусом, локалью, campaign code, active window и набором структурированных блоков.
2. Landing variant нельзя активировать без hero, хотя бы одного benefit block, CTA registration entrypoint, legal notice и корректного publication window.
3. Список landing variants поддерживает фильтрацию по статусу, типу, campaign code, локали, owner-у, периоду активности и поиску по slug/name.
4. Preview endpoint возвращает draft/active representation без публикации и без изменения публичного состояния.
5. При изменении активного landing variant создается новая версия; предыдущая версия остается доступной для аудита и отката.
6. Нельзя создать два активных landing variants с одинаковым slug, locale и пересекающимся active window.

## Registration funnels
1. CRM-администратор может создать и обновить registration funnel для beauty partner, business partner и customer referral scenario.
2. Funnel содержит шаги регистрации, обязательные consent codes, набор validation rules и default landing context.
3. Funnel нельзя активировать, если он не связан хотя бы с одним допустимым landing type или не содержит обязательные юридические согласия.
4. Изменения funnel rules версионируются; активные регистрации продолжают использовать версию funnel, с которой они стартовали.
5. Frontend получает структурированное описание funnel без backend user-facing текста.

## Invite/referral codes
1. CRM-администратор может сгенерировать одноразовый, многоразовый, персональный или campaign referral code.
2. Код имеет owner partner, campaign code, landing type, status, activeFrom, activeTo, maxUsageCount, usageCount и optional region constraints.
3. Backend валидирует уникальность public code и запрещает активацию кода с истекшим active window или некорректным owner.
4. Lifecycle кодов поддерживает статусы `DRAFT`, `ACTIVE`, `USED`, `EXPIRED`, `REVOKED`, `LOCKED`.
5. Повторный запрос генерации с тем же idempotency key не создает дубль, а возвращает уже созданный код.
6. Публичная проверка кода для feature #008 не раскрывает лишние персональные данные спонсора.

## Attribution policy
1. CRM-администратор может настроить priority источников атрибуции: URL referral code, ручной ввод, session context, campaign default sponsor и CRM override.
2. При конфликте источников backend сохраняет выбранный source, rejected sources и reason code для аудита.
3. Ручная корректировка sponsor attribution требует обязательного reason code и комментария.
4. Корректировка не удаляет исходную attribution history и формирует событие `ATTRIBUTION_OVERRIDDEN`.
5. Правила attribution применяются одинаково для публичной регистрации feature #008 и отчетности conversion analytics.

## Conversion analytics
1. Система фиксирует события `LANDING_VIEWED`, `CTA_CLICKED`, `REGISTRATION_STARTED`, `INVITE_CODE_VALIDATED`, `APPLICATION_SUBMITTED`, `CONTACT_CONFIRMED`, `PARTNER_ACTIVATED`, `ATTRIBUTION_OVERRIDDEN`.
2. Отчет агрегирует conversion metrics по campaign code, landing variant, funnel, source channel, sponsor, статусу события и периоду.
3. Отчет не содержит секретов, одноразовых токенов и приватных персональных данных кандидатов.
4. Фильтры отчета работают совместно и возвращают пустое состояние без ошибки, если данных нет.
5. Метрики имеют стабильные идентификаторы, чтобы frontend мог строить таблицы и графики без парсинга текста.

## Audit and security
1. Все операции создания, изменения, активации, паузы, архивации, отката, генерации кода и override attribution записываются в audit trail.
2. Audit trail содержит actorUserId, actionCode, entityType, entityId, oldValue, newValue, reasonCode, correlationId и timestamp.
3. API возвращает `403` при отсутствии RBAC scope и `409` при бизнес-конфликте версии, active window или duplicate slug/code.
4. Validation errors возвращаются как structured fields с mnemonic-кодами `STR_MNEMO_ADMIN_REFERRAL_*`.
5. Недоверенный ввод в slug, referral code, UTM, channel и comment валидируется на длину, допустимые символы и отсутствие script/markup payload.

## UI criteria
1. В админке есть экран со списком landing variants, referral codes, funnel rules, attribution policy, conversion report и audit trail.
2. Формы используют Ant Design-compatible controls: таблицы с фильтрами, segmented controls для статусов, date range picker, selects, switches, drawers/modal для редактирования и кнопки с понятными icons.
3. В UI нет видимых строк, не вынесенных в i18n dictionaries.
4. UI показывает loading, empty, validation, forbidden, conflict и retry states.
5. На desktop и mobile ширина таблиц, фильтры и action controls не перекрывают друг друга; длинные значения slug/code/campaign переносятся или обрезаются с tooltip.

## Testing criteria
1. Managed BDD scenarios начинаются с логина пользователя с соответствующей ролью.
2. Managed API test покрывает login, CRUD landing variant, активацию с validation conflict, генерацию referral code, idempotency, attribution override, conversion report и forbidden access.
3. Managed UI test покрывает вход в admin referral workspace, создание landing draft, генерацию referral code, просмотр отчета и проверку локализованных сообщений.
4. Managed end-to-end API/UI tests агрегируют реальные per-feature tests для green path и не содержат placeholder assertions.
5. Runtime-копии tests синхронизируются из `agents/tests/` по `agents/tests/targets.yml` и содержат marker comment о generated source.

## Версионная база
Критерии приемки рассчитаны на runtime baseline 27.04.2026: Java 25, Spring Boot 4.0.6, Maven, Liquibase XML, React, TypeScript, Vite и Ant Design через зависимости frontend-проекта. Новые technology-sensitive отклонения от latest-stable baseline не допускаются без отдельного compatibility decision.
