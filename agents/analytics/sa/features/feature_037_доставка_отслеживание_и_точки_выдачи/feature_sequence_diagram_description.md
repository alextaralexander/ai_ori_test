# Feature 037 sequence diagram description

## Версионный baseline
Feature #37 использует текущий backend/frontend baseline репозитория на дату старта 28.04.2026: Java 21, Spring Boot 3.x, Maven 3.9.x, PostgreSQL 16.x, Liquibase 4.x, TypeScript 5.x, React 18/19 по центральной настройке frontend, Ant Design 5.x. Версии должны задаваться централизованно в build baseline и package manager конфигурации, без локального дублирования в module_delivery.

## Основной поток checkout
1. Покупатель или партнер открывает checkout во frontend.
2. Frontend вызывает `GET /api/delivery/options` в module_delivery и передает orderDraftId.
3. module_delivery проверяет ограничения по товарам и складу через WMS/1C контур и возвращает варианты доставки.
4. Для самовывоза frontend запрашивает `GET /api/delivery/pickup-points`, показывает адрес, график, срок хранения и лимиты ПВЗ.
5. После подтверждения заказа frontend вызывает order contour, order фиксирует заказ и payment capture.
6. order вызывает `POST /api/delivery/shipments` с `Idempotency-Key` и `X-Correlation-Id`.
7. module_delivery создает shipment, публикует задачу во внешний delivery provider и возвращает `ShipmentDto`.

## Tracking и уведомления
Внешний delivery provider передает статусы через `POST /api/delivery/integration/status-events`. module_delivery валидирует последовательность, сохраняет immutable tracking event, обновляет current status shipment и публикует события:
- в order contour для деталей заказа и истории;
- в admin platform для SLA/KPI;
- во frontend notification layer как mnemonic-коды `STR_MNEMO_DELIVERY_*`;
- в service contour при проблемных статусах.

Покупатель, партнер и сотрудник поддержки получают tracking timeline через `GET /api/delivery/shipments/{shipmentId}/tracking`. Доступ ограничивается владельцем заказа, active partner scope, employee/admin permission scopes и привязкой владельца ПВЗ.

## Кабинет владельца ПВЗ
Владелец ПВЗ открывает список отправлений через `GET /api/delivery/pickup-owner/shipments`. Приемка выполняется командой `/accept`, полная выдача - `/deliver`, частичная выдача - `/partial-deliver`. Все команды идемпотентны, используют correlationId и создают audit trail.

Полная выдача переводит shipment в `DELIVERED` и обновляет order timeline. Частичная выдача переводит shipment в `PARTIALLY_DELIVERED`, сохраняет состав выданных и возвращаемых позиций, создает return-to-logistics событие для WMS/1C, service context event, order event и KPI/SLA event. Невыкуп после истечения срока хранения работает тем же контуром problem/return events.

## Проблемные статусы
Если внешний provider передает `DELIVERY_PROBLEM`, module_delivery сохраняет problem tracking event и делает его доступным оператору доставки. Оператор фильтрует журнал по orderId, shipmentId, pickupPointId, sourceSystem и correlationId. Некорректные переходы статусов отклоняются кодом `STR_MNEMO_DELIVERY_INVALID_STATUS_TRANSITION`, но сохраняются в integration error журнале для расследования без раскрытия секретов.

## Контракты и i18n
Backend не возвращает hardcoded user-facing text. Все предопределенные сообщения представлены mnemonic-кодами `STR_MNEMO_DELIVERY_*`; frontend обязан локализовать их через `resources_ru.ts` и `resources_en.ts`. Справочные данные ПВЗ, адреса и графики считаются бизнес-данными и могут приходить из backend.

## Audit и безопасность
Каждая state-changing операция содержит `Idempotency-Key`, `X-Correlation-Id`, actor identity, actorRole, actionCode и reasonCode, если причина требуется. Audit события связывают shipment, order, pickupPoint, externalShipmentId и source route. Проверочный код получателя хранится только в виде хэша и не возвращается в API.
