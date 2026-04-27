# Feature 034 / module admin-service ER description

## Назначение модели
Модуль `admin-service` хранит административный контур обработки претензий, возвратов, замен, бонусных компенсаций, SLA и supervisor override. Он не дублирует заказный, платежный, WMS или бонусный домены, а хранит ссылочные идентификаторы `order_id`, `payment_reference`, `warehouse_id`, `external_refund_id`, `shipment_reference` и `external_bonus_transaction_id` для согласованной интеграции.

## Таблицы и поля

### `admin_service_case`
Основная сущность сервисного кейса. Поле `service_case_id` является PK. `claim_number` уникален и используется для операторского поиска. `source_claim_id` связывает admin service с customer/employee claim flow feature #12/#21. `order_id`, `customer_id`, `partner_id` и `warehouse_id` задают контекст заказа и логистики. `queue_id` указывает текущую очередь, `owner_user_id` текущего ответственного. `claim_type`, `priority`, `case_status` и `sla_status` управляют lifecycle и фильтрами. `reaction_due_at` и `resolution_due_at` фиксируют SLA deadlines. `version` нужен для optimistic locking.

### `admin_service_queue`
Справочник рабочих очередей. `queue_code` уникален, `queue_name` отображается во frontend через i18n label mapping. `default_owner_group`, `region_code` и `warehouse_id` ограничивают применимость очереди. `active=false` запрещает маршрутизацию новых кейсов, но не удаляет исторические связи.

### `admin_service_routing_rule`
Правила автоматической маршрутизации. Каждое правило связано с `queue_id`; сочетание `claim_type`, `channel_code`, `region_code`, `warehouse_id`, `priority` и `sort_order` определяет матчинг. `rule_status` позволяет отключить правило без удаления. Для runtime рекомендуется уникальный индекс на `queue_id, claim_type, channel_code, region_code, warehouse_id, priority, sort_order`.

### `admin_service_sla_policy`
SLA policy содержит `policy_code`, `claim_type`, `priority`, интервалы реакции, решения и эскалации в минутах, а также `business_calendar_code`. Активная policy применяется при создании или повторной маршрутизации кейса. Рекомендуется уникальный индекс на `policy_code` и частичный индекс по `claim_type, priority` для `active=true`.

### `admin_service_message`
Хранит переписку и внутренние заметки. `message_type` различает customer message, internal note и system event. `visibility` запрещает показ внутренних заметок клиенту. `customer_visible_message_code` содержит mnemonic-код `STR_MNEMO_ADMIN_SERVICE_*`, если сообщение является предопределенным пользовательским результатом. Текстовые тела и rich content хранятся через `body_storage_ref`, чтобы не смешивать большие payload с основной таблицей.

### `admin_service_attachment`
Связанные вложения кейса или сообщения. `storage_key`, `checksum`, `mime_type`, `size_bytes` и `attachment_status` описывают файл в S3/MinIO. Удаление физического файла не выполняется обычным операторским действием; вместо этого используется статус `REDACTED` с audit event.

### `admin_service_decision`
Финальное или промежуточное решение по кейсу. `decision_type` принимает значения `APPROVE_REFUND`, `APPROVE_REPLACEMENT`, `APPROVE_BONUS_COMPENSATION`, `REJECT`, `REQUEST_INFO`, `ESCALATE`. `decision_status` отражает draft, requested, approved, rejected, completed или cancelled state. `reason_code`, `actor_user_id`, `supervisor_override`, `customer_message_code` и `correlation_id` обязательны для аудита и локализации.

### `admin_service_refund_action`
Денежный возврат, созданный из решения. Связан с `decision_id` и `service_case_id`, хранит `order_id`, `payment_reference`, сумму, валюту, `refund_status`, внешний идентификатор refund и `idempotency_key`. Backend обязан проверять, что сумма не превышает доступный остаток возврата в заказном/платежном контуре.

### `admin_service_replacement_action`
Замена товара по кейсу. Хранит `sku`, `quantity`, `warehouse_id`, `replacement_status`, `shipment_reference` и `correlation_id`. Фактическое резервирование и отгрузка выполняются WMS/логистическим контуром, а admin-service хранит только контролируемую задачу и внешний reference.

### `admin_service_bonus_compensation`
Бонусная компенсация через бонусный контур. Хранит клиента/партнера, сумму бонусов, статус и `external_bonus_transaction_id`. Нельзя создавать конфликтующую бонусную компенсацию и refund по одной позиции без supervisor override; это проверяется сервисным слоем.

### `admin_service_wms_event`
Входящие логистические события: прием возврата, дефект, пересорт, утеря, replacement shipment update. Идемпотентность обеспечивается через `source_system`, `external_event_id` и `payload_checksum`; `retry_status` показывает состояние обработки.

### `admin_service_audit_event`
Immutable audit trail для всех действий admin-service. Сохраняет `actor_user_id`, `action_code`, `entity_type`, `entity_id`, `old_value`, `new_value`, `reason_code`, `idempotency_key`, `correlation_id` и `occurred_at`. JSONB поля используются только для audit snapshot, не как основной источник бизнес-состояния.

## Ограничения и индексы
- Все PK имеют тип `uuid`.
- Все FK внутри модуля должны быть явными: `service_case.queue_id`, `routing_rule.queue_id`, `message.service_case_id`, `attachment.service_case_id`, `attachment.message_id`, `decision.service_case_id`, action tables к `decision_id` и `service_case_id`, `wms_event.service_case_id`.
- Внешние связи с order/payment/WMS/bonus контурами хранятся как references без FK между модулями.
- Индексы нужны по `claim_number`, `order_id`, `customer_id`, `partner_id`, `queue_id`, `owner_user_id`, `case_status`, `sla_status`, `claim_type`, `priority`, `reaction_due_at`, `resolution_due_at`, `correlation_id`.
- Уникальные индексы нужны для `admin_service_queue.queue_code`, `admin_service_sla_policy.policy_code`, `admin_service_refund_action.idempotency_key`, `admin_service_wms_event.source_system + external_event_id`.

## Package ownership
- `api`: DTO и request/response contracts admin-service.
- `domain`: JPA entities и repository interfaces для перечисленных таблиц.
- `db`: только Liquibase XML changelog `feature_034_admin_service.xml`.
- `impl/controller`: Spring MVC endpoints `/api/admin/service/**`.
- `impl/service`: lifecycle, routing, SLA, decisions, action orchestration.
- `impl/exception`: access, validation и conflict exceptions с mnemonic-кодами `STR_MNEMO_ADMIN_SERVICE_*`.
- `impl/config`: module config и Swagger group registration.

## Версионный baseline
Реализация использует текущий monolith stack Best Ori Gin: Java 25, Spring Boot 4.0.6, Maven, Hibernate, MapStruct, Lombok, Liquibase XML и PostgreSQL. Frontend-потребители используют React, TypeScript, Vite, Ant Design и существующие i18n dictionaries. Переход на более новую runtime baseline не входит в feature #34 и требует отдельного compatibility decision.
