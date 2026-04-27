# Архитектура Best Ori Gin после feature 017

`web-shell frontend` обращается к backend-модулям по HTTP JSON. Публичный контент, каталог, корзина, order, profile, bonus-wallet, partner-onboarding, partner-reporting и mlm-structure остаются отдельными bounded capabilities внутри monolith.

Feature 017 усиливает связи `web-shell -> order` для маршрутов `/vip-orders` и `/business/tools/order-management/vip-orders/partner-orders`. Модуль `order` публикует API партнерских офлайн-заказов, хранит проекцию клиентских заказов партнера и отдает payment/delivery/bonus/timeline данные. Для интеграции продукта `order` передает контекст bonus accrual в `bonus-wallet`, businessVolume и partnerPersonNumber в `mlm-structure`, а также order report context в `partner-reporting`.

Внешние связи module `order`: Payment API для статусов оплаты, WMS/1C для резервов и остатков, Delivery API для доставки и точек выдачи. Все протоколы в текущем контуре описаны как HTTP JSON contract links; runtime-классы backend следуют пакетной политике `api/domain/db/impl`.

Baseline feature 017: Java 25, Spring Boot 4.0.6, Maven, XML Liquibase, React 19.2, TypeScript 6.0, Ant Design/Vite latest-зависимости web-shell.