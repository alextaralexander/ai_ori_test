# Acceptance criteria. Feature 033. Админ: заказы, платежи и дозаказы

## Контекст проверки
Feature #33 считается принятой, если административный контур позволяет управлять заказами, корзинами, дозаказами, платежами, refund/dispute-сценариями, антифрод-проверками и операторскими корректировками с контролем RBAC, аудита, финансовых инвариантов и локализации. Критерии связаны с user stories `US-033-*` и стартовым описанием feature.

## AC-033-01. Список заказов
- Администратор видит таблицу заказов с номером, customerId/partnerId, каналом, catalog period, order status, payment status, fulfillment status, суммой, валютой, складом, датами создания и обновления.
- Фильтры по номеру, клиенту, партнеру, статусам, каналу, каталогу, складу, дате, сумме, payment reference и shipment reference применяются совместно.
- Пагинация, сортировка и поиск не теряют выбранные фильтры при обновлении списка.
- Пустой результат показывает локализованное empty state из i18n dictionary.

## AC-033-02. Карточка заказа
- Карточка заказа показывает состав, цены, скидки, бонусы, итоговые суммы, delivery data, reserve data, payment events, refund events, supplementary orders, risk events и audit trail.
- Пользователь видит только действия, разрешенные его permission scopes и текущими статусами заказа.
- Системные и операторские события отображаются в хронологическом порядке с actor, actionCode, reasonCode, timestamp и correlationId.
- Чувствительные персональные и платежные данные маскируются, если у пользователя нет scope на раскрытие.

## AC-033-03. Жизненный цикл заказа
- Backend разрешает только валидные переходы между статусами `DRAFT`, `PLACED`, `PAYMENT_PENDING`, `PAID`, `RESERVED`, `ASSEMBLY`, `SHIPPED`, `DELIVERED`, `CANCELLED`, `PARTIALLY_CANCELLED`.
- Невалидный переход возвращает mnemonic-код `STR_MNEMO_ADMIN_ORDER_INVALID_STATUS_TRANSITION`.
- Каждый ручной переход требует reasonCode и сохраняет audit event.
- Отмена или частичная отмена проверяет платеж, резерв, сборку, доставку и refund policy до изменения статуса.

## AC-033-04. Активные корзины
- Администратор видит активные корзины с customer/partner context, составом, текущими ценами, примененными benefits, конфликтами цены, недоступными SKU и просроченными резервами.
- Действия поддержки по корзине не меняют финансовый результат оформленного заказа.
- Очистка проблемного состояния корзины требует reasonCode и записывает audit trail.

## AC-033-05. Дозаказы
- Supplementary order создается только от существующего исходного заказа и хранит ссылку на parentOrderId.
- Дозаказ проверяет каталог, доступность SKU, текущие лимиты, payment policy, anti-fraud policy и правила бонусов.
- Цепочка основного заказа и всех дозаказов отображается в карточке заказа.
- Нельзя создать дозаказ к отмененному или заблокированному заказу без отдельного разрешения и audit reason.

## AC-033-06. Split и merge
- Split заказа создает связанные fulfillment groups с собственными статусами резерва, сборки и доставки.
- Merge доступен только для совместимых заказов одного customer/partner, валюты, региона, delivery constraints и payment policy.
- Любой split/merge сохраняет исходные ссылки и audit events.
- Нельзя потерять позицию, сумму или скидку при split/merge; backend проверяет инвариант суммы до и после операции.

## AC-033-07. Payment events
- Backend принимает события `AUTHORIZATION`, `CAPTURE`, `CANCEL`, `REFUND`, `PARTIAL_REFUND`, `DISPUTE`, `CHARGEBACK` с provider, externalPaymentId, idempotencyKey, amount, currency и correlationId.
- Повторное событие с тем же provider, externalPaymentId и idempotencyKey не создает дубль операции.
- Карточка заказа показывает исходные и нормализованные payment statuses.
- Ошибка обработки платежного события сохраняется с payload checksum, retry status и причиной.

## AC-033-08. Сверка оплаты
- Финансовый оператор может связать задержанное payment event с заказом только при совпадении разрешенных признаков: сумма, валюта, customer/partner context или подтвержденная external reference.
- Ручная сверка не меняет состав заказа и не может увеличить payable amount.
- Результат сверки записывается в audit trail и payment reconciliation log.

## AC-033-09. Refund и dispute
- Полный и частичный refund доступны только в пределах фактически оплаченной и еще не возвращенной суммы.
- Refund требует reasonCode, выбранных order lines или суммы, связи с отменой/претензией при наличии и подтверждения финансового оператора.
- Dispute/chargeback блокирует запрещенные fulfillment-действия до закрытия риска.
- Все refund/dispute статусы доступны в фильтрах и карточке заказа.

