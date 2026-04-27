# Feature sequence diagram description. Feature 023

## Назначение
Sequence описывает runtime-взаимодействие feature #23 между employee frontend, employee module backend, authorization/MFA, S3/MinIO, audit publisher и связанными employee flows. Диаграмма покрывает чтение и обновление настроек сотрудника, создание elevated request, решение супервайзера, использование active elevated session и завершение или отзыв повышенного режима.

## Чтение настроек сотрудника
Сотрудник открывает `/employee/profile-settings`. Frontend вызывает `GET /api/employee/profile-settings`. `EmployeeProfileSettingsController` передает actor context в `EmployeeProfileService`, который проверяет employee actor в authorization/MFA, загружает sections, active elevated session и security warnings из employee DB, записывает audit event `EMPLOYEE_PROFILE_SETTINGS_VIEWED` и возвращает `EmployeeProfileSettingsSummaryResponse`.

## Изменение profile settings
Для general, contacts, addresses, documents и security frontend вызывает соответствующие endpoints `/profile-settings/*`. Service валидирует payload, role/scope/locks и при необходимости MFA. Для documents metadata service дополнительно проверяет `fileReferenceId` через существующий S3/MinIO abstraction. Изменения сохраняются с optimistic version, audit publisher получает actionCode конкретного раздела, а frontend получает updated response или `EmployeeErrorResponse` с `STR_MNEMO_*`.

## Super-user dashboard
Сотрудник открывает `/employee/super-user`. `EmployeeSuperUserController` вызывает `EmployeeElevatedAccessService.loadDashboard`, а service через `EmployeePolicyService` рассчитывает доступные policies, denied codes, max duration и необходимость supervisor approval. Затем service загружает activeSession, pendingRequests и history, записывает `EMPLOYEE_SUPER_USER_DASHBOARD_VIEWED` и возвращает dashboard response.

## Создание elevated request
Frontend отправляет `POST /api/employee/super-user/requests` с policyCode, reasonCode, reasonText, targetScope, requestedDurationMinutes и optional linkedDocumentId. Backend проверяет employee actor, MFA, активные блокировки, policy, максимальный срок, document requirement и scope. Если request невалиден или policy запрещена, backend пишет audit denial и возвращает `STR_MNEMO_EMPLOYEE_ELEVATED_REQUEST_INVALID` или `STR_MNEMO_EMPLOYEE_ELEVATED_POLICY_DENIED`. Если policy требует supervisor approval, request сохраняется со статусом `PENDING_SUPERVISOR_APPROVAL`. Если self activation разрешена, backend создает approved request и active elevated session.

## Решение супервайзера
Супервайзер вызывает approve или reject endpoint для pending request. Service проверяет supervisor role/scope, блокирует request по version/state и записывает решение. При approve создается active elevated session с policyCode, targetScope, startedAt, expiresAt, approvedBy и correlationId. При reject request получает статус `REJECTED`. В обоих случаях audit trail содержит actorUserId супервайзера и decision actionCode.

## Использование elevated session
Связанные employee flows order, claim, partner, delivery и support получают elevatedSessionId и correlationId от frontend. Перед выполнением расширенной операции они вызывают `validateActiveSession`: employee module проверяет status `ACTIVE`, `expiresAt > now`, policy scope и linked operation. Успешное использование записывает audit event `EMPLOYEE_ELEVATED_LINKED_OPERATION_USED`; истекшая или отозванная session возвращает `STR_MNEMO_EMPLOYEE_ELEVATED_SESSION_EXPIRED`.

## Завершение или отзыв elevated mode
Сотрудник или супервайзер вызывает `POST /super-user/sessions/{sessionId}/close`. Service переводит session в `CLOSED` или `REVOKED`, записывает audit event и возвращает HTTP 204. После этого linked flows не могут использовать прежний elevatedSessionId.

## Ошибки и локализация
Все predefined user-facing результаты передаются во frontend только как mnemonic-коды `STR_MNEMO_*`. Frontend обязан разрешать их через i18n dictionaries. Backend не возвращает hardcoded UI text в response body, validation payload или supervisor decision result.

## Версионная база
Sequence соответствует baseline задачи 27.04.2026: Java/Spring Boot/Maven monolith, Spring MVC controllers, springdoc runtime Swagger, PostgreSQL/Liquibase XML, S3/MinIO document storage abstraction, TypeScript/React/Ant Design frontend и обязательный package ownership `api/domain/db/impl`.