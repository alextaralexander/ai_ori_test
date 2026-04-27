# Acceptance criteria. Feature 019. Сотрудник: новый заказ и поддержка

## Доступ и поиск
1. `/employee`, `/employee/new-order` и `/employee/order-support` доступны только ролям employee-support, order-support, supervisor и совместимым backoffice-сессиям.
2. Пользователь без employee-прав получает HTTP 403 и mnemonic `STR_MNEMO_EMPLOYEE_ACCESS_DENIED`; frontend показывает локализованное сообщение из i18n.
3. Поиск клиента или партнера принимает query по телефону, email, person number, customer id или order number и возвращает только разрешенные поля: идентификатор, отображаемое имя, сегмент, контакты в маскированном виде, активную корзину, последние заказы и предупреждения.
4. Пустой или слишком короткий query не выполняет поиск и возвращает `STR_MNEMO_EMPLOYEE_QUERY_INVALID`.

## Создание заказа сотрудником
1. Сотрудник может создать операторский заказ только при заполненных targetCustomerId, supportReasonCode и cartType.
2. Создание заказа сохраняет actorUserId, supportReasonCode, sourceChannel, idempotencyKey и признак auditRecorded.
3. Добавление позиций проверяет актуальную кампанию, наличие, лимиты, правила промо и доступность для выбранного сегмента клиента или партнера.
4. Подтверждение заказа возвращает orderNumber, checkoutId, paymentStatus, deliveryStatus, totals, nextAction и список auditEvents.
5. Повторный запрос с тем же idempotencyKey не создает второй заказ, а возвращает тот же результат.

## Поддержка проблемного заказа
1. Сотрудник может найти заказ по номеру, открыть детали и увидеть статус оплаты, доставки, сборки, историю событий, связанные претензии, доступные действия и переходы в профильные контуры.
2. Внутренняя заметка сохраняется как employee-only событие и не попадает в публичные сообщения клиента.
3. Сервисная корректировка требует reasonCode и amount, возвращает `STR_MNEMO_EMPLOYEE_SUPPORT_ADJUSTMENT_RECORDED` и создает audit-событие.
4. Эскалация требует reasonCode, ownerRole и dueAt, возвращает `STR_MNEMO_EMPLOYEE_SUPPORT_ESCALATION_CREATED` и видна супервизору.
5. Действия, влияющие на деньги, бонусы или доставку, помечаются как supervisorRequired, если роль текущего сотрудника не имеет повышенных прав.

## Интеграции и переходы
1. Из рабочего места сотрудника доступны deep links в историю заказов, претензии и карточку партнера с сохранением supportCustomerId и reason.
2. Контракт API не раскрывает персональные данные сверх маскированных полей в employee UI.
3. Все endpoint-ы модуля employee входят в dedicated Swagger group `/v3/api-docs/employee` и Swagger UI `/swagger-ui/employee` через module metadata.

## Тесты
1. Managed API-тесты покрывают поиск, создание заказа, поддержку заказа, корректировку, эскалацию и отказ в доступе.
2. Managed UI-тесты покрывают маршруты `/employee`, `/employee/new-order`, `/employee/order-support`, поиск и основные действия поддержки.
3. Runtime-копии тестов синхронизированы из `agents/tests/` и содержат marker comment о запрете ручного редактирования.