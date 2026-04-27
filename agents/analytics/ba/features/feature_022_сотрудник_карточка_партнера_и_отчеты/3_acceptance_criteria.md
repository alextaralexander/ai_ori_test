# Acceptance criteria. Feature 022. Сотрудник: карточка партнера и отчеты

## Обязательные критерии доступа и безопасности
1. Маршруты `/employee/partner-card` и `/employee/report/order-history` доступны только ролям `employee-support`, `backoffice`, `supervisor` и `regional-manager`; неавторизованный или гостевой пользователь получает отказ с mnemonic-кодом `STR_MNEMO_EMPLOYEE_ACCESS_DENIED`.
2. Backend проверяет региональный и операционный scope сотрудника для каждого поиска партнера, открытия карточки, отчета и перехода в связанные flows.
3. Если партнер не входит в зону ответственности сотрудника, API возвращает отказ без раскрытия персональных данных, KPI, заказов и существования чувствительных записей.
4. Каждый успешный просмотр карточки партнера, отчета истории заказов и переходный контекст фиксируется в audit trail с actorUserId, actorRole, supportReasonCode, sourceRoute, targetEntityType, targetEntityId, correlationId и occurredAt.
5. Контактные данные партнера и клиента отображаются в employee UI только в маскированном виде, если у роли нет повышенного права просмотра.

## Критерии карточки партнера
1. Поиск партнера принимает person number, partnerId, имя, телефон, email или региональный фильтр; строка поиска короче 3 символов отклоняется с `STR_MNEMO_EMPLOYEE_PARTNER_QUERY_INVALID`.
2. Карточка партнера возвращает partnerId, personNumber, displayName, status, activityState, levelName, regionCode, mentorPersonNumber, maskedPhone, maskedEmail, registrationDate, lastOrderDate и sourceChannel.
3. Карточка содержит KPI: personalVolume, groupVolume, orderCount, averageOrderAmount, bonusBalance, activeCustomerCount, openClaimCount, overdueActionCount, returnRatePercent и currentCampaignCode.
4. Карточка содержит список последних заказов партнера с orderNumber, campaignCode, createdAt, orderStatus, paymentStatus, deliveryStatus, totalAmount, problemFlags и linkedRoutes.
5. Карточка содержит блок riskSignals: падение активности, неоплаченные заказы, открытые претензии, блокировка бонусов, просрочка доставки или WMS hold.
6. Карточка содержит linkedRoutes в order, claim, support, bonus wallet и partner report flows с сохранением partnerId, orderNumber и supportReasonCode.
7. Если партнер не найден, API возвращает `STR_MNEMO_EMPLOYEE_PARTNER_NOT_FOUND`, а UI показывает локализованное состояние без hardcoded backend-текста.

## Критерии отчета по истории заказов партнера
1. `/employee/report/order-history` загружает историю заказов партнера по partnerId или personNumber и поддерживает фильтры: dateFrom, dateTo, campaignCode, orderStatus, paymentStatus, deliveryStatus, problemOnly, regionCode, page, size и sort.
2. Некорректный период, отрицательная страница, size меньше 1 или больше 100 отклоняются с `STR_MNEMO_EMPLOYEE_PARTNER_REPORT_FILTER_INVALID`.
3. Ответ отчета содержит items, page, size, totalElements, auditRecorded, appliedFilters и aggregates.
4. Каждый item отчета содержит orderId, orderNumber, campaignCode, customerDisplayName, orderStatus, paymentStatus, deliveryStatus, fulfillmentStatus, totalAmount, bonusVolume, problemFlags, linkedRoutes и updatedAt.
5. Aggregates отчета содержат totalOrders, totalAmount, paidAmount, returnedAmount, averageOrderAmount, personalVolume, groupVolume, openClaimCount и delayedDeliveryCount.
6. Сортировка по умолчанию выполняется по `updatedAt,desc`; backend возвращает стабильный порядок при одинаковых датах.
7. Переходы из отчета в order, claim и support flows сохраняют контекст партнера и основание просмотра.

## Критерии frontend и i18n
1. Все новые пользовательские строки в React-компонентах, фильтрах, заголовках, пустых состояниях, ошибках, статусах, CTA и table labels вынесены в текущие frontend i18n dictionaries для всех поддерживаемых языков.
2. React-компоненты не типизируют возвращаемое значение как `JSX.Element`; используется inference или `ReactElement` из `react`.
3. UI содержит test ids для green-path сценариев: `employee-partner-card-page`, `employee-partner-card-search`, `employee-partner-card-summary`, `employee-partner-card-kpi`, `employee-partner-order-row`, `employee-partner-report-page`, `employee-partner-report-table`, `employee-partner-report-aggregate`.
4. UI корректно отображает loading, empty, forbidden, validation error и success states без перекрытия текста и без hardcoded user-facing строк.
5. Пользователь может открыть карточку партнера, перейти к отчету, применить фильтр проблемных заказов и перейти в связанный order/claim/support route.

## Критерии backend contract
1. Backend не передает predefined user-facing текст во frontend; для ошибок, предупреждений и публичных сообщений используются только mnemonic-коды `STR_MNEMO_*`.
2. DTO находятся в `api`, runtime-контроллеры и сервисы находятся в `impl/controller` и `impl/service`, JPA/domain abstractions остаются в `domain`, Liquibase XML changeset создается в owning module `db` package.
3. Swagger/OpenAPI endpoint-ы employee module должны появляться в runtime группе monolith module автоматически через Spring MVC controller без ручной регистрации endpoint lists.
4. Новые Liquibase changesets создаются отдельным XML-файлом feature #22, не добавляются в общий changelog других фич.
5. API возвращает предсказуемые HTTP-коды: 200 для успешных чтений, 400 для validation errors, 403 для доступа вне роли или scope, 404 для разрешенного пользователя при отсутствии партнера/заказа.

## Критерии тестирования
1. Managed API test в `agents/tests/api/feature_022_сотрудник_карточка_партнера_и_отчеты/FeatureApiTest.java` начинается с логина пользователя с ролью сотрудника или регионального менеджера и проверяет карточку партнера, отчет, фильтры, auditRecorded, linkedRoutes, forbidden и validation сценарии.
2. Managed UI test в `agents/tests/ui/feature_022_сотрудник_карточка_партнера_и_отчеты/feature_ui_test.spec.ts` начинается с логина, открывает `/employee/partner-card`, проверяет карточку, переходит в `/employee/report/order-history`, применяет фильтр и проверяет таблицу/агрегаты.
3. End-to-end managed tests агрегируют реальный feature #22 test и ранее реализованные employee/order/support tests, а не используют placeholder assertions по имени фичи.
4. Runtime-копии тестов синхронизированы из `agents/tests/` по `agents/tests/targets.yml`, содержат marker comment и совпадают с canonical source после удаления marker-обертки.
5. Перед завершением workflow backend и frontend запускаются, API и UI проверки feature #22 выполняются или фиксируется технический blocker без создания пустых файлов.