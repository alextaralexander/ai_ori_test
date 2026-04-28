# Полное описание OpenAPI модуля admin-fulfillment

## Модуль и Swagger
`admin-fulfillment` является отдельным monolith module с package prefix `com.bestorigin.monolith.adminfulfillment`. Все Spring MVC controllers находятся под этим package prefix и автоматически попадают в OpenAPI group:
- `/v3/api-docs/admin-fulfillment`
- `/swagger-ui/admin-fulfillment`

Ручная регистрация endpoint-ов в Swagger запрещена; группировка должна использовать центральную конфигурацию monolith Swagger и `MonolithModule.moduleKey() = "admin-fulfillment"`.

## Version baseline
Целевой baseline на 28.04.2026: Java 26 / 26.0.1 CPU, Spring Boot 4.0.6, PostgreSQL 18.3, OpenAPI 3.0.3. Совместимый fallback допускается только через централизованный backend baseline с документированным upgrade follow-up.

## Endpoint coverage
- `GET /dashboard/shipments`: операционная панель заказов и отправлений с фильтрами склада, этапа, статуса, SLA-risk, ПВЗ и correlationId.
- `POST /tasks`: создание fulfillment task по заказу или shipment.
- `POST /tasks/{taskId}/stage`: переход конвейерного этапа с обязательным `Idempotency-Key`.
- `POST /tasks/{taskId}/exceptions`: создание exception/hold/reroute/return/lost события.
- `GET /delivery-services`: поиск служб доставки.
- `POST /delivery-services`: создание draft службы доставки.
- `POST /delivery-services/{serviceId}/activate`: публикация службы после проверки тарифов и SLA.
- `POST /delivery-services/{serviceId}/tariffs`: добавление тарифа доставки.
- `POST /delivery-services/{serviceId}/sla-rules`: добавление SLA-правила.
- `GET /pickup-points`: поиск ПВЗ.
- `POST /pickup-points`: создание draft ПВЗ.
- `POST /pickup-points/{pickupPointId}/activate`: публикация ПВЗ.
- `POST /pickup-points/{pickupPointId}/temporary-close`: временное закрытие ПВЗ.
- `POST /pickup-shipments/{shipmentId}/accept`: приемка отправления в ПВЗ.
- `POST /pickup-shipments/{shipmentId}/deliver`: выдача отправления получателю.
- `POST /pickup-shipments/{shipmentId}/not-collected`: фиксация невыкупа и запуск возвратной логистики.
- `GET /integration-events`: поиск интеграционных событий.

## DTO и mnemonic-контракт
Все DTO возвращают структурированные данные, статусы и mnemonic-коды. Backend не возвращает hardcoded user-facing text. Для frontend допустимы:
- `STR_MNEMO_FULFILLMENT_*`
- `STR_MNEMO_DELIVERY_*`

Новые mnemonic-коды должны быть добавлены во все поддерживаемые frontend dictionaries в implementation stream.

## Validation
- Stage transition validator разрешает только последовательность pick -> pack -> sort -> ready-to-ship -> shipped и отдельные ветки exception/return.
- Delivery service validator проверяет уникальность service code, наличие активного тарифа и SLA перед активацией.
- Tariff validator блокирует отрицательные суммы и конфликтующие периоды.
- Pickup point validator требует владельца, адрес, график, лимиты и зону обслуживания перед публикацией.
- Pickup owner scope validator запрещает владельцу ПВЗ работать с чужими точками и shipment-ами.
- Recipient code validator сравнивает введенный код только с hash.

## Security scopes
- `ADMIN_FULFILLMENT_VIEW`
- `ADMIN_FULFILLMENT_MANAGE`
- `ADMIN_FULFILLMENT_CONVEYOR_OPERATE`
- `ADMIN_DELIVERY_MANAGE`
- `ADMIN_PICKUP_POINT_MANAGE`
- `PICKUP_POINT_OWNER_OPERATE`
- `ADMIN_FULFILLMENT_AUDIT_VIEW`

## Audit и интеграции
Каждая state-changing операция пишет audit event с actorId, actorRole, actionCode, entityType, entityId, reasonCode, sourceSystem и correlationId. Интеграционный журнал не хранит секреты и использует endpoint alias. События публикуются в:
- `admin-wms`: warehouse tasks, reserve, stock discrepancy, return acceptance.
- `order`/`admin-order`: shipment lifecycle, return, order support context.
- `delivery`: external shipment lifecycle.
- `admin-bonus`: not collected, partial delivery, return для reversal/adjustment.
- `admin-platform`: fulfillment SLA, delivery SLA, pickup load, problem rate, integration health.
