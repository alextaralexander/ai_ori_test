# Acceptance criteria. Feature 023. Сотрудник: настройки и повышенные права

## Обязательные критерии доступа и безопасности
1. Маршруты `/employee/profile-settings`, `/employee/profile-settings/general`, `/employee/profile-settings/contacts`, `/employee/profile-settings/addresses`, `/employee/profile-settings/documents`, `/employee/profile-settings/security` доступны только авторизованным внутренним пользователям с employee-ролью; гость получает HTTP 403 и mnemonic-код `STR_MNEMO_EMPLOYEE_ACCESS_DENIED`.
2. Маршрут `/employee/super-user` доступен только сотрудникам с policy `EMPLOYEE_ELEVATED_ACCESS_ALLOWED` или супервайзерам, которые управляют запросами; остальные employee-роли получают `STR_MNEMO_EMPLOYEE_SUPER_USER_FORBIDDEN`.
3. Backend проверяет actorUserId, employeeId, роль, scope подразделения, MFA-статус, активные блокировки и policy перед каждым чтением или изменением настроек.
4. Backend не возвращает predefined user-facing текст во frontend; все ошибки, предупреждения и публичные сообщения передаются как mnemonic-коды с префиксом `STR_MNEMO_`.
5. Каждое изменение профиля, контакта, адреса, документа, security-настройки и elevated mode фиксируется в audit trail с actorUserId, targetEmployeeId, sourceRoute, actionCode, policyCode, elevatedSessionId, correlationId и occurredAt.
6. Секретные значения, токены MFA, полные номера документов и чувствительные security-поля не возвращаются во frontend и не пишутся в audit payload в открытом виде.

## Критерии разделов employee profile settings
1. `/employee/profile-settings` возвращает summary employee-профиля, список доступных разделов, readiness-флаги по general, contacts, addresses, documents, security и активные security warnings.
2. Раздел general поддерживает чтение и обновление displayName, jobTitle, departmentCode, preferredLanguage, timezone, notificationChannel и employeeStatus в пределах разрешенных policy.
3. Раздел contacts поддерживает список контактов с типом, maskedValue, primary flag, verificationStatus и allowedActions; добавление или изменение контакта требует валидного типа и непустого значения.
4. Раздел addresses поддерживает office, pickupPoint, remoteWork и legal адреса с regionCode, city, addressLine, postalCode, active flag и периодом действия.
5. Раздел documents поддерживает metadata документов: documentId, documentType, maskedNumber, issuedAt, expiresAt, verificationStatus, linkedPolicyCode и fileReferenceId; загрузка файла выполняется только через существующий S3/MinIO слой.
6. Раздел security возвращает MFA-статус, дату последней смены пароля, активные сессии, последние security-события, доступные действия и flags риска без раскрытия секретов.
7. Некорректные значения профиля, контактов, адресов или документов отклоняются HTTP 400 с точным mnemonic-кодом: `STR_MNEMO_EMPLOYEE_PROFILE_INVALID`, `STR_MNEMO_EMPLOYEE_CONTACT_INVALID`, `STR_MNEMO_EMPLOYEE_ADDRESS_INVALID`, `STR_MNEMO_EMPLOYEE_DOCUMENT_INVALID`.
8. Успешное обновление любого раздела возвращает актуальную версию раздела, `auditRecorded=true`, `updatedAt` и `version`, чтобы frontend мог обработать optimistic lock.

## Критерии повышенных прав
1. `/employee/super-user` показывает сотруднику список доступных elevated policies, текущее состояние elevated mode, обязательные условия, максимальный срок действия и историю последних сессий.
2. Создание elevated-запроса требует policyCode, reasonCode, reasonText, requestedDurationMinutes, targetScope и при необходимости linkedDocumentId.
3. Если policy требует подтверждения, запрос получает статус `PENDING_SUPERVISOR_APPROVAL`; повышенный режим не активируется до одобрения супервайзером.
4. Если policy разрешает self-activation, backend активирует elevated mode только при валидном MFA, отсутствии блокировок и сроке не выше максимального значения policy.
5. Супервайзер может одобрить, отклонить или отозвать elevated-запрос; каждое действие возвращает актуальный статус и audit event.
6. Активная elevated-сессия содержит elevatedSessionId, policyCode, scope, startedAt, expiresAt, remainingSeconds, approvedBy и allowedLinkedOperations.
7. Истекшая, отозванная или завершенная elevated-сессия не может использоваться для impersonation, сервисных операций или доступа к расширенным данным.
8. Попытка включить elevated mode без MFA, вне policy, на слишком долгий срок или с недопустимым scope возвращает HTTP 403 или 400 с `STR_MNEMO_EMPLOYEE_ELEVATED_POLICY_DENIED` либо `STR_MNEMO_EMPLOYEE_ELEVATED_REQUEST_INVALID`.

