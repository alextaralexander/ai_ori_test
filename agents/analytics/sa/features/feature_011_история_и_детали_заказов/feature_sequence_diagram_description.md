# Feature 011. Sequence diagram description

## Назначение
Sequence-диаграмма описывает основной runtime-поток feature #11: чтение списка истории, чтение деталей заказа, блокировку чужого заказа и запуск repeat order. Поток продолжает module `order` после checkout feature #10 и подготавливает переходы к будущей feature #12 для претензий.

## Основной поток
1. Frontend открывает `/order/order-history` и вызывает `GET /api/order/order-history` с фильтрами.
2. `OrderHistoryService` применяет ownership checks и фильтры, затем получает постраничный список из repository.
3. Пользователь открывает детали, frontend вызывает `GET /api/order/order-history/{orderNumber}`.
4. Сервис возвращает состав, payment/delivery snapshots, warnings, timeline events и разрешенные actions.
5. При попытке открыть чужой заказ сервис возвращает `STR_MNEMO_ORDER_HISTORY_ACCESS_DENIED` и пишет audit event.
6. При repeat order сервис проверяет текущую кампанию и доступность строк, затем переносит разрешенные позиции в основной или supplementary cart context.

## Пакетная ответственность
`OrderHistoryController` находится в `impl/controller`, `OrderHistoryService` и реализация в `impl/service`, mapper при необходимости в `impl/mapper`, repository interface и domain snapshots в `domain`, Liquibase XML changelog в `db`/resources changelog module `order`.

## Версионная база
Новые технологии не вводятся. Используется текущий monolith/web-shell baseline; backend возвращает только структурированные данные и mnemonic-коды, frontend локализует UI через существующие i18n dictionaries.
