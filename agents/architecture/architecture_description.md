# Architecture description

Архитектура Best Ori Gin остается domain-oriented monolith с frontend web-shell и backend monolith-app. Feature #19 добавляет модуль `employee`, который обслуживает employee workspace, операторское создание заказа и поддержку проблемного заказа. Feature #20 расширяет тот же модуль операторской историей заказов. Feature #21 добавляет employee-претензии. Feature #22 добавляет employee-карточку партнера и отчет истории заказов партнера. Feature #23 добавляет employee profile settings и контролируемый elevated mode для повышенных прав сотрудника. Feature #24 добавляет модуль `auth`, который владеет session context, role routing, invitation code, active partner и controlled impersonation. Feature #25 добавляет модуль `platform-experience`, который владеет runtime config, consent preferences, notification preferences, analytics diagnostics и i18n diagnostics.

## Backend modules and links
`auth` является владельцем runtime session state, role policies, invitation code state, active partner state, partner search audit и impersonation session. `platform-experience` является владельцем сквозного experience-state: consent preferences, notification preferences, runtime config snapshots, analytics adapter diagnostics и i18n missing key diagnostics. `employee` не нарушает владение существующих доменов: `order` остается владельцем заказов, `cart` - корзины, `profile` - персональных и партнерских данных, `authorization` - identity/roles/MFA/locks, `partner-office` - поставок, payment/WMS/delivery/bonus системы остаются интеграционными источниками. Employee хранит support-контекст, ссылки, order-history read-model, employee claims read/write model, partner-card audit, partner-report snapshot, employee profile settings, elevated requests/sessions и audit trail действий сотрудников.

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
- Frontend `I18nProvider` вызывает `/api/platform-experience/diagnostics/i18n-missing-keys` по HTTP/JSON для диагностики отсутствующих ключей.
- Frontend `NotificationProvider` вызывает `/api/platform-experience/notification/preferences` по HTTP/JSON для preferences notification/offline UI.
- Frontend `OfflineStatusProvider` вызывает auth session check после reconnect по HTTP/JSON, чтобы не продолжать операции с истекшей сессией.
- Frontend `AnalyticsProvider` вызывает `/api/platform-experience/runtime-config`, `/api/platform-experience/consent/preferences` и `/api/platform-experience/diagnostics/analytics-events` по HTTP/JSON.
- Frontend `ConsentPanel` вызывает `/api/platform-experience/consent/preferences` по HTTP/JSON.
- Frontend `AnalyticsDiagnostics UI` вызывает `/api/platform-experience/diagnostics/summary` по HTTP/JSON.
- `auth` вызывает `authorization` для проверки роли, route policy, MFA/elevated policy и блокировок.
- `auth` вызывает `employee` для проверки active elevatedSessionId при controlled impersonation.
- `auth` пишет sessions, roles, route policies, invitation code state, active partner, impersonation sessions и audit events в Postgres через Liquibase XML managed schema.
- `platform-experience` пишет consent preferences, notification preferences, runtime config snapshots, analytics diagnostics и i18n missing key diagnostics в Postgres через Liquibase XML managed schema.
- Frontend `AnalyticsProvider` отправляет consent-aware pageview/conversion events во внешние Yandex Metrika, Mindbox и Hybrid Pixel adapters; запрет consent блокирует отправку в соответствующий внешний канал.
- `employee` вызывает `authorization` для проверки роли, scope, MFA, блокировок и supervisor-доступа.
- `employee` читает partner profile/KPI из `profile` и `partner-reporting`, бонусный баланс и volume из `bonus-wallet`/bonus engine, order links и агрегированные статусы из `order`, payment event ids/statuses из payment, claim ids из claims, WMS batch/events из WMS/1C и delivery tracking из delivery.
- `employee` проверяет `fileReferenceId` employee-документов через S3/MinIO abstraction и не хранит приватные storage paths в API.
- `employee` передает `elevatedSessionId` и `correlationId` в связанные order, claim, partner, delivery и support flows как audit context для расширенных операций.
- `employee` пишет support/order-history/employee-claims/partner-card/profile/elevated read models и audit tables в Postgres через Liquibase XML managed schema.

