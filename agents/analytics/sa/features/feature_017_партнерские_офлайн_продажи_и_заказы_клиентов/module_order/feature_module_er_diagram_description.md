# Feature 017 module_order ER delta

Feature 017 добавляет проекцию `partner_offline_order` для клиентских офлайн-заказов партнера и `partner_offline_order_event` для timeline карточки заказа.

## partner_offline_order
- `order_number varchar(32)` PK, бизнес-номер заказа.
- `campaign_id varchar(32)` NOT NULL, каталожная кампания.
- `partner_person_number varchar(32)` NOT NULL, партнер, через которого оформлена офлайн-продажа.
- `customer_id varchar(32)` NOT NULL, клиент партнера.
- `customer_segment varchar(32)` NOT NULL, сегмент клиента.
- `order_status`, `payment_status`, `delivery_status`, `bonus_accrual_status varchar(32)` NOT NULL.
- `business_volume numeric(12,2)` NOT NULL, объем для MLM/отчетов.
- `grand_total_amount numeric(12,2)` NOT NULL и `currency_code char(3)` NOT NULL.

Индексы: `(campaign_id, partner_person_number)`, `(customer_id)`, `(order_status, payment_status, delivery_status)`. Baseline: Java 25, Spring Boot 4.0.6, XML Liquibase.