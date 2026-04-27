# Feature module ER diagram description. Feature 024. Module auth

## Назначение
Auth module хранит runtime-состояние аутентификации Best Ori Gin: сессии пользователей, роли, route policies, invitation code state, active partner, audit partner search и контролируемую имперсонацию. Persisted модель нужна для восстановления session context после reload, контроля роли на backend, запрета чужого partner scope и воспроизводимого audit trail сервисных действий.

## Таблица auth_session
- `session_id uuid` - первичный ключ session state.
- `user_id varchar(64)` - идентификатор actor-а или гостевого пользователя.
- `display_name varchar(255)` - отображаемое имя для session context; не является источником локализованного текста.
- `primary_role varchar(64)` - основная роль для defaultRoute.
- `token_hash varchar(128)` - хэш access/session token; открытый token не хранится.
- `default_route varchar(255)` - machine-readable стартовый маршрут.
- `invitation_code varchar(64)` - сохраненный invitation code, если передан.
- `invitation_status varchar(32)` - `VALID`, `EXPIRED`, `USED`, `UNKNOWN`, `NOT_PROVIDED`.
- `active_partner_id varchar(64)` - выбранный partner scope.
- `active_impersonation_session_id uuid` - ссылка на активную имперсонацию, если есть.
- `created_at timestamptz`, `expires_at timestamptz`, `revoked_at timestamptz` - жизненный цикл сессии.
- `version bigint` - optimistic locking для конкурентных изменений activePartner или impersonation state.

Индексы: unique по `token_hash`; btree по `user_id`, `expires_at`, `active_partner_id`.

## Таблица auth_session_role
Роли текущей сессии, возвращаемые frontend для route guards и role router.
- `session_role_id uuid` - первичный ключ.
- `session_id uuid` - FK на `auth_session`.
- `role_code varchar(64)` - `guest`, `customer`, `partner`, `employee-support`, `supervisor`, `admin` и другие роли платформы.
- `route_scope varchar(255)` - ограничение маршрутов или scope роли.

Ограничения: уникальность `session_id + role_code + route_scope`.

## Таблица auth_route_policy
Справочник policy для backend route checks и frontend role router.
- `route_policy_id uuid` - первичный ключ.
- `role_code varchar(64)` - роль, к которой применяется policy.
- `route_pattern varchar(255)` - route pattern, например `/employee/**`.
- `module_key varchar(64)` - owning module route-а.
- `allowed boolean` - разрешение доступа.
- `denied_mnemonic varchar(128)` - mnemonic `STR_MNEMO_*` для отказа.

Ограничения: уникальность `role_code + route_pattern + module_key`.

## Таблица auth_active_partner_state
Выбранный partner context для partner и employee flows.
- `active_partner_state_id uuid` - первичный ключ.
- `session_id uuid` - FK на `auth_session`.
- `partner_id varchar(64)` - внутренний идентификатор партнера.
- `person_number varchar(64)` - бизнес-номер партнера.
- `display_name varchar(255)` - имя в структурированных данных.
- `role_in_structure varchar(64)` - роль actor-а относительно выбранного партнера.
- `scope_code varchar(64)` - scope проверки, например `OWN`, `DOWNLINE`, `REGION`.
- `selected_at timestamptz` - момент выбора.

Индексы: btree `session_id`, `partner_id`, `scope_code`.

## Таблица auth_partner_search_audit
Audit поиска партнера без хранения исходной поисковой строки в открытом виде.
- `partner_search_audit_id uuid` - первичный ключ.
- `actor_user_id varchar(64)` - пользователь, выполнивший поиск.
- `query_hash varchar(128)` - хэш query.
- `result_count integer` - количество результатов.
- `scope_code varchar(64)` - примененный scope.
- `correlation_id varchar(64)` - связь с запросом.
- `occurred_at timestamptz` - время события.

## Таблица auth_impersonation_session
Состояние controlled impersonation.
- `impersonation_session_id uuid` - первичный ключ.
- `actor_user_id varchar(64)` - сотрудник, супервайзер или администратор.
- `target_user_id varchar(64)` - целевой пользователь.
- `target_role varchar(64)` - роль, в которой открыт target context.
- `elevated_session_id uuid` - ссылка на employee elevated session, если policy требует повышенных прав.
- `reason_code varchar(64)` - машинный код основания.
- `reason_text_hash varchar(128)` - хэш свободного текста причины; открытый текст не хранится в audit таблице.
- `status varchar(32)` - `ACTIVE`, `FINISHED`, `EXPIRED`, `REVOKED`.
- `started_at`, `expires_at`, `finished_at timestamptz` - жизненный цикл.
- `correlation_id varchar(64)` - сквозной идентификатор.

Ограничения: не более одной активной impersonation session на actor-а; `expires_at > started_at`.

## Таблица auth_audit_event
Единый audit trail auth module.
- `auth_audit_event_id uuid` - первичный ключ.
- `action_code varchar(64)` - `AUTH_LOGIN_SUCCEEDED`, `AUTH_ROUTE_DENIED`, `AUTH_ACTIVE_PARTNER_CHANGED`, `AUTH_IMPERSONATION_STARTED`, `AUTH_IMPERSONATION_FINISHED`.
- `actor_user_id varchar(64)` и `target_user_id varchar(64)` - участники события.
- `role_code varchar(64)` - роль в момент события.
- `route varchar(255)` - source route.
- `source_ip_hash varchar(128)` - хэш IP или технического source id.
- `correlation_id varchar(64)` - связь с request chain.
- `occurred_at timestamptz` - время события.

## Связи
`auth_session` связан one-to-many с `auth_session_role`, `auth_active_partner_state` и `auth_impersonation_session`. `auth_impersonation_session` и route policy порождают audit-события. `auth_partner_search_audit` привязан к session через actor/session context логически и используется для security расследований.

## Версионная база
ER модель соответствует baseline задачи 27.04.2026: PostgreSQL с Liquibase XML, backend package policy `api/domain/db/impl`, Spring MVC runtime Swagger и обязательный backend-to-frontend message contract через `STR_MNEMO_*`.
