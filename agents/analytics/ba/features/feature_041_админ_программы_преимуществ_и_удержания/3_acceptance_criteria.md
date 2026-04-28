# Acceptance criteria feature #41. Админ: программы преимуществ и удержания

## Управление программами преимуществ
1. Администратор программ преимуществ может открыть административный раздел программ и увидеть реестр с фильтрами по коду, типу, статусу, catalogId, периоду действия, сегменту, владельцу и дате изменения.
2. Администратор может создать программу типов `CASHBACK`, `REFERRAL_DISCOUNT`, `WELCOME`, `REWARD_SHOP`, `FREE_SHIPPING`, `RESERVATION`, `SUBSCRIPTION`, `RETENTION_OFFER`, указав обязательные параметры для выбранного типа.
3. Для cashback-программы система поддерживает процентную и фиксированную модель, валюту, максимальную сумму на заказ, максимальную сумму на участника, срок жизни выгоды, правила сгорания и связь с бонусным кошельком feature #14.
4. Для referral discount система поддерживает условия регистрации, первого заказа, qualifying action, sponsor attribution, лимиты по источнику и защиту от повторного засчитывания одного referral source.
5. Для welcome/start программ система поддерживает разные правила для VIP-клиента, бьюти-партнера и бизнес-партнера, включая срок активации, первый заказ, минимальную сумму и допустимые каналы.
6. Для reward shop система поддерживает каталог наград, стоимость, доступность по роли, региону, складу, уровню, catalogId, сроку действия и лимитам redemption.
7. Для free shipping система поддерживает условия по сумме заказа, региону, способу доставки, точке выдачи, роли, уровню партнера, catalogId и совместимости с другими скидками.
8. Для reservation/subscription механик система поддерживает срок удержания выгоды, продление, восстановление после пропуска, отмену, связь с корзиной/checkout и повторную покупку.
9. Система не позволяет сохранить программу без обязательных полей, с отрицательными лимитами, некорректной валютой, пустым периодом действия, конфликтующим кодом или невозможным status transition.
10. Опубликованную программу нельзя удалить физически; для остановки используются статусы `PAUSED` или `ARCHIVED`, а история остается доступной для аудита.

## Жизненный цикл, публикация и каталогозависимость
11. Программа проходит жизненный цикл `DRAFT -> READY_FOR_REVIEW -> SCHEDULED -> ACTIVE -> PAUSED -> ARCHIVED`; недопустимые переходы возвращают HTTP 409 с mnemonic-кодом.
12. Перед публикацией система выполняет validation summary: обязательные поля, eligibility, бюджет, конфликт периодов, conflict/stacking rules, наличие i18n/mnemonic-кодов и возможность dry-run.
13. Программа может быть привязана к одному или нескольким 21-дневным каталогам; выгоды разных каталогов не смешиваются в user-facing контуре feature #40.
14. Для окончания каталога система применяет заданные правила expiration, grace period, carry-over или revoke и сохраняет результат в истории программы.
15. Система поддерживает scheduled activation/deactivation по времени платформы и не активирует программу до наступления периода действия.
16. При изменении активной программы создается новая версия правил; уже созданные выгоды и финансовые события остаются связанными с версией, действовавшей в момент события.

## Eligibility, совместимость и dry-run
17. CRM/менеджер удержания может задать eligibility по роли, партнерскому уровню, региону, активности, ББ, сумме заказов, referral source, channel, risk score и признакам незавершенной корзины.
18. Система поддерживает priority, stackability, mutual exclusion, max benefit rules и ограничение количества выгод на заказ.
19. Dry-run применимости доступен для тестового partnerNumber/userId, catalogId, корзины, заказа и сценария checkout; dry-run не создает финансовых транзакций, reservation и notification events.
20. Dry-run возвращает структурированный результат: применимые программы, отказанные программы, причины отказа mnemonic-кодами, расчет скидок/cashback/free shipping и warnings.
21. Изменение eligibility, priority или compatibility rules фиксируется в аудите с before/after summary и correlationId.

## Финансовый контроль
22. Финансовый администратор может задать бюджет программы, лимит cashback, лимит скидки, лимит redemption, лимит бесплатной доставки и правило автоматической остановки при достижении лимита.
23. Публикация программы, влияющей на скидки, cashback liability или стоимость доставки, требует финансового approval или роли с правом `ADMIN_BENEFIT_PROGRAM_FINANCE`.
24. Ручные корректировки cashback, reward eligibility, reservation или subscription status требуют reasonCode, evidence, target user, amount/benefit delta, approval status и audit event.
25. Ручная корректировка не может создать отрицательный баланс или обойти опубликованные лимиты без отдельного approved exception.
26. Экспорт для сверки формируется по периоду, catalogId, programCode, типу выгоды, статусу, валюте и региону; персональные данные маскируются, если роль не имеет расширенного права.

