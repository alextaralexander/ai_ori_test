# Feature 023. Сотрудник: настройки и super-user

## Назначение
Воспроизвести employee profile settings и режимы super-user/impersonation для сервисных операций.

## Покрываемые маршруты
- `/employee/profile-settings`
- `/employee/profile-settings/general`
- `/employee/profile-settings/contacts`
- `/employee/profile-settings/addresses`
- `/employee/profile-settings/documents`
- `/employee/profile-settings/security`
- `/employee/super-user`

## Основной функционал
- Настройки профиля сотрудника.
- Управление security-параметрами и служебными документами.
- Включение/выход из super-user режима.
- Дополнительные guard rails, audit trail и таймауты сессии.
- Связка с impersonation и partner search.

## Роли
- Сотрудник
- Супервизор
- Администратор безопасности
