# Acceptance criteria. Feature 021. Сотрудник: претензии

## AC-021-01. Доступ и маршруты
- Маршруты `/employee/submit-claim`, `/employee/claims-history` и `/employee/claims-history/:claimId` доступны только сотруднику сервиса, супервизору сервиса и совместимым backoffice-ролям.
- Пользователь без employee-прав получает HTTP 403 и mnemonic `STR_MNEMO_EMPLOYEE_ACCESS_DENIED`; frontend показывает локализованное сообщение из i18n dictionaries.
- Сотрудник видит только претензии в разрешенном tenant scope и обязан передавать основание просмотра или обработки.
- Супервизор видит расширенные поля контроля, лимитов, SLA и audit trail.

## AC-021-02. Создание претензии сотрудником
- API создания принимает customerId или partnerId, orderId или orderNumber, reasonCode, sourceChannel, supportReasonCode, idempotencyKey, выбранные позиции заказа, вложения и комментарии.
- Для каждой позиции фиксируются sku, productCode, quantity, problemType, requestedResolution и служебный комментарий при наличии.
- Сотрудник не может создать претензию без основания обращения, выбранного заказа, хотя бы одной позиции и причины проблемы.
- Повторная отправка с тем же idempotencyKey возвращает ранее созданный кейс и не создает дубль.
- Созданная претензия получает claimId, claimNumber, status, slaDueAt, responsibleRole, compensationPreview и availableActions.

## AC-021-03. Валидации и ограничения
- Нельзя запросить quantity больше количества позиции в заказе или создать претензию по позиции с `claimAvailable=false`.
- Нельзя обещать компенсацию, замену или дозаказ, если заказ, платеж, складской статус или лимит роли не допускают выбранное решение.
- Компенсации выше лимита сотрудника переводят кейс в статус supervisor approval и не исполняются до решения супервизора.
- Невалидные поля возвращают HTTP 400 с mnemonic `STR_MNEMO_EMPLOYEE_CLAIM_VALIDATION_FAILED` и machine-readable details без hardcoded user-facing текста.
- Если заказ или претензия недоступны текущему сотруднику, backend возвращает HTTP 404 с mnemonic `STR_MNEMO_EMPLOYEE_CLAIM_NOT_FOUND`, не раскрывая чужие идентификаторы.

## AC-021-04. История претензий сотрудника
- API истории принимает фильтры `claimStatus`, `dateFrom`, `dateTo`, `slaState`, `responsibleRole`, `assigneeId`, `resolutionType`, `sourceChannel`, `warehouseCode`, `financeStatus`, `query`, `page`, `size` и `sort`.
- Пустой запрос возвращает актуальную операционную очередь за период по умолчанию с постраничной выдачей.
- Поиск `query` поддерживает claimNumber, orderNumber, customerId, partnerId, телефон и email, но персональные данные возвращаются только в маскированном виде.
- Каждый элемент списка содержит claimId, claimNumber, orderNumber, customerOrPartnerLabel, maskedContact, status, slaState, slaDueAt, resolutionType, compensationAmount, currencyCode, assignee, responsibleRole, updatedAt и availableActions.
- Просроченные и близкие к SLA претензии имеют machine-readable `slaState`, чтобы frontend мог отобразить приоритет без вычисления правил на клиенте.

## AC-021-05. Детальная карточка претензии
- API деталей возвращает claimId, claimNumber, order summary, customer или partner summary, выбранные позиции, вложения, комментарии, compensationPreview, approvedCompensation, route approvals, warehouseTask, financeTask, supportTask, status timeline, audit trail и availableActions.
- Вложения возвращаются как metadata с fileId, filename, mimeType, size, uploadedBy, uploadedAt и access policy; приватные storage paths не передаются во frontend.
- Публичные комментарии и внутренние заметки разделены; клиентский контур не получает internal notes сотрудника.
- Решение по претензии хранится структурированно: resolutionType, approvedItems, refundAmount, replacementItems, rejectedItems, publicReasonMnemonic и nextAction.
- Route approvals показывают текущий этап, исполнителя, дедлайн, результат и комментарий исполнителя без hardcoded пользовательских сообщений.

