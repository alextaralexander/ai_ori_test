# Module admin-catalog. Full ER description

Модуль `admin-catalog` хранит административную модель кампаний и выпусков каталога. На текущем этапе runtime использует in-memory service state для быстрых управляемых тестов, а Liquibase XML описывает целевую PostgreSQL-модель.

## Сущности
- `admin_catalog_campaign` - 21-дневная кампания с кодом, локалью, аудиторией, датами и статусом.
- `admin_catalog_issue` - выпуск каталога внутри кампании: current/next/archive flags, publication/archive dates, freeze и rollover windows.
- `admin_catalog_material` - PDF, cover, page image и связанные материалы с checksum, storageKey, версией и approval status.
- `admin_catalog_page` - отдельная страница выпуска с изображением и размерами.
- `admin_catalog_hotspot` - интерактивная зона страницы, связанная с PIM productId/SKU и promoCode.
- `admin_catalog_rollover_job` - идемпотентный результат переключения текущего и следующего выпуска.
- `admin_catalog_audit_event` - immutable журнал изменений.

## Ключи и ограничения
- Все таблицы используют `uuid` PK.
- `campaign_code` уникален среди неархивных кампаний через partial unique index.
- `issue_code` уникален глобально в модуле.
- `page_number` уникален в рамках `issue_id`.
- `checksum` индексируется для approved/uploaded материалов.
- Hotspot coordinates ограничены диапазоном `0..1`.
- `idempotency_key` уникален для rollover job.

## Связи
`campaign 1:N issue`, `issue 1:N material/page/hotspot/rollover/audit`. Hotspots ссылаются на PIM product contract по `product_id`/`sku`, но физический FK в feature #30 не вводится, чтобы не связывать модуль с внутренней схемой admin-pim.

## Версионная база
Baseline: 27.04.2026, Java 25, Spring Boot 4.0.6, PostgreSQL JSONB, Liquibase XML, Maven.
