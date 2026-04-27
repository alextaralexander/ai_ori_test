# Feature sequence diagram description. Employee

Последовательность feature #19 начинается с поиска в employee workspace. Controller извлекает actor из Authorization header, service проверяет employee-права, агрегирует профиль, активную корзину, последние заказы и предупреждения, затем сохраняет support session и audit event.

При создании операторского заказа service проверяет обязательную причину обращения, idempotency key, кампанию, товары, доставку и оплату. Результат сохраняется как employee operator order и возвращается во frontend с номером заказа, статусами, totals и nextAction.

Поддержка проблемного заказа агрегирует order timeline, оплату, доставку, сборку, претензии и доступные действия. Внутренние заметки, корректировки и эскалации сохраняются как support action и audit event. Супервизор получает список эскалаций через отдельный endpoint с проверкой повышенной роли.