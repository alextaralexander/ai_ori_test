# OpenAPI description. Feature 032 / module admin-wms

## Назначение API
`admin-wms` API предоставляет административный REST-контракт для управления WMS/1C контуром: склады, остатки, availability rules, поставки, приемка, резервы, синхронизация, quarantine, расхождения и аудит. Swagger генерируется автоматически через springdoc-openapi для monolith module `admin-wms`: `/v3/api-docs/admin-wms` и `/swagger-ui/admin-wms`.

## Группы endpoint-ов
- `Warehouses` — поиск, создание, просмотр и изменение складов, включая статус и reason code.
- `Stocks` — поиск stock snapshots и изменение availability policy для SKU/канала.
- `Supplies` — поиск, создание поставки и подтверждение приемки с idempotency key.
- `Reservations` — резерв и release reservation для order workflow.
- `Sync` — ручной запуск синхронизации и просмотр sync messages, duplicate/quarantine/retry states.
- `Discrepancies` — закрытие или эскалация расхождений.
- `Audit` — поиск immutable audit events.

## Security model
Каждый endpoint требует bearer token и отдельный permission scope:
- `ADMIN_WMS_READ` — просмотр складов, остатков, поставок, sync messages.
- `ADMIN_WMS_WRITE` — изменение складов и availability rules.
- `ADMIN_WMS_SUPPLY_WRITE` — создание и приемка поставок.
- `ADMIN_WMS_RESERVATION_WRITE` — резервирование и release stock для заказного контура.
- `ADMIN_WMS_SYNC_RUN` — запуск ручной синхронизации.
- `ADMIN_WMS_DISCREPANCY_RESOLVE` — закрытие расхождений.
- `ADMIN_WMS_AUDIT_VIEW` — просмотр audit trail.

## Error contract
Все предопределенные пользовательские ошибки возвращаются как mnemonic-коды `STR_MNEMO_ADMIN_WMS_*` в `ErrorResponse.code`. Backend не передает hardcoded user-facing text. Frontend обязан локализовать коды через `resources_ru.ts` и `resources_en.ts`. Field-level validation возвращает `fieldErrors[].code`, также в формате mnemonic.

## Идемпотентность
Мутации, которые могут повторяться из-за retry или внешнего обмена, требуют `Idempotency-Key`: создание поставки, приемка поставки, создание резерва, release reservation, ручной sync run. Повтор с тем же ключом возвращает существующий результат или безопасное состояние без повторного изменения остатка.

## Интеграционные потребители
- Public catalog и product card читают агрегированную availability model через сервисный слой, основанный на `StockResponse`.
- Cart и checkout используют reservation endpoints для hold/release и получают structured conflict response при недостатке товара.
- Partner office получает supply и discrepancy context для приемки офлайн-поставок.
- Employee order support использует stock, reservation и sync metadata для расследования проблемных заказов.

## Версионная база
Контракт рассчитан на baseline 27.04.2026: Java 25, Spring Boot 4.0.6, Spring MVC, springdoc-openapi runtime generation, PostgreSQL и Liquibase XML. Если implementation stream меняет baseline, соответствующее отклонение должно быть отражено в architecture decision и status-файле.