## Интеграции с платформой
27. Пользовательский контур feature #40 получает только опубликованные и активные версии программ, доступные по audience/eligibility, catalogId и периоду действия.
28. Интеграция с bonus wallet feature #14 не создает двойных начислений: cashback, reversal, expiration и manual adjustment имеют idempotency key и correlationId.
29. Корзина feature #9 и checkout feature #10 получают только идентификаторы программ и структурированные условия; окончательное применение выполняется backend-проверкой.
30. Reservation в корзине автоматически истекает по configured TTL и не блокирует выгоду после отмены корзины или checkout timeout.
31. Notification contour feature #25 получает события активации, истечения, reservation, subscription renewal, redemption, rejection и manual adjustment только с mnemonic-кодами и структурированными параметрами.
32. Интеграционные ошибки сохраняются без секретов и персональных данных; frontend получает только mnemonic-коды семейства `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_*`.

## RBAC, безопасность и ошибки
33. Административные endpoints доступны только ролям со scopes `ADMIN_BENEFIT_PROGRAM_VIEW`, `ADMIN_BENEFIT_PROGRAM_MANAGE`, `ADMIN_BENEFIT_PROGRAM_PUBLISH`, `ADMIN_BENEFIT_PROGRAM_FINANCE`, `ADMIN_BENEFIT_PROGRAM_AUDIT_VIEW`.
34. Пользователь без прав получает HTTP 403 с mnemonic `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_ACCESS_DENIED`.
35. Ошибки валидации возвращают HTTP 400 с mnemonic-кодами `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_VALIDATION_FAILED` или более точными кодами семейства фичи.
36. Неизвестная программа, версия, reward или ручная корректировка возвращает HTTP 404 без раскрытия чужих персональных данных.
37. Конфликт optimistic locking, повторная публикация, устаревшая версия или недопустимый status transition возвращает HTTP 409 с mnemonic `STR_MNEMO_ADMIN_BENEFIT_PROGRAM_VERSION_CONFLICT`.
38. Backend не отправляет во frontend предопределенные пользовательские сообщения текстом; используются только mnemonic-коды с префиксом `STR_MNEMO_`.

## Frontend и i18n
39. Админский frontend содержит реестр программ, форму создания/редактирования, экран версии и публикации, dry-run preview, финансовый блок, аудит и экран ручных корректировок.
40. Все новые user-facing строки frontend вынесены в i18n dictionaries для всех поддерживаемых языков; компоненты, маршруты, валидации, placeholders, alerts, confirmations, tabs, buttons и empty states не содержат hardcoded текста.
41. React-компоненты не имеют явного return type `JSX.Element`; при необходимости используется `ReactElement` из `react` или inference.
42. UI показывает локализованные статусы, причины отказа, validation summary и audit events на основе dictionaries и mnemonic-кодов.

## Аудит и наблюдаемость
43. Каждое создание, изменение, публикация, пауза, архивирование, dry-run, импорт, экспорт, ручная корректировка и интеграционная отправка фиксируются в аудите с actorUserId, role, actionCode, entityType, entityId, version, before/after summary, reasonCode, sourceSystem, correlationId и timestamp.
44. Аудитор может искать события по programCode, programId, status, actor, actionCode, entityId, date range, catalogId и correlationId.
45. Метрики activation rate, redemption rate, repeat order rate, retention uplift, churn risk, budget usage и integration error rate доступны для анализа эффективности программ.
46. Технические логи не содержат секретов, токенов, полных платежных данных или лишних персональных данных.

## Тестируемость и качество
47. Managed API test начинается с логина администратора соответствующей роли и покрывает зеленый путь создания, dry-run, публикации и просмотра аудита программы.
48. Managed API test покрывает RBAC 403, validation 400, not found 404, optimistic locking 409 и идемпотентность повторной публикации или интеграционного события.
49. Managed UI test начинается с логина администратора программ преимуществ и проверяет реестр, форму программы, validation summary, dry-run preview, публикацию и аудит.
50. Managed end-to-end API/UI tests агрегируют реальные feature tests и не содержат placeholder assertions, которые только проверяют feature id, имя файла или строковый литерал.
51. Если фича требует хранения данных, Liquibase changesets создаются только XML-файлами в owning module `db` package и отдельным changelog для feature #41.
52. Новые backend runtime-классы размещаются в `impl/<role>` подпакетах, JPA entities и repositories находятся только в `domain`, Liquibase changelogs находятся только в `db`.
53. Все новые или измененные текстовые файлы сохраняются в UTF-8; русский текст читается без mojibake.
54. Версионный baseline и compatibility ограничения отражены в BA/SA/architecture артефактах feature #41.
