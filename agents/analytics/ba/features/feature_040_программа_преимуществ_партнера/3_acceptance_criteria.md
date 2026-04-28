# Acceptance criteria feature #40. Программа преимуществ партнера

## Функциональные критерии
1. Авторизованный бьюти-партнер или бизнес-партнер открывает `/member-benefits` и видит карточку участия с текущим статусом, активным catalogId, датами каталога, доступными welcome-выгодами, кешбэком, recommendation discount, бесплатной доставкой, наградами и удерживающими офферами.
2. Для каждой выгоды отображаются тип, статус, источник, условия получения, прогресс выполнения, дата начала, дата окончания, оставшийся срок и сценарий применения: cart, checkout, reward shop, wallet или notification-only.
3. Система показывает текущий и следующий catalog period, не смешивает выгоды разных каталогов и не применяет expired, revoked, consumed или suspended выгоды.
4. Прогресс до выгоды пересчитывается после релевантных событий: заказ создан, заказ оплачен, заказ отменен, возврат подтвержден, referral link использован, qualifying action выполнен, reward redeemed.
5. Кешбэк и recommendation discount отображаются как прогноз до подтверждения события и как подтвержденная выгода после наступления бизнес-условий; frontend различает эти состояния визуально и текстово через i18n.
6. Партнер получает персональную referral link и QR-код, привязанные к userId/partnerNumber и текущему source campaign; повторная генерация не создает новый код без явной ротации.
7. Статусы привлеченных пользователей по referral link показывают минимум: invited, registered, firstOrderPlaced, qualified, rejected, rewardGranted; причины отказа возвращаются как mnemonic-коды.
8. Магазин наград показывает только доступные партнеру rewards с учетом баланса, роли, региона, склада, catalogId, периода действия, лимитов и статуса reward.
9. Попытка redeem недоступной или просроченной награды возвращает отказ без списания баланса и без создания fulfillment-задачи.
10. Удерживающие офферы показываются партнеру только при выполнении заданного audience/segment/risk rule и содержат срок жизни, условия применения, приоритет и совместимость с другими выгодами.
11. Раздел истории показывает начисления, применения, отмены, истечения, ручные корректировки и технические перерасчеты выгод с временем, sourceSystem, correlationId и локализованным описанием.
12. Переход из программы преимуществ в корзину и checkout передает только идентификаторы применимых выгод; окончательное применение выполняется backend-проверкой, а не только frontend-состоянием.
13. Для `/vip-customer-benefits` и `/vip-customer-benefits/become-vip` неавторизованный пользователь видит публичные условия и может начать переход в VIP/партнерский сценарий через invite/referral flow.
14. Раздел `/the-new-oriflame-app` показывает преимущества приложения и связанные выгоды без раскрытия персональных данных, пока пользователь не авторизован.

## Критерии интеграции
15. Feature #40 читает товарные и каталожные данные из публичного каталога и PIM/catalog контуров, но не дублирует master-data товаров и каталогов.
16. Применение выгод в корзине и checkout согласовано с feature #9 и feature #10: итоговая сумма, скидки, подарки, бесплатная доставка и ограничения совпадают в корзине, checkout и истории заказа.
17. События начисления и использования кешбэка синхронизируются с бонусным кошельком feature #14 и компенсационным контуром feature #38 без двойного начисления.
18. Referral activity и рост партнера согласованы с feature #16 и feature #28: один referral source не может быть засчитан нескольким партнерам без approved exception.
19. Уведомления о доступных, истекающих, примененных или отклоненных выгодах создаются через контур уведомлений feature #25 и содержат только i18n keys или mnemonic-коды.
20. Support view не позволяет сотруднику изменять выгоды напрямую, если действие требует CRM/marketing approval или отдельного административного permission.

## Ошибки, RBAC и безопасность
21. Пользователь без partner/VIP/customer permission получает HTTP 403 с mnemonic `STR_MNEMO_PARTNER_BENEFITS_ACCESS_DENIED` для приватных endpoints.
22. Попытка применить выгоду другого пользователя возвращает HTTP 403 или 404 без раскрытия чужого userId, partnerNumber, referralCode или benefitId.
23. Невалидные параметры, неизвестный benefitId, rewardId, referralCode или catalogId возвращают HTTP 400/404 с mnemonic `STR_MNEMO_PARTNER_BENEFITS_VALIDATION_FAILED` или специализированным кодом семейства `STR_MNEMO_PARTNER_BENEFITS_*`.
24. Конфликт версии, повторный redeem, повторное применение одноразовой выгоды или гонка при изменении баланса возвращают HTTP 409 с mnemonic `STR_MNEMO_PARTNER_BENEFITS_VERSION_CONFLICT`.
25. Все ручные действия поддержки требуют audit event с actorUserId, actionCode, targetUserId, sourceSystem, reasonCode, correlationId и timestamp.
26. Персональные данные привлеченных пользователей маскируются в referral list, если текущая роль не имеет права видеть контактные данные.

## Frontend и i18n
27. Все новые пользовательские строки frontend для разделов `/member-benefits`, `/vip-customer-benefits`, `/vip-customer-benefits/become-vip`, `/the-new-oriflame-app`, reward shop, referral status и warning banners вынесены в i18n dictionaries для всех поддерживаемых языков.
28. React-компоненты не имеют явного return type `JSX.Element`; при необходимости используется `ReactElement` из `react` или inference.
29. Frontend не содержит hardcoded user-facing text в компонентах, route metadata, validation messages, placeholders, alerts, tabs, buttons и empty states.
30. Backend не отправляет предопределенный пользовательский текст во frontend; все такие сообщения передаются mnemonic-кодами с префиксом `STR_MNEMO_`, а frontend локализует их из dictionaries.

## Тестируемость и качество
31. Managed API test начинается с логина партнера или сотрудника поддержки соответствующей роли и проверяет зеленый путь, ошибки RBAC, conflict и validation cases.
32. Managed UI test начинается с логина пользователя соответствующей роли и проверяет видимость основных блоков, referral link, reward shop, предупреждение об истечении выгоды и переход к checkout.
33. End-to-end managed tests агрегируют реальные feature tests и не содержат placeholder assertions, которые только проверяют feature id, имя файла или строковый литерал.
34. Liquibase changesets, если потребуются для хранения выгод, referral events, reward redemption или audit history, создаются только XML-файлами в owning module `db` package и отдельным changelog для feature #40.
35. Все новые или измененные текстовые файлы сохраняются в UTF-8; русский текст читается без mojibake.
36. Версионный baseline и возможные compatibility ограничения отражены в BA/SA/architecture артефактах feature #40.
