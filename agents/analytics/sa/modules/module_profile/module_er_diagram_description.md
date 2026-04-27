# Module profile. Полная ER модель

## Назначение модуля
`module_profile` является owning backend module для профиля пользователя Best Ori Gin. Модуль хранит персональные и профильные данные, контакты, адреса, документы, security events, audit trail и readiness statuses для checkout, delivery и claim flows. Order, claim и delivery контуры используют профиль через API или service contract, но не становятся владельцами профильных сущностей.

## Package ownership
- `profile/api` - REST DTO, enum-контракты, request/response модели, readiness DTO.
- `profile/domain` - JPA entities и repository interfaces.
- `profile/db` - только Liquibase XML changelog files.
- `profile/impl/controller` - Spring MVC controllers.
- `profile/impl/service` - бизнес-логика, validation, masking, readiness, audit.
- `profile/impl/security` - ownership guards, support permission checks.
- `profile/impl/mapper` - MapStruct mappers.
- `profile/impl/validator` - секционные валидаторы профиля и password policy adapter.

## Таблицы и связи
`profile_user` является корневой таблицей модуля. Она связана отношением one-to-many с `profile_contact`, `profile_address`, `profile_document`, `profile_security_event` и `profile_audit_event`. Удаление профиля не должно физически удалять audit history без отдельного compliance-решения; для MVP предпочтителен logical status `LOCKED` или `INACTIVE`, если потребуется деактивация.

## profile_user
- Primary key: `profile_id uuid`.
- Business key: `owner_user_id`, уникальный внешний идентификатор пользователя.
- Основные поля: `first_name`, `last_name`, `middle_name`, `birth_date`, `gender`, `preferred_language`.
- Status: `profile_status` со значениями `ACTIVE`, `INCOMPLETE`, `LOCKED`.
- Technical timestamps: `created_at`, `updated_at`.
- Индексы: unique `owner_user_id`, index `profile_status`.

## profile_contact
- Primary key: `contact_id uuid`.
- FK: `profile_id`.
- Тип: `contact_type` со значениями `EMAIL`, `PHONE`.
- Значения: `contact_value_encrypted` для хранения и `contact_value_masked` для API responses.
- Основной контакт: `primary_flag`.
- Проверка: `verification_status`, `verification_requested_at`, `verified_at`.
- Ограничения: один primary contact на `profile_id + contact_type`; verification status не может стать `VERIFIED` без подтвержденного события проверки.

## profile_address
- Primary key: `address_id uuid`.
- FK: `profile_id`.
- Structured fields: `country_code`, `region`, `city`, `street`, `house`, `building`, `apartment`, `postal_code`, `delivery_comment`.
- `default_flag` определяет адрес по умолчанию.
- `lock_reason` хранит machine reason блокировки изменения или удаления, например `ADDRESS_LOCKED_BY_ACTIVE_ORDER`.
- Ограничения: один default address на профиль; checkout readiness требует country, city, street, house и postal code.

## profile_document
- Primary key: `document_id uuid`.
- FK: `profile_id`.
- `document_type` определяет тип документа.
- `document_payload_encrypted` хранит защищенный payload.
- `document_number_masked` используется в API response.
- `active_flag` и `verification_status` управляют актуальностью и проверкой документа.
- Ограничения: один active document каждого типа, если для типа не утверждено другое бизнес-правило.

## profile_security_event
- Primary key: `security_event_id uuid`.
- FK: `profile_id`.
- Поля: `event_type`, `source_channel`, `metadata_json`, `occurred_at`.
- Назначение: фиксировать смену пароля, подтверждение контакта, support view/update и другие security-sensitive события без хранения секретов.

## profile_audit_event
- Primary key: `audit_event_id uuid`.
- FK: `profile_id`.
- Поля: `section_key`, `field_key`, `actor_type`, `actor_id`, `business_reason`, `old_value_masked`, `new_value_masked`, `occurred_at`.
- Назначение: воспроизводимая история критичных изменений профиля, включая support-доступ и изменения документов.

## Privacy constraints
Ни одна API-модель не возвращает encrypted payload, полный номер документа, полный контакт, пароль, token, secret или private storage path. Audit events используют только masked values и machine-readable keys. Support-доступ всегда требует reason и создает audit event.

## Версионная база
Модуль использует текущий baseline проекта: Java, Spring Boot monolith, Spring MVC, Hibernate/JPA, Liquibase XML, MapStruct, Lombok и PostgreSQL-compatible DB. Новые Liquibase changesets должны быть XML-файлами в `profile/db` и не смешиваться с changelog других модулей.
