# Module ER description. admin-identity

## Назначение модуля
`admin-identity` является owning backend module для административного управления пользователями, партнерами, сотрудниками, sponsor relationships, eligibility rules и controlled impersonation. Модуль предоставляет master-data проекции и audit trail для админского контура, не подменяя исходные пользовательские, партнерские и employee-домены.

## Таблицы и ограничения
- `admin_identity_subject`: PK `subject_id`; уникальный внешний reference через `external_user_id` и `subject_type`; индекс по `subject_type`, `status`, `display_name`.
- `admin_identity_profile_attribute`: PK `attribute_id`; FK `subject_id`; индекс по `subject_id`, `attribute_code`, `effective_from`; хранит `value_hash` и `masked_value`, а не полное PII.
- `admin_partner_relationship`: PK `relationship_id`; FK `partner_subject_id`, `sponsor_subject_id`; индекс по партнеру, sponsor и effective period; application-level constraint запрещает циклы MLM.
- `admin_partner_office_binding`: PK `binding_id`; FK `partner_subject_id`; индекс по `office_id`, `pickup_point_id`, `status`.
- `admin_employee_binding`: PK `binding_id`; FK `employee_subject_id`; индекс по `role_code`, `operational_scope`, `regional_scope`, `access_status`.
- `admin_identity_eligibility_rule`: PK `rule_id`; FK `subject_id`; индекс по `rule_type`, `effective_from`, `effective_to`.
- `admin_impersonation_policy`: PK `policy_id`; уникальный `policy_code`; stores allowed/forbidden actions in `jsonb`.
- `admin_impersonation_session`: PK `session_id`; FK `policy_id`, `actor_subject_id`, `target_subject_id`; индекс по `status`, `expires_at`.
- `admin_identity_audit_event`: PK `audit_event_id`; append-only audit table; индекс по subject, actor, action, reason, session, occurred_at и correlationId.

## Инварианты и audit
Каждая state-changing операция требует `reason_code` и создает `admin_identity_audit_event`. Sponsor changes и employee role changes валидируются до записи. Impersonation session проверяет policy и forbidden actions на каждом опасном действии.

## Package ownership
Java packages строятся от `com.bestorigin.adminidentity`:
- `api` - DTO и contracts.
- `domain` - JPA entities и repositories.
- `db` - Liquibase XML changelog.
- `impl.controller`, `impl.service`, `impl.mapper`, `impl.security`, `impl.exception` - runtime logic по техническим ролям.

## Версионный baseline
Текущий baseline: Java 25, Spring Boot 4.0.6, Maven, Hibernate, MapStruct, Lombok, Liquibase XML, PostgreSQL, React, TypeScript, Vite, Ant Design.
