# Feature 021. Sequence diagram description

## Создание претензии сотрудником
Сотрудник сервиса открывает `/employee/submit-claim` во frontend web-shell. Frontend отправляет `POST /api/employee/submit-claim` с `Idempotency-Key`, выбранным клиентом или партнером, заказом, основанием обращения, позициями, причиной, ожидаемым решением, вложениями и комментариями. `EmployeeController` извлекает user context и передает команду в `EmployeeService`.

`EmployeeService` проверяет employee-доступ, обязательный `supportReasonCode`, tenant scope, доступность заказа, позиции, quantity, лимит компенсации и idempotency. Для проверки заказа и публичного claim context сервис обращается к order module. После успешной проверки employee module создает `employee_claim_case`, `employee_claim_item`, `employee_claim_attachment` и audit event `EMPLOYEE_CLAIM_CREATED`. Ответ `EmployeeClaimDetailsResponse` возвращает claimNumber, status, SLA, compensation preview, availableActions и только mnemonic-коды `STR_MNEMO_*` для пользовательских сообщений.

## История претензий
Для `/employee/claims-history` frontend вызывает `GET /api/employee/claims` с фильтрами по статусу, периоду, SLA, ответственному, типу решения, источнику, складу, финансовому статусу, query, page, size и sort. `EmployeeService` валидирует фильтры, проверяет employee scope, выбирает page из `employee_claim_case` и связанных таблиц, маскирует контактные данные и записывает `EMPLOYEE_CLAIM_LIST_VIEWED`.

Ответ содержит операционную очередь с claimNumber, orderNumber, customerOrPartnerLabel, maskedContact, status, slaState, slaDueAt, compensationAmount, responsibleRole, assignee и availableActions. Frontend не вычисляет SLA самостоятельно, а использует `slaState` и `slaDueAt`.

## Детальная карточка претензии
Для `/employee/claims-history/:claimId` frontend вызывает `GET /api/employee/claims/{claimId}` с обязательным `supportReasonCode`. `EmployeeService` загружает кейс, позиции, вложения, route tasks и audit trail. При необходимости сервис сверяет order/claim links с order module. Успешный просмотр записывает `EMPLOYEE_CLAIM_DETAILS_VIEWED`.

Ответ детальной карточки содержит summary заказа, клиента или партнера, позиции, вложения metadata без приватных storage paths, компенсацию, supervisorRequired, routeTasks склада, финансов, support и supervisor approval, timeline и audit events. Видимость audit trail зависит от роли: супервизор получает расширенный набор, обычный сотрудник только разрешенный минимум.

## Переходы маршрута
Сотрудник применяет transition через `POST /api/employee/claims/{claimId}/transitions`. Сервис проверяет допустимость перехода по статусу, роли, сумме компенсации, результатам складской проверки и supportReasonCode. Для `SEND_TO_WAREHOUSE_REVIEW` создается складская задача в WMS/1C, для `SEND_TO_FINANCE_REFUND` создается финансовая задача, для `SEND_TO_CUSTOMER_SUPPORT` публикуется nextAction и publicReasonMnemonic в customer support.

Backend обновляет route task, статус кейса, SLA и audit trail. Frontend получает обновленную карточку и не собирает итоговый статус из разрозненных полей.

## Supervisor approval
Если компенсация выше лимита сотрудника, employee module переводит кейс в supervisor approval. Супервизор открывает карточку претензии, применяет transition `APPROVE_COMPENSATION` или `REJECT_CLAIM`. Сервис записывает audit event решения супервизора, обновляет availableActions и при утверждении создает финансовую задачу.

## Ошибки и сообщения
Ошибки доступа возвращают `STR_MNEMO_EMPLOYEE_ACCESS_DENIED`, ошибки валидации создания - `STR_MNEMO_EMPLOYEE_CLAIM_VALIDATION_FAILED`, ошибки фильтров - `STR_MNEMO_EMPLOYEE_CLAIM_FILTER_INVALID`, отсутствие кейса - `STR_MNEMO_EMPLOYEE_CLAIM_NOT_FOUND`, недопустимый transition - `STR_MNEMO_EMPLOYEE_CLAIM_TRANSITION_INVALID`. Все предопределенные сообщения остаются mnemonic-кодами; frontend локализует их через dictionaries.

## Package ownership
Контроллеры остаются в `com.bestorigin.monolith.employee.impl.controller`, сервисы - в `impl.service`, validators - в `impl.validator`, DTO - в `employee.api`, snapshots/repositories - в `employee.domain`, Liquibase XML - в changelog employee и marker package `employee.db`.
