# Sequence description. Feature 032. Admin WMS/1C

## Основной поток
Последовательность описывает взаимодействие административного frontend `/admin/wms`, backend module `admin-wms`, смежных модулей PIM/catalog/order/partner-office и внешних систем WMS/1C.

1. Логистический администратор открывает списки складов и остатков. Frontend вызывает `GET /warehouses` и `GET /stocks`, backend фильтрует данные с учетом RBAC scope, региона и склада.
2. Изменение availability policy SKU проходит через `POST /stocks/{stockItemId}/availability-rule`. Backend проверяет, что SKU опубликован в admin-pim, catalog period валиден в admin-catalog, затем атомарно обновляет rule и stock snapshot.
3. Поставка создается через `POST /supplies` с idempotency key, затем приемка подтверждается через `POST /supplies/{id}/acceptance`. Повтор того же key возвращает существующий результат без повторного stock movement.
4. Order/checkout workflow резервирует товар через `POST /reservations`. При достаточном sellable quantity backend создает резерв и меняет stock snapshot; при недостатке возвращает `STR_MNEMO_ADMIN_WMS_STOCK_NOT_ENOUGH` с доступным количеством и backorder possibility.
5. Оператор интеграции запускает `POST /sync-runs`; module `admin-wms` получает документы WMS/1C, проверяет идемпотентность, игнорирует дубли, конфликтные сообщения переводит в quarantine, валидные сообщения применяет к поставкам и остаткам.

## Транзакционные границы
- Изменение availability rule и stock snapshot выполняется в одной транзакции с audit event.
- Приемка поставки блокирует строки supply и stock rows на время расчета movements, чтобы исключить двойное увеличение остатка.
- Резервирование блокирует stock row по складу/SKU/каналу и не допускает отрицательный sellable quantity без backorder policy.
- Sync message фиксируется до применения движения, поэтому retry видит предыдущий результат и не создает дубль.

## Ошибки и компенсации
- Business conflicts возвращаются как `409` с `STR_MNEMO_ADMIN_WMS_*`.
- Conflict with confirmed movement переводит сообщение в `QUARANTINED`, не меняя sellable stock.
- Отмена или release reservation применяется один раз по idempotency key.
- Недостаточная доступность возвращает structured conflict response для order workflow, без hardcoded user-facing text.

## Package ownership
Реализация должна соблюдать `api/domain/db/impl`:
- `api`: REST DTO, enum contracts, request/response models.
- `domain`: JPA entities и repository interfaces.
- `db`: XML Liquibase changelog для feature #32.
- `impl/controller`, `impl/service`, `impl/validator`, `impl/mapper`, `impl/security`, `impl/scheduler`, `impl/client`, `impl/exception`: runtime orchestration code.

## Версионная база
Baseline на 27.04.2026: Java 25, Spring Boot 4.0.6, Maven, Hibernate/JPA, MapStruct, Lombok, Liquibase XML, PostgreSQL, React/TypeScript/Vite/Ant Design для frontend. Module `admin-wms` должен получить OpenAPI group `/v3/api-docs/admin-wms` и Swagger UI `/swagger-ui/admin-wms` через central monolith grouping.
