# Sequence-описание feature #41. Админ: программы преимуществ и удержания

## Участники
- `Admin Frontend /admin/benefit-programs`: административный UI React/Ant Design с i18n dictionaries.
- `admin-benefit-program`: owning backend module feature #41, package prefix `com.bestorigin.monolith.adminbenefitprogram`.
- `admin-benefit-program db`: таблицы `benefit_program`, `benefit_program_version`, `benefit_program_budget`, `benefit_program_reward`, `benefit_program_manual_adjustment`, `benefit_program_audit_event`, `benefit_program_integration_event`.
- `partnerbenefits`: пользовательский контур feature #40, который показывает опубликованные выгоды партнерам и VIP-клиентам.
- `bonuswallet`: кошелек feature #14 для cashback, adjustment, reversal и expiration events.
- `cart`: корзина feature #9, использующая reservation и free shipping правила.
- `checkout/order`: checkout feature #10 и заказный контур, выполняющие финальную backend-проверку применимости.
- `notification`: notification/i18n contour feature #25.
- `adminplatform`: KPI/audit/integration dashboard feature #36.

## Основной сценарий создания
Администратор программ открывает `/admin/benefit-programs` и создает программу cashback, referral discount, welcome, reward shop, free shipping, reservation, subscription или retention. Frontend отправляет `POST /api/admin/benefit-programs/programs`. Backend проверяет scope, уникальность `code`, период, обязательные настройки выбранного типа, eligibility, compatibility и lifecycle. После успешной проверки модуль создает `benefit_program`, первую `benefit_program_version` и audit event `CREATE`.

Backend возвращает `BenefitProgramResponse` и mnemonic `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_CREATED`. Предопределенный пользовательский текст не возвращается; frontend локализует результат из dictionaries.

## Обновление eligibility и retention settings
CRM/retention manager изменяет eligibility, reservation TTL, subscription renewal или retention trigger через `PUT /programs/{id}` с `If-Match`. Backend использует optimistic locking и создает новую версию правил, не изменяя события и выгоды, уже созданные по прошлой версии. Изменение фиксируется audit event `UPDATE`.

## Dry-run
Администратор запускает `POST /programs/{id}/dry-run`. `admin-benefit-program` читает партнерский сегмент и benefit context из `partnerbenefits`, simulation context из `cart` и preview compatibility из `checkout/order`. Расчет возвращает применимые и отказанные программы, суммы cashback/discount/free shipping, warnings и `correlationId`.

Dry-run не создает:
- financial transaction;
- reservation;
- reward redemption;
- notification event;
- integration event.

Событие `DRY_RUN` сохраняется только в audit trail.

## Финансовый контроль
Финансовый администратор обновляет budget через `PUT /programs/{id}/budgets`. Backend проверяет scope `ADMIN_BENEFIT_PROGRAM_FINANCE`, валидирует валюту, total budget, cashback limit, discount limit, redemption limit и stop policy. Публикация финансово значимых программ должна проверять наличие approval или соответствующего permission.

## Публикация
Администратор меняет статус через `POST /programs/{id}/status` с `If-Match`. Backend проверяет допустимость перехода `DRAFT -> READY_FOR_REVIEW -> SCHEDULED -> ACTIVE -> PAUSED -> ARCHIVED`, validation summary, финансовое approval и scheduled activation. После активации backend обновляет `benefit_program.status/current_version`, создает audit event и integration events для downstream-контуров.

Downstream-публикация:
- `partnerbenefits`: active program version для feature #40.
- `bonuswallet`: cashback, expiration, reversal и manual adjustment policies.
- `cart`: reservation и free shipping rules.
- `checkout/order`: compatibility, max benefit и final validation rules.
- `notification`: triggers активации, истечения, reservation, renewal, redemption и отказов.
- `adminplatform`: KPI/audit projection events.

Все исходящие события имеют idempotency key, payload checksum, target context, retry count и correlationId.

## Ручные корректировки
Финансовый администратор создает корректировку через `POST /programs/{id}/manual-adjustments`. Backend проверяет target user/partner, reasonCode, evidenceRef, лимиты, отсутствие отрицательного баланса и approval policy. После сохранения создается `benefit_program_manual_adjustment`, audit event и идемпотентное downstream-событие в `bonuswallet`; `partnerbenefits` получает trigger перерасчета применимости.

## Аудит и расследование
Аудитор открывает audit и integration tabs. Frontend вызывает:
- `GET /programs/{id}/audit-events`
- `GET /programs/{id}/integration-events`

Backend возвращает события с маскированием персональных данных без секретов, токенов и hardcoded UI-текстов. Ошибки downstream-интеграций возвращаются как `lastErrorMnemonic`, например `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_INTEGRATION_FAILED`.

## Ошибки
- Validation errors: `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_VALIDATION_FAILED`.
- Access denied: `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_ACCESS_DENIED`.
- Not found: `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_NOT_FOUND`.
- Optimistic locking: `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_VERSION_CONFLICT`.
- Invalid status transition: `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_INVALID_STATUS_TRANSITION`.
- Budget exhausted: `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_BUDGET_EXHAUSTED`.

## Версионная база и ownership
Фича стартует 28.04.2026. Новая реализация должна следовать latest-stable baseline, совместимому с текущим монолитом, и фиксировать fallback в архитектурных артефактах при невозможности обновления отдельных технологий. Backend runtime classes размещаются в `impl/<role>` подпакетах; JPA entities и repositories находятся только в `domain`; Liquibase XML changelog находится только в `db`.
