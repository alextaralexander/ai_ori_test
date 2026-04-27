# Acceptance criteria feature 014. Бонусный кошелек и транзакции

## AC-014-01. Сводка баланса
Пользователь с ролью покупателя или партнера получает сводку кошелька с bucket-ами `CASHBACK`, `REFERRAL_DISCOUNT`, `MANUAL_ADJUSTMENT` и `ORDER_REDEMPTION`, где для каждого bucket указаны available, hold, expiring soon и currency.

## AC-014-02. История операций
История возвращает операции типов `ACCRUAL`, `HOLD`, `REDEMPTION`, `REVERSAL`, `EXPIRE`, `MANUAL_ADJUSTMENT` с датой, суммой, bucket, статусом, source type, public mnemonic и correlation id.

## AC-014-03. Фильтры
API и UI поддерживают фильтры `type`, `status`, `campaignId`, `sourceType`, `orderNumber`, `dateFrom`, `dateTo`, `page`, `size`. Неприменимые фильтры не ломают выдачу и возвращают пустую страницу.

## AC-014-04. Маршрут frontend
Маршрут `/profile/transactions/:type` открывает страницу кошелька. Значение `:type` выбирает активный фильтр bucket или `all`.

## AC-014-05. Детали транзакции
Детали операции показывают сумму, валюту, bucket, статус, срок действия, источник, связанные order/claim/referral/qualification references и историю событий.

## AC-014-06. Связка с заказом
Если транзакция связана с заказом, UI показывает переход на `/order/order-history/{orderNumber}`.

## AC-014-07. Лимиты применения
Endpoint лимитов возвращает максимальную сумму применения к заказу, доступный баланс, причину ограничения и mnemonic при блокировке.

## AC-014-08. Hold и reversal
Операции hold и reversal корректно уменьшают или возвращают доступный баланс в расчетной модели ответа и отображаются отдельными строками истории.

## AC-014-09. Сгорание выгод
Истекающие операции содержат `expiresAt`; UI выделяет операции, срок действия которых наступит в ближайшие 14 дней.

## AC-014-10. Экспорт
Endpoint экспорта возвращает metadata экспорта с id, статусом, форматом `CSV`, количеством строк и mnemonic `STR_MNEMO_BONUS_WALLET_EXPORT_READY`.

## AC-014-11. Ручная корректировка
Финансовый оператор может создать ручную корректировку. Запрос требует bucket, amount, reasonCode, targetUserId и idempotency key. Повтор с тем же idempotency key не создает дубль.

## AC-014-12. Защита доступа
Пользователь не может открыть чужой кошелек. Финансовый оператор может открыть кошелек другого пользователя только через finance endpoint с reason.

## AC-014-13. Ошибки backend
Backend возвращает предопределенные пользовательские сообщения только mnemonic-кодами `STR_MNEMO_*`; hardcoded пользовательский текст в payload запрещен.

## AC-014-14. Frontend i18n
Все новые пользовательские строки frontend добавлены в `resources_ru.ts` и `resources_en.ts`; компоненты не содержат hardcoded пользовательских текстов.

## AC-014-15. Managed tests
Canonical API и UI тесты создаются в `agents/tests/` и синхронизируются в runtime-копии с marker comment. End-to-end тесты агрегируют реальный feature test #14.

## AC-014-16. Liquibase
Для backend module `bonus-wallet` создан отдельный XML changelog `feature_014_bonus_wallet.xml`; SQL/YAML/JSON changesets не используются.

## AC-014-17. Наблюдаемость
Ответы операций содержат audit/correlation metadata, достаточные для расследования начислений и корректировок без раскрытия лишних персональных данных.

## AC-014-18. Проверки запуска
Backend и frontend должны стартовать, API тест feature #14 должен проходить, а frontend build должен завершаться без ошибок TypeScript.
