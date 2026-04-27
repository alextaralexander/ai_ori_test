# Feature module ER description. Feature 023. Module employee

## Назначение модели
Feature #23 расширяет employee module persisted-моделью для собственных настроек сотрудника, security-событий и контролируемого elevated mode. Модель хранит employee-owned настройки и audit-контекст, не дублирует master-data пользователей из authorization module и не хранит секреты MFA или session secrets.

## employee_profile_settings
Корневая таблица настроек employee-профиля.

Поля:
- `employee_id varchar(64)` - первичный ключ, стабильный идентификатор внутреннего сотрудника.
- `display_name varchar(255)` - отображаемое имя сотрудника.
- `job_title varchar(128)` - рабочая должность.
- `department_code varchar(64)` - код подразделения.
- `preferred_language varchar(16)` - предпочитаемый язык frontend.
- `timezone varchar(64)` - timezone сотрудника.
- `notification_channel varchar(32)` - основной канал уведомлений.
- `employee_status varchar(32)` - `ACTIVE`, `SUSPENDED`, `LEFT`.
- `version bigint` - optimistic locking.
- `created_at timestamptz`, `updated_at timestamptz` - технические даты.

Ограничения и индексы:
- PK `pk_employee_profile_settings` по `employee_id`.
- Check `chk_employee_profile_settings_status` по допустимым статусам.
- Check на непустые `display_name`, `preferred_language`, `timezone`.

## employee_contact
Контакты сотрудника для employee-процессов.

Поля:
- `contact_id uuid` - первичный ключ контакта.
- `employee_id varchar(64)` - владелец контакта.
- `contact_type varchar(32)` - `WORK_PHONE`, `WORK_EMAIL`, `MESSENGER`, `INTERNAL_EXTENSION`.
- `contact_value_encrypted text` - зашифрованное значение; plaintext не возвращается во frontend.
- `masked_value varchar(128)` - безопасное отображение значения.
- `primary_flag boolean` - основной контакт типа.
- `verification_status varchar(32)` - `PENDING`, `VERIFIED`, `REJECTED`.
- `version bigint`, `created_at timestamptz`, `updated_at timestamptz`.

Ограничения и индексы:
- PK `pk_employee_contact` по `contact_id`.
- FK `fk_employee_contact_profile` на `employee_profile_settings(employee_id)`.
- Индекс `idx_employee_contact_employee` по `employee_id`.
- Unique partial index на основной контакт одного типа: `employee_id, contact_type where primary_flag = true`.
- Check constraints на непустые `contact_type`, `masked_value`, `verification_status`.

## employee_address
Операционные адреса сотрудника.

Поля:
- `address_id uuid` - первичный ключ адреса.
- `employee_id varchar(64)` - владелец адреса.
- `address_type varchar(32)` - `OFFICE`, `PICKUP_POINT`, `REMOTE_WORK`, `LEGAL`.
- `region_code varchar(32)` - региональный scope.
- `city varchar(128)`, `address_line varchar(512)`, `postal_code varchar(32)`.
- `active_flag boolean` - активность адреса.
- `valid_from date`, `valid_to date` - период действия.
- `version bigint`, `created_at timestamptz`, `updated_at timestamptz`.

Ограничения и индексы:
- PK `pk_employee_address` по `address_id`.
- FK `fk_employee_address_profile` на `employee_profile_settings(employee_id)`.
- Индекс `idx_employee_address_employee_region` по `employee_id, region_code`.
- Check `valid_to is null or valid_to >= valid_from`.

## employee_document_metadata
Metadata подтверждающих документов; файл хранится во внешнем S3/MinIO слое.

Поля:
- `document_id uuid` - первичный ключ metadata.
- `employee_id varchar(64)` - владелец документа.
- `document_type varchar(64)` - тип документа, например `POWER_OF_ATTORNEY`, `ROLE_CONFIRMATION`, `PICKUP_POINT_OPERATOR`.
- `masked_number varchar(128)` - безопасный номер документа.
- `issued_at date`, `expires_at date` - даты документа.
- `verification_status varchar(32)` - `PENDING`, `VERIFIED`, `REJECTED`, `EXPIRED`.
- `linked_policy_code varchar(96)` - policy, для которой документ может быть основанием.
- `file_reference_id varchar(128)` - идентификатор файла в S3/MinIO abstraction.
- `version bigint`, `created_at timestamptz`, `updated_at timestamptz`.

Ограничения и индексы:
- PK `pk_employee_document_metadata` по `document_id`.
- FK `fk_employee_document_profile` на `employee_profile_settings(employee_id)`.
- Индекс `idx_employee_document_employee_policy` по `employee_id, linked_policy_code, verification_status`.
- Check `expires_at is null or expires_at >= issued_at`.

## employee_security_event
Read-side журнал security-событий employee-профиля.

