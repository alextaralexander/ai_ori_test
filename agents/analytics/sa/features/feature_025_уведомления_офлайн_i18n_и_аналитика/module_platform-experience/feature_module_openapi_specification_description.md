# OpenAPI description. Feature 025. Module platform-experience

## Назначение API
API `platform-experience` предоставляет backend contract для frontend providers feature #25: `I18nProvider`, `NotificationProvider`, `OfflineStatusProvider` и `AnalyticsProvider`. Контракт не возвращает локализованные пользовательские тексты. Все предопределенные сообщения передаются только как mnemonic-коды `STR_MNEMO_*`, которые frontend разрешает через `resources_*.ts`.

## Endpoint `GET /api/platform-experience/runtime-config`
Назначение: вернуть frontend runtime config для включения analytics adapters, diagnostics и consent policy.

Ответ `200` содержит:
- `moduleKey = platform-experience`.
- `environmentCode` для local/test/stage/prod поведения.
- `consentPolicyVersion`.
- `analyticsChannels[]` с `channelCode`, `enabled`, `consentCategory`, `diagnosticsVisible`.
- `diagnosticsEnabled`.
- `messageCode = STR_MNEMO_PLATFORM_EXPERIENCE_CONFIG_READY`.

Контракт не содержит пользовательских UI-текстов, названий кнопок или описаний для consent panel.

## Endpoint `GET /api/platform-experience/consent/preferences`
Назначение: получить текущие consent preferences для пользователя и версии политики.

Параметры:
- `subjectUserId` - обязательный идентификатор пользователя.
- `policyVersion` - обязательная версия политики.

Ответ `200` содержит functional, analytics и marketing flags, role snapshot, version и `messageCode`. Ошибки авторизации возвращают `STR_MNEMO_AUTH_SESSION_EXPIRED` или `STR_MNEMO_AUTH_ACCESS_DENIED`, validation errors - `STR_MNEMO_PLATFORM_CONSENT_INVALID`.

## Endpoint `PUT /api/platform-experience/consent/preferences`
Назначение: обновить consent preferences.

Request body:
- `subjectUserId`, `subjectRole`, `policyVersion`, `analyticsAllowed`, `marketingAllowed`, `sourceRoute`, `version`.

Правила:
- Functional category остается обязательной, если platform policy требует ее для технической работы.
- Обновление использует optimistic locking по `version`; конфликт возвращает HTTP 409 и `STR_MNEMO_PLATFORM_CONSENT_VERSION_CONFLICT`.
- Изменение фиксируется в audit trail с actorUserId, subjectUserId, sourceRoute, policyVersion, old/new flags и correlationId.

## Endpoint `GET /api/platform-experience/notification/preferences`
Назначение: вернуть preferences для notification/offline UI.

Параметры:
- `subjectUserId`.
- `locale`.

Ответ содержит toast, modal, offline popup и critical notification flags. Критичные уведомления о security, оплате, заказе, претензии, elevated mode и имперсонации не отключаются пользовательской настройкой.

## Endpoint `POST /api/platform-experience/diagnostics/analytics-events`
Назначение: принять diagnostic event от AnalyticsProvider или adapter layer.

Request body:
- `channelCode` - `YANDEX_METRIKA`, `MINDBOX`, `HYBRID_PIXEL`, `LOCAL_DIAGNOSTICS`.
- `eventCode` - pageview/conversion/consent/offline/error event.
- `eventStatus` - `SENT`, `SKIPPED`, `FAILED`, `CONSENT_DENIED`.
- `reasonCode` - machine-readable причина.
- `sourceRoute`, `subjectRole`, `correlationId`, `occurredAt`.

Правила:
- Endpoint принимает только diagnostic metadata без токенов, паролей, платежных данных, документов и лишних персональных данных.
- Ошибка одного analytics channel не должна блокировать остальные adapters и пользовательский flow.
- Успех возвращает HTTP 202 и `STR_MNEMO_PLATFORM_DIAGNOSTIC_ACCEPTED`.

## Endpoint `POST /api/platform-experience/diagnostics/i18n-missing-keys`
Назначение: принять событие отсутствующего i18n key.

Request body:
- `i18nKey`, `locale`, `sourceRoute`, `componentKey`, `environmentCode`, `correlationId`, `occurredAt`.

Правила:
- В dev/test окружениях missing key должен явно диагностироваться.
- В production допускается безопасный fallback, но не пустая строка и не mojibake.
- Endpoint не сохраняет переводов и не возвращает локализованных текстов.

## Endpoint `GET /api/platform-experience/diagnostics/summary`
Назначение: показать summary analytics diagnostics администратору трекинга.

Параметры:
- `from`, `to` - обязательный период.
- `channelCode` - optional фильтр канала.

Доступ:
- Только роль `tracking-admin` или admin/employee policy для analytics diagnostics.
- Обычные пользователи получают HTTP 403 и `STR_MNEMO_ANALYTICS_DIAGNOSTICS_FORBIDDEN`.

Ответ:
- `channelSummaries[]` с `sentCount`, `skippedCount`, `failedCount`, `lastReasonCode`.
- `messageCode = STR_MNEMO_PLATFORM_DIAGNOSTICS_READY`.

## DTO и validation
DTO размещаются в package `com.bestorigin.monolith.platformexperience.api`. Runtime controllers, services, validators, mappers and config classes размещаются в `com.bestorigin.monolith.platformexperience.impl.<role>`. JPA entities and repositories размещаются в `com.bestorigin.monolith.platformexperience.domain`. Liquibase XML changelog размещается в `com.bestorigin.monolith.platformexperience.db`.

Обязательные mnemonic-коды:
- `STR_MNEMO_PLATFORM_EXPERIENCE_CONFIG_READY`.
- `STR_MNEMO_PLATFORM_CONSENT_UPDATED`.
- `STR_MNEMO_PLATFORM_CONSENT_INVALID`.
- `STR_MNEMO_PLATFORM_CONSENT_VERSION_CONFLICT`.
- `STR_MNEMO_PLATFORM_NOTIFICATION_PREFERENCES_READY`.
- `STR_MNEMO_PLATFORM_DIAGNOSTIC_ACCEPTED`.
- `STR_MNEMO_PLATFORM_DIAGNOSTICS_READY`.
- `STR_MNEMO_ANALYTICS_DIAGNOSTICS_FORBIDDEN`.
- `STR_MNEMO_PLATFORM_EXPERIENCE_VALIDATION_FAILED`.

Каждый новый mnemonic-код должен быть добавлен во все поддерживаемые frontend dictionaries.

## Swagger/OpenAPI policy
Контроллеры module `platform-experience` должны находиться внутри owning module package prefix и попадать в dedicated OpenAPI group автоматически через Spring MVC controllers. Canonical runtime URLs: `/v3/api-docs/platform-experience` и `/swagger-ui/platform-experience`. Ручные списки endpoint-ов и отдельные ad hoc Swagger registries не допускаются.

## Версионная база
Спецификация рассчитана на платформенную базу 27.04.2026: Java/Spring Boot/Maven monolith, springdoc-openapi runtime generation, Liquibase XML, Hibernate, MapStruct, Lombok, PostgreSQL, frontend TypeScript/React/Ant Design и обязательные i18n dictionaries. Новые технологические версии не вводятся.
