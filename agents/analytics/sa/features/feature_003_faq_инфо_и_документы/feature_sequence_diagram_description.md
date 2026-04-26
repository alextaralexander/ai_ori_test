# Feature 003. Описание sequence diagram

Пользователь открывает один из справочных маршрутов frontend: `/FAQ`, `/faq`, `/info/:section` или `/documents/:documentType`. `Web Shell` определяет аудиторию из текущей сессии и вызывает REST API модуля `public-content`.

`PublicContentController` нормализует параметры запроса и передает их в `PublicContentService`. Сервис применяет правила аудитории, фильтрацию категории FAQ, поиск и проверку публикации. Репозиторий возвращает структурированные DTO с i18n-ключами, ссылками на связанные разделы и документами.

Если материал не найден, backend возвращает mnemonic-код через `ErrorResponse`, а frontend показывает локализованное безопасное состояние без раскрытия закрытого или архивного содержимого.

Версионный baseline совпадает с feature module ER-описанием: Java 25, Spring Boot 4.0.6, Maven 3.9.13, React 19.2.0, TypeScript 5.9.3 и Ant Design 6.0.0.
