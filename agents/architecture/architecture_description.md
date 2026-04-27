# Architecture description

Архитектура Best Ori Gin остается domain-oriented monolith с frontend web-shell и backend monolith-app. Feature #19 добавляет модуль `employee`, который обслуживает employee workspace, операторское создание заказа и поддержку проблемного заказа. Feature #20 расширяет тот же модуль операторской историей заказов. Feature #21 добавляет employee-претензии. Feature #22 добавляет employee-карточку партнера и отчет истории заказов партнера. Feature #23 добавляет employee profile settings и контролируемый elevated mode для повышенных прав сотрудника. Feature #24 добавляет модуль `auth`, который владеет session context, role routing, invitation code, active partner и controlled impersonation.

## Backend modules and links
`auth` является владельцем runtime session state, role policies, invitation code state, active partner state, partner search audit и impersonation session. `employee` не нарушает владение существующих доменов: `order` остается владельцем заказов, `cart` - корзины, `profile` - персональных и партнерских данных, `authorization` - identity/roles/MFA/locks, `partner-office` - поставок, payment/WMS/delivery/bonus системы остаются интеграционными источниками. Employee хранит support-контекст, ссылки, order-history read-model, employee claims read/write model, partner-card audit, partner-report snapshot, employee profile settings, elevated requests/sessions и audit trail действий сотрудников.

Связи:
- Frontend `EmployeeWorkspace UI` вызывает `/api/employee/workspace`, `/operator-orders`, `/order-support` по HTTP/JSON.
- Frontend `EmployeeOrderHistory UI` вызывает `/api/employee/order-history` и `/api/employee/order-history/{orderId}` по HTTP/JSON.
- Frontend `EmployeeClaims UI` вызывает `/api/employee/submit-claim`, `/api/employee/claims`, `/api/employee/claims/{claimId}` и `/api/employee/claims/{claimId}/transitions` по HTTP/JSON.
- Frontend `EmployeePartnerCard UI` вызывает `/api/employee/partner-card` и `/api/employee/partner-card/{partnerId}` по HTTP/JSON.
- Frontend `EmployeePartnerReport UI` вызывает `/api/employee/report/order-history` по HTTP/JSON.
- Frontend `EmployeeProfileSettings UI` вызывает `/api/employee/profile-settings`, `/profile-settings/general`, `/contacts`, `/addresses`, `/documents` и `/security` по HTTP/JSON.
- Frontend `EmployeeSuperUser UI` вызывает `/api/employee/super-user`, `/super-user/requests`, `/super-user/requests/{requestId}/approve|reject` и `/super-user/sessions/{sessionId}/close` по HTTP/JSON.
- Frontend `AuthProvider` вызывает `/api/auth/session`, `/api/auth/test-login`, `/api/auth/invitation-code` и `/api/auth/session` logout по HTTP/JSON.
- Frontend `RoleRouter` вызывает `/api/auth/session/route-access` по HTTP/JSON для проверки закрытых маршрутов.
- Frontend `ActivePartnerSwitcher` вызывает `/api/auth/partners/search` и `/api/auth/partners/active` по HTTP/JSON.
- Frontend `ImpersonationPanel` вызывает `/api/auth/impersonation` и `/api/auth/impersonation/{impersonationSessionId}/finish` по HTTP/JSON.
- `auth` вызывает `authorization` для проверки роли, route policy, MFA/elevated policy и блокировок.
- `auth` вызывает `employee` для проверки active elevatedSessionId при controlled impersonation.
- `auth` пишет sessions, roles, route policies, invitation code state, active partner, impersonation sessions и audit events в Postgres через Liquibase XML managed schema.
- `employee` вызывает `authorization` для проверки роли, scope, MFA, блокировок и supervisor-доступа.
- `employee` читает partner profile/KPI из `profile` и `partner-reporting`, бонусный баланс и volume из `bonus-wallet`/bonus engine, order links и агрегированные статусы из `order`, payment event ids/statuses из payment, claim ids из claims, WMS batch/events из WMS/1C и delivery tracking из delivery.
- `employee` проверяет `fileReferenceId` employee-документов через S3/MinIO abstraction и не хранит приватные storage paths в API.
- `employee` передает `elevatedSessionId` и `correlationId` в связанные order, claim, partner, delivery и support flows как audit context для расширенных операций.
- `employee` пишет support/order-history/employee-claims/partner-card/profile/elevated read models и audit tables в Postgres через Liquibase XML managed schema.

## Backend package ownership
`com.bestorigin.monolith.auth.api` содержит DTO и REST-контракты auth module. `auth/domain` содержит JPA entities и repository interfaces. `auth/db` содержит Liquibase marker package и XML changesets, включая отдельный changeset feature #24. `auth/impl/controller`, `auth/impl/service`, `auth/impl/config`, `auth/impl/security`, `auth/impl/validator`, `auth/impl/event`, `auth/impl/exception` содержат runtime-логику. Новые runtime-классы не размещаются в root `impl`.

`com.bestorigin.monolith.employee.api` содержит DTO и REST-контракты. `domain` содержит JPA entities и repository interfaces. `db` содержит Liquibase marker package и XML changesets, включая отдельный changeset feature #23. `impl/controller`, `impl/service`, `impl/security`, `impl/validator`, `impl/mapper`, `impl/event`, `impl/exception` содержат runtime-логику. Новые runtime-классы не размещаются в root `impl`.

## Swagger and message contract
Swagger groups формируются через module metadata: `/v3/api-docs/auth` и `/swagger-ui/auth` для auth, `/v3/api-docs/employee` и `/swagger-ui/employee` для employee. Backend возвращает только mnemonic-коды `STR_MNEMO_AUTH_*` и `STR_MNEMO_EMPLOYEE_*` для предопределенных сообщений. Frontend локализует их через поддерживаемые `resources_*.ts` dictionaries.

## Frontend
Frontend использует auth components/hooks: `AuthProvider`, `PrivateRoute`, `ProfileRoute`, `EmployeeRoute`, `useInvitationCode`, `useActivePartner`, `usePartnerSearch`, `useSuperUserMode`, `useImpersonate`, `ActivePartnerSwitcher` и `ImpersonationPanel`. Employee views остаются: `EmployeeWorkspaceView`, `EmployeeOrderHistoryView`, employee claims UI, `EmployeePartnerCardView`, `EmployeePartnerReportView`, `EmployeeProfileSettingsView`, `EmployeeProfileSettingsGeneralView`, `EmployeeProfileSettingsContactsView`, `EmployeeProfileSettingsAddressesView`, `EmployeeProfileSettingsDocumentsView`, `EmployeeProfileSettingsSecurityView` и `EmployeeSuperUserView`. Маршруты auth feature включают `/test-login`, protected route restore, invitation routes, active partner switch и impersonation panel. Все user-facing строки находятся в i18n dictionaries; компоненты не типизируют return как `JSX.Element`.

## Infrastructure
Feature #24 не требует новых runtime infrastructure components. Используются существующие Postgres/Liquibase XML, backend monolith container, frontend web-shell и observability/logging каналы. Если deployment manifests содержат Swagger route exposure для module groups, auth endpoints попадают в `/v3/api-docs/auth` и `/swagger-ui/auth` без отдельного ingress.

## Version baseline
Версионный baseline на 27.04.2026: Java/Spring Boot/Maven monolith, TypeScript/React/Ant Design web-shell, Liquibase XML, PostgreSQL target schema, S3/MinIO document abstraction, Docker/Kubernetes stack без изменений в этом инкременте. Feature #24 не вводит новый технологический стек и не требует понижения версий.
