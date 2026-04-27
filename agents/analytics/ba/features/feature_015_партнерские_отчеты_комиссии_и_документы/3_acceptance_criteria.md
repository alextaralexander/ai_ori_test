# Acceptance criteria feature 015. Партнерские отчеты, комиссии и документы

## AC-015-01. Сводка отчетного периода
Партнер получает сводку за выбранный период, каталог и бонусную программу с показателями `grossSalesAmount`, `commissionBaseAmount`, `accruedCommissionAmount`, `withheldAmount`, `payableAmount`, `paidAmount`, `currency` и статусом сверки.

## AC-015-02. История заказов
Страница `/report/order-history` и API истории возвращают заказы, влияющие на комиссии партнера: номер заказа, дату, каталог, покупателя или структуру без лишних персональных данных, сумму заказа, комиссионную базу, статус заказа и статус расчета.

## AC-015-03. Детализация комиссии
Детали комиссии содержат order reference, partner id, structure level, compensation program, rate percent, base amount, commission amount, удержания, payout reference, public status mnemonic и correlation id.

## AC-015-04. Удержания и корректировки
Удержания отображаются отдельными строками с типами `RETURN`, `CANCEL`, `TAX`, `MANUAL_ADJUSTMENT`, `CHARGEBACK`, причиной, суммой, валютой и ссылкой на исходный заказ или документ.

## AC-015-05. История выплат
История выплат содержит дату, сумму, валюту, метод выплаты, payment reference, статус `SCHEDULED`, `PAID`, `FAILED`, `REVERSED`, а также список связанных комиссий и удержаний.

## AC-015-06. Документы по выплатам
Страница `/report/info-reciept` показывает документы типов `ACT`, `RECEIPT`, `CERTIFICATE`, `PAYOUT_STATEMENT`, `TAX_NOTE`, `RECONCILIATION_REPORT` со статусом, версией, периодом, checksum, датой публикации и ссылкой скачивания.

## AC-015-07. Скачать документ
Партнер может скачать только опубликованный документ своего отчета. Backend возвращает metadata файла и signed download URL или поток файла, не раскрывая документы других партнеров.

## AC-015-08. Печатная форма
Для опубликованных документов доступна print-view форма с тем же номером версии и checksum, что и скачиваемый документ.

## AC-015-09. Экспорт отчетов
Экспорт поддерживает форматы `PDF` и `XLSX`; запрос возвращает export id, статус, формат, количество строк, дату готовности и mnemonic `STR_MNEMO_PARTNER_REPORT_EXPORT_READY`.

## AC-015-10. Фильтры
API и UI поддерживают фильтры `dateFrom`, `dateTo`, `catalogId`, `orderNumber`, `documentType`, `payoutStatus`, `bonusProgramId`, `page`, `size`. Неприменимые фильтры возвращают пустую страницу без ошибки.

## AC-015-11. Сверка расхождений
Если сумма начислений, удержаний и выплат не сходится, отчет получает reconciliation status `MISMATCH`, список причин и mnemonic `STR_MNEMO_PARTNER_REPORT_RECONCILIATION_MISMATCH`.

## AC-015-12. Публикация документов бухгалтером
Бухгалтер может опубликовать документ только из статуса `READY`; публикация фиксирует version, checksum, publishedAt, author id, reason code и audit record.

## AC-015-13. Отзыв документа
Финансовый контролер может отозвать документ в статус `REVOKED` с обязательным reason code. Отозванный документ остается в истории, но недоступен как актуальная версия для скачивания.

## AC-015-14. Контроль выплат
Финансовый контролер видит агрегированную сверку начислено, удержано, к выплате, выплачено и расхождение по партнеру, периоду, каталогу и бонусной программе.

## AC-015-15. Защита доступа
Партнер не может открыть чужие отчеты, документы, выплаты или export jobs. Бухгалтер и финансовый контролер получают доступ только через role-based finance endpoints с audit reason.

## AC-015-16. Ошибки backend
Backend возвращает предопределенные пользовательские сообщения только mnemonic-кодами `STR_MNEMO_*`; hardcoded пользовательский текст в payload запрещен.

## AC-015-17. Frontend i18n
Все новые пользовательские строки frontend добавлены во все поддерживаемые словари ресурсов; React компоненты не содержат hardcoded пользовательского текста.

## AC-015-18. Managed tests
Canonical API и UI тесты создаются в `agents/tests/` и синхронизируются в runtime-копии с marker comment. End-to-end тесты агрегируют реальный feature test #15.

## AC-015-19. Liquibase
Для backend module `partner-reporting` создан отдельный XML changelog `feature_015_partner_reporting.xml`; SQL/YAML/JSON changesets не используются.

## AC-015-20. Наблюдаемость
Ответы отчетов, документов, выплат, экспортов и корректировок содержат correlation id, source reference и audit metadata, достаточные для финансового расследования.

## AC-015-21. Проверки запуска
Backend и frontend должны стартовать, API test feature #15 должен проходить, а frontend build должен завершаться без ошибок TypeScript.
