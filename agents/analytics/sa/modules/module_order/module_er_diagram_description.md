# Module order ER diagram description

Module `order` покрывает checkout, историю заказов, повтор заказа, претензии и возвраты. После feature #12 module-level модель включает `order_claim` как сервисный aggregate, связанный с order history projection по orderNumber. Позиции, события, комментарии и вложения вынесены в отдельные коллекции, чтобы поддерживать частичные решения, audit trail, публичную коммуникацию и безопасную работу с файлами.

Пакетная ownership-модель сохраняется: DTO в `api`, repository/snapshot в `domain`, Liquibase XML в db/changelog owning module, runtime orchestration в `impl/controller` и `impl/service`.