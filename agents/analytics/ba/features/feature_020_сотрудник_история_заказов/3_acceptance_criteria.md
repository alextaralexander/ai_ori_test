# Acceptance criteria. Feature 020. Сотрудник: история заказов

## Доступ и аудит
1. Маршруты `/employee/order-history` и `/employee/order-history/:orderId` доступны только ролям `employee-support`, `order-support`, `supervisor` и совместимым backoffice-сессиям.
2. Пользователь без employee-прав получает HTTP 403 и mnemonic `STR_MNEMO_EMPLOYEE_ACCESS_DENIED`; frontend показывает локализованное сообщение из i18n.
3. Каждый просмотр списка заказов, открытие деталей и переход в связанный контур сохраняет audit-событие с actorUserId, actorRole, actionType, orderId, supportReasonCode при наличии, occurredAt и sourceRoute.
4. Audit-события employee-контура не отображаются в публичном клиентском timeline как пользовательские сообщения.

## Список заказов сотрудника
1. API списка принимает фильтры `partnerId`, `customerId`, `dateFrom`, `dateTo`, `orderStatus`, `paymentStatus`, `deliveryStatus`, `problemOnly`, `query`, `page`, `size` и `sort`.
2. Пустой запрос без фильтров возвращает последнюю операторскую выборку за период по умолчанию, ограниченную постраничной выдачей.
3. Поиск по `query` поддерживает order number, customer id, partner id, телефон и email, но возвращает персональные данные только в маскированном виде.
4. Каждый элемент списка содержит orderId, orderNumber, campaignCode, customerName, partnerName, maskedContact, orderStatus, paymentStatus, deliveryStatus, fulfillmentStatus, totalAmount, problemFlags, createdAt, updatedAt и доступные переходы.
5. Фильтр `problemOnly=true` возвращает только заказы с задержкой оплаты, задержкой сборки, открытой претензией, ошибкой доставки, WMS-исключением или ручной корректировкой.
6. Некорректный период, размер страницы или неподдерживаемый статус возвращают mnemonic `STR_MNEMO_EMPLOYEE_ORDER_HISTORY_FILTER_INVALID`.

## Детали заказа сотрудника
1. API деталей возвращает расширенную карточку заказа по `orderId` или номеру заказа: состав, цены, промо, бонусы, totals, оплату, доставку, сборку, WMS-события, претензии, обращения поддержки, платежные события и audit trail.
2. Если заказ не найден или недоступен текущему сотруднику, backend возвращает HTTP 404 с mnemonic `STR_MNEMO_EMPLOYEE_ORDER_NOT_FOUND` без раскрытия внутренних идентификаторов.
3. Детали заказа включают сервисные поля `sourceChannel`, `supportCaseIds`, `claimIds`, `paymentEventIds`, `wmsBatchId`, `deliveryTrackingId`, `manualAdjustmentPresent` и `supervisorRequired`.
4. Доступные действия и deep links формируются backend как коды/идентификаторы маршрутов без hardcoded user-facing текстов.
5. Супервизор видит расширенные audit-события по сотрудникам; обычный сотрудник видит только разрешенный служебный минимум.

## Переходы и интеграции
1. Из списка и карточки доступны переходы в `/employee/order-support`, claims, payment events и карточку партнера с сохранением `orderId`, `customerId`, `partnerId` и reason-контекста.
2. Переход в support не создает дубль обращения, если по заказу уже есть открытый support case.
3. Платежные события и претензии отображаются как связанные объекты с id, статусом, временем и типом проблемы; пользовательские тексты frontend берет из i18n.
4. Endpoint-ы модуля employee входят в dedicated Swagger group `/v3/api-docs/employee` и Swagger UI `/swagger-ui/employee` через module metadata.

## UI и локализация
1. Frontend-страница `/employee/order-history` показывает таблицу заказов, панель фильтров, быстрые problem tabs, пагинацию, состояние загрузки, пустую выборку и ошибку доступа.
2. Frontend-страница `/employee/order-history/:orderId` показывает сводку заказа, блок клиента/партнера, состав, оплату, доставку, сборку, претензии, support, payment events и audit trail.
3. Все новые user-facing строки вынесены в `resources_ru.ts` и `resources_en.ts`; React-компоненты не содержат hardcoded пользовательских текстов.
4. Компоненты не типизируют возвращаемое значение как `JSX.Element`; используется inference или `ReactElement`.

## Тесты
1. Managed API-тесты покрывают логин сотрудника, список с фильтрами, список problemOnly, детали заказа, audit просмотра, отказ в доступе и невалидный фильтр.
2. Managed UI-тесты покрывают маршруты `/employee/order-history` и `/employee/order-history/:orderId`, применение фильтров, открытие деталей, наличие переходов и отказ для пользователя без employee-прав.
3. Runtime-копии тестов синхронизированы из `agents/tests/` по `agents/tests/targets.yml` и содержат marker comment о запрете ручного редактирования.