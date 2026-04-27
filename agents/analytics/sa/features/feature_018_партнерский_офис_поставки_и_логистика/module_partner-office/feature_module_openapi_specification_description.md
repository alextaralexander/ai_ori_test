# OpenAPI description feature 018. Module partner-office

## Назначение API
API module `partner-office` обслуживает frontend routes `/partner-office/all-orders`, `/partner-office/report`, `/partner-office/supply`, `/partner-office/supply/:supplyId`, `/partner-office/supply/orders/:orderId`. Canonical backend base path: `/api/partner-office`. Runtime Swagger/OpenAPI group должен быть доступен по `/v3/api-docs/partner-office` и `/swagger-ui/partner-office`.

## Endpoint `GET /api/partner-office/orders`
Возвращает `PartnerOfficeOrderPageResponse` со списком заказов офиса. Запрос принимает `X-User-Context-Id` и фильтры `dateFrom`, `dateTo`, `campaignId`, `officeId`, `regionId`, `query`, `orderStatus`, `paymentStatus`, `assemblyStatus`, `supplyId`, `deliveryStatus`, `pickupPointId`, `hasDeviation`, `page`, `size`.

Правила:
- `partner-office` видит только свой `officeId`.
- `regional-manager` видит офисы своего `regionId`.
- `logistics-operator` видит только назначенные поставки и заказы.
- Некорректные фильтры возвращают `STR_MNEMO_PARTNER_OFFICE_FILTER_INVALID`.
- Запрещенный доступ возвращает `STR_MNEMO_PARTNER_OFFICE_ACCESS_DENIED`.

## Endpoint `GET /api/partner-office/supply`
Возвращает `PartnerOfficeSupplyPageResponse` со supply-поставками. Основные поля summary: `supplyId`, `officeId`, `regionId`, `warehouseId`, `externalWmsDocumentId`, `status`, даты отгрузки и прибытия, `orderCount`, `boxCount`, `skuCount`, `deviationCount`.

## Endpoint `GET /api/partner-office/supply/{supplyId}`
Возвращает `PartnerOfficeSupplyDetailsResponse`: summary поставки, связанные orders, supply items, movements, deviations и availableActions. Если поставка не принадлежит разрешенному офису или региону, ответ должен быть `STR_MNEMO_PARTNER_OFFICE_ACCESS_DENIED`; если поставка отсутствует в разрешенной области, `STR_MNEMO_PARTNER_OFFICE_SUPPLY_NOT_FOUND`.

## Endpoint `POST /api/partner-office/supply/{supplyId}/transition`
Меняет lifecycle supply-поставки. Требует `Idempotency-Key`, request `SupplyTransitionRequest` с `targetStatus`, `reasonCode`, `comment`.

Разрешенные состояния: `PLANNED`, `IN_TRANSIT`, `ARRIVED`, `ACCEPTANCE_IN_PROGRESS`, `ACCEPTED`, `PARTIALLY_ACCEPTED`, `BLOCKED`. Недопустимый transition возвращает `STR_MNEMO_PARTNER_OFFICE_SUPPLY_STATUS_INVALID`. Успешное действие возвращает `SupplyActionResponse` с `STR_MNEMO_PARTNER_OFFICE_SUPPLY_STATUS_UPDATED` и `correlationId`.

## Endpoint `GET /api/partner-office/supply/orders/{orderNumber}`
Возвращает заказ в контексте supply-поставки: order summary, item lines, movements, deviations и `workflowLinks`. Links могут указывать на order details, claim workflow, pickup workflow и delivery workflow, но module `partner-office` не становится владельцем этих сущностей.

## Endpoint `POST /api/partner-office/supply/orders/{orderNumber}/deviations`
Фиксирует отклонение приемки. Request `DeviationCreateRequest` содержит `supplyId`, `deviationType`, `sku`, `quantity`, `reasonCode`, `comment`. Quantity должна быть больше нуля. Успешный ответ возвращает `STR_MNEMO_PARTNER_OFFICE_DEVIATION_RECORDED`; созданное отклонение доступно для дальнейшего claim workflow.

## Endpoint `GET /api/partner-office/report`
Возвращает `PartnerOfficeReportResponse` с KPI офиса и региона: `supplyCount`, `orderCount`, `shortageCount`, `damagedCount`, `shipmentSlaPercent`, `acceptanceSlaPercent`, список эскалаций. Route используется страницей `/partner-office/report`.

## DTO and validation contract
- Все monetary values передаются строкой decimal или структурой money в реализации, чтобы избежать float rounding.
- В response не передаются predefined пользовательские тексты. Для UI сообщений используются только mnemonic codes `STR_MNEMO_*`.
- `ErrorResponse.code` всегда соответствует pattern `^STR_MNEMO_[A-Z0-9_]+$`.
- Все списковые endpoints поддерживают `page` и `size` с верхней границей `100`.
- Все state-changing endpoints требуют `Idempotency-Key`.

## Required mnemonic codes
- `STR_MNEMO_PARTNER_OFFICE_FILTER_INVALID`.
- `STR_MNEMO_PARTNER_OFFICE_ACCESS_DENIED`.
- `STR_MNEMO_PARTNER_OFFICE_SUPPLY_NOT_FOUND`.
- `STR_MNEMO_PARTNER_OFFICE_SUPPLY_STATUS_INVALID`.
- `STR_MNEMO_PARTNER_OFFICE_SUPPLY_STATUS_UPDATED`.
- `STR_MNEMO_PARTNER_OFFICE_DEVIATION_RECORDED`.
- `STR_MNEMO_PARTNER_OFFICE_ESCALATION_CREATED`.

Все mnemonic codes, которые могут попасть во frontend, должны быть добавлены во все поддерживаемые frontend i18n dictionaries.

## Backend package ownership
- `api`: DTO records, enum-like public contract types.
- `domain`: JPA entities и repository interfaces.
- `db`: XML Liquibase changelog `feature_018_partner_office_supply_logistics.xml`.
- `impl/controller`: Spring MVC controller.
- `impl/service`: service interface and implementation.
- `impl/config`: module config and OpenAPI grouping.
- `impl/mapper`, `impl/validator`, `impl/exception`: runtime support classes по ответственности.

## Version baseline
Реализация использует Java 25 и Spring Boot 4.0.6 baseline текущего monolith, Maven, Hibernate/JPA, XML Liquibase и Spring MVC generated OpenAPI. Java 26.0.1 отмечен как latest stable на 27.04.2026, но не включается в feature #18 из-за текущего `maven.compiler.release=25`.
