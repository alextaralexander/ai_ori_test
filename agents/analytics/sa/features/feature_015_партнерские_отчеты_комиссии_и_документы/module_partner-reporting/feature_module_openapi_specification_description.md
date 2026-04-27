# Feature 015 module partner-reporting OpenAPI description

## Назначение API
OpenAPI specification описывает публичный контракт модуля `partner-reporting` для партнерских отчетов, комиссий, выплат, документов, экспортов и finance-view сверки. Runtime Swagger должен генерироваться автоматически из Spring MVC controllers модуля через `springdoc-openapi` в группе `/v3/api-docs/partner-reporting` и Swagger UI `/swagger-ui/partner-reporting`.

## Endpoint ownership
Все controllers размещаются внутри package prefix модуля `com.bestorigin.monolith.partnerreporting.impl.controller`. DTO размещаются в `api`, business services и orchestration в `impl/service`, validators в `impl/validator`, security policy в `impl/security`, mappers в `impl/mapper`. JPA entities и repositories находятся только в `domain`; Liquibase XML changelog находится в `db`.

## Partner report endpoints
- `GET /api/partner-reporting/reports/summary` возвращает сводку отчетного периода текущего партнера. Фильтры: `dateFrom`, `dateTo`, `catalogId`, `bonusProgramId`. Ответ содержит totals по продажам, базе комиссии, начислениям, удержаниям, сумме к выплате, выплаченной сумме, reconciliation status, `publicMnemo` и `correlationId`.
- `GET /api/partner-reporting/reports/orders` возвращает pageable список заказов, влияющих на комиссию. Фильтры: `dateFrom`, `dateTo`, `catalogId`, `orderNumber`, `payoutStatus`, `bonusProgramId`, `page`, `size`. Неприменимые фильтры возвращают пустую страницу.
- `GET /api/partner-reporting/reports/orders/{orderNumber}/commission` возвращает детализацию комиссии по заказу: order source, structure level, commission base, rate percent, commission amount, adjustments, payout reference, `publicMnemo` и `correlationId`.

## Partner document endpoints
- `GET /api/partner-reporting/documents` возвращает документы текущего партнера с фильтрами периода, типа, статуса, page и size.
- `POST /api/partner-reporting/documents/{documentId}/download` возвращает metadata опубликованного документа и signed download URL. Доступ разрешен только владельцу документа или finance role через отдельные endpoints.
- `GET /api/partner-reporting/documents/{documentId}/print-view` возвращает metadata печатной формы документа с тем же `versionNumber` и `checksumSha256`, что у скачиваемого файла.

## Export endpoint
- `POST /api/partner-reporting/exports` создает или возвращает export job для PDF/XLSX отчета. Ответ содержит `exportId`, `exportStatus`, формат, `rowCount` и `publicMnemo`, включая `STR_MNEMO_PARTNER_REPORT_EXPORT_READY` для готовой выгрузки.

## Finance endpoints
- `GET /api/partner-reporting/finance/reconciliations` доступен бухгалтеру и финансовому контролеру. Требует `partnerId`, период и обязательный `reason`; возвращает агрегированную сверку начислено, удержано, к выплате, выплачено, mismatch reasons, audit flag и reason.
- `POST /api/partner-reporting/finance/documents/{documentId}/publish` публикует документ только из статуса `READY`; request требует `reasonCode`.
- `POST /api/partner-reporting/finance/documents/{documentId}/revoke` отзывает опубликованный или готовый документ; request требует `reasonCode`. Отозванная версия остается в истории.

## DTO и схемы
- `MoneyAmount` хранит сумму как decimal string и `currencyCode`.
- `PartnerReportSummaryResponse` является корневым summary DTO и содержит totals, reconciliation status, mnemonic и correlation id.
- `PartnerReportOrderLineResponse` описывает строку заказа и расчет комиссии без раскрытия лишних персональных данных покупателя структуры.
- `PartnerCommissionDetailResponse` агрегирует строку заказа, корректировки, payout reference и публичный mnemonic.
- `PartnerReportDocumentResponse`, `PartnerReportDocumentDownloadResponse`, `PartnerReportPrintViewResponse` описывают lifecycle документа, checksum, version и ссылки на скачивание или печать.
- `PartnerReportExportRequest` и `PartnerReportExportResponse` описывают PDF/XLSX выгрузку.
- `PartnerReportFinanceReconciliationResponse` расширяет summary audit-полями и причинами расхождения.

## Валидации
- `dateFrom <= dateTo`; период не должен превышать лимит, заданный конфигурацией модуля.
- `size` ограничен диапазоном `1..100`, `page >= 0`.
- `documentId` должен быть UUID.
- `reasonCode` обязателен для publish/revoke; `reason` обязателен для finance-view.
- Скачать можно только документ в статусе `PUBLISHED`.
- Публикация разрешена только из `READY`; отзыв разрешен для `READY` или `PUBLISHED`.
- Export format принимает только `PDF` и `XLSX`.

## STR_MNEMO contract
Backend не возвращает hardcoded user-facing text. Предопределенные пользовательские сообщения передаются только mnemonic-кодами:

- `STR_MNEMO_PARTNER_REPORT_ACCESS_DENIED`
- `STR_MNEMO_PARTNER_REPORT_ORDER_NOT_FOUND`
- `STR_MNEMO_PARTNER_REPORT_DOCUMENT_NOT_FOUND`
- `STR_MNEMO_PARTNER_REPORT_EXPORT_READY`
- `STR_MNEMO_PARTNER_REPORT_RECONCILIATION_MISMATCH`
- `STR_MNEMO_PARTNER_REPORT_DOCUMENT_NOT_PUBLISHED`
- `STR_MNEMO_PARTNER_REPORT_INVALID_PERIOD`
- `STR_MNEMO_PARTNER_REPORT_DOCUMENT_STATUS_CONFLICT`

Frontend обязан добавить эти ключи во все поддерживаемые `resources_*.ts` dictionaries и локализовать сообщения на клиенте.

## Security
- Partner endpoints работают только в контексте текущего партнера и не принимают произвольный `partnerId`.
- Finance endpoints требуют роли бухгалтера или финансового контролера, обязательный reason и audit trail.
- Download и print-view проверяют владение документом, актуальный статус, version и checksum.
- Ответы не раскрывают полный набор персональных данных покупателей структуры; используется обезличенный `customerRef`.

## Версионный baseline на 27.04.2026
- Java 25, Spring Boot 4.0.6, Maven baseline текущего monolith.
- springdoc-openapi latest stable в рамках выбранного Spring Boot baseline.
- OpenAPI 3.1.0.
