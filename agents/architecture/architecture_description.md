# Architecture description

Архитектура Best Ori Gin остается domain-oriented monolith с frontend web-shell и backend monolith-app. Feature #19 добавляет модуль `employee`, который обслуживает employee workspace, операторское создание заказа и поддержку проблемного заказа. Feature #20 расширяет тот же модуль операторской историей заказов. Feature #21 добавляет employee-претензии. Feature #22 добавляет employee-карточку партнера и отчет истории заказов партнера. Feature #23 добавляет employee profile settings и контролируемый elevated mode для повышенных прав сотрудника.

## Backend modules and links
`employee` не нарушает владение существующих доменов: `order` остается владельцем заказов, `cart` - корзины, `profile` - персональных и партнерских данных, `authorization` - identity/roles/MFA/locks, `partner-office` - поставок, payment/WMS/delivery/bonus системы остаются интеграционными источниками. Employee хранит support-контекст, ссылки, order-history read-model, employee claims read/write model, partner-card audit, partner-report snapshot, employee profile settings, elevated requests/sessions и audit trail действий сотрудников.

Связи:
- Frontend `EmployeeWorkspace UI` вызывает `/api/employee/workspace`, `/operator-orders`, `/order-support` по HTTP/JSON.
- Frontend `EmployeeOrderHistory UI` вызывает `/api/employee/order-history` и `/api/employee/order-history/{orderId}` по HTTP/JSON.
- Frontend `EmployeeClaims UI` вызывает `/api/employee/submit-claim`, `/api/employee/claims`, `/api/employee/claims/{claimId}` и `/api/employee/claims/{claimId}/transitions` по HTTP/JSON.
- Frontend `EmployeePartnerCard UI` вызывает `/api/employee/partner-card` и `/api/employee/partner-card/{partnerId}` по HTTP/JSON.
- Frontend `EmployeePartnerReport UI` вызывает `/api/employee/report/order-history` по HTTP/JSON.
- Frontend `EmployeeProfileSettings UI` вызывает `/api/employee/profile-settings`, `/profile-settings/general`, `/contacts`, `/addresses`, `/documents` и `/security` по HTTP/JSON.
- Frontend `EmployeeSuperUser UI` вызывает `/api/employee/super-user`, `/super-user/requests`, `/super-user/requests/{requestId}/approve|reject` и `/super-user/sessions/{sessionId}/close` по HTTP/JSON.
- `employee` вызывает `authorization` для проверки роли, scope, MFA, блокировок и supervisor-доступа.
- `employee` читает partner profile/KPI из `profile` и `partner-reporting`, бонусный баланс и volume из `bonus-wallet`/bonus engine, order links и агрегированные статусы из `order`, payment event ids/statuses из payment, claim ids из claims, WMS batch/events из WMS/1C и delivery tracking из delivery.
- `employee` проверяет `fileReferenceId` employee-документов через S3/MinIO abstraction и не хранит приватные storage paths в API.
- `employee` передает `elevatedSessionId` и `correlationId` в связанные order, claim, partner, delivery и support flows как audit context для расширенных операций.
- `employee` пишет support/order-history/employee-claims/partner-card/profile/elevated read models и audit tables в Postgres через Liquibase XML managed schema.

## Backend package ownership
`com.bestorigin.monolith.employee.api` содержит DTO и REST-контракты. `domain` содержит JPA entities и repository interfaces. `db` содержит Liquibase marker package и XML changesets, включая отдельный changeset feature #23. `impl/controller`, `impl/service`, `impl/security`, `impl/validator`, `impl/mapper`, `impl/event`, `impl/exception` содержат runtime-логику. Новые runtime-классы не размещаются в root `impl`.

## Swagger and message contract
Swagger group формируется через module metadata: `/v3/api-docs/employee` и `/swagger-ui/employee`. Backend возвращает только mnemonic-коды `STR_MNEMO_EMPLOYEE_*` для предопределенных сообщений, включая employee claims codes, partner card/report codes и feature #23 codes `STR_MNEMO_EMPLOYEE_PROFILE_*`, `STR_MNEMO_EMPLOYEE_CONTACT_INVALID`, `STR_MNEMO_EMPLOYEE_ELEVATED_*`. Frontend локализует их через поддерживаемые `resources_*.ts` dictionaries.

## Frontend
Frontend использует employee views: `EmployeeWorkspaceView`, `EmployeeOrderHistoryView`, employee claims UI, `EmployeePartnerCardView`, `EmployeePartnerReportView`, `EmployeeProfileSettingsView`, `EmployeeProfileSettingsGeneralView`, `EmployeeProfileSettingsContactsView`, `EmployeeProfileSettingsAddressesView`, `EmployeeProfileSettingsDocumentsView`, `EmployeeProfileSettingsSecurityView` и `EmployeeSuperUserView`. Маршруты: `/employee`, `/employee/new-order`, `/employee/order-support`, `/employee/order-history`, `/employee/order-history/:orderId`, `/employee/submit-claim`, `/employee/claims-history`, `/employee/claims-history/:claimId`, `/employee/partner-card`, `/employee/report/order-history`, `/employee/profile-settings`, `/employee/profile-settings/general`, `/employee/profile-settings/contacts`, `/employee/profile-settings/addresses`, `/employee/profile-settings/documents`, `/employee/profile-settings/security`, `/employee/super-user`. Все user-facing строки находятся в i18n dictionaries; компоненты не типизируют return как `JSX.Element`.

## Infrastructure
Feature #23 не требует новых runtime infrastructure components. Используются существующие Postgres/Liquibase XML, backend monolith container, frontend web-shell, S3/MinIO document storage abstraction и существующие observability/logging каналы. Если deployment manifests содержат Swagger route exposure для employee group, новые endpoints попадают в существующую группу без отдельного ingress.

## Version baseline
Версионный baseline на 27.04.2026: Java/Spring Boot/Maven monolith, TypeScript/React/Ant Design web-shell, Liquibase XML, PostgreSQL target schema, S3/MinIO document abstraction, Docker/Kubernetes stack без изменений в этом инкременте. Feature #23 не вводит новый технологический стек и не требует понижения версий.