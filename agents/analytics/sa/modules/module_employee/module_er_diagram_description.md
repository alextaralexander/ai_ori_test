# Module ER diagram description. Employee

## Назначение модуля
Глобальный модуль `employee` отвечает за backoffice/call-center сценарии, которые выполняются от имени клиента или партнера. Он не становится владельцем заказов, корзины, профиля, платежей, WMS или претензий, а хранит employee-specific контекст: support session, operator order reference, support action, audit event и order-history read-model.

## Feature 019 entities
- `employee_support_session` хранит employee-сессию поддержки: сотрудник, целевой клиент или партнер, причина, канал, время начала и закрытия.
- `employee_operator_order` хранит ссылку на операторский заказ, checkout, номер заказа, cart type, статусы оплаты/доставки, idempotency key и признак audit.
- `employee_support_action` хранит внутренние заметки, корректировки и эскалации поддержки.
- `employee_audit_event` хранит общий audit employee-действий feature 019.

## Feature 020 entities
- `employee_order_history_snapshot` хранит агрегированный snapshot заказа для списка и деталей сотрудника: order/customer/partner identifiers, маскированные контакты, campaign, order/payment/delivery/fulfillment statuses, totals, problem flags, support/claim/payment/WMS links и supervisor flags.
- `employee_order_history_item_snapshot` хранит позиции заказа для employee details: SKU, название, количество, цены, promo, bonus points и reserve status.
- `employee_order_history_audit_event` хранит audit просмотров списка, открытия деталей и deep links: actorUserId, actorRole, actionType, supportReasonCode, sourceRoute, metadata и occurredAt.

## Ограничения и индексы
`employee_order_history_snapshot.order_number` уникален. Для списка требуются индексы по `customer_id`, `partner_id`, `created_at`, статусам и GIN-индекс по `problem_flags_json`. Для audit требуются индексы по `order_id`, `actor_user_id` и `occurred_at`. Количество позиции заказа всегда больше нуля.

## Package ownership
DTO и enum-контракты находятся в `api`. Domain snapshot и repository interface находятся в `domain`. Liquibase XML находится в `db`/resources changelog employee. Контроллеры, сервисы, validators, exceptions и in-memory repository находятся в role-specific подпакетах `impl`, а не в root `impl`.

## Version baseline
Baseline на 27.04.2026: Java 25, Spring Boot 4.0.6, Maven 4-compatible build, Hibernate/Liquibase/MapStruct/Lombok по latest stable baseline задачи без понижения версий текущего monolith.