## Backend package ownership
`com.bestorigin.monolith.auth.api` содержит DTO и REST-контракты auth module. `auth/domain` содержит JPA entities и repository interfaces. `auth/db` содержит Liquibase marker package и XML changesets, включая отдельный changeset feature #24. `auth/impl/controller`, `auth/impl/service`, `auth/impl/config`, `auth/impl/security`, `auth/impl/validator`, `auth/impl/event`, `auth/impl/exception` содержат runtime-логику. Новые runtime-классы не размещаются в root `impl`.

`com.bestorigin.monolith.employee.api` содержит DTO и REST-контракты. `domain` содержит JPA entities и repository interfaces. `db` содержит Liquibase marker package и XML changesets, включая отдельный changeset feature #23. `impl/controller`, `impl/service`, `impl/security`, `impl/validator`, `impl/mapper`, `impl/event`, `impl/exception` содержат runtime-логику. Новые runtime-классы не размещаются в root `impl`.

`com.bestorigin.monolith.platformexperience.api` содержит DTO и REST-контракты platform-experience module. `platformexperience/domain` содержит JPA entities и repository interfaces для consent, notification, analytics diagnostics, i18n diagnostics и runtime config snapshots. `platformexperience/db` содержит Liquibase marker package и отдельный XML changeset feature #25. `platformexperience/impl/controller`, `impl/service`, `impl/config`, `impl/validator`, `impl/mapper`, `impl/exception` содержат runtime-логику. Новые runtime-классы не размещаются в root `impl`.

## Swagger and message contract
Swagger groups формируются через module metadata: `/v3/api-docs/auth` и `/swagger-ui/auth` для auth, `/v3/api-docs/employee` и `/swagger-ui/employee` для employee, `/v3/api-docs/platform-experience` и `/swagger-ui/platform-experience` для platform-experience. Backend возвращает только mnemonic-коды `STR_MNEMO_AUTH_*`, `STR_MNEMO_EMPLOYEE_*`, `STR_MNEMO_PLATFORM_*` и `STR_MNEMO_ANALYTICS_*` для предопределенных сообщений. Frontend локализует их через поддерживаемые `resources_*.ts` dictionaries.

## Frontend
Frontend использует platform providers `I18nProvider`, `NotificationProvider`, `OfflineStatusProvider`, `AnalyticsProvider`, `ConsentPanel` и `AnalyticsDiagnostics UI`, а также auth components/hooks: `AuthProvider`, `PrivateRoute`, `ProfileRoute`, `EmployeeRoute`, `useInvitationCode`, `useActivePartner`, `usePartnerSearch`, `useSuperUserMode`, `useImpersonate`, `ActivePartnerSwitcher` и `ImpersonationPanel`. Employee views остаются: `EmployeeWorkspaceView`, `EmployeeOrderHistoryView`, employee claims UI, `EmployeePartnerCardView`, `EmployeePartnerReportView`, `EmployeeProfileSettingsView`, `EmployeeProfileSettingsGeneralView`, `EmployeeProfileSettingsContactsView`, `EmployeeProfileSettingsAddressesView`, `EmployeeProfileSettingsDocumentsView`, `EmployeeProfileSettingsSecurityView` и `EmployeeSuperUserView`. Маршруты auth feature включают `/test-login`, protected route restore, invitation routes, active partner switch и impersonation panel. Feature #25 добавляет platform shell behavior для notification root, offline popup, reconnect notification, language switcher, consent panel и analytics diagnostics. Все user-facing строки находятся в i18n dictionaries; компоненты не типизируют return как `JSX.Element`.

## Infrastructure
Feature #25 не требует новых runtime infrastructure components. Используются существующие Postgres/Liquibase XML, backend monolith container, frontend web-shell и observability/logging каналы. Внешние Yandex Metrika, Mindbox и Hybrid Pixel подключаются как frontend adapters и получают только consent-aware события. Если deployment manifests содержат Swagger route exposure для module groups, platform-experience endpoints попадают в `/v3/api-docs/platform-experience` и `/swagger-ui/platform-experience` без отдельного ingress.

## Version baseline
Версионный baseline на 27.04.2026: Java/Spring Boot/Maven monolith, TypeScript/React/Ant Design web-shell, Liquibase XML, PostgreSQL target schema, S3/MinIO document abstraction, Docker/Kubernetes stack без изменений в этом инкременте. Feature #25 не вводит новый технологический стек и не требует понижения версий.
