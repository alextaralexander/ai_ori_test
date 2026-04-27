# ER description. Feature 025. Module platform-experience

## Назначение модели
Модуль `platform-experience` хранит минимальный backend state для сквозного UX-слоя Best Ori Gin: consent preferences, notification preferences, analytics diagnostics, i18n missing key diagnostics и runtime config snapshots. Модель не хранит тексты пользовательского интерфейса, переводы, payload-и маркетинговых пикселей, пароли, токены, платежные данные или документы.

## Таблица `platform_consent_preference`
Назначение: хранение текущих consent preferences пользователя для functional, analytics и marketing categories.

Поля:
- `id uuid PK` - технический идентификатор записи.
- `subject_user_id varchar(64) not null` - идентификатор пользователя или системного субъекта.
- `subject_role varchar(64) not null` - роль на момент сохранения consent, например `customer`, `partner`, `employee`, `tracking-admin`.
- `functional_allowed boolean not null` - обязательная функциональная категория, по умолчанию `true`.
- `analytics_allowed boolean not null` - разрешение продуктовой аналитики.
- `marketing_allowed boolean not null` - разрешение маркетинговых пикселей.
- `source_route varchar(256) not null` - маршрут, где был сохранен выбор.
- `policy_version varchar(64) not null` - версия политики согласий.
- `version integer not null` - optimistic locking для защиты от параллельного обновления.
- `created_at timestamptz not null` - дата создания.
- `updated_at timestamptz not null` - дата последнего изменения.

Ограничения и индексы:
- `ux_platform_consent_subject_policy(subject_user_id, policy_version)` - одна актуальная запись на пользователя и версию политики.
- `check functional_allowed = true` применяется, если functional category является обязательной по политике платформы.

## Таблица `platform_notification_preference`
Назначение: хранение предпочтений notification/offline UI для пользователя и локали.

Поля:
- `id uuid PK`.
- `subject_user_id varchar(64) not null`.
- `locale varchar(16) not null` - текущая локаль пользователя.
- `toast_enabled boolean not null` - разрешены toast-уведомления.
- `modal_enabled boolean not null` - разрешены modal/blocking уведомления.
- `offline_popup_enabled boolean not null` - разрешен offline popup.
- `critical_notifications_required boolean not null` - критичные сообщения не отключаются.
- `version integer not null`.
- `created_at timestamptz not null`.
- `updated_at timestamptz not null`.

Ограничения и индексы:
- `ux_platform_notification_subject_locale(subject_user_id, locale)`.
- Критичные сообщения о security, оплате, заказе, претензии, elevated mode и имперсонации остаются обязательными даже при отключенных toast.

## Таблица `platform_analytics_diagnostic_event`
Назначение: хранение диагностических событий analytics adapters без персональных данных.

Поля:
- `id uuid PK`.
- `channel_code varchar(64) not null` - `YANDEX_METRIKA`, `MINDBOX`, `HYBRID_PIXEL`, `LOCAL_DIAGNOSTICS`.
- `event_code varchar(96) not null` - `PAGEVIEW`, `CHECKOUT_COMPLETED`, `CONSENT_CHANGED`, `OFFLINE_STATE_ENTERED`, `ADAPTER_FAILED`.
- `event_status varchar(32) not null` - `SENT`, `SKIPPED`, `FAILED`, `CONSENT_DENIED`.
- `reason_code varchar(96)` - machine-readable причина, например `CONSENT_DENIED`, `ADAPTER_TIMEOUT`, `PAYLOAD_REJECTED`.
- `source_route varchar(256) not null`.
- `subject_role varchar(64) not null`.
- `correlation_id varchar(96) not null`.
- `occurred_at timestamptz not null`.

Ограничения и индексы:
- `ix_platform_analytics_diag_channel_time(channel_code, occurred_at)`.
- `ix_platform_analytics_diag_correlation(correlation_id)`.
- Payload события хранится только как диагностическая metadata без персональных и секретных данных.

## Таблица `platform_i18n_missing_key_event`
Назначение: диагностика отсутствующих i18n keys в dev/test/prod-safe режиме.

Поля:
- `id uuid PK`.
- `i18n_key varchar(256) not null`.
- `locale varchar(16) not null`.
- `source_route varchar(256) not null`.
- `component_key varchar(128) not null`.
- `environment_code varchar(32) not null`.
- `correlation_id varchar(96) not null`.
- `occurred_at timestamptz not null`.

Ограничения и индексы:
- `ix_platform_i18n_missing_key_locale(i18n_key, locale)`.
- `ix_platform_i18n_missing_time(occurred_at)`.

## Таблица `platform_runtime_config_snapshot`
Назначение: фиксировать опубликованную backend runtime-конфигурацию для frontend providers и diagnostics.

Поля:
- `id uuid PK`.
- `environment_code varchar(32) not null` - `local`, `test`, `stage`, `prod`.
- `yandex_metrika_enabled boolean not null`.
- `mindbox_enabled boolean not null`.
- `hybrid_pixel_enabled boolean not null`.
- `diagnostics_enabled boolean not null`.
- `config_version varchar(64) not null`.
- `effective_from timestamptz not null`.
- `created_at timestamptz not null`.

Ограничения и индексы:
- `ux_platform_runtime_config_env_version(environment_code, config_version)`.
- Runtime config не содержит локализованных пользовательских строк.

## Liquibase и package ownership
Для feature #25 создается отдельный XML changelog в backend module `platform-experience` под `db`. JPA entities и repositories размещаются только в `domain`, REST DTO в `api`, controllers, services, mappers, validators, config и diagnostics adapters - в role-specific subpackages внутри `impl`.

## Версионная база
Модель рассчитана на платформенную базу 27.04.2026: Java/Spring Boot/Maven monolith, Hibernate, Liquibase XML, PostgreSQL, MapStruct, Lombok и frontend TypeScript/React/Ant Design с обязательными i18n dictionaries. Новые технологические версии не вводятся.
