# Acceptance criteria. Feature 024. Аутентификация, роли и имперсонация

## Обязательные критерии доступа и безопасности
1. Backend предоставляет session API для login, logout, восстановления текущей сессии, смены активного партнера, поиска партнеров и управления имперсонацией.
2. Каждый защищенный endpoint проверяет роль, activePartner scope и impersonation state; гость или пользователь без нужной роли получает HTTP 403 и mnemonic-код `STR_MNEMO_AUTH_ACCESS_DENIED`.
3. Backend не возвращает predefined user-facing текст во frontend; все ошибки, предупреждения и публичные сообщения передаются как mnemonic-коды с префиксом `STR_MNEMO_`.
4. Токены, session secrets, MFA-секреты, полные телефоны, email и документы не возвращаются во frontend и не пишутся в audit payload в открытом виде.
5. Все события login, logout, route denied, active partner change, partner search, invitation code store, impersonation start и impersonation finish фиксируются в audit trail с actorUserId, targetUserId, role, route, sourceIp, correlationId и occurredAt.
6. Имперсонация разрешается только сотруднику, супервайзеру или администратору с active elevated mode или policy `AUTH_IMPERSONATION_ALLOWED`; остальные роли получают `STR_MNEMO_AUTH_IMPERSONATION_FORBIDDEN`.

## Критерии session context
1. Login возвращает token, userId, displayName, roles, defaultRoute, доступные route policies, activePartner, invitationCodeState и признак active impersonation.
2. Восстановление сессии по токену возвращает тот же session context без повторного ввода учетных данных.
3. Logout инвалидирует текущую session state на backend и frontend очищает token, role, activePartner, invitationCode и impersonation context.
4. Если session token отсутствует, истек или не распознан, current session endpoint возвращает HTTP 401 с `STR_MNEMO_AUTH_SESSION_EXPIRED`.
5. Role-based defaultRoute формируется предсказуемо: guest получает публичный маршрут, customer - `/profile-settings`, partner - `/business`, employee - `/employee`, supervisor - `/employee/super-user`, admin - `/admin`.
6. Route policies возвращаются в структурированном виде и не содержат локализованных пользовательских текстов.

## Критерии invitation code
1. Frontend сохраняет invitation code из query string или invite-route в session context до регистрации, login или явного logout.
2. Backend валидирует invitation code и возвращает status `VALID`, `EXPIRED`, `USED`, `UNKNOWN` или `NOT_PROVIDED`.
3. Некорректный invitation code не блокирует публичный просмотр, но login/session context содержит предупреждение `STR_MNEMO_AUTH_INVITATION_CODE_INVALID`.
4. После успешной регистрации или login invitation code связывается с userId или partnerId и фиксируется audit-событием `AUTH_INVITATION_CODE_STORED`.

## Критерии active partner и partner search
1. Partner search принимает query минимум из 3 символов и возвращает только партнеров в доступном scope пользователя.
2. Некорректный query возвращает HTTP 400 и `STR_MNEMO_AUTH_PARTNER_SEARCH_INVALID`.
3. Пользователь может установить activePartner только из списка доступных партнеров; чужой partnerId возвращает HTTP 403 и `STR_MNEMO_AUTH_PARTNER_SCOPE_DENIED`.
4. Успешная смена activePartner возвращает обновленный session context, partnerId, personNumber, displayName, roleInStructure, scope и auditRecorded=true.
5. Все последующие frontend API-клиенты передают activePartnerId через заголовок `X-Active-Partner-Id`, если текущая роль работает в partner scope.

