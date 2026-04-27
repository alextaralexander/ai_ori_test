# Module profile. Полная OpenAPI specification

## Назначение
OpenAPI specification `module_profile` описывает публичный backend contract для маршрутов `/profile-settings/**` и module-facing readiness contract для checkout, delivery и claim flows. Runtime Swagger должен генерироваться автоматически из Spring MVC controllers в package prefix `com.bestorigin.monolith.profile`.

## Swagger/OpenAPI group
- module key: `profile`
- OpenAPI JSON: `/v3/api-docs/profile`
- Swagger UI: `/swagger-ui/profile`
- Controllers package: `com.bestorigin.monolith.profile.impl.controller`
- Не допускаются ручные hardcoded endpoint registries вне стандартной группировки monolith modules.

## Endpoint groups
- Overview: `GET /api/profile` возвращает section summaries, readiness statuses и masked values.
- General: `PUT /api/profile/general` обновляет основные данные текущего пользователя.
- Contacts: `POST /api/profile/contacts` добавляет контакт, `POST /api/profile/contacts/{contactId}/verification` запускает подтверждение.
- Addresses: `POST /api/profile/addresses`, `PUT /api/profile/addresses/{addressId}`, `DELETE /api/profile/addresses/{addressId}` управляют адресной книгой.
- Documents: `POST /api/profile/documents` добавляет или заменяет активный документ.
- Security: `POST /api/profile/security/password` меняет пароль и пишет security event.
- Audit: `GET /api/profile/audit-events` возвращает историю критичных изменений.
- Readiness: `GET /api/profile/readiness?flow=...` возвращает готовность профиля для `CHECKOUT`, `DELIVERY`, `CLAIM`.
- Support: `GET /api/profile/support/{userId}?reason=...` возвращает support view с обязательной записью audit event.

## DTO и enum contracts
- `ProfileSectionStatus`: `COMPLETE`, `INCOMPLETE`, `REQUIRES_VERIFICATION`, `LOCKED`.
- `ProfileReadinessFlow`: `CHECKOUT`, `DELIVERY`, `CLAIM`.
- `ContactType`: `EMAIL`, `PHONE`.
- `VerificationStatus`: `UNVERIFIED`, `REQUIRES_VERIFICATION`, `VERIFIED`, `FAILED`.
- `ProfileOverviewResponse`: корневой response для обзора и support view.
- `ProfileSectionSummary`: статус секции, missing fields и mnemonic.
- `ProfileReadinessResponse`: результат readiness для связанного flow.
- `ProfileAuditEventResponse`: masked history record без секретов.

## Validation and mnemonics
Backend validation возвращает field keys, machine reasons и mnemonic-коды. Предопределенные user-facing тексты запрещены в backend responses. Обязательные mnemonic-коды:
- `STR_MNEMO_PROFILE_ACCESS_DENIED`
- `STR_MNEMO_PROFILE_NOT_FOUND`
- `STR_MNEMO_PROFILE_CHECKOUT_INCOMPLETE`
- `STR_MNEMO_PROFILE_ADDRESS_LOCKED`
- `STR_MNEMO_PROFILE_PASSWORD_WEAK`
- `STR_MNEMO_PROFILE_CONTACT_REQUIRES_VERIFICATION`
- `STR_MNEMO_PROFILE_SUPPORT_REASON_REQUIRED`

## Security and privacy
Все endpoints текущего пользователя определяют owner из security context. Support endpoint требует отдельной роли и reason. API возвращает только masked contacts и masked document numbers. Пароли, tokens, encrypted payloads, full document numbers и private secrets не логируются и не возвращаются.

## Frontend contract
Frontend route layer `/profile-settings`, `/profile-settings/general`, `/profile-settings/contacts`, `/profile-settings/addresses`, `/profile-settings/documents`, `/profile-settings/security` должен использовать эти endpoints. Все user-facing строки должны быть вынесены в frontend i18n dictionaries для всех поддерживаемых языков.

## Версионная база
Модуль использует текущий baseline проекта: Java/Spring Boot monolith, Spring MVC, springdoc-openapi, Maven, Hibernate/JPA, Liquibase XML, MapStruct, Lombok, PostgreSQL-compatible DB, React/TypeScript и Ant Design-compatible UI. При реализации нельзя вводить новый OpenAPI generator или отдельный documentation registry без отдельного approved compatibility exception.
