# Acceptance criteria. Feature 035. Админ: пользователи, партнеры, сотрудники и имперсонация

## Контекст проверки
Feature #35 считается принятой, если административный identity/master-data модуль позволяет искать, просматривать и безопасно изменять пользователей, партнеров, сотрудников, sponsor relationships, eligibility rules и impersonation policy с контролем RBAC, audit trail, маскированием персональных данных и локализуемым backend-to-frontend контрактом.

## AC-035-01. Единый поиск
- Администратор видит единый поиск по пользователям, партнерам, сотрудникам и организациям.
- Фильтры по subject type, статусу, роли, sponsor code, officeId, employee role, сегменту, security flag и диапазону дат применяются совместно.
- Результаты показывают только поля, разрешенные permission scopes текущего пользователя.
- Пустой результат отображает локализованное empty state из frontend i18n dictionaries.

## AC-035-02. Сводная карточка субъекта
- Карточка показывает identity profile, контакты, роли, статусы, partner data, employee bindings, sponsor relationships, office bindings, eligibility rules, active restrictions и audit trail.
- Для ролей без PII scope телефоны, email, адреса, документы и платежные признаки маскируются.
- Доступные действия зависят от роли, scopes, subject type, текущего статуса и impersonation mode.
- Все вкладки карточки загружаются через typed API contract без hardcoded backend text.

## AC-035-03. Жизненный цикл аккаунта
- Backend поддерживает статусы `ACTIVE`, `PENDING_VERIFICATION`, `SUSPENDED`, `BLOCKED`, `ARCHIVED`.
- Невалидный переход возвращает mnemonic-код `STR_MNEMO_ADMIN_IDENTITY_INVALID_STATUS_TRANSITION`.
- Любой ручной переход требует reasonCode и сохраняет audit event со старым и новым значением.
- Архивирование доступно только при отсутствии активных блокирующих операций, определенных бизнес-правилами.

## AC-035-04. Редактирование master-data
- Разрешенные поля customer/partner/employee profile сохраняются только после серверной валидации формата и business constraints.
- Критичные поля требуют отдельного permission scope и reasonCode.
- Backend не принимает изменение полей, которые недоступны текущей роли или subject type.
- Успешное изменение сохраняет audit event с actorUserId, subjectId, actionCode, changed fields, reasonCode и correlationId.

## AC-035-05. Eligibility rules и сегменты
- Администратор может просматривать и изменять eligibility rules для покупок, партнерских заказов, bonus accrual, offline sales и программ преимуществ.
- Каждое правило имеет область действия, дату начала, дату окончания или признак бессрочности, источник изменения и reasonCode.
- Конфликтующие rules не сохраняются; backend возвращает `STR_MNEMO_ADMIN_IDENTITY_ELIGIBILITY_CONFLICT`.
- Сегменты пользователя или партнера сохраняются с периодом действия и доступны в audit trail.

## AC-035-06. Карточка партнера
- Карточка партнера показывает partner level, sponsor, downline summary, office bindings, offline order limits, bonus status, compliance flags и связанные user account references.
- Partner operations admin может менять только разрешенные партнерские поля и связи.
- Изменение partner status или compliance flag требует reasonCode.
- Любое изменение партнерской карточки не меняет историю заказов, бонусов и выплат задним числом без отдельного approved flow.

## AC-035-07. Sponsor relationships
- Создание или изменение sponsor relationship проверяет существование sponsor и partner subject.
- Backend запрещает циклы, самоспонсорство и связи, нарушающие MLM policy.
- Перенос партнера между командами требует даты вступления в силу, reasonCode и impact preview по downline.
- История sponsor relationships сохраняется версионно и доступна для аудита.

## AC-035-08. Офисы, точки выдачи и offline sales
- Партнер может быть привязан к office, pickup point или service area только при наличии разрешенного статуса партнера.
- Изменение office binding сохраняет дату действия и reasonCode.
- Offline sales limits и блокировки проверяются до партнерского заказа или дозаказа.
- Недопустимая привязка возвращает mnemonic-код `STR_MNEMO_ADMIN_IDENTITY_INVALID_OFFICE_BINDING`.

## AC-035-09. Управление сотрудниками
- Employee admin может создавать и обновлять employee profile, administrative roles, operational scopes, regional scopes и статус доступа.
- Backend проверяет role conflict rules и excessive permissions до сохранения.
- Конфликт ролей возвращает `STR_MNEMO_ADMIN_IDENTITY_ROLE_CONFLICT`.
- Приостановка доступа сотрудника требует причины, области блокировки и срока пересмотра.

