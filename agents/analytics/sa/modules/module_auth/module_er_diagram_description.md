# Module ER diagram description. Module auth

## Назначение
Auth module является владельцем session context, role policies, invitation code state, active partner и controlled impersonation. После feature #24 полная ER модель модуля совпадает с feature model, потому что модуль создается впервые и не наследует более ранние auth-таблицы.

## Основные сущности
- `auth_session` - корневая сессия пользователя с token hash, primary role, default route, invitation status, active partner, active impersonation, сроками действия и optimistic version.
- `auth_session_role` - набор ролей и route scopes, доступных текущей сессии.
- `auth_route_policy` - policy-справочник для backend route checks и frontend role router; хранит `denied_mnemonic`, а не UI-текст.
- `auth_active_partner_state` - выбранный partner context для partner, partner leader и employee flows.
- `auth_partner_search_audit` - audit поиска партнера с hash query и result count без хранения исходной строки.
- `auth_impersonation_session` - controlled impersonation session с actorUserId, targetUserId, targetRole, optional elevatedSessionId, reasonCode, status и сроком действия.
- `auth_audit_event` - audit trail auth module для login, logout, route denial, active partner change, invitation code и impersonation events.

## Ключи и ограничения
Все таблицы используют `uuid` primary key. `auth_session.token_hash` уникален. `auth_session_role` уникален по `session_id + role_code + route_scope`. `auth_route_policy` уникален по `role_code + route_pattern + module_key`. Для `auth_impersonation_session` должна действовать бизнес-проверка: не более одной активной session на actor-а и `expires_at > started_at`.

## Индексы
Обязательные индексы: `auth_session(user_id)`, `auth_session(expires_at)`, `auth_session(active_partner_id)`, `auth_active_partner_state(session_id)`, `auth_active_partner_state(partner_id)`, `auth_impersonation_session(actor_user_id, status)`, `auth_impersonation_session(target_user_id)`, `auth_audit_event(actor_user_id, occurred_at)`, `auth_audit_event(correlation_id)`.

## Связи
`auth_session` связан one-to-many с roles, active partner history и impersonation sessions. `auth_impersonation_session` порождает audit events. Route policy используется для route decisions и denial audit. Partner search audit связан с actor/session context логически через actorUserId и correlationId.

## Security notes
Открытые tokens, MFA secrets, полные поисковые строки и свободный reasonText не хранятся. Для token, source IP, partner search query и reasonText используются hash-поля. Все predefined user-facing сообщения передаются только mnemonic-кодами `STR_MNEMO_*`.

## Версионная база
Описание соответствует baseline задачи 27.04.2026: PostgreSQL/Liquibase XML, Java/Spring Boot/Maven monolith, runtime Swagger через springdoc и backend package ownership `api/domain/db/impl`.
