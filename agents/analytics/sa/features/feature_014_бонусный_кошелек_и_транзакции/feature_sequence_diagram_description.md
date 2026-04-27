# Sequence описание feature 014

## Пользовательский просмотр
Покупатель или партнер открывает `/profile/transactions/:type`. Frontend загружает summary, затем историю транзакций с фильтрами. Пользователь может открыть детали транзакции и перейти к связанному заказу через order module.

## Checkout integration
Checkout запрашивает `GET /api/bonus-wallet/limits/order/{orderNumber}` перед применением баланса. Модуль возвращает доступный лимит без включения hold и expired операций.

## Finance adjustment
Финансовый оператор отправляет корректировку через `/api/bonus-wallet/finance/adjustments`. Backend проверяет роль, reason и idempotency key, создает signed ledger transaction и возвращает audit flag. Повтор с тем же idempotency key возвращает исходную операцию.

## Failure flows
Чужой пользователь получает `STR_MNEMO_BONUS_WALLET_ACCESS_DENIED`. Отсутствующая транзакция возвращает `STR_MNEMO_BONUS_WALLET_TRANSACTION_NOT_FOUND`. Превышение лимита применения возвращает `STR_MNEMO_BONUS_WALLET_LIMIT_EXCEEDED`.

## Version baseline
Java 25, Spring Boot 4.0.6, React latest, TypeScript latest, Ant Design latest. API должен соответствовать runtime Swagger `/v3/api-docs/bonus-wallet` и UI `/swagger-ui/bonus-wallet`.
