# Architecture description after feature 013

## Контекст
Платформа Best Ori Gin использует frontend web-shell и Spring Boot monolith. Feature #13 добавляет пользовательский контур профиля и настроек. Основной owning backend module - `profile`; он хранит персональные данные, контакты, адреса, документы, security events, audit trail и readiness statuses для checkout, delivery и claim flows.

## Компоненты
- `ProfileSettingsView` отвечает за маршруты `/profile-settings`, `/profile-settings/general`, `/profile-settings/contacts`, `/profile-settings/addresses`, `/profile-settings/documents`, `/profile-settings/security`.
- `Profile API` предоставляет REST endpoints `/api/profile/**`, runtime Swagger group `/v3/api-docs/profile` и `/swagger-ui/profile`.
- `ProfileReadinessService` рассчитывает готовность профиля для checkout, delivery и claim flows.
- `ProfileAuditService` фиксирует критичные изменения, support view/update и security-sensitive события.
- `OrderClaimsView` отвечает за создание претензии, историю претензий и детали кейса на маршрутах `/order/claims/claim-create`, `/order/claims/claims-history`, `/order/claims/claims-history/:claimId`.
- `Order claims API` расширяет module_order ресурсами `/api/order/claims`.
- `OrderClaimService` валидирует заказ и позиции, рассчитывает компенсацию, ведет audit trail и маршрутизирует проверку в склад, логистику или платежи.
- `i18n resources` содержат все новые пользовательские строки и mnemonic-коды для русского и английского языков, включая `STR_MNEMO_PROFILE_*`.

## Связи
- `ProfileSettingsView -> Profile API`: HTTPS/JSON REST для чтения и изменения профиля.
- `Checkout API -> ProfileReadinessService`: in-process service call в монолите для проверки обязательных данных перед заказом.
- `Order claims API -> ProfileReadinessService`: in-process service call в монолите для проверки контакта, адреса возврата, документов и сервисных ограничений.
- `OrderClaimService -> Payment`: внешний HTTPS/API контракт для утвержденных возвратов.
- `OrderClaimService -> Warehouse and logistics`: внешний интеграционный контракт для проверки возврата, пересорта, недостачи или замены.
- `OrderClaimService -> S3 or MinIO attachments`: S3-compatible protocol для вложений претензий.

## Пакетная ownership-модель
Backend сохраняет `api/domain/db/impl`. Для `profile`: DTO и API contracts в `profile/api`, JPA entities и repositories в `profile/domain`, Liquibase XML changelog в `profile/db`, controllers/services/security/validators/mappers в role-specific subpackages внутри `profile/impl`. Для `order` сохраняется прежняя ownership-модель: order DTO в `order/api`, domain snapshots/repository в `order/domain`, runtime classes в `order/impl/controller` и `order/impl/service`, dedicated Liquibase XML в order changelog resources.

## Версионная база
Новые технологии не вводятся. Реализация использует текущие Java, Spring Boot, Maven, React, TypeScript and Ant Design-compatible patterns repository baseline. При последующем upgrade нужно обновить этот раздел в task artifacts. Backend не передает hardcoded user-facing сообщения во frontend; публичные предопределенные сообщения передаются только mnemonic-кодами `STR_MNEMO_*`.