## AC-033-10. Финансовые блокировки
- Финансовая блокировка хранит reasonCode, comment, actorUserId, срок действия и связь с payment/risk event.
- Активная блокировка запрещает отгрузку, ручной capture и дозаказ, если у пользователя нет отдельного override scope.
- Снятие блокировки требует reasonCode и создает audit event.

## AC-033-11. Антифрод
- Антифрод-сводка показывает risk score, rule codes, provider response, source event, related orders и текущий decision status.
- Решение `APPROVED`, `REJECTED`, `NEEDS_MORE_INFO`, `EXPIRED` требует actor, reasonCode и timestamp.
- Risk decision влияет на доступность payment capture, fulfillment и supplementary order actions.
- Предопределенные антифрод-предупреждения передаются на frontend только mnemonic-кодами `STR_MNEMO_ADMIN_ORDER_*`.

## AC-033-12. Операторские корректировки
- Корректировка контактов, комментария, адреса до отгрузки и способа связи доступна только при валидном статусе заказа и permission scope.
- Каждая корректировка сохраняет old value, new value, actorUserId, reasonCode и correlationId.
- Нельзя вручную менять сумму заказа, paid amount, refund amount или бонусные списания через обычную поддержку.

## AC-033-13. Партнерские офлайн-заказы
- Заказ хранит source channel, partnerId, offline order reference и customer note, если он создан из партнерской офлайн-продажи.
- Фильтры позволяют выделять offline orders и supplementary orders.
- Карточка показывает связь с партнерским офисом, сотрудническим созданием заказа и исходными документами.

## AC-033-14. Складской контекст
- В заказе отображаются warehouseId, reserve status, shipment reference, partial availability, blocking warehouse reasons и связь с feature #32.
- Складские блокировки не редактируются напрямую из заказного контура, а отображаются как внешний операционный контекст.
- При частичной доступности администратор видит допустимые варианты: partial cancel, supplementary order, backorder или ожидание поставки.

## AC-033-15. Audit trail и экспорт
- Audit search поддерживает фильтры по orderId, cartId, paymentId, actorUserId, actionCode, reasonCode, date range, external reference и correlationId.
- Экспорт результатов доступен только пользователю с отдельным permission scope и учитывает текущие фильтры.
- Экспорт не раскрывает чувствительные данные сверх прав пользователя.
- CSV/Excel-файл содержит timestamp генерации, набор фильтров и идентификатор пользователя, запросившего выгрузку.

## AC-033-16. RBAC
- Все admin endpoints проверяют permission scopes, роль, регион, канал продаж и тип действия.
- Frontend скрывает недоступные actions, но backend остается источником истины для отказа.
- Отказ по правам возвращает mnemonic-код `STR_MNEMO_ADMIN_ORDER_FORBIDDEN_ACTION`.

## AC-033-17. Локализация
- Все новые user-facing строки frontend вынесены в `resources_ru.ts` и `resources_en.ts`.
- Статусы, reason codes, filter labels, actions, validation messages, empty states и ошибки не хардкодятся в React-компонентах.
- Backend не отправляет готовый пользовательский текст для предопределенных ошибок; используются только `STR_MNEMO_ADMIN_ORDER_*`.

## AC-033-18. Наблюдаемость
- Backend публикует метрики заказов по статусам, зависших оплат, refund failures, duplicate payment events, anti-fraud decisions, operator actions и latency внешних callbacks.
- Логи содержат correlationId для заказа, платежа, refund, risk decision и operator action.
- Ошибки idempotency, финансовых инвариантов и внешних callbacks различимы по error code.

## AC-033-19. Финансовые инварианты
- `orderTotal = paidAmount + unpaidAmount - refundedAmount` проверяется в допустимой доменной форме для каждого платежного изменения.
- Сумма refund не превышает доступную к возврату сумму.
- Split/merge и supplementary order не создают отрицательную сумму, потерянную скидку или несогласованное bonus write-off.
- Неконсистентное состояние блокирует операцию и возвращает mnemonic-код `STR_MNEMO_ADMIN_ORDER_FINANCIAL_INVARIANT_FAILED`.

## AC-033-20. Версионный baseline и storage
- Backend-реализация использует текущий monolith stack Best Ori Gin: Java 25, Spring Boot 4.0.6, Maven, Hibernate, MapStruct, Lombok, Liquibase XML и PostgreSQL.
- Новые Liquibase changesets создаются отдельным XML-файлом owning module `admin-order`.
- Frontend-реализация использует текущий `frontend/monolith/apps/web-shell` stack: React, TypeScript, Vite, Ant Design и существующие i18n dictionaries.
- Отклонения от baseline фиксируются в архитектурных артефактах и статусе feature до реализации.
