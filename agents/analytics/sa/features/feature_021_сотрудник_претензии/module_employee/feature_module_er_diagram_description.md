# Feature 021. Module employee ER diagram description

## Назначение
Feature #21 добавляет в `module_employee` операторскую проекцию претензий для маршрутов `/employee/submit-claim`, `/employee/claims-history` и `/employee/claims-history/:claimId`. Employee module владеет backoffice-очередью, SLA, маршрутом согласования, audit trail, idempotency и доступными действиями сотрудника. Публичный клиентский контур претензий остается в order module; employee module хранит операторское представление и ссылки на order claim/order/payment/WMS/support контексты.

## Таблица `employee_claim_case`
- `claim_id varchar(64)` - первичный ключ кейса employee-контура.
- `claim_number varchar(64)` - человекочитаемый номер претензии, уникальный в employee-контуре.
- `order_id varchar(64)`, `order_number varchar(64)` - ссылка на заказ и его номер для поиска и переходов.
- `customer_id varchar(64)`, `partner_id varchar(64)` - контекст владельца обращения; одно из полей обязательно.
- `source_channel varchar(32)` - источник обращения: phone, chat, email, backoffice, partner_office.
- `support_reason_code varchar(64)` - основание просмотра или изменения, обязательное для employee-действий.
- `status varchar(32)` - машинный статус: draft, submitted, in_review, warehouse_review, finance_processing, support_reply, supervisor_approval, completed, rejected.
- `sla_state varchar(32)`, `sla_due_at timestamptz`, `breached_at timestamptz` - состояние SLA и контрольные даты.
- `requested_resolution varchar(32)`, `approved_resolution varchar(32)` - запрошенное и утвержденное решение.
- `compensation_amount numeric(19,2)`, `currency_code char(3)` - утвержденная или предварительная сумма компенсации.
- `public_reason_mnemonic varchar(128)` - публичный итог для frontend только как `STR_MNEMO_*`.
- `responsible_role varchar(64)`, `assignee_id varchar(64)` - текущий владелец обработки.
- `supervisor_required boolean` - признак превышения лимита или спорного решения.
- `idempotency_key varchar(128)` - ключ защиты от дубля создания претензии сотрудником.
- `created_at timestamptz`, `updated_at timestamptz` - технические даты.

Ключи и ограничения: primary key `claim_id`, unique index по `claim_number`, unique partial index по `idempotency_key` для непустых значений, index по `(status, sla_state, sla_due_at)`, index по `order_number`, index по `customer_id`, index по `partner_id`, check на обязательность customer или partner, check на положительную компенсацию.

## Таблица `employee_claim_item`
- `claim_item_id varchar(64)` - первичный ключ позиции претензии.
- `claim_id varchar(64)` - внешний ключ на `employee_claim_case`.
- `sku varchar(128)`, `product_code varchar(128)`, `product_name_snapshot varchar(512)` - снимок позиции заказа.
- `quantity integer` - заявленное количество; `quantity > 0`.
- `problem_type varchar(64)` - тип проблемы: damaged, missing, wrong_item, expired, delivery_issue.
- `requested_resolution varchar(32)`, `approved_resolution varchar(32)` - решение на уровне позиции.
- `unit_price numeric(19,2)`, `compensation_amount numeric(19,2)` - цена и компенсация по позиции.
- `claim_available boolean` - снимок доступности претензии по позиции на момент создания.

Связь: `employee_claim_case 1:N employee_claim_item`. Индексы по `claim_id`, `sku`, `product_code`.

## Таблица `employee_claim_attachment`
- `attachment_id varchar(64)` - первичный ключ вложения.
- `claim_id varchar(64)` - внешний ключ на кейс.
- `filename varchar(255)`, `mime_type varchar(128)`, `size_bytes bigint` - metadata файла.
- `uploaded_by varchar(64)`, `uploaded_at timestamptz` - автор и время.
- `access_policy varchar(64)` - видимость: internal, public_to_customer, supervisor_only.
- `public_token varchar(128)` - безопасный token доступа; приватные S3/MinIO paths не хранятся в API-контракте.

Индексы по `claim_id`, `uploaded_at`, `access_policy`.

## Таблица `employee_claim_route_task`
- `task_id varchar(64)` - первичный ключ маршрутной задачи.
- `claim_id varchar(64)` - внешний ключ на кейс.
- `task_type varchar(32)` - warehouse, finance, customer_support, supervisor.
- `external_task_id varchar(128)` - идентификатор задачи во внешнем контуре при наличии.
- `status varchar(32)` - open, in_progress, completed, rejected, cancelled.
- `assignee_role varchar(64)`, `assignee_id varchar(64)` - исполнитель.
- `due_at timestamptz`, `completed_at timestamptz` - сроки исполнения.
- `result_code varchar(64)`, `result_payload_json jsonb` - машинный результат без пользовательского hardcoded текста.

Индексы по `claim_id`, `(task_type, status)`, `due_at`.

## Таблица `employee_claim_audit_event`
- `audit_event_id varchar(64)` - первичный ключ события аудита.
- `claim_id varchar(64)`, `order_id varchar(64)` - связанный кейс и заказ.
- `actor_user_id varchar(64)`, `actor_role varchar(64)` - сотрудник или супервизор.
- `action_type varchar(64)` - list_viewed, details_viewed, created, updated, routed, compensation_calculated, approved, rejected.
- `support_reason_code varchar(64)` - основание действия.
- `changed_fields_json jsonb` - список измененных полей и безопасные значения.
- `source_route varchar(255)` - `/employee/submit-claim`, `/employee/claims-history` или `/employee/claims-history/:claimId`.
- `correlation_id varchar(128)`, `occurred_at timestamptz` - трассировка и время.

Индексы по `claim_id`, `actor_user_id`, `action_type`, `occurred_at`, `correlation_id`. События employee audit не попадают в публичный клиентский timeline как комментарии.

## Liquibase и ownership
Изменения должны быть оформлены отдельным XML changelog для feature #21 в owning module employee. DTO размещаются в `com.bestorigin.monolith.employee.api`, доменные snapshot/repository в `com.bestorigin.monolith.employee.domain`, changelog marker package в `com.bestorigin.monolith.employee.db`, runtime-код в role-specific подпакетах `com.bestorigin.monolith.employee.impl`.
