# Acceptance criteria. Feature 025. Уведомления, офлайн, i18n и аналитика

## Обязательные критерии notification layer
1. Frontend предоставляет единый `NotificationProvider` и типизированный API для success, error, warning, info и blocking-modal сообщений.
2. Все пользовательские notification-сообщения строятся из i18n keys или backend mnemonic-кодов `STR_MNEMO_*`; hardcoded user-facing строки в React-компонентах не допускаются.
3. Повторяющиеся уведомления с одинаковым `dedupeKey` заменяются или группируются, чтобы offline/retry и validation errors не создавали поток одинаковых toast.
4. Критичные сообщения доступны с клавиатуры, имеют корректный focus management, `aria-live` или modal role и не исчезают раньше минимального времени чтения.
5. Notification payload содержит severity, titleKey или messageCode, optional params, sourceRoute, correlationId и timestamp; персональные и секретные данные в payload не помещаются.
6. Сбой notification rendering не ломает основной пользовательский сценарий и фиксируется как техническая ошибка frontend без показа пустого toast.

## Критерии offline handling
1. Frontend централизованно отслеживает `online/offline` состояние браузера и состояние последнего API reconnect через `OfflineStatusProvider`.
2. При потере соединения показывается offline popup с test id `platform-offline-popup`, локализованным текстом и признаком affected action, если пользователь был в процессе операции.
3. После восстановления соединения показывается reconnect notification с test id `platform-reconnect-notification`, а зависшие idempotent-запросы могут быть повторены только по явному безопасному правилу.
4. Неидемпотентные операции, включая оплату, оформление заказа, претензию, дозаказ и изменение прав, не повторяются автоматически без подтвержденного backend idempotency key.
5. Offline state не скрывает уже загруженные данные, но блокирует новые действия, которым требуется сеть, и показывает локализованную причину через i18n.
6. Offline/reconnect события фиксируются в аналитике как технические события без персональных данных и без блокировки пользовательского интерфейса.

## Критерии i18n
1. Все новые user-facing строки уведомлений, offline popup, consent states, analytics diagnostics, validation feedback и кнопок добавлены во все поддерживаемые frontend dictionaries `resources_*.ts`.
2. Backend не возвращает predefined user-facing текст во frontend; для предопределенных сообщений используется только `STR_MNEMO_*`.
3. Все новые backend mnemonic-коды, которые могут попасть во frontend, добавлены во все поддерживаемые frontend dictionaries в той же задаче.
4. Переключение языка применяет новые переводы к текущим notification, modal, offline popup и consent UI без reload страницы.
5. Missing i18n key в dev/test окружениях явно диагностируется, а в production показывает безопасный fallback без mojibake и без пустой строки.
6. React-компоненты не типизируют возвращаемое значение как `JSX.Element`; используется inference или `ReactElement` из `react`.

## Критерии analytics и marketing pixels
1. Frontend предоставляет единый `AnalyticsProvider` и `trackEvent` API для pageview, conversion, consent, notification, offline и error-событий.
2. Yandex Metrika, Mindbox и Hybrid pixel подключаются только через централизованный adapter layer; feature-компоненты не вызывают SDK пикселей напрямую.
3. Pageview отправляется при изменении route после восстановления session context и содержит route, role, catalogCode, campaignCode и безопасные технические идентификаторы, если они доступны.
4. Conversion events поддерживают регистрацию, invite переход, add-to-cart, checkout, reorder, claim creation, offline sale, partner action и employee support action.
5. События отправляются только после разрешения соответствующей consent category: functional, analytics или marketing.
6. Если consent запрещает analytics или marketing, маркетинговые пиксели не получают pageview/conversion payload, а техническое событие отказа фиксируется локально без передачи в запрещенный канал.
7. Ошибка одного analytics adapter не блокирует остальные adapters и не ломает пользовательский flow; ошибка фиксируется в diagnostics с channel, eventCode, reasonCode и timestamp.
8. Analytics payload не содержит пароли, токены, полные платежные данные, документы, полные адреса или лишние персональные данные.

