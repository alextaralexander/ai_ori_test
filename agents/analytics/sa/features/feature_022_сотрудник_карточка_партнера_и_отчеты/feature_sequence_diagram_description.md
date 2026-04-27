# Feature sequence diagram description. Feature 022

## Основной поток карточки партнера
Сотрудник открывает `/employee/partner-card`, frontend вызывает `GET /api/employee/partner-card` с query и supportReasonCode. `EmployeeController` передает userContext в `EmployeeService`. Сервис проверяет роль `employee-support`, `backoffice`, `supervisor` или `regional-manager`, затем проверяет regional scope для выбранного партнера. После проверки сервис собирает read projection из партнерских master data, order history, claims, bonus wallet, WMS/delivery indicators и сохраняет audit event `EMPLOYEE_PARTNER_CARD_VIEWED` через `EmployeeSupportRepository`.

Ответ `EmployeePartnerCardResponse` возвращает partner identity, маскированные контакты, KPI, recentOrders, riskSignals, auditContext и linkedRoutes. Frontend отображает карточку только через i18n keys и структурированные значения.

## Поток отчета истории заказов партнера
Из карточки сотрудник переходит на `/employee/report/order-history`. Frontend вызывает `GET /api/employee/report/order-history` с partnerId/personNumber, периодом, campaignCode, статусами и `problemOnly`. Сервис валидирует фильтры, проверяет scope, получает items и aggregates из read models и сохраняет snapshot отчета вместе с audit event `EMPLOYEE_PARTNER_REPORT_VIEWED`.

Ответ содержит постраничные items, aggregates, appliedFilters и `auditRecorded=true`. В items есть linkedRoutes в order details, claim, support и bonus/partner report flows.

## Regional manager branch
Для `regional-manager` сервис разрешает только партнеров зоны ответственности. Если partnerId/personNumber не входит в scope, backend возвращает `403 STR_MNEMO_EMPLOYEE_ACCESS_DENIED` без персональных данных и KPI. Если scope корректен, менеджер видит тот же отчет, но без повышенного раскрытия контактов.

## Переходы в смежные flows
При клике на order, claim, support или bonus route frontend сохраняет partnerId, orderNumber и supportReasonCode. Backend фиксирует audit `EMPLOYEE_PARTNER_CARD_LINK_OPENED`. Это связывает расследование партнера с дальнейшими order/claim/support действиями.

## Ошибки
- Короткий query: `400 STR_MNEMO_EMPLOYEE_PARTNER_QUERY_INVALID`.
- Невалидные фильтры отчета: `400 STR_MNEMO_EMPLOYEE_PARTNER_REPORT_FILTER_INVALID`.
- Нет роли или scope: `403 STR_MNEMO_EMPLOYEE_ACCESS_DENIED`.
- Партнер не найден: `404 STR_MNEMO_EMPLOYEE_PARTNER_NOT_FOUND`.

## Версионная база
Фича использует baseline задачи на 27.04.2026: Spring MVC controller в monolith employee package, service/repository внутри `impl/service` и `domain`, Liquibase XML changeset в `db`, TypeScript/React frontend с Ant Design и i18n dictionaries.