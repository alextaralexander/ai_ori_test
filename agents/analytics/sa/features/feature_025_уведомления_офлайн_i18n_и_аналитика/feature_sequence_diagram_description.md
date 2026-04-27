# Sequence description. Feature 025. Уведомления, offline, i18n и аналитика

## Назначение
Диаграмма описывает runtime-взаимодействие feature #25 между frontend shell, сквозными providers, backend module `platform-experience`, auth API и внешними analytics adapters. Поток относится к продуктовой работе платформы Best Ori Gin, а не к delivery workflow.

## Участники
- `Frontend App Shell` монтирует providers и управляет маршрутом.
- `I18nProvider` загружает `resources_*.ts`, разрешает i18n keys и фиксирует missing key diagnostics.
- `NotificationProvider` показывает toast, modal и offline/reconnect notifications через единый root.
- `OfflineStatusProvider` отслеживает browser online/offline state и проверку session после reconnect.
- `AnalyticsProvider` принимает pageview, conversion, notification, offline, consent и error events.
- `Auth API` восстанавливает session context, role policies, active partner и impersonation state.
- `platform-experience API` возвращает runtime config, consent preferences и принимает diagnostics.
- `Yandex Metrika`, `Mindbox`, `Hybrid Pixel` получают только разрешенные consent-aware события через adapters.

## Основной поток
Пользователь открывает маршрут платформы. Frontend shell загружает локализацию, монтирует notification root, подписывается на offline/online события и восстанавливает auth session. После этого frontend запрашивает у `platform-experience` runtime config и consent preferences. Backend возвращает только structured data и mnemonic-коды, без локализованных UI-текстов.

Когда пользователь выполняет бизнес-действие, feature-компонент не показывает текст напрямую. Он передает i18n key или `STR_MNEMO_*` в NotificationProvider. Provider запрашивает перевод у I18nProvider и показывает локализованный toast или modal. Событие `NOTIFICATION_SHOWN` передается в AnalyticsProvider как техническое событие без персональных данных.

Если consent разрешает analytics category, AnalyticsProvider отправляет pageview или conversion в Yandex Metrika. Если marketing category разрешена, conversion также может быть отправлен в Mindbox и Hybrid Pixel. Если consent запрещает категорию, событие не отправляется во внешний канал, а backend получает diagnostics event с reasonCode `CONSENT_DENIED`.

## Offline и reconnect
При browser offline event OfflineStatusProvider показывает `platform-offline-popup` через NotificationProvider. Popup локализуется через i18n dictionaries. Неидемпотентные операции, включая checkout, payment, claim, elevated rights и impersonation, не повторяются автоматически без idempotency key. После online event frontend проверяет session через Auth API и показывает `platform-reconnect-notification`.

## Diagnostics и отказоустойчивость
Если один analytics adapter падает, AnalyticsProvider фиксирует diagnostics event в `platform-experience` и продолжает отправку в остальные разрешенные adapters. Сбой диагностики не блокирует пользовательский flow. Missing i18n key отправляется на `/diagnostics/i18n-missing-keys`; production fallback не должен быть пустым и не должен содержать mojibake.

## Доступ к diagnostics
Маршрут `/admin/analytics-diagnostics` доступен только `tracking-admin` или роли с соответствующей policy. Обычный пользователь получает HTTP 403 и mnemonic-код `STR_MNEMO_ANALYTICS_DIAGNOSTICS_FORBIDDEN`, который frontend локализует через dictionaries.

## Контракты сообщений
Backend возвращает только mnemonic-коды:
- `STR_MNEMO_PLATFORM_EXPERIENCE_CONFIG_READY`.
- `STR_MNEMO_PLATFORM_CONSENT_UPDATED`.
- `STR_MNEMO_PLATFORM_NOTIFICATION_PREFERENCES_READY`.
- `STR_MNEMO_PLATFORM_DIAGNOSTIC_ACCEPTED`.
- `STR_MNEMO_PLATFORM_DIAGNOSTICS_READY`.
- `STR_MNEMO_ANALYTICS_DIAGNOSTICS_FORBIDDEN`.

Все эти коды должны быть добавлены во все поддерживаемые frontend dictionaries.

## Версионная база
Описание соответствует платформенной базе 27.04.2026: Java/Spring Boot/Maven monolith, Spring MVC + springdoc-openapi, Liquibase XML, Hibernate, MapStruct, Lombok, PostgreSQL, frontend TypeScript/React/Ant Design и обязательные i18n dictionaries. Новые технологические версии не вводятся.