## Критерии имперсонации
1. Start impersonation требует targetUserId, reasonCode, reasonText, targetRole, durationMinutes и active elevatedSessionId либо policy allowance.
2. Backend проверяет, что targetUserId существует, targetRole разрешена actor-у, durationMinutes не превышает policy limit, а elevated session активна и не истекла.
3. Успешный start impersonation возвращает impersonationSessionId, actorUserId, targetUserId, targetRole, reasonCode, startedAt, expiresAt и updated session context.
4. Active impersonation явно видна во frontend через баннер с test id `auth-impersonation-banner`; баннер содержит targetUserId, targetRole, reasonCode и кнопку завершения.
5. Finish impersonation закрывает session, возвращает session context исходного actor-а и запрещает дальнейшее использование закрытого impersonationSessionId.
6. Истекшая или завершенная имперсонация не может использоваться для route access и API-запросов; frontend обязан восстановить actor context.
7. Попытка имперсонации без policy, MFA/elevated mode, reason или разрешенного target scope возвращает HTTP 403 или 400 с `STR_MNEMO_AUTH_IMPERSONATION_FORBIDDEN` либо `STR_MNEMO_AUTH_IMPERSONATION_INVALID`.

## Критерии frontend и i18n
1. `AuthProvider` централизует session context, login, logout, current session restore, active partner, invitation code, partner search и impersonation state.
2. Route guards `PrivateRoute`, `ProfileRoute` и `EmployeeRoute` используют session context и role policies, а не прямые localStorage-проверки в компонентах.
3. Hooks `useInvitationCode`, `useActivePartner`, `usePartnerSearch`, `useSuperUserMode` и `useImpersonate` возвращают типизированные команды, loading/error state и не дублируют пользовательские строки.
4. Все новые пользовательские строки маршрутов, форм, validation messages, статусов, предупреждений, кнопок, empty states и баннеров вынесены в текущие frontend i18n dictionaries для всех поддерживаемых языков.
5. React-компоненты не типизируют возвращаемое значение как `JSX.Element`; используется inference или `ReactElement` из `react`.
6. UI содержит test ids: `auth-login-page`, `auth-role-router`, `auth-active-partner-switcher`, `auth-partner-search`, `auth-invitation-code-state`, `auth-impersonation-panel`, `auth-impersonation-banner`, `auth-logout-button`.
7. UI показывает loading, forbidden, expired session, invalid invitation, partner scope denied, active impersonation и success states без hardcoded user-facing строк.

## Критерии backend contract и хранения данных
1. DTO находятся в `api`, JPA entities и repository interfaces находятся в `domain`, Liquibase XML changelog находится в `db`, runtime controller/service/mapper/validator/security/audit classes находятся в role-specific subpackages внутри `impl`.
2. Swagger/OpenAPI endpoint-ы auth module появляются в runtime группе monolith module автоматически через Spring MVC controllers, без ручной регистрации списков endpoint-ов.
3. Для фичи создается отдельный Liquibase XML changelog в owning module `auth`; изменения не добавляются в общий changelog другой фичи.
4. Persisted модель должна поддерживать auth session, route policy snapshot, invitation code state, active partner state, partner search audit и impersonation session.
5. API возвращает предсказуемые HTTP-коды: 200 для чтения и login/logout, 201 для start impersonation, 204 для finish без тела, 400 для validation errors, 401 для истекшей сессии, 403 для запрета доступа, 404 для разрешенного пользователя при отсутствии target, 409 для конфликтного session state.

## Критерии тестирования
1. Managed API test в `agents/tests/api/feature_024_аутентификация_роли_и_имперсонация/FeatureApiTest.java` начинается с логина пользователя и проверяет current session, role routing, invitation code, partner search, active partner switch, start/finish impersonation, forbidden и validation сценарии.
2. Managed UI test в `agents/tests/ui/feature_024_аутентификация_роли_и_имперсонация/feature_ui_test.spec.ts` начинается с логина, проверяет role router, active partner switcher, invitation code state, impersonation panel/banner и logout.
3. End-to-end managed tests агрегируют реальные managed feature tests customer, partner и employee-потоков, включая feature #24, а не используют placeholder assertions по id или имени фичи.
4. Runtime-копии тестов синхронизированы из `agents/tests/` по `agents/tests/targets.yml`, содержат marker comment и совпадают с canonical source после учета marker-обертки.
5. Перед завершением workflow backend и frontend запускаются, feature API/UI проверки выполняются или фиксируется технический blocker без создания пустых файлов.
