# Анализ oriflame.ru от 2026-04-24

## Метод сканирования
- Источник истины для SPA-маршрутов: клиентский manifest `https://oriflame.ru/assets/manifest-c6aae4ba.js`.
- Из manifest извлечены все зарегистрированные client-side routes, route modules и parameterized paths.
- Требуемая глубина обхода была принята равной 10 уровням. Фактически deepest discovered route path в приложении имеет глубину 6 сегментов, поэтому весь доступный route graph был покрыт полностью.
- Дополнительно учтены скрытые подсистемы, выявленные по импортам модулей: офлайн-режим, уведомления, инициализация invitation code, partner search, impersonation, Yandex Metrika, Mindbox, Hybrid AI pixel, i18n, PDF viewer.

## Сводка покрытия
- Всего обнаружено маршрутов: 80.
- Параметризованных маршрутов: 16.
- Максимальная фактическая глубина пути: 6.
- Самые насыщенные разделы: `/employee`, `/business`, `/order`, `/profile-settings`, `/partner-office`, `/cart`, `/invite`.

## Обнаруженные route-группы
- Публичные и контентные: `/`, `/home`, `/community`, `/news`, `/FAQ`, `/info/:section?`, `/content/:contentId`, `/documents/:documentType`, `/offer/:offerId`.
- Каталог и продукт: `/search`, `/product/:productCode`, `/products/digital-catalogue-current`, `/products/digital-catalogue-next`.
- Benefit и invitation flows: `/beauty-benefits`, `/beauty-benefits/:code`, `/business-benefits`, `/business-benefits/:code`, `/invite/beauty-partner-registration`, `/invite/business-partner-registration`, `/invite/partners-activation`, `/invite/sponsor-cabinet`.
- Корзина и checkout: `/cart`, `/cart/shopping-offers`, `/cart/supplementary`, `/cart/supplementary/shopping-offers`, `/order`, `/order/supplementary`.
- История заказов и претензии: `/order/order-history`, `/order/order-history/:orderId`, `/order/claims/claim-create`, `/order/claims/claims-history`, `/order/claims/claims-history/:claimId`.
- Профиль и финансы: `/profile-settings`, `/profile-settings/addresses`, `/profile-settings/contacts`, `/profile-settings/documents`, `/profile-settings/general`, `/profile-settings/security`, `/profile/transactions/:type`, `/report/info-reciept`, `/report/order-history`.
- Business/partner кабинет: `/business`, `/business/beauty-community`, `/business/conversion`, `/business/team-activity`, `/business/upgrade`, `/business/partner-card/:personNumber`, `/vip-orders`, `/business/tools/order-management/vip-orders/partner-orders`, `/business/tools/order-management/vip-orders/partner-orders/:orderId`.
- Partner office и логистика: `/partner-office/all-orders`, `/partner-office/report`, `/partner-office/supply`, `/partner-office/supply/:supplyId`, `/partner-office/supply/orders/:orderId`.
- Employee контур: `/employee`, `/employee/new-order`, `/employee/order-support`, `/employee/order-history`, `/employee/order-history/:orderId`, `/employee/submit-claim`, `/employee/claims-history`, `/employee/claims-history/:claimId`, `/employee/partner-card`, `/employee/report/order-history`, `/employee/profile-settings`, `/employee/profile-settings/addresses`, `/employee/profile-settings/contacts`, `/employee/profile-settings/documents`, `/employee/profile-settings/general`, `/employee/profile-settings/security`, `/employee/super-user`.

## Инвентарь фич
- `feature_001_публичная_главная_и_навигация`
- `feature_002_контентные_страницы_и_новости`
- `feature_003_faq_инфо_и_документы`
- `feature_004_каталог_и_поиск_товаров`
- `feature_005_карточка_товара`
- `feature_006_цифровые_каталоги_и_pdf_материалы`
- `feature_007_бьюти_и_бизнес_бенефиты`
- `feature_008_регистрация_и_инвайты_партнеров`
- `feature_009_корзина_и_shopping_offers`
- `feature_010_оформление_заказа_и_дозаказ`
- `feature_011_история_и_детали_заказов`
- `feature_012_претензии_и_возвраты`
- `feature_013_профиль_и_настройки_пользователя`
- `feature_014_кошелек_и_транзакции`
- `feature_015_отчеты_и_квитанции_партнера`
- `feature_016_бизнес_кабинет_и_рост_партнера`
- `feature_017_vip_заказы_и_управление_партнерами`
- `feature_018_партнерский_офис_и_логистика`
- `feature_019_сотрудник_новый_заказ_и_поддержка`
- `feature_020_сотрудник_история_заказов`
- `feature_021_сотрудник_претензии`
- `feature_022_сотрудник_карточка_партнера_и_отчеты`
- `feature_023_сотрудник_настройки_и_super_user`
- `feature_024_аутентификация_роли_и_имперсонация`
- `feature_025_уведомления_офлайн_i18n_и_аналитика`
- `feature_026_админ_rbac_и_учетные_записи`
- `feature_027_админ_cms_контент_новости_faq_документы`
- `feature_028_админ_лендинги_инвайты_и_рефералы`
- `feature_029_админ_pim_каталог_товары_категории_медиа`
- `feature_030_админ_каталоги_выпуски_и_pdf`
- `feature_031_админ_цены_акции_предложения_и_бенефиты`
- `feature_032_админ_остатки_склады_поставки_и_логистика`
- `feature_033_админ_заказы_корзины_дозаказы_и_платежи`
- `feature_034_админ_претензии_возвраты_и_сервис`
- `feature_035_админ_пользователи_партнеры_сотрудники_и_имперсонация`
- `feature_036_админ_отчеты_kpi_аудит_и_интеграции`

## Правило декомпозиции
- Каждая фича ниже создана как стартовая единица backlog-а с `1_textual_description.md`, чтобы дальше можно было запускать стандартный поток `FEATURE_WORKFLOW__NEW.md`.
- Пользовательский контур и админский контур разделены намеренно: это позволит независимо прорабатывать BA/SA-артефакты, API и тесты.
- Админские фичи покрывают управление контентом, каталогом, ценами, остатками, заказами, претензиями, пользователями, ролями, аудитом и интеграциями.
