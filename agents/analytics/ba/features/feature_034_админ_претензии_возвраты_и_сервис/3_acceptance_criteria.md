# Acceptance criteria. Feature 034. Админ: претензии, возвраты и сервис

## Контекст проверки
Feature #34 считается принятой, если административная service console позволяет обрабатывать претензии, возвраты, замены, компенсации и SLA сервиса с контролем RBAC, audit trail, финансовых лимитов, связей с заказами, платежами, WMS/логистикой и пользовательским контуром feature #012/#021. Критерии связаны с user stories `US-034-*` и исходным описанием feature.

## AC-034-01. Очередь сервисных кейсов
- Оператор видит таблицу кейсов с claimId, orderId, customerId/partnerId, типом проблемы, приоритетом, SLA status, текущим статусом, ответственным, каналом, регионом, датами создания/обновления и дедлайном следующего действия.
- Фильтры по статусу, приоритету, SLA, типу проблемы, заказу, клиенту, партнеру, складу, ответственному, каналу и диапазону дат применяются совместно.
- Сортировка по SLA deadline, priority, createdAt и updatedAt не сбрасывает выбранные фильтры.
- Пустой результат показывает локализованное empty state из frontend i18n dictionaries.

## AC-034-02. Карточка сервисного кейса
- Карточка показывает order context, позиции заказа, платежи, доставку, историю претензий, вложения, переписку, SLA, ответственных, связанные refund/replacement/bonus compensation actions и audit trail.
- Пользователь видит только действия, разрешенные его role/permission scopes и текущим статусом кейса.
- Системные и операторские события отображаются в хронологическом порядке с actorUserId, actionCode, reasonCode, timestamp и correlationId.
- Персональные данные, платежные реквизиты и вложения маскируются или скрываются, если у пользователя нет соответствующего scope.

## AC-034-03. Жизненный цикл кейса
- Backend поддерживает контролируемые статусы `NEW`, `ROUTED`, `IN_PROGRESS`, `WAITING_CUSTOMER`, `WAITING_WMS`, `WAITING_FINANCE`, `ESCALATED`, `RESOLVED`, `REJECTED`, `CLOSED`.
- Невалидный переход возвращает mnemonic-код `STR_MNEMO_ADMIN_SERVICE_INVALID_STATUS_TRANSITION`.
- Каждый ручной переход требует reasonCode и сохраняет audit event.
- Закрытие кейса возможно только после финального решения или явного отказа с reasonCode.

## AC-034-04. Настройка очередей и маршрутизации
- Service admin может создать и изменить очередь с правилами по claim type, channel, region, warehouseId, priority, customer/partner context и SLA policy.
- Новые кейсы автоматически маршрутизируются в подходящую очередь; если правило не найдено, кейс попадает в fallback-очередь.
- Изменение routing rule не ломает уже назначенные кейсы, но применяется к новым и вручную переоткрытым кейсам.
- Каждое изменение правил маршрутизации записывается в audit trail.

## AC-034-05. SLA
- SLA deadline рассчитывается по типу кейса, приоритету, каналу и рабочему календарю.
- Очередь и карточка показывают SLA status: `ON_TRACK`, `AT_RISK`, `BREACHED`, `PAUSED`.
- При наступлении `AT_RISK` или `BREACHED` система создает escalation event и обновляет supervisor board.
- Пауза SLA доступна только для статусов ожидания клиента, WMS или финансового подтверждения и требует reasonCode.

## AC-034-06. Переписка, заметки и вложения
- Оператор может добавить клиентское сообщение, внутреннюю заметку и вложение только при наличии permission scope.
- Клиентское сообщение публикуется в пользовательский контур feature #012/#021, внутренняя заметка не видна клиенту или партнеру.
- Вложения проходят проверку типа, размера, связи с claimId и доступности для текущей роли.
- Удаление вложения запрещено; допускается только пометка `REDACTED` с audit reason при наличии отдельного scope.

## AC-034-07. Запрос дополнительной информации
- Оператор может перевести кейс в `WAITING_CUSTOMER` с перечнем требуемых данных и сроком ответа.
- Клиент или партнер видит запрос в пользовательском интерфейсе и может добавить ответ/вложения.
- Получение ответа возвращает кейс в рабочую очередь или назначенному оператору.
- Просрочка ответа клиента фиксируется отдельным событием и не считается SLA breach оператора, если SLA был корректно paused.

## AC-034-08. Решения по кейсу
- Поддерживаются решения `APPROVE_REFUND`, `APPROVE_REPLACEMENT`, `APPROVE_BONUS_COMPENSATION`, `REJECT`, `REQUEST_INFO`, `ESCALATE`.
- Каждое решение требует reasonCode, actorUserId, comment, timestamp и correlationId.
- Решение проверяет текущий статус кейса, order context, payment status, delivery/WMS status и compensation policy.
- Предопределенные причины отказа и предупреждения передаются на frontend только mnemonic-кодами `STR_MNEMO_ADMIN_SERVICE_*`.

## AC-034-09. Денежный refund
- Refund доступен только в пределах фактически оплаченной и еще не возвращенной суммы заказа.
- Refund требует reasonCode, суммы или выбранных order lines, связи с claimId и проверки payment policy.
- Если сумма или payment status требуют финансового подтверждения, создается состояние `WAITING_FINANCE`, а refund не отправляется провайдеру до approval.
- Попытка превышения доступной суммы возвращает mnemonic-код `STR_MNEMO_ADMIN_SERVICE_REFUND_AMOUNT_EXCEEDED`.

