# Module ER description. partner-office

## Область владения
Module `partner-office` владеет данными supply-поставок партнерского офиса, заказов в контексте поставки, строк поставки, складских движений, отклонений приемки и эскалаций. Полные заказы, претензии, доставка, пункты выдачи, клиенты и MLM-партнеры остаются в своих bounded contexts; `partner-office` хранит только stable references.

## Сущности
- `partner_office_supply` - корневая сущность поставки с бизнес-ключом `supply_id`, офисом, регионом, складом, WMS/1C документом, кампанией, lifecycle status и SLA датами.
- `partner_office_supply_order` - заказ внутри поставки с order reference, partner/customer references, pickup reference, операционными статусами и суммой.
- `partner_office_supply_item` - SKU line поставки с ожидаемым и принятым количеством, названием товара из snapshot и номером короба.
- `partner_office_movement` - неизменяемая история событий supply/order, включая WMS, delivery и partner-office source events.
- `partner_office_deviation` - отклонение приемки по supply/order/SKU с типом, количеством, причиной, комментарием и optional claim reference.
- `partner_office_escalation` - управленческая эскалация SLA или проблемного маршрута с владельцем, сроком и статусом.

## Ключи и связи
- `partner_office_supply.supply_id` - primary business key.
- Все дочерние таблицы содержат `supply_id` и связаны с `partner_office_supply` через FK `on delete restrict`.
- `partner_office_supply_order` имеет unique constraint `(supply_id, order_number)`.
- `partner_office_supply_item` и `partner_office_deviation` используют `order_number` как external reference без DB FK на order module.
- `claim_id`, `pickup_point_id`, `partner_person_number`, `customer_id`, `external_wms_document_id` являются external references и не создают межмодульные FK.

## Индексы
- `idx_partner_office_supply_office_status(office_id, status)` для списков офиса.
- `idx_partner_office_supply_region_status(region_id, status)` для regional-manager.
- `idx_partner_office_supply_campaign(campaign_id)` для фильтра кампании.
- `idx_partner_office_supply_order_order_number(order_number)` для route `/partner-office/supply/orders/:orderId`.
- `idx_partner_office_deviation_supply(supply_id, deviation_type)` для приемки и отчетов.
- `idx_partner_office_movement_supply_time(supply_id, occurred_at)` для timeline.
- `idx_partner_office_escalation_supply_status(supply_id, status)` для SLA управления.

## Ограничения
- Status supply ограничивается значениями `PLANNED`, `IN_TRANSIT`, `ARRIVED`, `ACCEPTANCE_IN_PROGRESS`, `ACCEPTED`, `PARTIALLY_ACCEPTED`, `BLOCKED`.
- Deviation type ограничивается значениями `SHORTAGE`, `SURPLUS`, `DAMAGED`, `WRONG_ITEM`, `MISSING_BOX`.
- Quantity поля не могут быть отрицательными; deviation quantity должна быть больше нуля.
- Currency хранится в ISO code длиной 3.
- Audit timestamps обязательны для изменяемых сущностей.

## Package ownership
`api` содержит DTO и contract types. `domain` содержит JPA entities и repository interfaces. `db` содержит только XML Liquibase changelogs. `impl` содержит runtime orchestration в role-specific subpackages: `controller`, `service`, `config`, `mapper`, `validator`, `exception`.

## Baseline
Module создается под Java 25 и Spring Boot 4.0.6 текущего monolith. Java 26.0.1 доступен как latest stable на 27.04.2026, но upgrade не входит в scope module `partner-office` из-за текущего `maven.compiler.release=25`.
