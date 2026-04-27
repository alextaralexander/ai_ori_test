# Feature 020. Module employee ER description

## Назначение модели
Feature 020 добавляет в `module_employee` backoffice read-model для операторской истории заказов. Модель хранит агрегированный snapshot заказа и служебные audit-события employee-контура. Транзакционными владельцами заказа, платежа, доставки, WMS и претензий остаются профильные модули; employee-модуль хранит только консистентное представление для поиска, фильтрации, деталей и аудита просмотров.

## Таблица `employee_order_history_snapshot`
- `order_id varchar(64)` - первичный ключ, внутренний идентификатор заказа в employee read-model.
- `order_number varchar(64)` - уникальный номер заказа для поиска и deep links; уникальный индекс `ux_employee_order_history_order_number`.
- `campaign_code varchar(32)` - код каталожной кампании.
- `customer_id varchar(64)`, `partner_id varchar(64)` - идентификаторы клиента и партнера.
- `customer_display_name varchar(255)`, `partner_display_name varchar(255)` - отображаемые имена для employee UI.
- `masked_phone varchar(64)`, `masked_email varchar(255)` - только маскированные контакты.
- `order_status varchar(32)`, `payment_status varchar(32)`, `delivery_status varchar(32)`, `fulfillment_status varchar(32)` - статусы для фильтрации.
- `total_amount numeric(19,2)`, `currency_code varchar(3)` - сумма и валюта.
- `problem_flags_json jsonb` - массив кодов проблем: `PAYMENT_DELAY`, `FULFILLMENT_DELAY`, `OPEN_CLAIM`, `DELIVERY_EXCEPTION`, `WMS_HOLD`, `MANUAL_ADJUSTMENT`.
- `support_case_ids_json jsonb`, `claim_ids_json jsonb`, `payment_event_ids_json jsonb` - связанные объекты для переходов.
- `wms_batch_id varchar(64)`, `delivery_tracking_id varchar(128)` - внешние ключи интеграционных систем как значения, без FK на другие модули.
- `manual_adjustment_present boolean`, `supervisor_required boolean` - flags для супервизорского контроля.
- `source_channel varchar(64)` - источник заказа.
- `created_at timestamptz`, `updated_at timestamptz` - даты заказа и последнего изменения snapshot.

Индексы: `idx_employee_order_history_customer`, `idx_employee_order_history_partner`, `idx_employee_order_history_created_at`, `idx_employee_order_history_statuses`, `idx_employee_order_history_problem_flags` на JSONB через GIN.

## Таблица `employee_order_history_item_snapshot`
- `item_id uuid` - первичный ключ позиции.
- `order_id varchar(64)` - FK на `employee_order_history_snapshot.order_id` с удалением cascade.
- `sku varchar(64)`, `product_name varchar(255)` - товар.
- `quantity integer` - количество, constraint `quantity > 0`.
- `unit_price numeric(19,2)`, `total_price numeric(19,2)` - цены.
- `promo_code varchar(64)`, `bonus_points integer`, `reserve_status varchar(32)` - промо, бонусы и резерв.

Индекс: `idx_employee_order_history_item_order` по `order_id`.

## Таблица `employee_order_history_audit_event`
- `audit_event_id uuid` - первичный ключ события.
- `order_id varchar(64)` - FK на `employee_order_history_snapshot.order_id`.
- `actor_user_id varchar(64)`, `actor_role varchar(64)` - сотрудник или супервизор.
- `action_type varchar(64)` - `ORDER_HISTORY_LIST_VIEWED`, `ORDER_DETAILS_VIEWED`, `ORDER_HISTORY_DEEP_LINK_OPENED`.
- `support_reason_code varchar(64)` - причина обращения, если задана.
- `source_route varchar(255)` - `/employee/order-history`, `/employee/order-history/:orderId`, `/employee/order-support` и другие переходы.
- `metadata_json jsonb` - фильтры, target route, safe identifiers.
- `occurred_at timestamptz` - время события.

Индексы: `idx_employee_order_history_audit_order`, `idx_employee_order_history_audit_actor`, `idx_employee_order_history_audit_occurred_at`.

## Liquibase
Для feature 020 создается отдельный XML changelog `feature_020_employee_order_history.xml` в owning module db/changelog `employee`, не дополняющий файл feature #19. Changelog создает три таблицы, ограничения, индексы и стартовые seed-данные для managed tests.