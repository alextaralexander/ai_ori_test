# Feature 011. Module order ER diagram description

## Назначение
ER-диаграмма описывает расширение module `order` для истории и деталей заказов. Фича не вводит отдельный сервис истории: все данные принадлежат bounded context `order`, а runtime-код должен оставаться в пакетах `api/domain/impl/db` текущего backend module.

## Основные сущности
- `order_order` остается центральной сущностью заказа и используется как источник ownership, типа заказа, campaign context, платежного и доставочного статусов.
- `order_history_item_snapshot` хранит снимок строк заказа для чтения истории: товар, SKU, количество, цены, признак подарка, доступность repeat order и claim eligibility.
- `order_history_delivery_snapshot` хранит безопасный снимок доставки с маскированными контактами, пунктом выдачи, адресной строкой и tracking number.
- `order_history_payment_snapshot` хранит платежное состояние без закрытых реквизитов и token; action availability используется для продолжения pending payment.
- `order_history_event` хранит публичный timeline событий заказа с `description_mnemo`, чтобы frontend локализовал предопределенные описания.
- `order_history_warning` хранит активные предупреждения по заказу через `STR_MNEMO_*`.
- `order_repeat_request` фиксирует попытки repeat order и idempotency key.
- `order_audit_event` используется для support-просмотра, security denial и repeat order action.

## Правила владения пакетов
JPA entities и repository interfaces размещаются только в `domain`. Контроллеры, сервисы, валидаторы, mappers и security checks размещаются в `impl/<role>` subpackages. Liquibase XML changelog feature #11 размещается в owning module `db`/resources changelog path module `order`.

## Версионная база
Новые технологии не вводятся. Используется текущий Spring Boot monolith, Java/Maven baseline, Hibernate/Liquibase XML baseline и существующий модульный подход Best Ori Gin.
