# Acceptance criteria. Feature 026. Админ: RBAC и учетные записи

## Обязательные критерии доступа и безопасности
1. Backend предоставляет admin RBAC API для управления внутренними учетными записями, ролями, permission sets, responsibility scopes, политиками паролей, MFA, сессий, служебными учетными записями и audit trail.
2. Каждый admin endpoint проверяет authenticated session, роль, permission set, responsibility scope, elevated session/MFA requirement и emergency deactivation policy; пользователь без доступа получает HTTP 403 и mnemonic-код `STR_MNEMO_ADMIN_RBAC_ACCESS_DENIED`.
3. Backend не возвращает predefined user-facing текст во frontend; все ошибки, предупреждения и статусы передаются как mnemonic-коды с префиксом `STR_MNEMO_`.
4. Пароли, MFA-секреты, session secrets, service account secrets и одноразовые recovery codes никогда не возвращаются во frontend и не сохраняются в audit payload в открытом виде.
5. Все изменения учетных записей, ролей, permission sets, scopes, политик, служебных учетных записей, блокировок и аварийных деактиваций фиксируются в audit trail с actorUserId, targetUserId или serviceAccountId, actionCode, oldValue, newValue, sourceIp, correlationId и occurredAt.
6. Операции назначения admin-ролей, изменения security policy, создания или ротации служебной учетной записи и emergency deactivation требуют active elevated session или MFA challenge.

## Критерии управления внутренними учетными записями
1. Суперадмин или разрешенный HR/операционный администратор может создать внутреннюю учетную запись со статусом `DRAFT` или `ACTIVE`, обязательными полями fullName, email, department, position и accountType.
2. Email внутренней учетной записи уникален; повторное создание с тем же email возвращает HTTP 409 и `STR_MNEMO_ADMIN_RBAC_ACCOUNT_ALREADY_EXISTS`.
3. Учетная запись поддерживает статусы `DRAFT`, `ACTIVE`, `BLOCKED`, `ACCESS_EXPIRING`, `DEACTIVATED`; переходы статусов валидируются backend policy.
4. Блокировка учетной записи завершает активные session tokens и запрещает новый login до разблокировки.
5. Деактивация учетной записи сохраняет audit trail и историю назначений, но запрещает дальнейшее назначение новых ролей и permission sets.
6. Изменение подразделения, должности, контактов и даты окончания доступа доступно только actor-у с разрешенным scope и фиксируется отдельным audit-событием.

## Критерии ролей, permission sets и зон ответственности
1. Backend хранит roles и permission sets как отдельные управляемые сущности; итоговая матрица доступа пользователя вычисляется из активных ролей, активных permission sets и responsibility scopes.
2. Роль содержит code, name, description, moduleAccess, active flag и список permission set bindings.
3. Permission set содержит code, permissions, riskLevel, active flag и признак необходимости elevated session для применения.
4. Responsibility scope может ограничивать доступ по regionId, warehouseId, pickupPointId, catalogId, productCategoryId, departmentId или partnerStructureSegmentId.
5. Попытка назначить role или permission set вне scope actor-а возвращает HTTP 403 и `STR_MNEMO_ADMIN_RBAC_SCOPE_DENIED`.
6. Попытка назначить неактивную роль, истекший permission set или конфликтующий набор разрешений возвращает HTTP 409 и `STR_MNEMO_ADMIN_RBAC_PERMISSION_CONFLICT`.
7. Перед сохранением frontend может запросить preview effective permissions; preview возвращает effectivePermissions, conflicts, requiredMfa, affectedModules и auditPreview без изменения данных.

## Критерии password, MFA и session policies
1. Password policy поддерживает минимальную длину, запрет повторного использования, срок действия, lockout threshold и reset requirement для внутренних пользователей.
2. MFA policy поддерживает required roles, required risk levels, allowed methods и grace period; отключение MFA для admin-роли запрещено без elevated session.
3. Session policy поддерживает idle timeout, absolute timeout, max concurrent sessions и forced logout при блокировке или emergency deactivation.
4. Изменение любой policy возвращает актуальную версию policy и auditRecorded=true.
5. Некорректные значения policy возвращают HTTP 400 и `STR_MNEMO_ADMIN_RBAC_POLICY_INVALID`.

## Критерии служебных учетных записей
1. Суперадмин или администратор безопасности может создать service account с ownerUserId, integrationType, permission scopes, expiresAt и allowedIpRanges.
2. Secret служебной учетной записи показывается frontend только один раз при создании или ротации; далее backend возвращает только maskedSecretHint и lastUsedAt.
3. Ротация secret инвалидирует предыдущий secret после configurable overlap window и фиксирует `ADMIN_SERVICE_ACCOUNT_ROTATED`.
4. Отключенная или истекшая служебная учетная запись получает HTTP 401 или 403 с `STR_MNEMO_ADMIN_RBAC_SERVICE_ACCOUNT_DISABLED`.
5. Служебная учетная запись не может выполнять интерактивный login и не может запускать имперсонацию.

