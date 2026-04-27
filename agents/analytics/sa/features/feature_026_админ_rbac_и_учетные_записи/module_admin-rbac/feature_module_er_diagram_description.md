# ER description. Feature 026. Module admin-rbac

## Назначение модуля
`admin-rbac` хранит и обслуживает административный контур управления внутренними учетными записями, ролями, permission sets, responsibility scopes, security policies, service accounts и immutable audit trail. Runtime package prefix: `com.bestorigin.monolith.adminrbac`. Module key: `admin-rbac`.

## Таблицы и поля

### admin_rbac_internal_account
Внутренняя учетная запись сотрудника.

- `id uuid primary key` - идентификатор учетной записи.
- `full_name varchar(255) not null` - ФИО сотрудника.
- `email varchar(320) not null unique` - рабочий email, уникальный в admin-rbac.
- `phone varchar(64)` - рабочий телефон.
- `department varchar(128) not null` - подразделение.
- `position_title varchar(128) not null` - должность.
- `account_type varchar(32) not null` - `HUMAN` или `SERVICE_OWNER`.
- `status varchar(32) not null` - `DRAFT`, `ACTIVE`, `BLOCKED`, `ACCESS_EXPIRING`, `DEACTIVATED`.
- `access_expires_at timestamptz` - дата окончания доступа.
- `version bigint not null` - optimistic lock/version.
- `created_at timestamptz not null`, `updated_at timestamptz not null` - технические даты.

Индексы: unique index по `email`; index по `status`; index по `department`.

### admin_rbac_role
Административная или employee-роль.

- `id uuid primary key`.
- `code varchar(96) not null unique` - стабильный machine-readable код роли.
- `name varchar(255) not null` - административное имя роли.
- `description text` - описание назначения.
- `module_access varchar(128) not null` - модуль или область: `employee`, `partner-office`, `admin`, `integration`.
- `active boolean not null`.
- `risk_level varchar(32) not null` - `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.
- `created_at`, `updated_at`.

### admin_rbac_permission_set
Набор разрешений, из которого вычисляется effective permission matrix.

- `id uuid primary key`.
- `code varchar(96) not null unique`.
- `name varchar(255) not null`.
- `description text`.
- `permissions_json jsonb not null` - список разрешений и action scopes.
- `risk_level varchar(32) not null`.
- `elevated_required boolean not null` - требуется MFA/elevated session для назначения или применения.
- `active boolean not null`.
- `valid_until timestamptz` - срок действия набора.

Индексы: GIN index по `permissions_json`; index по `active`, `risk_level`.

### admin_rbac_role_permission
Связь many-to-many между ролями и permission sets.

- `id uuid primary key`.
- `role_id uuid not null references admin_rbac_role(id)`.
- `permission_set_id uuid not null references admin_rbac_permission_set(id)`.

Ограничение: unique index `(role_id, permission_set_id)`.

### admin_rbac_account_role
Назначение роли учетной записи.

- `id uuid primary key`.
- `account_id uuid not null references admin_rbac_internal_account(id)`.
- `role_id uuid not null references admin_rbac_role(id)`.
- `assigned_by_user_id uuid` - actor, выдавший роль.
- `valid_from timestamptz`, `valid_until timestamptz`.
- `active boolean not null`.

Индексы: `(account_id, active)`, `(role_id, active)`.

### admin_rbac_responsibility_scope
Зоны ответственности учетной записи.

- `id uuid primary key`.
- `account_id uuid not null references admin_rbac_internal_account(id)`.
- `region_id uuid`, `warehouse_id uuid`, `pickup_point_id uuid`, `catalog_id uuid`, `product_category_id uuid`, `partner_structure_segment_id uuid` - optional scope dimensions.
- `department_id varchar(128)` - scope подразделения.
- `active boolean not null`.

Индексы: `(account_id, active)`, отдельные indexes по часто фильтруемым dimensions `region_id`, `warehouse_id`, `department_id`.

### admin_rbac_security_policy
Версионированные password, MFA и session policies.

- `id uuid primary key`.
- `policy_type varchar(48) not null` - `PASSWORD`, `MFA`, `SESSION`, `EMERGENCY`.
- `policy_code varchar(96) not null`.
- `settings_json jsonb not null` - параметры политики.
- `version bigint not null`.
- `active boolean not null`.
- `updated_by_user_id uuid not null`.
- `updated_at timestamptz not null`.

Ограничение: unique active policy на `(policy_type, policy_code, active)` для active=true.

### admin_rbac_service_account
Техническая учетная запись интеграции.

- `id uuid primary key`.
- `code varchar(96) not null unique`.
- `owner_user_id uuid not null` - владелец.
- `integration_type varchar(64) not null` - `WMS`, `ASSEMBLY`, `DELIVERY`, `PAYMENT`, `BONUS`, etc.
- `permission_scopes_json jsonb not null`.
- `allowed_ip_ranges_json jsonb not null`.
- `status varchar(32) not null` - `ACTIVE`, `DISABLED`, `EXPIRED`.
- `expires_at timestamptz`, `last_used_at timestamptz`.

Секрет не хранится открытым текстом.

### admin_rbac_service_secret_rotation
Версии secret для service account.

- `id uuid primary key`.
- `service_account_id uuid not null references admin_rbac_service_account(id)`.
- `secret_hash varchar(255) not null`.
- `masked_secret_hint varchar(64) not null`.
- `valid_from timestamptz not null`.
- `valid_until timestamptz`.
- `status varchar(32) not null` - `ACTIVE`, `EXPIRING`, `REVOKED`.

Индексы: `(service_account_id, status)`.

### admin_rbac_audit_event
Immutable audit trail админских изменений.

- `id uuid primary key`.
- `actor_user_id uuid` - пользователь, выполнивший действие.
- `target_user_id uuid` - целевая учетная запись, если применимо.
- `service_account_id uuid references admin_rbac_service_account(id)` - service account, если применимо.
- `action_code varchar(96) not null` - например `ADMIN_ROLE_ASSIGNED`.
- `old_value_json jsonb not null`, `new_value_json jsonb not null` - diff без секретов.
- `source_route varchar(255)`, `source_ip varchar(64)`.
- `correlation_id varchar(128) not null`.
- `occurred_at timestamptz not null`.

Индексы: `(target_user_id, occurred_at)`, `(actor_user_id, occurred_at)`, `(action_code, occurred_at)`, `(correlation_id)`.

## Ограничения и политики хранения
- Все Liquibase changesets создаются XML-файлом в package `com.bestorigin.monolith.adminrbac.db`.
- JPA entities и repository interfaces принадлежат package `domain`; DTO - `api`; controllers/services/validators/mappers/security/audit - role-specific packages внутри `impl`.
- Secret values, passwords, MFA seed и session tokens не попадают в response DTO и audit JSON.
- Backend-to-frontend predefined messages используют только mnemonic-коды `STR_MNEMO_ADMIN_RBAC_*`.

## Версионная база
Проектируется для текущей backend baseline Best Ori Gin на 27.04.2026: Java/Spring Boot/Maven, Hibernate/JPA, Liquibase XML, MapStruct, Lombok, PostgreSQL jsonb и monolith OpenAPI generation через Spring MVC controllers.