## AC-021-06. Обработка решения и маршрутизация
- Сотрудник может перевести кейс в складскую проверку, финансовую обработку, customer support, supervisor approval или завершение только через допустимые transition-коды.
- Складская задача получает orderId, claimId, позиции, количество, проблему, вложения и ожидаемое действие.
- Финансовая задача получает claimId, payment context, approved compensation, currencyCode, основание решения и статус исполнения.
- Customer support получает публичный итог решения как mnemonic `STR_MNEMO_*`, nextAction и ссылки на клиентский или партнерский контур.
- Backend рассчитывает статусы исполнения по складскому, финансовому и support результату; frontend не должен вручную выводить итоговый статус из разрозненных полей.

## AC-021-07. Audit trail и SLA
- Каждый просмотр списка, открытие карточки, создание, изменение статуса, вложение, расчет компенсации, переход маршрута и решение супервизора фиксируются в audit trail.
- Audit event содержит actorUserId, actorRole, actionType, claimId, orderId, supportReasonCode, changedFields, sourceRoute, occurredAt и correlationId.
- SLA рассчитывается по типу претензии, источнику обращения, приоритету, текущему статусу и рабочему календарю, а результат отдается как dueAt, breachedAt при наличии и slaState.
- Audit trail employee-контура не попадает в публичную клиентскую историю как пользовательский комментарий.

## AC-021-08. UI и локализация
- Frontend-страница `/employee/submit-claim` содержит поиск клиента, партнера или заказа, выбор позиций, причину, ожидаемое решение, вложения, preview компенсации и отправку.
- Frontend-страница `/employee/claims-history` содержит таблицу претензий, фильтры, SLA tabs, пагинацию, loading, empty, forbidden и error states.
- Frontend-страница `/employee/claims-history/:claimId` содержит сводку, позиции, вложения, компенсацию, маршрут согласования, задачи склада/финансов/support, timeline, audit и доступные действия.
- Все новые user-facing строки добавлены во все поддерживаемые frontend i18n dictionaries; React-компоненты не содержат hardcoded пользовательских текстов.
- React-компоненты не типизируют возвращаемое значение как `JSX.Element`; используется inference или `ReactElement`.

## AC-021-09. Backend package, Liquibase и Swagger
- DTO и внешние контракты размещаются в owning module `api`, JPA entities и repositories в `domain`, Liquibase XML changelog в `db`, runtime-код в role-specific подпакетах `impl/controller`, `impl/service`, `impl/mapper`, `impl/validator`, `impl/security` или аналогичных.
- Dedicated Liquibase XML для feature #21 создан отдельно и не смешан с changelog других фич.
- Endpoint-ы employee claims входят в dedicated Swagger group модуля по каноническим URL `/v3/api-docs/<module-key>` и `/swagger-ui/<module-key>` без ручных endpoint registry.
- Backend не отправляет hardcoded user-facing тексты во frontend; предопределенные сообщения передаются только mnemonic-кодами `STR_MNEMO_*`.

## AC-021-10. Тесты и синхронизация
- Managed API test покрывает login сотрудника, создание претензии, idempotency, историю с фильтрами, SLA выборку, детали, transition в склад/финансы/support, supervisor approval, forbidden и validation error.
- Managed UI test покрывает `/employee/submit-claim`, `/employee/claims-history`, `/employee/claims-history/:claimId`, фильтры, открытие деталей, действие решения, empty и forbidden states.
- Managed end-to-end tests агрегируют реальные feature tests для зеленого пути и не содержат placeholder assertions.
- Runtime-копии тестов синхронизируются только из `agents/tests/` по `agents/tests/targets.yml` и содержат marker comment о запрете ручного редактирования.
