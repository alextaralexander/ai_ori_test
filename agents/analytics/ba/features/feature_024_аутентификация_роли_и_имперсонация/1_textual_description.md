# Feature 024. Аутентификация, роли и имперсонация

## Назначение
Выделить сквозную платформенную фичу авторизации, role-based routing, partner search, impersonation и invitation-code состояния.

## Обнаруженные признаки
- Используются route guards `PrivateRoute`, `ProfileRoute`, `EmployeeRoute`.
- В модулях присутствуют `AuthProvider`, `useImpersonate`, `useActivePartner`, `usePartnerSearch`, `useSuperUserMode`, `useInvitationCode`.

## Основной функционал
- Логин/логаут и восстановление сессии.
- Роутинг в зависимости от роли и режима пользователя.
- Переключение активного партнера и impersonation.
- Хранение invitation code и контекста входа.
- Защита критичных действий и аудит impersonation-сессий.

## Роли
- Гость
- Пользователь
- Партнер
- Сотрудник
- Администратор
