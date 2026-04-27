# Sequence description feature 018. Партнерский офис, поставки и логистика

## Общий поток
Frontend web-shell обслуживает маршруты `/partner-office/all-orders`, `/partner-office/report`, `/partner-office/supply`, `/partner-office/supply/:supplyId`, `/partner-office/supply/orders/:orderId` и обращается к backend base path `/api/partner-office`. Каждый запрос передает `X-User-Context-Id`; state-changing операции дополнительно передают `Idempotency-Key`.

## Список заказов офиса
Партнер-офис открывает `/partner-office/all-orders`. Frontend вызывает `GET /api/partner-office/orders` с фильтрами. Controller передает запрос в service, service проверяет доступ по `officeId`, `regionId` и роли, читает page из partner-office DB, обогащает ссылками на order module и delivery/pickup workflow, после чего возвращает `PartnerOfficeOrderPageResponse`.

## Карточка supply-поставки
Пользователь открывает `/partner-office/supply/:supplyId`. Backend загружает supply, связанные orders, items, movements, deviations и availableActions. WMS/1C adapter может отдавать latest external status snapshot как интеграционный источник, но persisted truth для UI остается в partner-office DB.

## Transition supply
Логистический оператор выполняет transition поставки, например `ARRIVED`. Service проверяет lifecycle transition, idempotency key, роль и office/region scope. При успехе status поставки обновляется, создается movement event, frontend получает mnemonic `STR_MNEMO_PARTNER_OFFICE_SUPPLY_STATUS_UPDATED` и показывает localized text через i18n.

## Фиксация отклонения
Партнер-офис фиксирует отклонение приемки по заказу внутри поставки. Service проверяет, что order принадлежит разрешенной supply, валидирует `deviationType`, `sku`, `quantity`, `reasonCode`, сохраняет deviation и movement `DEVIATION_RECORDED`, возвращает `STR_MNEMO_PARTNER_OFFICE_DEVIATION_RECORDED`. Claim workflow получает reference data через ссылку или последующую интеграцию, но не переносится в ownership partner-office.

## Отчет и эскалации
Региональный менеджер открывает `/partner-office/report`. Backend агрегирует supply, orders, deviations, SLA и escalations по office/region scope. UI показывает KPI, проблемные маршруты и владельцев эскалаций. Создание эскалации выполняется через service partner-office с audit metadata и reason code.

## Negative access flow
Если пользователь запрашивает чужой `supplyId` или `orderNumber`, service не раскрывает данные и возвращает `STR_MNEMO_PARTNER_OFFICE_ACCESS_DENIED`. Frontend отображает локализованное сообщение из dictionaries.

## Интеграционные границы
- `order module`: источник детальных order данных и order workflow links.
- `claim workflow`: получает references на deviations для претензий.
- `delivery/pickup workflow`: предоставляет delivery status, pickup point и переходы.
- `WMS/1C adapter`: источник внешних статусов и документов, не владеет UI state partner-office.

## Package ownership and baseline
Runtime-классы backend размещаются по package policy: `api`, `domain`, `db`, `impl/controller`, `impl/service`, `impl/config`, `impl/mapper`, `impl/validator`, `impl/exception`. Baseline: Java 25 current monolith, Spring Boot 4.0.6, Maven, XML Liquibase, React 19.2, TypeScript 6.0, Ant Design/Vite web-shell latest dependencies. Java 26.0.1 доступен на дату старта, но не включается в feature #18 из-за текущего `maven.compiler.release=25`.
