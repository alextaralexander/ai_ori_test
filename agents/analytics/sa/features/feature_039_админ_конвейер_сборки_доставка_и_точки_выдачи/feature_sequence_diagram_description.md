# Описание sequence diagram feature #39

## Назначение
Sequence diagram описывает runtime-взаимодействие feature #39 между frontend `/admin/fulfillment`, новым backend-модулем `admin-fulfillment`, существующими monolith modules `admin-wms`, `order`, `admin-order`, `delivery`, `admin-bonus`, `admin-platform` и PostgreSQL таблицами `admin_fulfillment_*`.

## Основной поток
1. Логистический администратор открывает операционную панель. Frontend вызывает `GET /api/admin/fulfillment/dashboard/shipments`, backend читает fulfillment tasks, shipments, pickup points и immutable events, затем возвращает страницу с машинными статусами и mnemonic-кодами причин.
2. `admin-wms` публикует reserve/order readiness event. `admin-fulfillment` создает `admin_fulfillment_task`, рассчитывает SLA и публикует KPI-событие в `admin-platform`.
3. Оператор конвейера сканирует заказ, позиции и упаковку. Frontend отправляет `POST /tasks/{taskId}/stage` с `Idempotency-Key`. Backend проверяет допустимую последовательность этапов, пишет immutable event и audit trail.
4. Если переход некорректен, backend возвращает `STR_MNEMO_FULFILLMENT_INVALID_STAGE_TRANSITION`, а задание остается на прежнем этапе. Если задание дошло до `READY_TO_SHIP`, backend идемпотентно передает shipment в `delivery` и записывает integration event.
5. Администратор доставки создает и активирует службу доставки с тарифами и SLA. Backend валидирует business constraints, сохраняет versioned entity и пишет audit event.
6. Администратор сети ПВЗ временно закрывает точку выдачи. Новые назначения блокируются, существующие shipment остаются видимыми владельцу ПВЗ и операторам.
7. Владелец ПВЗ принимает shipment, затем выдает заказ по проверочному коду. Backend сверяет hash кода, меняет статус shipment, публикует event в orders и audit.
8. При невыкупе или частичной выдаче backend создает return logistics event, отправляет структурированные события в orders, delivery и bonus, а также обновляет KPI problem rate и pickup load.

## Идемпотентность
- Все state-changing операции конвейера, приемки, выдачи, невыкупа и внешней передачи shipment требуют `Idempotency-Key`.
- Повтор того же ключа с тем же payload возвращает текущий результат без дублей.
- Повтор того же ключа с другим payload возвращает `STR_MNEMO_FULFILLMENT_IDEMPOTENCY_CONFLICT`.
- Внешние события дополнительно дедуплицируются по `(sourceSystem, externalId, idempotencyKey)`.

## Ошибки и локализация
Backend не передает во frontend hardcoded user-facing text. Предопределенные ошибки и пользовательские статусы возвращаются только mnemonic-кодами:
- `STR_MNEMO_FULFILLMENT_INVALID_STAGE_TRANSITION`
- `STR_MNEMO_FULFILLMENT_TASK_NOT_FOUND`
- `STR_MNEMO_FULFILLMENT_REASON_REQUIRED`
- `STR_MNEMO_DELIVERY_PICKUP_POINT_TEMPORARILY_CLOSED`
- `STR_MNEMO_DELIVERY_PICKUP_POINT_CAPACITY_EXCEEDED`
- `STR_MNEMO_DELIVERY_RECIPIENT_CODE_INVALID`

Frontend обязан локализовать эти коды через supported i18n dictionaries.

## Security и audit
- Операционная панель требует `ADMIN_FULFILLMENT_VIEW`.
- Изменение этапов конвейера требует `ADMIN_FULFILLMENT_CONVEYOR_OPERATE`.
- Настройка служб доставки требует `ADMIN_DELIVERY_MANAGE`.
- Настройка ПВЗ требует `ADMIN_PICKUP_POINT_MANAGE`.
- Операции владельца ПВЗ требуют `PICKUP_POINT_OWNER_OPERATE` и server-side owner scope.
- Audit trail фиксирует actorId, actorRole, actionCode, entityType, entityId, reasonCode, sourceSystem, before/after summary, createdAt и correlationId.

## Package ownership
- `api`: REST DTO, response/request models и enum-like contract values.
- `domain`: JPA entities и repository interfaces для `admin_fulfillment_*`.
- `db`: отдельный Liquibase XML changelog feature #39.
- `impl/controller`: Spring MVC controllers.
- `impl/service`: orchestration services.
- `impl/validator`: stage, tariff, SLA, pickup point и owner-scope validators.
- `impl/mapper`: MapStruct mappers между domain и API DTO.
- `impl/client`: module clients для WMS, delivery, orders, bonus и platform KPI.
- `impl/security`: permission checks и pickup owner scope checks.
- `impl/config`: `MonolithModule` и OpenAPI group registration.