## Критерии audit trail
1. Audit trail доступен только суперадмину, администратору безопасности и аудитору с соответствующим permission set.
2. Поиск audit trail поддерживает фильтры actorUserId, targetUserId, serviceAccountId, roleCode, permissionSetCode, actionCode, dateFrom, dateTo и correlationId.
3. Audit response содержит immutable eventId, actionCode, actor, target, diff, source, sourceIp, correlationId и occurredAt.
4. Audit trail нельзя редактировать или удалять через admin UI и admin API.
5. Экспорт audit trail возвращает файл или export job id без раскрытия секретов, паролей, MFA seed и токенов.

## Критерии frontend и i18n
1. Admin RBAC UI содержит разделы учетных записей, ролей, permission sets, responsibility scopes, security policies, service accounts и audit trail.
2. Frontend строит доступность admin-разделов и действий только по permission matrix из session context или admin RBAC API, а не по hardcoded role names.
3. Все новые пользовательские строки для заголовков, таблиц, фильтров, форм, validation messages, кнопок, статусов, предупреждений, modal confirmations, empty states и audit diff вынесены в текущие frontend i18n dictionaries для всех поддерживаемых языков.
4. UI использует mnemonic-коды `STR_MNEMO_ADMIN_RBAC_*`, полученные от backend, и разрешает их через i18n dictionaries.
5. React-компоненты не типизируют возвращаемое значение как `JSX.Element`; используется inference или `ReactElement` из `react`.
6. UI содержит test ids: `admin-rbac-page`, `admin-rbac-account-table`, `admin-rbac-account-form`, `admin-rbac-role-assignment`, `admin-rbac-permission-preview`, `admin-rbac-security-policy`, `admin-rbac-service-account-table`, `admin-rbac-audit-table`, `admin-rbac-emergency-deactivate`.
7. UI показывает loading, forbidden, validation, conflict, saved, blocked, deactivated, secret one-time display, audit empty state и emergency confirmation states без hardcoded user-facing строк.

## Критерии backend contract и хранения данных
1. DTO находятся в `api`, JPA entities и repository interfaces находятся в `domain`, Liquibase XML changelog находится в `db`, runtime controller/service/mapper/validator/security/audit classes находятся в role-specific subpackages внутри `impl`.
2. Swagger/OpenAPI endpoint-ы admin RBAC module появляются в runtime группе monolith module автоматически через Spring MVC controllers, без ручной регистрации списков endpoint-ов.
3. Для фичи создается отдельный Liquibase XML changelog в owning module `admin-rbac`; изменения не добавляются в общий changelog другой фичи.
4. Persisted модель должна поддерживать internal account, role, permission set, role-permission binding, account-role assignment, responsibility scope, security policy, service account, service account secret rotation metadata и audit event.
5. API возвращает предсказуемые HTTP-коды: 200 для чтения и preview, 201 для создания account, role, permission set или service account, 204 для блокировки/деактивации без тела, 400 для validation errors, 401 для отсутствующей/истекшей session, 403 для запрета доступа, 404 для разрешенного пользователя при отсутствии target, 409 для конфликтов доступа.
6. Backend-to-frontend predefined messages используют только mnemonic-коды: `STR_MNEMO_ADMIN_RBAC_ACCESS_DENIED`, `STR_MNEMO_ADMIN_RBAC_ACCOUNT_ALREADY_EXISTS`, `STR_MNEMO_ADMIN_RBAC_SCOPE_DENIED`, `STR_MNEMO_ADMIN_RBAC_PERMISSION_CONFLICT`, `STR_MNEMO_ADMIN_RBAC_POLICY_INVALID`, `STR_MNEMO_ADMIN_RBAC_SERVICE_ACCOUNT_DISABLED`.

## Критерии тестирования
1. Managed API test в `agents/tests/api/feature_026_админ_rbac_и_учетные_записи/FeatureApiTest.java` начинается с логина суперадмина и проверяет account create, role assignment, permission preview, block/deactivate, policy update, service account rotate, audit search, forbidden и validation сценарии.
2. Managed UI test в `agents/tests/ui/feature_026_админ_rbac_и_учетные_записи/feature_ui_test.spec.ts` начинается с логина суперадмина и проверяет таблицу учетных записей, форму сотрудника, назначение ролей, preview прав, security policy, service account secret one-time display, audit trail и emergency deactivation.
3. End-to-end managed tests агрегируют реальные managed feature tests admin-потока, включая feature #26, а не используют placeholder assertions по id или имени фичи.
4. Runtime-копии тестов синхронизированы из `agents/tests/` по `agents/tests/targets.yml`, содержат marker comment и совпадают с canonical source после учета marker-обертки.
5. Перед завершением workflow backend и frontend запускаются, feature API/UI проверки выполняются или фиксируется технический blocker без создания пустых файлов.
