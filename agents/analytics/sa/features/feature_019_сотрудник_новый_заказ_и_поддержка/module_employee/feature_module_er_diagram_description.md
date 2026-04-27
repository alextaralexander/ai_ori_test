# Feature module ER diagram description. Employee

Модуль employee вводит управляемый контур поддержки сотрудников для feature #19. В аналитической модели выделены четыре группы данных: support session, operator order, support action и audit event. Реализация текущего инкремента использует in-memory repository, но Liquibase XML фиксирует целевую структуру таблиц для Postgres.

`employee_support_session` хранит факт начала работы сотрудника с клиентом, партнером или заказом: actorUserId, целевого пользователя, причину обращения и канал. `employee_operator_order` связывает операторский заказ с support session, checkout/order результатом, payment/delivery статусами и idempotency key. `employee_support_action` хранит внутренние заметки, сервисные корректировки и эскалации, включая reasonCode, amount и признак supervisorRequired. `employee_audit_event` фиксирует неизменяемую историю действий сотрудника по связанным сущностям.

Пакетное владение соответствует repository policy: DTO и REST-контракты находятся в `api`, доменная модель и repository-интерфейс — в `domain`, Liquibase XML — в `db`, runtime-классы — в `impl/controller`, `impl/service`, `impl/config` и `impl/exception`.

Версионный baseline на 27.04.2026: Java 25, Spring Boot 4.0.6, Maven build текущего monolith-app, Hibernate/Liquibase целевой baseline без понижения существующих версий репозитория.