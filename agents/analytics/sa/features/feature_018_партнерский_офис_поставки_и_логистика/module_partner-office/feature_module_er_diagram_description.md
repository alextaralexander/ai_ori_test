# ER description feature 018. Module partner-office

## Назначение модели
Модель module `partner-office` хранит операционный контур supply-поставок партнерского офиса: список заказов офиса, состав поставки, строки SKU, складские движения, отклонения приемки и эскалации SLA. Owning module не хранит полные order, claim, delivery или pickup сущности; он хранит стабильные references (`orderNumber`, `claimId`, `pickupPointId`, delivery status), чтобы не нарушать ownership соседних bounded contexts.

## Таблица `partner_office_supply`
- `supply_id varchar(64)` - бизнес-ключ поставки, primary key.
- `office_id varchar(64)` - идентификатор партнерского офиса, mandatory, indexed.
- `region_id varchar(64)` - регион для доступа regional-manager и отчетов, mandatory, indexed.
- `warehouse_id varchar(64)` - склад или WMS location source, mandatory.
- `external_wms_document_id varchar(128)` - внешний документ WMS/1C, unique nullable.
- `campaign_id varchar(64)` - каталог или кампания, indexed.
- `status varchar(32)` - lifecycle `PLANNED`, `IN_TRANSIT`, `ARRIVED`, `ACCEPTANCE_IN_PROGRESS`, `ACCEPTED`, `PARTIALLY_ACCEPTED`, `BLOCKED`.
- `planned_shipment_at`, `planned_arrival_at`, `actual_arrival_at timestamptz` - SLA даты.
- `created_at`, `updated_at timestamptz` - audit timestamps.

## Таблица `partner_office_supply_order`
- `supply_order_id uuid` - surrogate primary key.
- `supply_id varchar(64)` - foreign key на `partner_office_supply.supply_id`, indexed.
- `order_number varchar(64)` - reference на order module, indexed.
- `partner_person_number varchar(64)` - reference на MLM partner.
- `customer_id varchar(64)` - customer reference без избыточных персональных данных.
- `pickup_point_id varchar(64)` - reference на pickup workflow.
- `order_status`, `payment_status`, `assembly_status`, `delivery_status varchar(32)` - snapshot статусов для списка офиса.
- `grand_total_amount numeric(19,2)`, `currency varchar(3)` - сумма заказа для операционного списка.
- `has_deviation boolean` - быстрый фильтр проблемной приемки.
- Уникальное ограничение: `(supply_id, order_number)`.

## Таблица `partner_office_supply_item`
- `supply_item_id uuid` - primary key.
- `supply_id varchar(64)` - foreign key на supply.
- `order_number varchar(64)` - reference на заказ внутри поставки.
- `sku varchar(64)` - SKU товара, indexed.
- `product_name varchar(255)` - display data из supply snapshot, не master PIM.
- `expected_quantity integer` - ожидаемое количество.
- `accepted_quantity integer` - принятое количество.
- `box_number varchar(64)` - упаковочное место.
- Проверки: количества неотрицательные, accepted quantity не должна быть отрицательной.

## Таблица `partner_office_movement`
- `movement_id uuid` - primary key.
- `supply_id varchar(64)` - foreign key на supply, indexed.
- `order_number varchar(64)` - optional reference на заказ.
- `movement_type varchar(48)` - `WMS_RESERVED`, `ASSEMBLED`, `SHIPPED`, `IN_TRANSIT`, `ARRIVED_AT_OFFICE`, `ACCEPTED_BY_OFFICE`, `DEVIATION_RECORDED`.
- `source_system varchar(32)` - `WMS_1C`, `DELIVERY`, `PARTNER_OFFICE`, `SYSTEM`.
- `external_reference varchar(128)` - внешний event/document id.
- `occurred_at timestamptz`, `actor_id varchar(64)` - audit of event source.

## Таблица `partner_office_deviation`
- `deviation_id uuid` - primary key.
- `supply_id varchar(64)` - foreign key на supply, indexed.
- `order_number varchar(64)` - reference на order.
- `sku varchar(64)` - SKU отклонения.
- `deviation_type varchar(32)` - `SHORTAGE`, `SURPLUS`, `DAMAGED`, `WRONG_ITEM`, `MISSING_BOX`.
- `quantity integer` - количество отклонения, должно быть больше нуля.
- `reason_code varchar(64)` - обязательная причина.
- `comment text` - служебный комментарий.
- `claim_id varchar(64)` - optional reference на claim workflow после создания претензии.
- `created_at timestamptz`, `created_by varchar(64)` - audit.

## Таблица `partner_office_escalation`
- `escalation_id uuid` - primary key.
- `supply_id varchar(64)` - foreign key на supply, indexed.
- `reason_code varchar(64)` - причина SLA/отклонения.
- `owner_user_id varchar(64)` - ответственный.
- `due_at timestamptz` - срок реакции.
- `status varchar(32)` - `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CANCELLED`.
- `comment text`, `created_at timestamptz` - audit and context.

## Индексы и ограничения
- `idx_partner_office_supply_office_status` по `(office_id, status)`.
- `idx_partner_office_supply_region_status` по `(region_id, status)`.
- `idx_partner_office_supply_campaign` по `campaign_id`.
- `idx_partner_office_supply_order_order_number` по `order_number`.
- `idx_partner_office_deviation_supply` по `(supply_id, deviation_type)`.
- `idx_partner_office_movement_supply_time` по `(supply_id, occurred_at)`.
- Все FK на owning tables используют `on delete restrict`; references на внешние modules не оформляются FK, чтобы не связывать bounded contexts на уровне БД.

## Package ownership
JPA entities и repository interfaces принадлежат `com.bestorigin.monolith.partneroffice.domain`. Liquibase XML changelog находится в `com.bestorigin.monolith.partneroffice.db`. Controllers, services, mappers, validators, config и exception classes находятся в `com.bestorigin.monolith.partneroffice.impl/<role>`. Runtime-классы не размещаются в root `impl`.

## Version baseline
Feature #18 не вводит новые runtime-технологии. Реализация использует Java 25 baseline текущего monolith, Spring Boot 4.0.6, Maven, Hibernate/JPA и XML Liquibase. Java 26.0.1 фиксируется как доступная latest stable на дату старта, но upgrade заблокирован текущим `maven.compiler.release=25` и должен выполняться отдельной задачей.
