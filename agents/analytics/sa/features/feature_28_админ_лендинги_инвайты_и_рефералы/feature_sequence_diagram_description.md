# Feature sequence diagram description. Feature 028

## Участники
- `Маркетинг-администратор` управляет landing variants, preview, activation и conversion report.
- `CRM-администратор` управляет registration funnels, referral codes, attribution policy и ручными override.
- `Партнер-спонсор` получает personal referral link в sponsor cabinet.
- `Кандидат` открывает referral link и проходит регистрацию feature #008.
- `Frontend web-shell /admin/referrals` отображает административный UI, локализует mnemonic-коды и не содержит hardcoded user-facing text.
- `Auth/RBAC module` возвращает session context и проверяет permission scopes feature #26.
- `admin-referral API` реализует основной backend module feature #28.
- `admin_ref_* tables` хранят лендинги, funnel rules, codes, attribution, analytics и audit.
- `partner-onboarding API` принимает campaign/referral context в registration flow.
- `platform analytics layer` получает consent-aware conversion events.

## Основной поток
1. Администратор открывает `/admin/referrals`; frontend получает session context, роли, scopes, locale и dictionaries.
2. Frontend загружает landing variants через `GET /api/admin-referral/landing-variants`; backend проверяет `ADMIN_REFERRAL_VIEW`.
3. Маркетинг-администратор создает business landing draft через `POST /landing-variants`; backend валидирует slug, blocks, campaign code и active window.
4. Backend сохраняет landing, version, blocks и audit event в `admin_ref_*` таблицах.
5. Preview собирается через `POST /landing-variants/{landingId}/preview` и не меняет публичную версию.
6. Активация выполняется через `POST /landing-variants/{landingId}/activate`; backend проверяет обязательные blocks и конфликты active window.
7. CRM-администратор создает и активирует registration funnel с consent codes и validation rules.
8. CRM-администратор генерирует referral code через `POST /referral-codes` с `Idempotency-Key`; повторный idempotency key возвращает существующий код.
9. CRM-администратор задает attribution policy через `PUT /attribution-policy`.
10. Sponsor cabinet получает active referral rules и строит personal referral link.
11. Кандидат открывает referral link; frontend и backend фиксируют `LANDING_VIEWED`, сохраняют campaign/referral context и передают его в partner-onboarding.
12. Partner-onboarding вызывает admin-referral для validation referral code и resolve attribution.
13. Admin-referral сохраняет attribution event и conversion event, затем возвращает selected sponsor attribution.
14. Frontend отправляет consent-aware conversion events через analytics layer feature #25.
15. При споре CRM-администратор вызывает `POST /attribution/override`; backend требует `ADMIN_REFERRAL_ATTRIBUTION_OVERRIDE`, reason code и comment.
16. Conversion report строится через `GET /analytics/conversions` и возвращает totals/rows без секретов и лишних персональных данных.

## Ошибки и контракты
- Forbidden access возвращает `403` и mnemonic `STR_MNEMO_ADMIN_REFERRAL_FORBIDDEN`.
- Конфликт slug или active window возвращает `409` и соответствующий `STR_MNEMO_ADMIN_REFERRAL_*`.
- Validation errors возвращаются как structured details и mnemonic-код, frontend берет текст из `resources_ru.ts` и `resources_en.ts`.
- Backend не возвращает hardcoded user-facing text.
- Audit events создаются для создания, изменения, активации, генерации referral code, policy update, attribution override и report-sensitive access.

## Интеграции
- С feature #008 связь идет через registration context, referral code validation и sponsor attribution.
- С feature #007 связь идет через landing type и benefit landing render model.
- С feature #001 связь идет через публичные entrypoints и campaign CTA.
- С feature #25 связь идет через consent-aware analytics и notification/i18n layer.
- С feature #26 связь идет через RBAC scopes и audit identity.

## Версионная база
Взаимодействия рассчитаны на baseline 27.04.2026: Java 25, Spring Boot 4.0.6, Spring MVC/OpenAPI grouping, React/TypeScript/Vite frontend и Ant Design-compatible UI. Новые технологии в sequence flow не вводятся.