## AC-034-10. Замена товара
- Replacement создается только по существующему claimId и order line, с указанием SKU, количества, склада, delivery method и reasonCode.
- Backend проверяет доступность SKU, возможность замены по policy, отсутствие конфликтующего refund и WMS constraints.
- Replacement shipment получает связку claimId, originalOrderId, replacementOrderReference и correlationId.
- Невозможность замены возвращает mnemonic-код `STR_MNEMO_ADMIN_SERVICE_REPLACEMENT_UNAVAILABLE`.

## AC-034-11. Бонусная компенсация
- Bonus compensation проверяет лимиты policy, статус клиента/партнера, связь с claimId и правила бонусного кошелька.
- Начисление выполняется через интеграцию с бонусным контуром и сохраняет externalBonusTransactionId.
- Нельзя одновременно начислить бонусную компенсацию и денежный refund за одну и ту же позицию без supervisor override.
- Превышение лимита возвращает mnemonic-код `STR_MNEMO_ADMIN_SERVICE_COMPENSATION_LIMIT_EXCEEDED`.

## AC-034-12. Отказ по претензии
- Отказ требует reasonCode, операторский комментарий и локализуемую customer-facing причину.
- Клиенту или партнеру не отправляется hardcoded backend text; frontend получает mnemonic-код и параметры.
- После отказа кейс может быть переоткрыт только пользователем с supervisor scope или при новом клиентском ответе по разрешенному сценарию.
- Отказ сохраняется в audit trail и доступен в фильтрах аналитики.

## AC-034-13. Эскалации и supervisor override
- Эскалация требует причины, текущего owner, целевой группы или супервизора и ожидаемого действия.
- Supervisor board показывает все эскалированные, просроченные и близкие к просрочке кейсы.
- Override compensation/refund/rejection доступен только при отдельном permission scope и всегда требует reasonCode.
- Override-событие сохраняет old decision, new decision, actorUserId, reasonCode и correlationId.

## AC-034-14. Финансовое подтверждение
- Финансовый оператор видит ожидающие confirmation refund actions с суммой, валютой, payment reference, orderId, claimId и reasonCode.
- Approval запускает разрешенный refund flow, rejection возвращает кейс оператору с reasonCode.
- Финансовое решение не меняет содержимое заказа и не может увеличить сумму refund сверх доступной.
- Все финансовые решения доступны в audit trail и операционной аналитике.

## AC-034-15. WMS/логистический контекст
- В карточке отображаются warehouseId, return shipment status, item inspection status, defect code, lost/damaged flags, replacement fulfillment status и внешние references.
- WMS-статусы не редактируются напрямую из service console, а отображаются как внешний операционный контекст.
- Прием нового WMS-события обновляет связанные кейсы и может снять ожидание `WAITING_WMS`.
- Невозможность обработки WMS-события сохраняется с retry status и причиной.

## AC-034-16. Связанные кейсы и повторные обращения
- Карточка показывает другие кейсы по тому же orderId, customerId, partnerId, delivery reference и SKU.
- Система предупреждает оператора о конфликтующих активных refund/replacement/bonus decisions.
- Повторное обращение может быть связано с исходным claimId без потери независимой истории статусов.

## AC-034-17. Операционная аналитика
- Service admin и супервизор видят метрики количества кейсов, SLA breach rate, average response time, average resolution time, escalation rate, refund/replacement/bonus totals, отказов и reopen rate.
- Метрики фильтруются по периоду, очереди, оператору, региону, складу, типу проблемы и решению.
- Экспорт аналитики доступен только пользователю с отдельным scope и учитывает текущие фильтры.

## AC-034-18. Audit trail и экспорт
- Audit search поддерживает фильтры по claimId, orderId, customerId, partnerId, actorUserId, actionCode, reasonCode, decisionCode, date range, external reference и correlationId.
- Экспорт результатов доступен только пользователю с отдельным permission scope.
- Экспорт не раскрывает персональные данные, платежные реквизиты и вложения сверх прав пользователя.
- CSV/Excel-файл содержит timestamp генерации, набор фильтров и идентификатор пользователя, запросившего выгрузку.

## AC-034-19. RBAC
- Все admin service endpoints проверяют permission scopes, роль, регион, канал, тип действия, сумму компенсации, override-признаки и доступность вложений.
- Frontend скрывает недоступные actions, но backend остается источником истины для отказа.
- Отказ по правам возвращает mnemonic-код `STR_MNEMO_ADMIN_SERVICE_FORBIDDEN_ACTION`.

## AC-034-20. Локализация и backend-to-frontend contract
- Все новые user-facing строки frontend вынесены в `resources_ru.ts` и `resources_en.ts`.
- Статусы, reason codes, filter labels, actions, validation messages, empty states, SLA labels и ошибки не хардкодятся в React-компонентах.
- Backend не отправляет готовый пользовательский текст для предопределенных ошибок, предупреждений и отказов; используются только `STR_MNEMO_ADMIN_SERVICE_*`.

## AC-034-21. Наблюдаемость
- Backend публикует метрики service cases by status, SLA breaches, escalations, refund failures, replacement failures, compensation decisions, operator actions и latency внешних callbacks.
- Логи содержат correlationId для claim, order, refund, replacement, bonus compensation, WMS event и operator action.
- Ошибки routing, SLA calculation, financial limit, WMS callback и idempotency различимы по error code.

## AC-034-22. Версионный baseline и storage
- Backend-реализация использует текущий monolith stack Best Ori Gin: Java 25, Spring Boot 4.0.6, Maven, Hibernate, MapStruct, Lombok, Liquibase XML и PostgreSQL.
- Новые Liquibase changesets создаются отдельным XML-файлом owning module `admin-service`.
- Frontend-реализация использует текущий `frontend/monolith/apps/web-shell` stack: React, TypeScript, Vite, Ant Design и существующие i18n dictionaries.
- Отклонения от baseline фиксируются в архитектурных артефактах и status-файле до реализации.
