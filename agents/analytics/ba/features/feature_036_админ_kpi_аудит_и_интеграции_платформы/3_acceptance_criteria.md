# Acceptance criteria feature #36

## KPI dashboard
- Администратор с ролью `business-admin`, `bi-analyst` или `super-admin` открывает `/admin/platform` и видит KPI: GMV, conversion, orders, fulfillment SLA, delivery SLA, bonus accruals и claims rate.
- KPI возвращаются backend endpoint-ом `/api/admin/platform/kpis` с периодом, валютой, временем обновления и трендом без hardcoded user-facing текста.
- Фильтры периода, кампании, региона и канала не ломают ответ: при пустом результате возвращается структурированный пустой список и mnemonic-код.

## Audit
- Роль `audit-admin` или `super-admin` получает `/api/admin/platform/audit-events` с фильтрами `actor`, `domain`, `actionCode`, `correlationId`, `from`, `to`.
- Audit response содержит `actorRole`, `domain`, `actionCode`, `reasonCode`, `correlationId`, `occurredAt` и masked subject reference.
- Backend не возвращает предопределенные русские UI-тексты; ошибки представлены кодами `STR_MNEMO_ADMIN_PLATFORM_*`.

## Integrations
- Роль `integration-admin` или `super-admin` видит `/api/admin/platform/integrations` для WMS/1С, assembly, delivery, payment, bonus и analytics adapters.
- Изменение настроек интеграции через `PUT /api/admin/platform/integrations/{integrationCode}` требует reasonCode и idempotency key.
- Система валидирует SLA minutes, retry policy и maintenance window; при ошибке возвращает mnemonic-код.

## Reports and alerts
- Экспорт запускается через `POST /api/admin/platform/reports/exports`, возвращает exportId, format, status, requestedByRole и correlationId.
- Alerting показывает активные предупреждения по failed exchanges, stale KPI source, SLA breach и metric anomaly.
- Frontend локализует все пользовательские строки через `resources_ru.ts` и `resources_en.ts`.