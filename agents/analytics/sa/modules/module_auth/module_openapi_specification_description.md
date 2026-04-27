# Feature module OpenAPI specification description. Feature 024. Module auth

## Назначение API
Auth API формирует единый runtime contract для frontend shell и backend route checks. Он возвращает session context, route policies, invitation code state, active partner и impersonation state как структурированные данные. Все predefined user-facing сообщения передаются только через mnemonic-коды `STR_MNEMO_*`.

## Endpoints
- `POST /api/auth/test-login` - тестовый login для managed API/UI проверок и локального режима. Возвращает `AuthSessionResponse` с token, userId, displayName, roles, defaultRoute, policies и состояниями auth context.
- `GET /api/auth/session` - восстановление текущей сессии по Authorization token. При истекшей сессии возвращает HTTP 401 и `STR_MNEMO_AUTH_SESSION_EXPIRED`.
- `DELETE /api/auth/session` - logout. Backend отзывает session state, frontend очищает token, role, activePartner, invitationCode и impersonation context.
- `POST /api/auth/session/route-access` - проверка конкретного route по роли и policy. Возвращает allowed/defaultRoute или HTTP 403 с `STR_MNEMO_AUTH_ACCESS_DENIED`.
- `POST /api/auth/invitation-code` - сохранение и проверка invitation code. Возвращает `VALID`, `EXPIRED`, `USED`, `UNKNOWN` или `NOT_PROVIDED`.
- `GET /api/auth/partners/search` - поиск партнера в доступном scope. Query должен быть не короче 3 символов; иначе `STR_MNEMO_AUTH_PARTNER_SEARCH_INVALID`.
- `PUT /api/auth/partners/active` - установка active partner. Чужой partner scope возвращает `STR_MNEMO_AUTH_PARTNER_SCOPE_DENIED`.
- `POST /api/auth/impersonation` - запуск controlled impersonation. Требует targetUserId, targetRole, reasonCode, reasonText, durationMinutes и при необходимости `X-Elevated-Session-Id`.
- `POST /api/auth/impersonation/{impersonationSessionId}/finish` - завершение имперсонации и восстановление actor context.

## DTO и валидации
`AuthLoginRequest` принимает username, role и optional invitationCode. `AuthSessionResponse` является основным session contract для frontend и включает route policies, activePartner, invitationCodeState и impersonation. `AuthImpersonationStartRequest` валидирует обязательные поля и ограничивает durationMinutes диапазоном 1-120. `AuthErrorResponse` содержит только `code`, details и metadata без user-facing текста.

## STR_MNEMO коды
- `STR_MNEMO_AUTH_ACCESS_DENIED` - route или role policy запрещает доступ.
- `STR_MNEMO_AUTH_SESSION_EXPIRED` - token отсутствует, истек или отозван.
- `STR_MNEMO_AUTH_INVITATION_CODE_INVALID` - invitation code неизвестен или недействителен.
- `STR_MNEMO_AUTH_PARTNER_SEARCH_INVALID` - query поиска партнера невалиден.
- `STR_MNEMO_AUTH_PARTNER_SCOPE_DENIED` - active partner вне доступного scope.
- `STR_MNEMO_AUTH_IMPERSONATION_INVALID` - payload имперсонации невалиден.
- `STR_MNEMO_AUTH_IMPERSONATION_FORBIDDEN` - policy, MFA/elevated mode или target scope запрещают имперсонацию.

## Безопасность и аудит
Каждый endpoint получает actor context из token и фиксирует audit-событие там, где меняется security state или происходит отказ доступа. Partner search хранит hash query, а не исходную строку. Имперсонация всегда связывает actorUserId, targetUserId, impersonationSessionId, optional elevatedSessionId и correlationId.

## Swagger
Модуль `auth` должен иметь dedicated Swagger group с canonical URL `/v3/api-docs/auth` и UI `/swagger-ui/auth`. Контроллеры должны находиться внутри package prefix `com.bestorigin.monolith.auth.impl`, чтобы springdoc включал endpoint-ы автоматически без ручных списков.

## Версионная база
OpenAPI соответствует baseline задачи 27.04.2026: Java/Spring Boot/Maven monolith, Spring MVC controllers, springdoc-openapi runtime generation, TypeScript/React/Ant Design frontend и обязательный package ownership `api/domain/db/impl`.
