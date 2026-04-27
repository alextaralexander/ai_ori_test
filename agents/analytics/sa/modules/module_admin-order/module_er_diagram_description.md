# ER-описание module_admin-order

## Назначение модели
Модель `admin-order` фиксирует административный контур заказов Best Ori Gin: просмотр и сопровождение заказа, supplementary order, split/merge fulfillment, payment events, refund/dispute-сценарии, financial hold, anti-fraud decisions, операторские корректировки и audit trail. Модуль является владельцем административного представления заказа и финансово-операционных действий; публичный checkout, WMS, доставка, бонусы и платежный провайдер остаются смежными bounded contexts.

## Основные сущности
- `admin_order_order` хранит агрегат административного заказа: order number, parent order для дозаказа, cart/customer/partner context, catalog period, source channel, статусы заказа, оплаты и fulfillment, warehouse reference, суммы, скидки, бонусные списания, refund amount и признак active financial hold.
- `admin_order_line` хранит состав заказа и связь строки с fulfillment group. В строке фиксируются SKU, productId, количество, цена, скидки, бонусы, итог строки и line status.
- `admin_order_fulfillment_group` описывает split/merge результат: склад, резерв, shipment reference, fulfillment status, блокирующую складскую причину и признак частичной доступности.
- `admin_order_payment_event` хранит платежные события провайдера: provider, externalPaymentId, idempotencyKey, operation type, payment status, amount, currency, payload checksum, retry status и correlationId.
- `admin_order_refund_operation` хранит полный или частичный возврат с типом, статусом, суммой, reasonCode, external refund id, idempotencyKey и correlationId.
- `admin_order_financial_hold` фиксирует финансовую блокировку заказа, связанную с платежом или ручной проверкой: reasonCode, comment, срок, постановщик и снятие блокировки.
- `admin_order_risk_event` хранит антифрод-событие: risk score, rule codes, provider response, decision status, reasonCode, actor и correlationId.
- `admin_order_operator_action` фиксирует контролируемые ручные действия оператора: корректировки контакта, комментария, адреса до отгрузки, split/merge, создание дозаказа и сервисные переходы.
- `admin_order_audit_event` является неизменяемым журналом изменений для расследования действий по order/cart/payment/refund/risk/operator action.

## Связи и инварианты
- Один `admin_order_order` содержит много `admin_order_line`, `admin_order_fulfillment_group`, `admin_order_payment_event`, `admin_order_refund_operation`, `admin_order_financial_hold`, `admin_order_risk_event` и `admin_order_operator_action`.
- `parent_order_id` связывает supplementary order с исходным заказом. У дозаказа должен быть валидный parent order, а цепочка должна отображаться в клиентской истории, employee support и admin order card.
- `admin_order_payment_event` обрабатывается идемпотентно по `provider_code + external_payment_id + idempotency_key`. Повтор не меняет `paid_amount` повторно.
- `admin_order_refund_operation` не может превышать доступную к возврату сумму: фактически оплаченная сумма минус уже возвращенные суммы.
- `admin_order_financial_hold` блокирует capture, fulfillment и дозаказы, пока hold active и у пользователя нет отдельного override scope.
- `admin_order_operator_action` не предназначен для прямой ручной смены `paid_amount`, `refunded_amount`, `bonus_writeoff_amount` или финансового итога заказа.
- Split/merge через `admin_order_fulfillment_group` должен сохранять сумму заказа, скидки, бонусы и количество строк.

## Backend package ownership
- `api` содержит REST DTO, enum-контракты статусов, request/response модели и OpenAPI-facing типы.
- `domain` содержит JPA entities и repository interfaces для таблиц `admin_order_*`.
- `db` содержит только Liquibase XML changelog feature #33.
- `impl/controller` содержит Spring MVC controllers административного API.
- `impl/service` содержит orchestration services для заказов, дозаказов, платежей, refund, risk и audit.
- `impl/mapper` содержит MapStruct mappers между domain entities и API DTO.
- `impl/validator` содержит проверки RBAC, статусов, idempotency, financial invariants и refund limits.
- `impl/event` содержит публикацию внутренних domain events для интеграций с WMS, delivery, bonus и observability.

## Интеграции
- WMS/admin-wms передает warehouseId, reserve status, partial availability, shipment reference и blocking reasons.
- Платежный провайдер передает authorization, capture, cancel, refund, dispute и chargeback events.
- Employee support и customer order history читают цепочку заказа и дозаказов, но не владеют финансовыми переходами.
- Admin RBAC feature #26 является источником permission scopes `ADMIN_ORDER_*`.
- Frontend получает predefined ошибки и предупреждения только mnemonic-кодами `STR_MNEMO_ADMIN_ORDER_*`.

## Версионный baseline на 28.04.2026
Backend реализуется в текущем monolith stack Best Ori Gin: Java 25, Spring Boot 4.0.6, Maven, Hibernate, MapStruct, Lombok, Liquibase XML и PostgreSQL. Frontend использует React, TypeScript, Vite, Ant Design и существующие i18n dictionaries. Фича не вводит новый runtime stack.