Поля:
- `security_event_id uuid` - первичный ключ события.
- `employee_id varchar(64)` - сотрудник, к которому относится событие.
- `event_type varchar(64)` - `MFA_ENABLED`, `PASSWORD_CHANGED`, `SESSION_OPENED`, `SESSION_CLOSED`, `RISK_DETECTED`.
- `source_route varchar(256)` - route или system source.
- `risk_level varchar(32)` - `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.
- `metadata_json jsonb` - безопасные machine-readable детали без секретов.
- `correlation_id varchar(128)` - сквозной идентификатор.
- `occurred_at timestamptz` - время события.

Ограничения и индексы:
- PK `pk_employee_security_event` по `security_event_id`.
- Индекс `idx_employee_security_event_employee_time` по `employee_id, occurred_at desc`.
- Check на непустые `event_type`, `risk_level`.

## employee_elevated_request
Запрос сотрудника на временный elevated mode.

Поля:
- `elevated_request_id uuid` - первичный ключ запроса.
- `employee_id varchar(64)` - сотрудник, запросивший права.
- `policy_code varchar(96)` - policy повышенных прав.
- `reason_code varchar(64)` - структурированное основание.
- `reason_text varchar(1000)` - дополнительное описание.
- `target_scope varchar(256)` - область действия.
- `requested_duration_minutes integer` - запрошенный срок.
- `linked_document_id uuid` - подтверждающий документ, если требуется.
- `status varchar(48)` - `DRAFT`, `PENDING_SUPERVISOR_APPROVAL`, `APPROVED`, `REJECTED`, `EXPIRED`, `CANCELLED`.
- `requested_at timestamptz`, `decided_by varchar(64)`, `decided_at timestamptz`, `decision_comment varchar(1000)`.
- `version bigint` - optimistic locking.

Ограничения и индексы:
- PK `pk_employee_elevated_request` по `elevated_request_id`.
- FK `fk_employee_elevated_request_profile` на `employee_profile_settings(employee_id)`.
- FK `fk_employee_elevated_request_document` на `employee_document_metadata(document_id)` nullable.
- Индекс `idx_employee_elevated_request_employee_status` по `employee_id, status, requested_at desc`.
- Check `requested_duration_minutes between 1 and 480`.

## employee_elevated_session
Активная или завершенная elevated session.

Поля:
- `elevated_session_id uuid` - первичный ключ session.
- `elevated_request_id uuid` - запрос-основание.
- `employee_id varchar(64)` - владелец session.
- `policy_code varchar(96)`, `target_scope varchar(256)` - фактические права.
- `status varchar(32)` - `ACTIVE`, `CLOSED`, `REVOKED`, `EXPIRED`.
- `started_at timestamptz`, `expires_at timestamptz`, `closed_at timestamptz`, `closed_by varchar(64)`.
- `correlation_id varchar(128)` - идентификатор для связанных employee flows.
- `version bigint` - optimistic locking.

Ограничения и индексы:
- PK `pk_employee_elevated_session` по `elevated_session_id`.
- FK `fk_employee_elevated_session_request` на `employee_elevated_request(elevated_request_id)`.
- Индекс `idx_employee_elevated_session_employee_status` по `employee_id, status, expires_at desc`.
- Unique partial index `uidx_employee_elevated_session_active_policy` на `employee_id, policy_code where status = 'ACTIVE'`.
- Check `expires_at > started_at`.

## employee_elevated_audit
Audit-события по настройкам и elevated mode.

Поля:
- `audit_event_id uuid` - первичный ключ события.
- `actor_user_id varchar(64)` - пользователь, выполнивший действие.
- `target_employee_id varchar(64)` - сотрудник, к которому относится действие.
- `elevated_request_id uuid` nullable.
- `elevated_session_id uuid` nullable.
- `action_code varchar(96)` - действие, например `EMPLOYEE_PROFILE_GENERAL_UPDATED`, `EMPLOYEE_ELEVATED_SESSION_APPROVED`.
- `policy_code varchar(96)` nullable.
- `source_route varchar(256)` - frontend route или system source.
- `target_entity_type varchar(64)`, `target_entity_id varchar(128)` - связанная сущность order, claim, partner или support case.
- `correlation_id varchar(128)` - сквозной идентификатор.
- `occurred_at timestamptz` - время события.

Ограничения и индексы:
- PK `pk_employee_elevated_audit` по `audit_event_id`.
- FK на request/session nullable, чтобы сохранять audit даже для отказанных попыток.
- Индекс `idx_employee_elevated_audit_target_time` по `target_employee_id, occurred_at desc`.
- Индекс `idx_employee_elevated_audit_session` по `elevated_session_id, occurred_at desc`.

## Связи с другими модулями
- Authorization module является источником employee identity, ролей, MFA и базовых блокировок; employee module хранит только feature-owned настройки и security read-side.
- S3/MinIO слой хранит бинарные файлы документов; employee module хранит `file_reference_id` и verification metadata.
- Order, claim, partner, delivery и support flows получают `elevatedSessionId` и `correlationId` как audit context без жестких FK между доменными таблицами.

## Версионная база
Фича не вводит новые технологии и использует baseline задачи на 27.04.2026: Java/Spring Boot/Maven monolith, PostgreSQL, Liquibase XML, Hibernate-compatible `uuid`, `jsonb`, `numeric`, `date` и `timestamptz`, package policy `api/domain/db/impl`, frontend i18n и backend mnemonic contract `STR_MNEMO_*`.