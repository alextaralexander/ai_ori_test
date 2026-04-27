# Module bonus-wallet ER description

Модуль `bonus-wallet` хранит кошельки пользователей, агрегированные bucket balances, ledger transactions и ручные корректировки. Таблицы и ограничения соответствуют feature-level ER описанию для feature #14.

Ключевые индексы: владелец кошелька, bucket, дата операции, order number, campaign id и idempotency key корректировки. Все денежные поля задаются как `numeric(19,2)`, даты сгорания как `date`, технические даты как `timestamp`.