## AC-035-10. Security signals
- Security admin видит active sessions, failed login signals, suspicious actions, current restrictions и последние admin actions.
- Emergency block доступен только пользователю с отдельным scope.
- Emergency block сохраняет subjectId, actorUserId, affected scopes, reasonCode, review deadline и correlationId.
- Разблокировка требует отдельного reasonCode и audit event.

## AC-035-11. Impersonation policy
- Security admin может задавать policy по allowed actor roles, target subject types, allowed actions, forbidden actions, max duration, approval requirement и mandatory reason codes.
- Policy не сохраняется, если разрешает платежи, вывод бонусов, смену пароля или необратимые действия без явного elevated scope.
- Любое изменение policy сохраняется в audit trail.
- Frontend отображает policy labels и validation messages только через i18n dictionaries.

## AC-035-12. Impersonation session
- Запуск impersonation session требует actor scope, target subject, reasonCode, policy match и срок действия.
- Во время session интерфейс показывает постоянный banner с actorUserId, targetSubjectId, reasonCode и временем окончания.
- Запрещенные действия блокируются backend и возвращают `STR_MNEMO_ADMIN_IDENTITY_IMPERSONATION_ACTION_FORBIDDEN`.
- Session завершается вручную, по timeout или при emergency block actor/target subject.

## AC-035-13. Audit trail
- Audit search поддерживает фильтры по subjectId, actorUserId, actionCode, reasonCode, old value, new value, date range, affected module и correlationId.
- Audit events для impersonation содержат sessionId, actorUserId, targetSubjectId, reasonCode, viewed area, attempted action и result.
- Audit records недоступны для редактирования и удаления через административный интерфейс.
- Экспорт audit events доступен только пользователю с отдельным permission scope и сохраняет собственный audit event.

## AC-035-14. Маскирование и экспорт
- Экспорт master-data или audit events учитывает текущие фильтры и permission scopes.
- Персональные данные маскируются в export file так же строго, как в UI.
- Экспорт содержит timestamp генерации, идентификатор пользователя, набор фильтров и correlationId.
- Попытка экспорта без scope возвращает `STR_MNEMO_ADMIN_IDENTITY_EXPORT_FORBIDDEN`.

## AC-035-15. RBAC
- Все admin identity endpoints проверяют роль, permission scopes, регион, subject type, action type, impersonation mode и PII access.
- Frontend скрывает недоступные действия, но backend остается источником истины для отказа.
- Отказ по правам возвращает mnemonic-код `STR_MNEMO_ADMIN_IDENTITY_FORBIDDEN_ACTION`.
- Permission checks покрывают чтение, изменение, экспорт, impersonation и emergency actions.

## AC-035-16. Локализация и backend-to-frontend contract
- Все новые user-facing строки frontend вынесены в `resources_ru.ts` и `resources_en.ts`.
- Статусы, role labels, filter labels, validation messages, empty states, policy labels, impersonation banner и audit labels не хардкодятся в React-компонентах.
- Backend не отправляет готовый пользовательский текст для предопределенных ошибок, предупреждений и отказов.
- Все новые backend mnemonic codes имеют префикс `STR_MNEMO_ADMIN_IDENTITY_*` и добавлены во все поддерживаемые frontend languages.

## AC-035-17. Наблюдаемость
- Backend публикует метрики master-data changes, validation failures, blocked actions, impersonation sessions, forbidden impersonation attempts, audit export requests и endpoint latency.
- Логи содержат correlationId для search, profile update, sponsor update, employee update, impersonation session и audit export.
- Ошибки RBAC, validation, sponsor cycle, policy mismatch и export permission различимы по error code.

## AC-035-18. Версионный baseline и storage
- Backend-реализация использует текущий monolith stack Best Ori Gin: Java 25, Spring Boot 4.0.6, Maven, Hibernate, MapStruct, Lombok, Liquibase XML и PostgreSQL.
- Новые Liquibase changesets создаются отдельным XML-файлом owning module `admin-identity`.
- Frontend-реализация использует текущий `frontend/monolith/apps/web-shell` stack: React, TypeScript, Vite и Ant Design.
- Отклонения от baseline фиксируются в архитектурных артефактах и status-файле до реализации.
