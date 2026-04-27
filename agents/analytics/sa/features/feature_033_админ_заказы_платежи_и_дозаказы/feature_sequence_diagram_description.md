# Описание sequence-диаграммы feature 033

## Назначение
Диаграмма показывает основные runtime-взаимодействия feature #33: поиск заказов в админке, открытие карточки заказа, создание supplementary order, прием идемпотентного payment event, запуск partial refund и применение anti-fraud decision. Поток описывает продуктовый backend/frontend runtime, а не delivery workflow.

## Участники
- `Операционный администратор` работает с `/admin/orders`, карточкой заказа, supplementary order и безопасными операторскими действиями.
- `Финансовый оператор` запускает refund и управляет финансовыми блокировками.
- `Антифрод-администратор` принимает risk decision.
- `Admin Web Frontend` вызывает REST API и локализует mnemonic-коды через i18n dictionaries.
- `AdminOrderController` является Spring MVC entrypoint module `admin-order`.
- `AdminOrderService`, `PaymentEventService`, `RefundService`, `RiskDecisionService` выполняют доменную оркестрацию.
- `RBAC/Policy` проверяет scopes `ADMIN_ORDER_*`, регион, канал и тип действия.
- `admin_order_* tables` хранят заказные, платежные, refund, risk и audit данные.
- `Admin WMS module` предоставляет reserve/availability/blocked reason context.
- `Payment Provider` передает payment events и принимает refund request.
- `Audit/Observability` получает доменные события и correlationId.

## Основные потоки
1. Поиск заказов: frontend вызывает `GET /api/admin/orders`, backend проверяет `ADMIN_ORDER_VIEW`, применяет фильтры и возвращает masked order summaries.
2. Карточка заказа: backend загружает заказ, строки, платежи, возвраты, риски, audit trail и дополнительно получает WMS-контекст резерва и частичной доступности.
3. Supplementary order: backend проверяет `ADMIN_ORDER_MANAGE`, idempotency key, статус parent order, active financial hold, WMS availability, anti-fraud policy и финансовые инварианты. Успешный результат создает child order и audit event.
4. Payment event ingestion: payment event обрабатывается идемпотентно. Дубль возвращает существующий результат, новое событие обновляет payment status и audit.
5. Partial refund: backend проверяет `ADMIN_ORDER_REFUND_MANAGE`, доступную к возврату сумму, hold status и финансовые инварианты. Нарушение возвращает mnemonic-код `STR_MNEMO_ADMIN_ORDER_FINANCIAL_INVARIANT_FAILED`.
6. Anti-fraud decision: risk decision обновляет risk event и пересчитывает доступность capture, fulfillment и supplementary order actions.

## Ошибки и локализация
Все business conflicts возвращаются как `ErrorResponse.code` в формате `STR_MNEMO_ADMIN_ORDER_*`. Backend не отправляет hardcoded user-facing text. Frontend отображает результат из `resources_ru.ts` и `resources_en.ts`.

## Идемпотентность и аудит
Мутирующие операции используют `Idempotency-Key`. Audit trail фиксирует actorUserId, reasonCode, old/new values, entity reference, idempotencyKey и correlationId. Payment provider callbacks и refund requests не должны создавать дубли при retry.

## Backend package ownership
Controllers размещаются в `impl/controller`, orchestration services в `impl/service`, validators в `impl/validator`, MapStruct mappers в `impl/mapper`, domain entities и repositories в `domain`, API DTO в `api`, Liquibase XML changelog в `db`.

## Версионный baseline на 28.04.2026
Последовательность рассчитана на текущий monolith stack: Java 25, Spring Boot 4.0.6, Maven, Hibernate, MapStruct, Lombok, Liquibase XML, PostgreSQL и springdoc-openapi. Frontend использует React, TypeScript, Vite, Ant Design и существующие i18n dictionaries.
