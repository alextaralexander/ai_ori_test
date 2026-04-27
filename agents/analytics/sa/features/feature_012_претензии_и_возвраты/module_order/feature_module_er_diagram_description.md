# Feature module ER diagram description. Feature 012, module_order

## Назначение
Feature #12 добавляет в module_order сервисный контур претензий и возвратов. Модель отделяет заголовок претензии, позиции, события, комментарии и вложения, чтобы поддержать частичные решения, audit trail и разные исполнительские контуры.

## Сущности
- `order_claim` хранит владельца, номер заказа, статус, запрошенное и утвержденное решение, сумму компенсации, валюту и признак partnerImpact.
- `order_claim_item` хранит выбранные позиции заказа, количество, цену, запрошенное и утвержденное решение по каждой позиции.
- `order_claim_event` фиксирует историю статусов и источник события: customer, service, warehouse, logistics, payment.
- `order_claim_comment` хранит публичные и внутренние комментарии. Во frontend попадают только публичные записи.
- `order_claim_attachment` хранит metadata вложения и публичный token, но не раскрывает приватный storage path.

## Правила владения пакетами
DTO и enums размещаются в `order/api`. Runtime orchestration, service interface, implementation and controllers размещаются в `order/impl/service` и `order/impl/controller`. Domain repository and snapshot structures belong to `order/domain`. Dedicated Liquibase XML for feature #12 belongs to order changelog resources.