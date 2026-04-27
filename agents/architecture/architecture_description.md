# Architecture description

Архитектура Best Ori Gin остается domain-oriented monolith с frontend web-shell и backend monolith-app. Feature #19 добавляет модуль `employee`, который обслуживает employee workspace, операторское создание заказа и поддержку проблемного заказа. Модуль не нарушает владение существующих доменов: order остается владельцем заказов и претензий, cart — корзины, profile — персональных данных, partner-office — поставок, а employee хранит только support-контекст, ссылки и audit trail действий сотрудников.

Backend package ownership: `com.bestorigin.monolith.employee.api` содержит DTO и REST-контракты; `domain` содержит доменную snapshot-модель и repository-интерфейс; `db` содержит Liquibase marker package и XML changeset; `impl/controller`, `impl/service`, `impl/config`, `impl/exception` содержат runtime-логику. Swagger group формируется через module metadata: `/v3/api-docs/employee` и `/swagger-ui/employee`.

Frontend использует `EmployeeWorkspaceView` и `api/employee.ts`, маршруты `/employee`, `/employee/new-order`, `/employee/order-support`. Все user-facing строки добавлены в `resources_ru.ts` и `resources_en.ts`. Backend возвращает только mnemonic-коды `STR_MNEMO_EMPLOYEE_*` для предопределенных сообщений.

Версионный baseline на 27.04.2026: Java 25, Spring Boot 4.0.6, Maven текущего monolith-app, TypeScript/React/Ant Design текущего web-shell, Liquibase XML, PostgreSQL target schema, Docker/Kubernetes stack без изменений в этом инкременте.