## Критерии backend contract и audit
1. Если backend формирует предопределенные сообщения для frontend, он возвращает mnemonic-коды `STR_MNEMO_NOTIFICATION_*`, `STR_MNEMO_OFFLINE_*`, `STR_MNEMO_I18N_*` или `STR_MNEMO_ANALYTICS_*`.
2. Backend API для runtime config analytics/consent возвращает только структурированные channel flags, counter ids, environment mode, consent defaults и diagnostic flags без локализованного текста.
3. Runtime classes backend располагаются в role-specific subpackages внутри `impl`; DTO находятся в `api`, JPA entities и repositories в `domain`, Liquibase XML changelog в `db`.
4. Для feature #25 создается отдельный Liquibase XML changelog в owning module, если требуется хранение consent preferences, notification preferences или analytics diagnostics.
5. Audit trail фиксирует изменение consent preferences, включение или отключение analytics channel, критичные notification failures и попытки отправки запрещенного consent-события.
6. Swagger/OpenAPI endpoint-ы owning module появляются в runtime группе monolith module автоматически через Spring MVC controllers, без ручной регистрации endpoint lists.

## Критерии frontend UX и test ids
1. Root application shell содержит providers в порядке, который позволяет notification, offline, i18n и analytics работать на публичных, customer, partner, employee и admin маршрутах.
2. UI содержит test ids: `platform-notification-root`, `platform-offline-popup`, `platform-reconnect-notification`, `platform-consent-panel`, `platform-analytics-diagnostics`, `platform-language-switcher`.
3. Consent panel позволяет включить и отключить analytics/marketing categories, сохраняет выбор и немедленно применяет его к новым событиям.
4. Analytics diagnostics доступны только роли администратора трекинга или employee/admin с нужной policy; обычные пользователи не видят технические счетчики и ключи.
5. Offline popup и notification root не перекрывают критичные формы так, чтобы пользователь потерял введенные данные.
6. UI сохраняет читаемость на desktop и mobile viewport, а длинные локализованные строки не выходят за границы popup, modal и toast.

## Критерии тестирования
1. Managed API test в `agents/tests/api/feature_025_уведомления_офлайн_i18n_и_аналитика/FeatureApiTest.java` начинается с логина пользователя и проверяет runtime config, consent preferences, mnemonic-коды и отсутствие hardcoded user-facing текста в backend responses.
2. Managed UI test в `agents/tests/ui/feature_025_уведомления_офлайн_i18n_и_аналитика/feature_ui_test.spec.ts` начинается с логина пользователя и проверяет notification root, offline popup, reconnect notification, language switcher, consent panel и analytics diagnostics.
3. UI test перехватывает analytics calls и проверяет, что pageview/conversion отправляются после consent и не отправляются при запрете category.
4. End-to-end managed tests агрегируют реальные managed feature tests, включая feature #25, а не используют placeholder assertions по id, имени фичи или имени файла.
5. Runtime-копии тестов синхронизированы из `agents/tests/` по `agents/tests/targets.yml`, содержат marker comment и совпадают с canonical source после учета marker-обертки.
6. Перед завершением workflow backend и frontend запускаются, feature API/UI проверки выполняются или фиксируется технический blocker без создания пустых файлов.

## Версионная база
Критерии основаны на версии платформенной базы на 27.04.2026: Java/Spring Boot/Maven monolith, Liquibase XML, Hibernate, MapStruct, Lombok, PostgreSQL, frontend TypeScript/React/Ant Design, обязательные i18n dictionaries, backend package policy `api/domain/db/impl`, frontend React Type Policy и backend-to-frontend message contract через `STR_MNEMO_*`. Новые технологические версии не вводятся без отдельного compatibility decision.
