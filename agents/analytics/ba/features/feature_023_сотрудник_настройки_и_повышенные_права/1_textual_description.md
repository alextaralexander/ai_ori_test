# Feature 023. Сотрудник: настройки и повышенные права

## Назначение
Воспроизвести employee profile settings и контролируемые режимы повышенных прав для внутреннего пользователя платформы.

## Покрываемые маршруты
- `/employee/profile-settings`
- `/employee/profile-settings/general`
- `/employee/profile-settings/contacts`
- `/employee/profile-settings/addresses`
- `/employee/profile-settings/documents`
- `/employee/profile-settings/security`
- `/employee/super-user`

## Основной функционал
- Управление собственным employee-профилем и реквизитами.
- Настройки контактов, документов, безопасности и адресов.
- Режим повышенных прав с переключением полномочий в пределах допустимых политик.
- Просмотр внутренних политик доступа, security-событий и истории повышений прав.
- Связка с impersonation, сервисными операциями и аудитом.

## Роли
- Сотрудник
- Супервайзер
- Сотрудник с расширенными правами
