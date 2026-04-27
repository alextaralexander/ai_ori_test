# Feature 013. Sequence diagram description

## Назначение
Sequence diagram описывает технический поток feature #13 между frontend web-shell, backend module `profile`, profile DB, auth/security context и связанными order/claim flows. Основная цель - показать, что профиль является owning bounded context для персональных данных, а другие flow получают только readiness и безопасные snapshot/DTO.

## Обзор профиля
1. Пользователь открывает `/profile-settings`.
2. Frontend вызывает `GET /api/profile`.
3. `profile controller` получает текущего пользователя из auth/security context.
4. `profile service` читает profile, contacts, addresses, documents, security events и audit events из profile DB.
5. Сервис рассчитывает section statuses, readiness для checkout/delivery/claim и masked values.
6. Backend возвращает `ProfileOverviewResponse` без hardcoded user-facing текста; frontend локализует mnemonic-коды через dictionaries.

## Сохранение секций
1. Пользователь сохраняет general, contact, address или document section.
2. Frontend вызывает соответствующий endpoint `/api/profile/**`; операции создания используют `Idempotency-Key`.
3. Backend проверяет ownership, валидирует payload и бизнес-ограничения.
4. Profile service сохраняет изменения и обязательно пишет `profile_audit_event` для критичных полей.
5. Ответ содержит только безопасные значения: masked contacts, masked document number, section status, machine reason и mnemonic-код.

## Security flow
1. Пользователь меняет пароль через `/profile-settings/security`.
2. Frontend вызывает `POST /api/profile/security/password`.
3. Backend проверяет текущий пароль или разрешенный security challenge.
4. Profile service валидирует password policy через security baseline проекта.
5. При успехе записываются `profile_security_event` и `profile_audit_event`; при отказе возвращается mnemonic, например `STR_MNEMO_PROFILE_PASSWORD_WEAK`.

## Readiness для order/claim flows
1. Checkout, delivery или claim flow запрашивает readiness через `GET /api/profile/readiness?flow=...` или module-facing service с тем же контрактом.
2. Profile service проверяет обязательные поля для конкретного flow.
3. Ответ содержит `ready`, `missingFields` и `messageMnemo`.
4. Order/claim flow не владеет профилем и не копирует персональные данные сверх необходимых snapshot-полей собственного бизнес-события.

## Support view
1. Оператор поддержки открывает профиль пользователя только с разрешенной ролью и reason.
2. Backend проверяет permission и обязательный audit context.
3. Profile service записывает audit event просмотра.
4. Support view возвращает masked values и `auditRecorded=true`; full document numbers, password data, tokens и secrets не раскрываются.

## Ошибки и i18n
Все предопределенные публичные сообщения передаются mnemonic-кодами `STR_MNEMO_*`. Frontend обязан добавить соответствующие значения во все поддерживаемые dictionaries. Backend validation details используют machine-readable field keys и reasons.

## Версионная база
Фича использует текущий технологический baseline проекта: Java/Spring Boot monolith, Spring MVC, Hibernate/JPA, Liquibase XML, MapStruct, Lombok, PostgreSQL-compatible DB, React/TypeScript и Ant Design-compatible frontend. Новые runtime classes должны соблюдать backend package policy: DTO в `profile/api`, entities/repositories в `profile/domain`, changelog в `profile/db`, controllers/services/security/mappers в role-specific subpackages внутри `profile/impl`.
