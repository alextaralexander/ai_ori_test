# Module ER diagram description. Employee

## Назначение модуля
Глобальный модуль `employee` отвечает за backoffice/call-center сценарии, которые выполняются сотрудниками Best Ori Gin от имени клиента, партнера или в рамках внутренней поддержки. Он не становится владельцем заказов, корзины, профиля покупателя, платежей, WMS или претензий, а хранит employee-specific контекст: support session, operator order reference, support action, audit event, order-history read-model, employee claims, partner card/report audit, employee profile settings и elevated access.

## Feature 019 entities
- `employee_support_session` хранит employee-сессию поддержки: сотрудник, целевой клиент или партнер, причина, канал, время начала и закрытия.
- `employee_operator_order` хранит ссылку на операторский заказ, checkout, номер заказа, cart type, статусы оплаты/доставки, idempotency key и признак audit.
- `employee_support_action` хранит внутренние заметки, корректировки и эскалации поддержки.
- `employee_audit_event` хранит общий audit employee-действий feature #19.

## Feature 020 entities
- `employee_order_history_snapshot` хранит агрегированный snapshot заказа для списка и деталей сотрудника: order/customer/partner identifiers, маскированные контакты, campaign, order/payment/delivery/fulfillment statuses, totals, problem flags, support/claim/payment/WMS links и supervisor flags.
- `employee_order_history_item_snapshot` хранит позиции заказа для employee details: SKU, название, количество, цены, promo, bonus points и reserve status.
- `employee_order_history_audit_event` хранит audit просмотров списка, открытия деталей и deep links: actorUserId, actorRole, actionType, supportReasonCode, sourceRoute, metadata и occurredAt.

## Feature 021 entities
- `employee_claim_case` хранит operator-facing претензию: claim/order/customer/partner identifiers, source channel, support reason, status, SLA, requested/approved resolution, compensation, public mnemonic, responsible role, assignee, supervisor flag, idempotency key и технические даты.
- `employee_claim_item` хранит позиции претензии: SKU, productCode, productName snapshot, quantity, problem type, requested/approved resolution, unit price, compensation amount и claimAvailable snapshot.
- `employee_claim_attachment` хранит metadata вложений: filename, mimeType, size, uploadedBy, uploadedAt, accessPolicy и public token без приватных S3/MinIO paths.
- `employee_claim_route_task` хранит маршрутные задачи склада, финансов, customer support и supervisor approval.
- `employee_claim_audit_event` хранит audit employee-действий по претензии.

## Feature 022 entities
- `employee_partner_card_audit` фиксирует просмотры карточки партнера, отчета и переходы в связанные order/claim/support flows. Ключевые поля: actor_user_id, actor_role, support_reason_code, source_route, target_entity_type, target_entity_id, partner_id, person_number, correlation_id и occurred_at.
- `employee_partner_report_snapshot` хранит агрегированный snapshot отчета партнера: partner_id, person_number, campaign_code, region_code, total_orders, total_amount, paid_amount, returned_amount, personal_volume, group_volume, open_claim_count, delayed_delivery_count и generated_at.

## Feature 023 entities
- `employee_profile_settings` - корневая запись employee-настроек с `employee_id` PK, displayName, jobTitle, departmentCode, preferredLanguage, timezone, notificationChannel, employeeStatus, version, createdAt и updatedAt.
- `employee_contact` - контакты сотрудника с encrypted value, maskedValue, primary flag и verificationStatus. Для одного employee/contactType допустим один primary contact.
- `employee_address` - операционные адреса сотрудника: office, pickup point, remote work и legal, с regionCode, city, addressLine, postalCode, active flag и периодом действия.
- `employee_document_metadata` - metadata подтверждающих документов, связанных с elevated policy. Бинарный файл хранится через S3/MinIO abstraction, в таблице сохраняется только `file_reference_id`.
- `employee_security_event` - read-side security events без секретов: MFA/password/session/risk события, metadataJson, correlationId и occurredAt.
- `employee_elevated_request` - запрос на временный elevated mode: policyCode, reasonCode, reasonText, targetScope, requestedDurationMinutes, linkedDocumentId, status, requestedAt, decidedBy, decidedAt и version.
- `employee_elevated_session` - активная или завершенная elevated session: request, employee, policy, scope, status, startedAt, expiresAt, closedAt, closedBy, correlationId и version.
- `employee_elevated_audit` - audit настроек и elevated mode: actorUserId, targetEmployeeId, request/session, actionCode, policyCode, sourceRoute, target entity, correlationId и occurredAt.

## Ограничения и индексы
`employee_order_history_snapshot.order_number` уникален. Для списка заказов требуются индексы по `customer_id`, `partner_id`, `created_at`, статусам и GIN-индекс по `problem_flags_json`. Для order-history audit требуются индексы по `order_id`, `actor_user_id` и `occurred_at`. Количество позиции заказа всегда больше нуля.

Для feature #21 `employee_claim_case.claim_number` уникален, `idempotency_key` имеет unique partial index для непустых значений, а очередь претензий требует индексы по `(status, sla_state, sla_due_at)`, `order_number`, `customer_id`, `partner_id`, `assignee_id` и `updated_at`. `employee_claim_item.quantity` всегда больше нуля. Для вложений нужен index по `claim_id` и `access_policy`; для route tasks - index по `claim_id`, `(task_type, status)` и `due_at`; для claim audit - index по `claim_id`, `actor_user_id`, `action_type`, `occurred_at` и `correlation_id`.

Для feature #22 нужны индексы `employee_partner_card_audit(partner_id, occurred_at desc)`, `employee_partner_card_audit(actor_user_id, occurred_at desc)` и `employee_partner_report_snapshot(partner_id, campaign_code, generated_at desc)`. Денежные, volume-поля и счетчики отчета имеют check constraints на неотрицательные значения.

Для feature #23 нужны индексы `employee_contact(employee_id)`, unique partial index primary contact, `employee_address(employee_id, region_code)`, `employee_document_metadata(employee_id, linked_policy_code, verification_status)`, `employee_security_event(employee_id, occurred_at desc)`, `employee_elevated_request(employee_id, status, requested_at desc)`, `employee_elevated_session(employee_id, status, expires_at desc)`, unique partial index active session by employee/policy и audit indexes по targetEmployeeId, sessionId, correlationId. Duration check: requestedDurationMinutes от 1 до 480; expiresAt должен быть позже startedAt; validTo не раньше validFrom.

## Связи и владение
Employee module хранит только employee-owned persisted data и read-side snapshots. Связи с order, claim, partner, payment, delivery, WMS, bonus и authorization module выполняются через identifiers, correlationId, elevatedSessionId и service contracts, без жестких FK через module boundary. Предопределенные публичные причины и сообщения хранятся только как mnemonic-коды `STR_MNEMO_*`.

## Package ownership
DTO и enum-контракты находятся в `api`. Domain entities и repository interfaces находятся в `domain`. Liquibase XML находится в `db`/resources changelog employee. Контроллеры, сервисы, validators, security/policy checks, mappers, exceptions, events и audit publishers находятся в role-specific подпакетах `impl`, а не в root `impl`.

## Version baseline
Baseline на 27.04.2026: Java/Spring Boot/Maven monolith, PostgreSQL, Liquibase XML, Hibernate-compatible `uuid/jsonb/numeric/date/timestamptz`, MapStruct/Lombok, frontend TypeScript/React/Ant Design и backend-to-frontend mnemonic contract `STR_MNEMO_*`. Feature #23 не вводит новый runtime framework; compatibility upgrade должен оформляться отдельным decision.