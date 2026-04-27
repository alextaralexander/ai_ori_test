# Module OpenAPI description. partner-office

## Module API group
Module `partner-office` публикует Spring MVC endpoints под base path `/api/partner-office`. Swagger/OpenAPI формируется автоматически через springdoc для package prefix `com.bestorigin.monolith.partneroffice` и module key `partner-office`. Канонические runtime URL: `/v3/api-docs/partner-office` и `/swagger-ui/partner-office`.

## Endpoints
- `GET /orders` - список заказов партнерского офиса с фильтрами офиса, региона, кампании, supply, поиска и признака отклонения.
- `GET /supply` - список supply-поставок по office/region/status/deviation.
- `GET /supply/{supplyId}` - карточка supply с orders, items, movements, deviations и available actions.
- `POST /supply/{supplyId}/transition` - идемпотентное изменение lifecycle статуса поставки.
- `GET /supply/orders/{orderNumber}` - заказ в контексте supply с workflow links.
- `POST /supply/orders/{orderNumber}/deviations` - идемпотентная фиксация отклонения приемки.
- `GET /report` - KPI, SLA и проблемные маршруты офиса или региона.

## DTOs
- `OrderPage`, `OrderSummary`.
- `SupplyPage`, `SupplySummary`, `SupplyDetails`.
- `SupplyOrderDetails`, `SupplyItem`, `Movement`, `Deviation`.
- `TransitionRequest`, `DeviationRequest`, `ActionResponse`, `Report`, `Error`.

## Validation
- `X-User-Context-Id` обязателен для всех endpoints.
- `Idempotency-Key` обязателен для all state-changing endpoints.
- `size` ограничен диапазоном `1..100`.
- `DeviationRequest.quantity` должен быть больше нуля.
- `TransitionRequest.targetStatus` должен соответствовать разрешенному lifecycle transition.

## Mnemonic codes
Backend возвращает predefined пользовательские сообщения только через `STR_MNEMO_*`:
- `STR_MNEMO_PARTNER_OFFICE_FILTER_INVALID`.
- `STR_MNEMO_PARTNER_OFFICE_ACCESS_DENIED`.
- `STR_MNEMO_PARTNER_OFFICE_SUPPLY_NOT_FOUND`.
- `STR_MNEMO_PARTNER_OFFICE_SUPPLY_STATUS_INVALID`.
- `STR_MNEMO_PARTNER_OFFICE_SUPPLY_STATUS_UPDATED`.
- `STR_MNEMO_PARTNER_OFFICE_DEVIATION_RECORDED`.
- `STR_MNEMO_PARTNER_OFFICE_ESCALATION_CREATED`.

Frontend обязан локализовать эти mnemonic codes через поддерживаемые dictionaries.

## Security model
Service layer применяет role and scope checks:
- `partner-office` ограничен своим `officeId`.
- `regional-manager` ограничен своим `regionId`.
- `logistics-operator` ограничен назначенными supply-поставками.
- Доступ к чужим supply/order возвращает `STR_MNEMO_PARTNER_OFFICE_ACCESS_DENIED` без раскрытия данных.

## Package ownership
DTO records находятся в `api`. JPA entities и repository interfaces находятся в `domain`. Liquibase XML находится в `db`. Controllers, services, config, mapper, validator и exception classes находятся в соответствующих subpackages `impl`.

## Version baseline
Module работает в текущем monolith baseline: Java 25, Spring Boot 4.0.6, Maven, Hibernate/JPA, XML Liquibase. Java 26.0.1 доступен на 27.04.2026, но upgrade исключен из scope feature #18 из-за текущего `maven.compiler.release=25`.
