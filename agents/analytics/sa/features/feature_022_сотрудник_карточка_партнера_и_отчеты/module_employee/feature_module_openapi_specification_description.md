# Feature module OpenAPI description. Feature 022. Module employee

## Назначение
Employee module предоставляет REST contract для карточки партнера и отчета истории заказов партнера. Контракт является частью monolith Swagger group employee и должен появляться в runtime OpenAPI автоматически из Spring MVC controller.

## Endpoint `GET /api/employee/partner-card`
Поиск и открытие карточки партнера по query.

Параметры:
- `query` обязательный, минимум 3 символа; принимает person number, partnerId, имя, телефон, email или региональный поисковый ключ.
- `supportReasonCode` необязательный, default `EMPLOYEE_PARTNER_CARD_VIEW`.
- `regionCode` необязательный фильтр регионального scope.

Успешный ответ `EmployeePartnerCardResponse` содержит идентификацию партнера, статус, уровень, регион, наставника, маскированные контакты, даты регистрации и последнего заказа, KPI, последние заказы, риск-сигналы, audit context и linkedRoutes.

Ошибки:
- `400 STR_MNEMO_EMPLOYEE_PARTNER_QUERY_INVALID` для короткого или пустого query.
- `403 STR_MNEMO_EMPLOYEE_ACCESS_DENIED` для роли вне employee/regional scope.
- `404 STR_MNEMO_EMPLOYEE_PARTNER_NOT_FOUND` для разрешенного пользователя, когда партнер не найден.

## Endpoint `GET /api/employee/partner-card/{partnerId}`
Открывает карточку конкретного партнера по `partnerId`. Используется при переходах из order history, claims, support и отчетов.

## Endpoint `GET /api/employee/report/order-history`
Возвращает постраничный отчет истории заказов партнера.

Фильтры:
- `partnerId` или `personNumber` - целевой партнер.
- `dateFrom`, `dateTo` - период отчета.
- `campaignCode` - каталог/кампания.
- `orderStatus`, `paymentStatus`, `deliveryStatus` - операционные статусы.
- `problemOnly` - ограничение выборки заказами с problemFlags.
- `regionCode` - региональная зона.
- `page`, `size`, `sort` - пагинация и сортировка, default `updatedAt,desc`.

Ответ содержит `items`, `aggregates`, `page`, `size`, `totalElements`, `auditRecorded`, `appliedFilters`. Каждый item содержит linkedRoutes в order details, claim, support и bonus/partner report flows.

Ошибки:
- `400 STR_MNEMO_EMPLOYEE_PARTNER_REPORT_FILTER_INVALID` для некорректного периода, page/size или отсутствия partner selector.
- `403 STR_MNEMO_EMPLOYEE_ACCESS_DENIED` для недоступного регионального scope.

## DTO и mnemonic-коды
Backend не возвращает predefined user-facing текст. Все ошибки и предупреждения представлены кодами:
- `STR_MNEMO_EMPLOYEE_ACCESS_DENIED`
- `STR_MNEMO_EMPLOYEE_PARTNER_QUERY_INVALID`
- `STR_MNEMO_EMPLOYEE_PARTNER_NOT_FOUND`
- `STR_MNEMO_EMPLOYEE_PARTNER_REPORT_FILTER_INVALID`
- `STR_MNEMO_EMPLOYEE_PARTNER_CARD_OPENED`
- `STR_MNEMO_EMPLOYEE_PARTNER_REPORT_READY`

Frontend обязан добавить эти коды в `resources_ru.ts` и `resources_en.ts`.

## Валидации и доступ
Все endpoint-ы требуют роль `employee-support`, `backoffice`, `supervisor` или `regional-manager`. Для `regional-manager` дополнительно проверяется `regionCode`/partner scope. Сервис сохраняет audit event для успешных чтений и переходов, но не создает успешный audit для validation errors.

## Версионная база
Фича использует baseline задачи на 27.04.2026: Java/Spring Boot/Maven monolith, Spring MVC + springdoc-openapi runtime generation, TypeScript/React/Ant Design frontend, i18n dictionaries и backend-to-frontend mnemonic contract `STR_MNEMO_*`.