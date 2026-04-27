# Архитектура Best Ori Gin после feature 018

`web-shell frontend` обращается к backend-модулям по HTTP JSON. Публичный контент, каталог, корзина, order, profile, bonus-wallet, partner-onboarding, partner-reporting, mlm-structure и partner-office остаются отдельными bounded capabilities внутри monolith.

Feature 017 усиливает связи `web-shell -> order` для маршрутов `/vip-orders` и `/business/tools/order-management/vip-orders/partner-orders`. Модуль `order` публикует API партнерских офлайн-заказов, хранит проекцию клиентских заказов партнера и отдает payment/delivery/bonus/timeline данные. Для интеграции продукта `order` передает контекст bonus accrual в `bonus-wallet`, businessVolume и partnerPersonNumber в `mlm-structure`, а также order report context в `partner-reporting`.

Внешние связи module `order`: Payment API для статусов оплаты, WMS/1C для резервов и остатков, Delivery API для доставки и точек выдачи. Все протоколы в текущем контуре описаны как HTTP JSON contract links; runtime-классы backend следуют пакетной политике `api/domain/db/impl`.

Feature 018 добавляет module `partner-office` для маршрутов `/partner-office/all-orders`, `/partner-office/report`, `/partner-office/supply`, `/partner-office/supply/:supplyId`, `/partner-office/supply/orders/:orderId`. `web-shell` вызывает `/api/partner-office` по HTTP JSON. Модуль хранит supply-поставки, заказы в контексте поставки, строки SKU, складские движения, отклонения приемки и эскалации. Он связан с `order` через stable order references and workflow links, с `claim workflow` через deviation/claim references, с `partner-reporting` через office logistics KPI context, с WMS/1C через supply and movement status API, с Delivery через delivery and pickup status links.

`partner-office` соблюдает package policy: `api` для DTO, `domain` для JPA entities и repository interfaces, `db` для XML Liquibase changelog, `impl/controller`, `impl/service`, `impl/config`, `impl/mapper`, `impl/validator`, `impl/exception` для runtime-кода. Предопределенные пользовательские сообщения backend передает frontend только через `STR_MNEMO_*`; frontend локализует их в dictionaries.

Baseline feature 018: Java 25 как текущий monolith runtime при доступном Java 26.0.1 на 27.04.2026, Spring Boot 4.0.6, Maven, XML Liquibase, React 19.2, TypeScript 6.0, Ant Design/Vite latest-зависимости web-shell. Upgrade Java 26 не входит в feature #18, потому что monolith зафиксирован на `maven.compiler.release=25`.
