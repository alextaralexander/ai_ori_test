# Feature 013. ER changes for module_profile

## Назначение
`module_profile` хранит личный профиль пользователя Best Ori Gin и предоставляет единый источник данных для checkout, delivery, claim flows и support-сценариев. Feature #13 вводит отдельный bounded context профиля, чтобы order module не владел персональными данными напрямую.

## Package ownership
- `profile/api` содержит REST DTO, request/response contracts, enum-контракты и module-facing readiness DTO.
- `profile/domain` содержит JPA entities и repository interfaces для таблиц профиля.
- `profile/db` содержит только Liquibase XML changelog feature #13.
- `profile/impl/controller` содержит REST controllers.
- `profile/impl/service` содержит orchestration, validation, masking, readiness и audit services.
- `profile/impl/security` содержит support access checks и ownership guards.
- `profile/impl/mapper` содержит MapStruct mappers между domain и api DTO.

## Таблица profile_user
- `profile_id uuid` - primary key.
- `owner_user_id string` - внешний идентификатор пользователя из auth/security контекста; обязательный, уникальный.
- `first_name string`, `last_name string`, `middle_name string` - основные ФИО, `first_name` и `last_name` обязательны после завершения general section.
- `birth_date date` - дата рождения, не может быть в будущем.
- `gender enum` - machine-readable значение `FEMALE`, `MALE`, `NOT_SPECIFIED`.
- `preferred_language string` - язык интерфейса, используется frontend i18n и коммуникациями.
- `profile_status enum` - `ACTIVE`, `INCOMPLETE`, `LOCKED`.
- `created_at timestamp`, `updated_at timestamp` - технические даты.
- Индексы: unique index по `owner_user_id`, index по `profile_status`.

## Таблица profile_contact
- `contact_id uuid` - primary key.
- `profile_id uuid` - FK на `profile_user.profile_id`, cascade delete запрещен для audit integrity.
- `contact_type enum` - `EMAIL` или `PHONE`.
- `contact_value_encrypted string` - зашифрованное значение контакта.
- `contact_value_masked string` - безопасное masked value для API responses.
- `primary_flag boolean` - признак основного контакта в рамках типа.
- `verification_status enum` - `UNVERIFIED`, `REQUIRES_VERIFICATION`, `VERIFIED`, `FAILED`.
- `verification_requested_at timestamp`, `verified_at timestamp` - даты процесса подтверждения.
- Ограничения: unique partial index на один primary contact per `profile_id + contact_type`; unique index для активного encrypted normalized contact при технической возможности.

## Таблица profile_address
- `address_id uuid` - primary key.
- `profile_id uuid` - FK на `profile_user`.
- `country_code string`, `region string`, `city string`, `street string`, `house string`, `building string`, `apartment string`, `postal_code string` - structured address fields.
- `delivery_comment string` - комментарий для доставки, не используется для системных решений.
- `default_flag boolean` - адрес по умолчанию.
- `lock_reason string` - machine reason блокировки удаления или изменения, например `ADDRESS_LOCKED_BY_ACTIVE_ORDER`.
- Ограничения: один default address на профиль; обязательны `country_code`, `city`, `street`, `house`, `postal_code` для checkout readiness.

## Таблица profile_document
- `document_id uuid` - primary key.
- `profile_id uuid` - FK на `profile_user`.
- `document_type enum` - `PASSPORT`, `TAX_ID`, `PARTNER_CONTRACT` или другой approved type.
- `document_payload_encrypted text` - зашифрованный payload документа.
- `document_number_masked string` - masked number для frontend.
- `active_flag boolean` - активная версия документа.
- `verification_status enum` - `UNVERIFIED`, `VERIFIED`, `REJECTED`, `EXPIRED`.
- `updated_at timestamp` - дата последнего изменения.
- Ограничения: не более одного active document одного типа для профиля, если бизнес-правило не разрешает несколько активных документов.

## Таблица profile_security_event
- `security_event_id uuid` - primary key.
- `profile_id uuid` - FK на `profile_user`.
- `event_type string` - `PASSWORD_CHANGED`, `CONTACT_VERIFICATION_REQUESTED`, `SUPPORT_VIEW`, `SUPPORT_UPDATE`.
- `source_channel string` - `WEB`, `SUPPORT`, `SYSTEM`.
- `metadata_json jsonb` - обезличенные технические признаки события.
- `occurred_at timestamp` - дата события.
- Индексы: `profile_id + occurred_at desc`, `event_type`.

## Таблица profile_audit_event
- `audit_event_id uuid` - primary key.
- `profile_id uuid` - FK на `profile_user`.
- `section_key string` - `GENERAL`, `CONTACTS`, `ADDRESSES`, `DOCUMENTS`, `SECURITY`.
- `field_key string` - machine-readable поле.
- `actor_type enum` - `USER`, `SUPPORT`, `SYSTEM`.
- `actor_id string` - идентификатор инициатора.
- `business_reason string` - обязательная причина для support-сценариев.
- `old_value_masked string`, `new_value_masked string` - только masked values.
- `occurred_at timestamp` - дата события.
- Индексы: `profile_id + occurred_at desc`, `actor_type`, `section_key`.

## Связи и ограничения безопасности
Все дочерние таблицы связаны с `profile_user` отношением one-to-many. API никогда не возвращает `*_encrypted`, полные document numbers, парольные данные, tokens или private storage paths. Любое критичное изменение создает запись в `profile_audit_event`, а security-sensitive события дополнительно попадают в `profile_security_event`.

## Версионная база
Фича не вводит новые технологии. Используется текущий monolith baseline проекта: Java, Spring Boot, Maven, Hibernate/JPA, Liquibase XML, MapStruct, Lombok и PostgreSQL-compatible schema. Если при реализации потребуется криптографический provider или отдельное secret storage решение, оно должно быть оформлено отдельным compatibility decision перед кодированием.
