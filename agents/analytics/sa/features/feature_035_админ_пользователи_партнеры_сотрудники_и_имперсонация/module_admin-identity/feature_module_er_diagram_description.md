# ER description. Feature 035 / module_admin-identity

## Назначение модели
Модель `admin-identity` хранит административное представление субъектов Best Ori Gin: пользователей, партнеров, сотрудников и организаций. Она не заменяет исходные доменные профили пользовательских модулей, а создает контролируемый master-data слой для административного поиска, изменения статусов, sponsor relationships, employee access, eligibility rules, impersonation policy и audit trail.

## Основные сущности
- `admin_identity_subject` - нормализованный субъект администрирования. Поле `subject_type` различает `USER`, `PARTNER`, `EMPLOYEE`, `ORGANIZATION`. Поля с контактами хранятся только в маскированном или хешированном виде для административной выдачи.
- `admin_identity_profile_attribute` - версионные атрибуты профиля с `value_hash`, `masked_value`, источником и периодом действия. Полные PII-значения не должны попадать в таблицу административного чтения.
- `admin_partner_relationship` - история sponsor/downline связей партнера. Связь версионная, с `effective_from`, `effective_to`, `reason_code` и `correlation_id`.
- `admin_partner_office_binding` - связь партнера с офисом, точкой выдачи или зоной обслуживания для offline sales, доставки и офисных поставок.
- `admin_employee_binding` - административные роли, operational scopes, regional scopes и статус доступа сотрудника.
- `admin_identity_eligibility_rule` - ограничения и разрешения для покупок, партнерских заказов, bonus accrual, offline sales и программ преимуществ.
- `admin_impersonation_policy` - policy controlled impersonation по actor role, target subject type, allowed/forbidden actions, duration и approval requirement.
- `admin_impersonation_session` - активные и завершенные impersonation sessions с actor, target, reasonCode, status и сроками.
- `admin_identity_audit_event` - неизменяемый журнал действий admin identity/master-data контура.

## Инварианты
- Sponsor relationship не допускает циклы MLM и самоспонсорство.
- Нельзя изменять `subject.status`, критичные атрибуты, employee bindings, sponsor relationships или impersonation policy без `reason_code`.
- Employee role conflicts проверяются до сохранения `admin_employee_binding`.
- Impersonation session не может разрешать платежи, вывод бонусов, смену пароля, изменение PII и необратимые действия без отдельного elevated scope.
- Любое изменение состояния создает `admin_identity_audit_event`.

## Backend package ownership
Owning module: `admin-identity`.

Предполагаемая структура Java-пакетов строится от домена `com.bestorigin`:
- `api` - REST DTO, request/response contracts, mnemonic codes.
- `domain` - JPA entities и repository interfaces для перечисленных таблиц.
- `db` - только Liquibase XML changelog feature #35.
- `impl.controller` - Spring MVC controllers.
- `impl.service` - orchestration, validation, RBAC checks, sponsor cycle checks, impersonation policy checks.
- `impl.mapper` - MapStruct mappers.
- `impl.security` - permission and impersonation guards.
- `impl.exception` - domain exceptions mapped to `STR_MNEMO_ADMIN_IDENTITY_*`.

## Версионный baseline
Feature #35 использует текущий monolith stack Best Ori Gin: Java 25, Spring Boot 4.0.6, Maven, Hibernate, MapStruct, Lombok, Liquibase XML, PostgreSQL, React, TypeScript, Vite и Ant Design. Отклонений от baseline в рамках ER-модели не требуется.
