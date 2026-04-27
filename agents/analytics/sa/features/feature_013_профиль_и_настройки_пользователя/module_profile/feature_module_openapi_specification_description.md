# Feature 013. OpenAPI description for module_profile

## Назначение API
`module_profile` предоставляет frontend и внутренним flow единый контракт профиля пользователя. API покрывает обзор профиля, редактирование основных данных, контактов, адресов и документов, смену пароля, историю критичных изменений, readiness для checkout/delivery/claim flows и support view с audit context.

## OpenAPI group
Для monolith module должен быть создан отдельный Swagger/OpenAPI group:
- module key: `profile`
- OpenAPI JSON: `/v3/api-docs/profile`
- Swagger UI: `/swagger-ui/profile`
- package prefix: `com.bestorigin.monolith.profile`

## Endpoints
- `GET /api/profile` возвращает `ProfileOverviewResponse` для текущего пользователя: section summaries, readiness statuses и признак `auditRecorded` для support-сценариев.
- `PUT /api/profile/general` обновляет основные данные текущего пользователя: ФИО, дату рождения, gender и preferred language.
- `POST /api/profile/contacts` добавляет email или phone с обязательным `Idempotency-Key`.
- `POST /api/profile/contacts/{contactId}/verification` запускает подтверждение контакта и возвращает verification status.
- `POST /api/profile/addresses` добавляет адрес с обязательным `Idempotency-Key`.
- `PUT /api/profile/addresses/{addressId}` обновляет адрес текущего пользователя.
- `DELETE /api/profile/addresses/{addressId}` удаляет адрес, если он не заблокирован активными order, delivery или claim flows.
- `POST /api/profile/documents` добавляет или заменяет активный документ с обязательным `Idempotency-Key`.
- `POST /api/profile/security/password` меняет пароль после проверки текущего пароля или разрешенного challenge.
- `GET /api/profile/audit-events` возвращает историю критичных изменений текущего профиля.
- `GET /api/profile/readiness?flow=CHECKOUT|DELIVERY|CLAIM` возвращает готовность профиля для связанного flow.
- `GET /api/profile/support/{userId}?reason=...` возвращает support view только при разрешенном доступе и фиксирует audit event.

## DTOs
- `ProfileOverviewResponse` содержит `profileId`, `ownerUserId`, `sections`, `readiness`, `auditRecorded`.
- `ProfileSectionSummary` содержит `sectionKey`, `status`, `missingFields`, `messageMnemo`.
- `ProfileGeneralUpdateRequest` содержит основные поля профиля и валидируется на обязательность и допустимые даты.
- `ProfileContactCreateRequest` содержит `contactType`, `value`, `primary`; ответ возвращает только `maskedValue`.
- `ProfileAddressUpsertRequest` содержит structured address fields, необходимые для доставки.
- `ProfileDocumentUpsertRequest` содержит `documentType` и `documentPayload`; ответ возвращает только `documentNumberMasked`.
- `ProfilePasswordChangeRequest` содержит `currentPassword` и `newPassword`; значения не логируются и не возвращаются.
- `ProfileReadinessResponse` содержит `flow`, `ready`, `missingFields`, `messageMnemo`.
- `ProfileAuditEventResponse` содержит section/field keys, actor type, business reason, masked old/new values и `occurredAt`.

## Валидации и ошибки
- Все пользовательские сообщения, которые могут попасть во frontend, возвращаются mnemonic-кодами `STR_MNEMO_*`.
- Access denied для чужого профиля: `STR_MNEMO_PROFILE_ACCESS_DENIED`.
- Профиль не найден: `STR_MNEMO_PROFILE_NOT_FOUND`.
- Неполный профиль для checkout: `STR_MNEMO_PROFILE_CHECKOUT_INCOMPLETE`.
- Адрес заблокирован активным заказом: `STR_MNEMO_PROFILE_ADDRESS_LOCKED`.
- Слабый пароль: `STR_MNEMO_PROFILE_PASSWORD_WEAK`.
- Контакт требует подтверждения: `STR_MNEMO_PROFILE_CONTACT_REQUIRES_VERIFICATION`.
- Backend validation payload должен содержать field keys, machine reason и mnemonic, но не hardcoded user-facing text.

## Security и privacy
API всегда определяет текущего пользователя из security context, а не из client-provided owner id. Support endpoint требует отдельную роль, audit reason и фиксирует просмотр профиля. Полные значения контактов, document payload, номера документов, пароли, tokens и private secrets не возвращаются во frontend и не попадают в audit events.

## Интеграции
Checkout, delivery и claim flows используют `GET /api/profile/readiness` или module-facing service contract с теми же DTO-полями. Order module не копирует персональные данные профиля в собственную доменную модель, кроме snapshot-полей заказа, которые нужны для юридически значимой истории заказа.

## Frontend i18n
Все labels, placeholders, statuses, validation errors, success messages, empty states и CTA для маршрутов `/profile-settings/**` должны быть вынесены в поддерживаемые dictionaries. Mnemonic-коды backend добавляются во все frontend languages в той же задаче.

## Версионная база
Фича не вводит новые технологии и использует текущий baseline проекта: Spring MVC + springdoc-openapi для runtime Swagger, Java/Spring Boot monolith, React/TypeScript frontend и Ant Design-compatible components. Спецификация должна соответствовать runtime Swagger module group `profile`.
