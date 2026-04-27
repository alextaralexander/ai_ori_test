# Architecture description

Архитектура Best Ori Gin остается domain-oriented monolith с frontend web-shell и backend monolith-app. Feature #19 добавляет модуль `employee`, который обслуживает employee workspace, операторское создание заказа и поддержку проблемного заказа. Feature #20 расширяет тот же модуль операторской историей заказов: список `/employee/order-history`, детали `/employee/order-history/:orderId`, фильтры проблемных кейсов и audit просмотров. Feature #21 расширяет employee module контуром претензий: создание `/employee/submit-claim`, история `/employee/claims-history`, детали `/employee/claims-history/:claimId`, SLA, маршруты склада/финансов/support и supervisor approval.

## Backend modules and links
`employee` не нарушает владение существующих доменов: `order` остается владельцем заказов и публичных клиентских претензий, `cart` — корзины, `profile` — персональных данных, `partner-office` — поставок, payment/WMS/delivery/bonus системы остаются интеграционными источниками. Employee хранит support-контекст, ссылки, order-history read-model, employee claims read/write model, SLA, route tasks и audit trail действий сотрудников.

Связи:
- Frontend `EmployeeWorkspace UI` вызывает `/api/employee/workspace`, `/operator-orders`, `/order-support` по HTTP/JSON.
- Frontend `EmployeeOrderHistory UI` вызывает `/api/employee/order-history` и `/api/employee/order-history/{orderId}` по HTTP/JSON.
- Frontend `EmployeeClaims UI` вызывает `/api/employee/submit-claim`, `/api/employee/claims`, `/api/employee/claims/{claimId}` и `/api/employee/claims/{claimId}/transitions` по HTTP/JSON.
- `employee` читает order links и агрегированные статусы из `order`, payment event ids/statuses из payment, claim ids из claims, WMS batch/events из WMS/1C.
- `employee` создает employee-претензию по order context, но публичный клиентский claims contract остается за order module.
- `employee` создает route tasks для WMS/1C warehouse review, payment/finance refund и customer support nextAction через интеграционные протоколы.
- `employee` пишет support/order-history/employee-claims read-model и audit tables в Postgres через Liquibase XML managed schema.

## Backend package ownership
`com.bestorigin.monolith.employee.api` содержит DTO и REST-контракты. `domain` содержит snapshot-модель и repository-интерфейс. `db` содержит Liquibase marker package и XML changeset. `impl/controller`, `impl/service`, `impl/config`, `impl/exception` содержат runtime-логику. Новые runtime-классы не размещаются в root `impl`.

## Swagger and message contract
Swagger group формируется через module metadata: `/v3/api-docs/employee` и `/swagger-ui/employee`. Backend возвращает только mnemonic-коды `STR_MNEMO_EMPLOYEE_*` для предопределенных сообщений, включая employee claims codes `STR_MNEMO_EMPLOYEE_CLAIM_*`. Frontend локализует их через `resources_ru.ts` и `resources_en.ts`.

## Frontend
Frontend использует `EmployeeWorkspaceView`, `EmployeeOrderHistoryView`, новый employee claims UI и `api/employee.ts`. Маршруты: `/employee`, `/employee/new-order`, `/employee/order-support`, `/employee/order-history`, `/employee/order-history/:orderId`, `/employee/submit-claim`, `/employee/claims-history`, `/employee/claims-history/:claimId`. Все user-facing строки находятся в i18n dictionaries; компоненты не типизируют return как `JSX.Element`.

## Version baseline
Версионный baseline на 27.04.2026: Java 25, Spring Boot 4.0.6, Maven текущего monolith-app, TypeScript/React/Ant Design текущего web-shell, Liquibase XML, PostgreSQL target schema, Docker/Kubernetes stack без изменений в этом инкременте.
