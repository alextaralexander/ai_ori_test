# Feature 008. Описание sequence diagram

## Участники
- Гость / кандидат: пользователь, который открывает invite route, заполняет регистрацию и проходит активацию.
- Спонсор/партнер: авторизованный партнер, который открывает sponsor cabinet, создает invite и сопровождает кандидата.
- Frontend web-shell: React/TypeScript приложение, которое обрабатывает routes `/invite/beauty-partner-registration`, `/invite/business-partner-registration`, `/invite/partners-activation`, `/invite/sponsor-cabinet`.
- Frontend i18n: словари локализации, через которые разрешаются labels, CTA, statuses, errors и mnemonic-коды `STR_MNEMO_*`.
- `partner-onboarding API`: backend module `partner-onboarding`, который валидирует invite/referral-коды, создает заявки, выполняет activation flow и обслуживает sponsor cabinet.
- `partner-onboarding data`: таблицы invite, registration application, activation token, partner profile, referral link и onboarding events.
- CRM / marketing integration: внешний или внутренний контур получения registration lead events.
- Auth/session: текущая security-модель для sponsor cabinet и будущего партнерского доступа.

## Основной поток регистрации
1. Гость открывает invite registration route с optional `code`.
2. Frontend вычисляет `onboardingType` по route и вызывает `GET /api/partner-onboarding/invites/validate`.
3. Backend проверяет invite в `partner-onboarding data`: status, `expires_at`, `campaign_id`, `onboarding_type`, sponsor attribution.
4. Если invite активен, backend возвращает `InviteValidationResponse` с public sponsor context.
5. Если invite не найден, истек, отключен или не подходит типу onboarding, backend возвращает controlled status и `messageCode`, не возвращая hardcoded user-facing text.
6. Frontend разрешает тексты формы и статусы через i18n dictionaries и показывает пошаговую регистрацию.
7. Пользователь заполняет данные, контакт и согласия.
8. Frontend отправляет `POST /api/partner-onboarding/registrations` с `Idempotency-Key`.
9. Backend проверяет duplicate `contact_hash`, attribution conflict и создает registration application.
10. Backend записывает onboarding event и отправляет registration lead event в CRM.
11. Если CRM недоступна, backend сохраняет заявку, ставит событие в retry/audit контур и не показывает пользователю техническую ошибку.
12. Frontend показывает следующий шаг: confirmation, CRM review или activation.

## Поток активации
1. Приглашенный партнер открывает `/invite/partners-activation` с activation token.
2. Frontend вызывает `GET /api/partner-onboarding/activations/{token}`.
3. Backend проверяет hash token, срок действия, application status и доступность активации.
4. Пользователь подтверждает контакт через `POST /activations/{token}/confirm-contact`.
5. Пользователь принимает актуальные партнерские условия.
6. Frontend вызывает `POST /activations/{token}/complete` с `Idempotency-Key`.
7. Backend создает `partner_profile`, генерирует `partner_referral_link`, записывает `PARTNER_ACTIVATED` и `REFERRAL_LINK_CREATED`.
8. Frontend показывает partner number, начальный статус и personal referral link.

## Поток sponsor cabinet
1. Спонсор открывает `/invite/sponsor-cabinet`.
2. Frontend получает текущий security context и вызывает `GET /api/partner-onboarding/sponsor-cabinet/invites`.
3. Backend проверяет роль и выбирает invite только по текущему `sponsor_partner_id`, не принимая sponsor id из клиента.
4. Frontend показывает список invite, статусы, безопасный candidate progress и действия.
5. Для создания invite frontend вызывает `POST /sponsor-cabinet/invites` с `Idempotency-Key`.
6. Backend проверяет право партнера, активность кампании, создает invite code, срок действия и event `INVITE_CREATED`.
7. Для повторной отправки frontend вызывает `POST /sponsor-cabinet/invites/{inviteId}/resend`; backend не создает дубль invite и возвращает существующую ссылку или controlled messageCode.

## Ошибки и устойчивость
- Неверный invite/referral-код не назначает sponsor attribution и возвращает mnemonic `STR_MNEMO_INVITE_CODE_INVALID`.
- Истекший invite возвращает `STR_MNEMO_INVITE_CODE_EXPIRED`.
- Duplicate contact возвращает `STR_MNEMO_REGISTRATION_DUPLICATE_CONTACT` и предлагает вход или восстановление доступа.
- Attribution conflict фиксируется в audit/event stream и возвращает `STR_MNEMO_ATTRIBUTION_CONFLICT`.
- Истекший activation token возвращает controlled state без раскрытия заявки другого пользователя.
- Sponsor cabinet не раскрывает invite другого партнера и возвращает `STR_MNEMO_SPONSOR_CABINET_FORBIDDEN`.
- Backend не передает hardcoded пользовательские сообщения; frontend разрешает mnemonic-коды через dictionaries.

## Версионная база
Новые технологии не вводятся. Взаимодействие реализуется в текущем Spring Boot monolith и React/TypeScript web-shell. OpenAPI формируется springdoc из контроллеров `partner-onboarding`, пользовательские строки остаются во frontend i18n dictionaries, а CRM transport скрывается за runtime service/client abstraction внутри `impl`.