## Критерии frontend и i18n
1. Все новые пользовательские строки маршрутов, меню, вкладок, форм, validation messages, статусов, предупреждений, кнопок, empty states и таблиц вынесены в текущие frontend i18n dictionaries для всех поддерживаемых языков.
2. React-компоненты не типизируют возвращаемое значение как `JSX.Element`; используется inference или `ReactElement` из `react`.
3. UI содержит test ids: `employee-profile-settings-page`, `employee-profile-general-form`, `employee-profile-contacts-list`, `employee-profile-addresses-list`, `employee-profile-documents-list`, `employee-profile-security-panel`, `employee-super-user-page`, `employee-super-user-policy-list`, `employee-super-user-request-form`, `employee-elevated-session-banner`.
4. UI показывает loading, empty, validation error, forbidden, pending approval, active elevated mode, expired session и success states без hardcoded user-facing строк.
5. Навигация между `/employee/profile-settings/*` сохраняет активный раздел, не теряет несохраненные изменения без предупреждения и корректно восстанавливает данные после reload.
6. Индикатор активного elevated mode виден на `/employee/super-user` и в связанных employee flows, пока elevated session активна.

## Критерии backend contract и хранения данных
1. DTO находятся в `api`, JPA entities и repository interfaces находятся в `domain`, Liquibase XML changelog находится в `db`, runtime controller/service/mapper/validator/security/audit classes находятся в role-specific subpackages внутри `impl`.
2. Swagger/OpenAPI endpoint-ы employee module появляются в runtime группе monolith module автоматически через Spring MVC controllers, без ручной регистрации списков endpoint-ов.
3. Для фичи создается отдельный Liquibase XML changelog в owning module `employee`; изменения не добавляются в общий changelog другой фичи.
4. Persisted модель должна поддерживать employee profile settings, contacts, addresses, documents metadata, security events, elevated access request, elevated access session и audit links.
5. Все операции обновления используют optimistic versioning или эквивалентный механизм защиты от потери данных при параллельном редактировании.
6. API возвращает предсказуемые HTTP-коды: 200 для чтения, 201 для создания запроса или документа metadata, 204 для завершения elevated session без тела, 400 для validation errors, 403 для запрета доступа, 404 для разрешенного пользователя при отсутствии записи, 409 для конфликта версии.

## Критерии тестирования
1. Managed API test в `agents/tests/api/feature_023_сотрудник_настройки_и_повышенные_права/FeatureApiTest.java` начинается с логина employee-пользователя и проверяет чтение/обновление profile settings, контактов, адресов, документов, security-раздела, запрос elevated mode, approval/revoke и forbidden/validation сценарии.
2. Managed UI test в `agents/tests/ui/feature_023_сотрудник_настройки_и_повышенные_права/feature_ui_test.spec.ts` начинается с логина employee-пользователя, проходит маршруты настроек, сохраняет изменения, открывает `/employee/super-user`, создает запрос и проверяет active/pending state.
3. End-to-end managed tests агрегируют реальные managed feature tests employee-потока, включая feature #23, а не используют placeholder assertions по id или имени фичи.
4. Runtime-копии тестов синхронизированы из `agents/tests/` по `agents/tests/targets.yml`, содержат marker comment и совпадают с canonical source после учета marker-обертки.
5. Перед завершением workflow backend и frontend запускаются, feature API/UI проверки выполняются или фиксируется технический blocker без создания пустых файлов.