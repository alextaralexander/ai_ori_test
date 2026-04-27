# Feature 034. Sequence diagram description

## Назначение
Sequence diagram описывает основной runtime-поток feature #34: администратор или оператор работает в `/admin/service`, backend `admin-service` проверяет RBAC, читает и меняет service case state, получает order/payment/WMS context, создает решения, refund/replacement/bonus actions и сохраняет audit trail.

## Основной поток очереди и карточки
1. Оператор открывает очередь service cases во frontend.
2. Frontend вызывает `GET /api/admin/service/cases` с фильтрами по claim, order, SLA, queue, owner и warehouse.
3. Controller передает запрос в service layer.
4. Service layer проверяет `ADMIN_SERVICE_VIEW` через admin-rbac.
5. Данные читаются из `admin_service_case`, связанных queue/SLA/audit таблиц.
6. Backend возвращает `ServiceCasePage` и machine-readable `allowedActions`; frontend локализует labels через i18n dictionaries.

При открытии карточки backend дополнительно агрегирует order/payment context из `admin-order`, WMS/replacement context из `admin-wms`, attachment metadata из S3/MinIO и локальное audit состояние. Ответ не содержит hardcoded user-facing text; предопределенные результаты передаются mnemonic-кодами `STR_MNEMO_ADMIN_SERVICE_*`.

## Assignment и ожидание данных
Взятие кейса в работу выполняется через `POST /cases/{id}/assignment` с `Idempotency-Key`. Backend проверяет `ADMIN_SERVICE_MANAGE`, обновляет owner, фиксирует audit event и возвращает обновленную карточку. Если нужны дополнительные данные, оператор добавляет customer-visible message через `POST /cases/{id}/messages`; service layer переводит case в `WAITING_CUSTOMER`, `WAITING_WMS` или `WAITING_FINANCE`, при необходимости ставит SLA на pause и сохраняет reasonCode.

## Decision и refund
Решение создается через `POST /cases/{id}/decisions`. Для `APPROVE_REFUND` service layer проверяет:
- текущий статус кейса;
- наличие permission scope;
- paid/refunded amount в order/payment контуре;
- payment policy и необходимость financial approval;
- отсутствие конфликтующего replacement или bonus compensation без supervisor override.

Refund action создается через `POST /decisions/{decisionId}/refund-actions`. Backend использует `Idempotency-Key`, передает claimId/orderId/amount/currency/reasonCode в payment/order контур и сохраняет локальный `admin_service_refund_action`. Если сумма превышает доступный остаток, возвращается `STR_MNEMO_ADMIN_SERVICE_REFUND_AMOUNT_EXCEEDED`.

## Replacement и bonus compensation
Replacement action создается через `POST /decisions/{decisionId}/replacement-actions`. Backend проверяет SKU, количество, склад и доступность через `admin-wms`. Успешный ответ содержит `shipmentReference`; локально сохраняется `admin_service_replacement_action`.

Bonus compensation создается из решения `APPROVE_BONUS_COMPENSATION` через service layer и интеграцию с bonuswallet. Локально сохраняется `externalBonusTransactionId`, статус компенсации и audit event. Превышение policy limit возвращает `STR_MNEMO_ADMIN_SERVICE_COMPENSATION_LIMIT_EXCEEDED`.

## Supervisor flow
Супервизор открывает `GET /supervisor/sla-board`, backend проверяет `ADMIN_SERVICE_SUPERVISOR` и возвращает агрегаты active/at risk/breached cases. Override выполняется как новое решение с `supervisor_override=true`; audit trail сохраняет old decision, new decision, reasonCode, actorUserId, permission scope и correlationId.

## Ошибки и контракты
- 403: `STR_MNEMO_ADMIN_SERVICE_FORBIDDEN_ACTION`.
- 400: validation details с field-level mnemonic-кодами.
- 409: lifecycle, refund limit, replacement unavailable, compensation limit или optimistic locking conflict.
- Все state-changing endpoints требуют `Idempotency-Key`.
- Frontend локализует статусы, ошибки, action labels и reason codes через `resources_ru.ts` и `resources_en.ts`.

## Версионный baseline
Backend sequence реализуется в текущем monolith stack Best Ori Gin: Java 25, Spring Boot 4.0.6, Maven, Hibernate, MapStruct, Lombok, Liquibase XML и PostgreSQL. Frontend sequence использует React, TypeScript, Vite, Ant Design и существующий i18n/runtime shell.
