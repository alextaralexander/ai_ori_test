# Feature 018. Партнерский офис и логистика

## Назначение
Воссоздать partner-office контур для all orders, отчетов, поставок и детализации supply/order логистики.

## Покрываемые маршруты
- `/partner-office/all-orders`
- `/partner-office/report`
- `/partner-office/supply`
- `/partner-office/supply/:supplyId`
- `/partner-office/supply/orders/:orderId`

## Основной функционал
- Список всех заказов и supply-поставок партнерского офиса.
- Просмотр supply details и заказов внутри поставки.
- Отчетность по офису и логистическим операциям.
- Контроль статусов поставки, комплектности и отклонений.
- Переходы в order/claim workflows по связанным сущностям.

## Роли
- Партнер-офис
- Логистический оператор
- Региональный менеджер
