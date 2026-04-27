# Module order ER description after feature 017

Модуль `order` содержит checkout snapshot, историю заказов, претензии и новую проекцию партнерских офлайн-заказов.

Feature 017 добавляет `partner_offline_order` и `partner_offline_order_event`. Проекция хранит business identifiers заказа клиента партнера, статусы оплаты/доставки/бонусов, campaignId, customerId, partnerPersonNumber, сумму и businessVolume. Timeline событий вынесен в `partner_offline_order_event` с внешним ключом на `order_number`.

Индексы для новой проекции: `(campaign_id, partner_person_number)`, `(customer_id)`, `(order_status, payment_status, delivery_status)`. Liquibase changesets остаются XML и разделяются по feature-файлам.