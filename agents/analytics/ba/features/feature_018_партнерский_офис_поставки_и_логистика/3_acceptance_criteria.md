# Acceptance criteria feature 018. Партнерский офис, поставки и логистика

## AC-018-01. Список всех заказов офиса
Страница `/partner-office/all-orders` доступна после логина ролям `partner-office`, `logistics-operator` и `regional-manager` и показывает заказы с полями `orderNumber`, `officeId`, `partnerPersonNumber`, `customerId`, `campaignId`, `orderStatus`, `paymentStatus`, `assemblyStatus`, `supplyId`, `deliveryStatus`, `pickupPointId`, `hasDeviation`, `grandTotalAmount`, `currency`.

## AC-018-02. Фильтры заказов офиса
API и UI списка заказов поддерживают фильтры `dateFrom`, `dateTo`, `campaignId`, `officeId`, `regionId`, `query`, `orderStatus`, `paymentStatus`, `assemblyStatus`, `supplyId`, `deliveryStatus`, `pickupPointId`, `hasDeviation`, `page`, `size`. Некорректные фильтры возвращают mnemonic `STR_MNEMO_PARTNER_OFFICE_FILTER_INVALID`.

## AC-018-03. Сводный отчет офиса
Страница `/partner-office/report` показывает агрегаты по офису и периоду: количество заказов, supply-поставок, SKU, коробов, отгружено, принято, недостачи, излишки, повреждения, SLA отгрузки, SLA транзита, SLA приемки, количество эскалаций и список проблемных маршрутов.

## AC-018-04. Список supply-поставок
Страница `/partner-office/supply` показывает supply-поставки с полями `supplyId`, `officeId`, `regionId`, `warehouseId`, `plannedShipmentDate`, `plannedArrivalDate`, `actualArrivalDate`, `status`, `orderCount`, `boxCount`, `skuCount`, `deviationCount`, `externalWmsDocumentId`.

## AC-018-05. Детали supply-поставки
Маршрут `/partner-office/supply/:supplyId` открывает карточку поставки с составом заказов, позициями, коробами, складскими движениями, статусной историей, audit metadata, внешними WMS/1C идентификаторами и доступными действиями по приемке.

## AC-018-06. Заказ внутри поставки
Маршрут `/partner-office/supply/orders/:orderId` показывает заказ в контексте поставки: состав, упаковочные места, статусы комплектности, delivery reference, pickup point, claim links, deviation records и доступные переходы.

## AC-018-07. Статусы supply lifecycle
Supply-поставка может переходить только по разрешенным состояниям `PLANNED`, `IN_TRANSIT`, `ARRIVED`, `ACCEPTANCE_IN_PROGRESS`, `ACCEPTED`, `PARTIALLY_ACCEPTED`, `BLOCKED`. Недопустимый переход возвращает mnemonic `STR_MNEMO_PARTNER_OFFICE_SUPPLY_STATUS_INVALID`.

## AC-018-08. Фиксация отклонений приемки
Партнер-офис может зафиксировать отклонения типов `SHORTAGE`, `SURPLUS`, `DAMAGED`, `WRONG_ITEM`, `MISSING_BOX` с `sku`, количеством, reason code, комментарием и ссылкой на заказ или короб. Отклонение создает audit record и доступно для claim workflow.

## AC-018-09. История складских движений
Логистический оператор видит движения типов `WMS_RESERVED`, `ASSEMBLED`, `SHIPPED`, `IN_TRANSIT`, `ARRIVED_AT_OFFICE`, `ACCEPTED_BY_OFFICE`, `DEVIATION_RECORDED` с timestamp, source system, external reference и responsible actor.

## AC-018-10. Эскалации и SLA
Региональный менеджер видит поставки с нарушенным SLA и может создать эскалацию с `reasonCode`, `comment`, `ownerUserId`, `dueAt`. Эскалация не скрывает исходное отклонение и отображается в отчете офиса.

## AC-018-11. Переходы в связанные workflows
Из карточки заказа или supply доступны переходы к связанным order, claim, pickup и delivery workflows, если у пользователя есть соответствующие права. При отсутствии прав backend возвращает `STR_MNEMO_PARTNER_OFFICE_ACCESS_DENIED`.

## AC-018-12. Защита доступа
Партнер-офис видит только свой officeId, региональный менеджер видит офисы своего regionId, логистический оператор видит назначенные supply-поставки. Прямой URL на чужой заказ или supply не раскрывает данные.

## AC-018-13. Backend package policy
Owning backend module `partner-office` соблюдает разделение `api`, `domain`, `db`, `impl`; контроллеры находятся в `impl/controller`, сервисы в `impl/service`, мапперы в `impl/mapper`, валидаторы в `impl/validator`, конфигурация OpenAPI group в `impl/config`.

## AC-018-14. Liquibase
Для feature #18 создается отдельный XML changelog `feature_018_partner_office_supply_logistics.xml` в `db` package owning module `partner-office`. SQL/YAML/JSON changesets не используются.

## AC-018-15. Swagger/OpenAPI
Runtime Swagger/OpenAPI для monolith автоматически включает endpoint group module key `partner-office` по каноническим URL `/v3/api-docs/partner-office` и `/swagger-ui/partner-office` без ручного реестра endpoint-ов.

## AC-018-16. Backend-frontend message contract
Backend не передает hardcoded predefined пользовательский текст во frontend. Все предопределенные сообщения возвращаются mnemonic-кодами `STR_MNEMO_*`; frontend резолвит их через i18n-словари.

## AC-018-17. Frontend i18n
Все новые user-facing строки маршрутов `/partner-office/all-orders`, `/partner-office/report`, `/partner-office/supply`, `/partner-office/supply/:supplyId`, `/partner-office/supply/orders/:orderId` добавлены во все поддерживаемые frontend dictionaries. React-компоненты не содержат hardcoded пользовательских строк.

## AC-018-18. UI desktop и mobile
Desktop UI показывает фильтры, таблицу заказов или поставок, KPI, timeline, deviation panel и actions без перекрытий. Mobile UI перестраивает фильтры и карточки в одну колонку; длинные `orderNumber`, `supplyId`, `externalWmsDocumentId` не выходят за контейнеры.

## AC-018-19. Тестовые данные
Тестовые данные содержат supply `BOG-SUP-018-001` для офиса `BOG-OFFICE-018-MSK`, заказ `BOG-ORD-018-001` без отклонений и заказ `BOG-ORD-018-002` с недостачей SKU, чтобы покрыть зеленый путь и проблемную приемку.

## AC-018-20. Managed tests
Canonical API и UI тесты создаются в `agents/tests/` и синхронизируются в runtime-копии по `agents/tests/targets.yml` с marker comment. End-to-end managed tests агрегируют реальный feature test #18 и уже реализованные feature tests, а не содержат placeholder assertions.

## AC-018-21. Проверки запуска
Backend и frontend должны стартовать после реализации. Managed API test feature #18, managed UI test feature #18 и end-to-end агрегирующие тесты должны проходить либо должен быть зафиксирован конкретный технический blocker без создания пустых артефактов.
