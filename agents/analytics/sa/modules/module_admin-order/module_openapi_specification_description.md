# OpenAPI-описание module_admin-order

## Назначение API
`Best Ori Gin Admin Orders API` описывает административный REST-контракт для заказов, дозаказов, платежей, refund, financial hold, антифрода, операторских действий и audit trail. Runtime Swagger должен генерироваться автоматически через springdoc-openapi для monolith module key `admin-order`: JSON `/v3/api-docs/admin-order`, UI `/swagger-ui/admin-order`.

## Группы endpoints
- `Orders`: поиск заказов, карточка заказа и статусные переходы по разрешенному lifecycle.
- `SupplementaryOrders`: создание дозаказа с parentOrderId, reasonCode, проверками каталога, склада, оплаты, бонусов и антифрода.
- `OperatorActions`: безопасные операторские действия, включая комментарий, контактный сценарий, адрес до отгрузки, split и merge.
- `Payments`: прием и поиск payment events от провайдера с идемпотентностью по `Idempotency-Key`, provider и externalPaymentId.
- `Refunds`: запуск полного или частичного возврата в пределах доступной к возврату суммы.
- `FinancialHolds`: создание и снятие финансовых блокировок, которые управляют capture, fulfillment и дозаказами.
- `Risk`: ручное решение по anti-fraud risk event.
- `Audit`: поиск и export audit events с учетом прав доступа и маскирования чувствительных данных.

## Security и RBAC
Все endpoints требуют bearer JWT. Frontend может скрывать недоступные actions, но backend остается источником истины:
- `ADMIN_ORDER_VIEW` нужен для поиска и просмотра заказов, платежей и audit summary.
- `ADMIN_ORDER_MANAGE` нужен для статусных переходов, supplementary order и операторских действий.
- `ADMIN_ORDER_PAYMENT_MANAGE` нужен для payment events и financial holds.
- `ADMIN_ORDER_REFUND_MANAGE` нужен для refund.
- `ADMIN_ORDER_RISK_MANAGE` нужен для anti-fraud decisions.
- `ADMIN_ORDER_AUDIT_VIEW` нужен для поиска audit events.
- `ADMIN_ORDER_EXPORT` нужен для выгрузки audit data.

Отказ по правам возвращает `ErrorResponse.code`, например `STR_MNEMO_ADMIN_ORDER_FORBIDDEN_ACTION`. Backend не возвращает hardcoded user-facing text; frontend локализует mnemonic-коды через `resources_ru.ts` и `resources_en.ts`.

## Идемпотентность
Мутирующие операции принимают обязательный header `Idempotency-Key`, кроме простого чтения:
- payment event ingestion не должен повторно увеличивать `paidAmount`;
- supplementary order не должен создаваться повторно при retry;
- refund не должен дублировать provider operation;
- status transition, financial hold release и risk decision должны возвращать прежний результат при повторном ключе.

## Финансовые инварианты
API должен блокировать:
- refund выше доступной к возврату суммы;
- split/merge с потерей строки, скидки, бонуса или итоговой суммы;
- ручное изменение `paidAmount`, `refundedAmount`, `bonusWriteoffAmount` через operator action;
- supplementary order к финансово заблокированному или недопустимому order status;
- payment event с конфликтом валюты или суммы.

Нарушения возвращают mnemonic-коды `STR_MNEMO_ADMIN_ORDER_FINANCIAL_INVARIANT_FAILED`, `STR_MNEMO_ADMIN_ORDER_INVALID_STATUS_TRANSITION`, `STR_MNEMO_ADMIN_ORDER_REFUND_LIMIT_EXCEEDED` или близкий доменный код.

## DTO и чувствительные данные
`AdminOrderDetails` объединяет summary, lines, fulfillment groups, payment events, refunds, financial holds, risk events, supplementary order chain, audit events и allowed actions. Платежные и персональные данные в DTO должны быть маскированы, если scope пользователя не разрешает раскрытие. Export API должен учитывать те же ограничения.

## Backend package ownership
- API DTO и enum-контракты размещаются в `api`.
- JPA entities и repositories для `admin_order_*` размещаются в `domain`.
- Liquibase XML changelog feature #33 размещается в `db`.
- Controllers размещаются в `impl/controller`.
- Services размещаются в `impl/service`.
- Validators размещаются в `impl/validator`.
- Mappers размещаются в `impl/mapper`.
- Events и observability hooks размещаются в `impl/event` или role-specific подпакетах `impl`.

## Версионный baseline на 28.04.2026
Контракт рассчитан на текущий monolith stack Best Ori Gin: Java 25, Spring Boot 4.0.6, Maven, Hibernate, MapStruct, Lombok, Liquibase XML, PostgreSQL и springdoc-openapi. Frontend-клиент использует React, TypeScript, Vite, Ant Design и существующие i18n dictionaries.
