# Feature sequence diagram description. Feature 024

## Назначение
Sequence описывает runtime-взаимодействие feature #24 между frontend AuthProvider, auth module backend, employee elevated access, auth DB и audit publisher. Диаграмма покрывает session restore, login, role routing, invitation code, partner search, active partner switch, start impersonation и finish impersonation.

## Восстановление сессии и route routing
При открытии публичного, партнерского, employee или admin route frontend вызывает `GET /api/auth/session`. `AuthController` передает token в `AuthService`, service загружает `auth_session`, роли, active partner и active impersonation из Auth DB. Если session валидна, frontend получает `AuthSessionResponse` и `AuthProvider` строит role router. Если token отсутствует, истек или отозван, backend возвращает `STR_MNEMO_AUTH_SESSION_EXPIRED`, а frontend показывает локализуемый expired/forbidden state.

## Login и invitation code
При login frontend отправляет username, role и optional invitationCode в `POST /api/auth/test-login`. Backend создает session, session roles, invitation code state и route policies. Invitation code валидируется в auth service и возвращается как `VALID`, `EXPIRED`, `USED`, `UNKNOWN` или `NOT_PROVIDED`; предупреждения передаются mnemonic-кодами, не user-facing текстом.

## Partner search и active partner
Partner leader или сотрудник вызывает `GET /api/auth/partners/search?query=...`. Service проверяет query length и role scope, возвращает только доступные `AuthPartnerOptionResponse`, а исходный query пишет в audit только как hash. При `PUT /api/auth/partners/active` backend проверяет partner scope, обновляет `auth_active_partner_state`, увеличивает `auth_session.version`, записывает `AUTH_ACTIVE_PARTNER_CHANGED` и возвращает обновленный session context. Frontend после этого добавляет `X-Active-Partner-Id` в API-клиенты partner scope.

## Controlled impersonation
Супервайзер, администратор или сотрудник с elevated mode отправляет `POST /api/auth/impersonation` с targetUserId, targetRole, reasonCode, reasonText, durationMinutes и optional `X-Elevated-Session-Id`. AuthService проверяет policy, target scope, max duration и при необходимости вызывает employee elevated access для подтверждения активной elevated session. Успешная операция создает `auth_impersonation_session`, обновляет session context, пишет `AUTH_IMPERSONATION_STARTED` и возвращает active impersonation. Frontend показывает `auth-impersonation-banner`.

## Завершение имперсонации
При `POST /api/auth/impersonation/{id}/finish` service закрывает impersonation session, восстанавливает actor context в `auth_session`, пишет `AUTH_IMPERSONATION_FINISHED` и возвращает обычный `AuthSessionResponse`. Закрытая или истекшая impersonation session не может использоваться для route access и API-запросов.

## Ошибки и локализация
Все predefined user-facing результаты передаются во frontend только как mnemonic-коды `STR_MNEMO_*`: `STR_MNEMO_AUTH_ACCESS_DENIED`, `STR_MNEMO_AUTH_SESSION_EXPIRED`, `STR_MNEMO_AUTH_INVITATION_CODE_INVALID`, `STR_MNEMO_AUTH_PARTNER_SEARCH_INVALID`, `STR_MNEMO_AUTH_PARTNER_SCOPE_DENIED`, `STR_MNEMO_AUTH_IMPERSONATION_INVALID`, `STR_MNEMO_AUTH_IMPERSONATION_FORBIDDEN`. Frontend обязан разрешать их через i18n dictionaries.

## Версионная база
Sequence соответствует baseline задачи 27.04.2026: Java/Spring Boot/Maven monolith, Spring MVC controllers, springdoc runtime Swagger, PostgreSQL/Liquibase XML, TypeScript/React/Ant Design frontend и обязательный package ownership `api/domain/db/impl`.
