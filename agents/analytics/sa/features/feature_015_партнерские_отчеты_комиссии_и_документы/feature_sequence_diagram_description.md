# Feature 015 sequence diagram description

## Назначение
Диаграмма описывает runtime-взаимодействия feature #15 между frontend маршрутами `/report/order-history` и `/report/info-reciept`, backend module `partner-reporting`, существующими backend modules `order` и `bonus-wallet`, компенсационным расчетом, платежным контуром и S3/MinIO для документов и экспортов.

## Поток партнера: отчет и комиссии
1. Партнер открывает `/report/order-history`.
2. Frontend вызывает `GET /api/partner-reporting/reports/summary` с периодом, каталогом и бонусной программой.
3. `partner-reporting` читает `partner_report_period`; при необходимости синхронизирует snapshot с order module, bonus-wallet и compensation plan.
4. Backend возвращает `PartnerReportSummaryResponse`, `publicMnemo` в формате `STR_MNEMO_*` и `correlationId`.
5. Frontend локализует mnemonic через i18n dictionaries и показывает продажи, комиссионную базу, начисления, удержания, сумму к выплате, выплаченную сумму и reconciliation status.

## Поток партнера: строки заказов и детализация комиссии
1. Партнер фильтрует отчет по периоду, каталогу, заказу, статусу выплаты и бонусной программе.
2. Frontend вызывает `GET /api/partner-reporting/reports/orders`.
3. Backend возвращает pageable список `PartnerReportOrderLineResponse`.
4. При открытии заказа frontend вызывает `GET /api/partner-reporting/reports/orders/{orderNumber}/commission`.
5. Backend возвращает строку заказа, structure level, commission base, rate, commission amount, adjustments, payout reference и audit/correlation metadata.

## Поток партнера: документы
1. Партнер открывает `/report/info-reciept`.
2. Frontend вызывает `GET /api/partner-reporting/documents` с фильтрами периода, типа и статуса документа.
3. Backend возвращает документы только текущего партнера.
4. Для скачивания frontend вызывает `POST /api/partner-reporting/documents/{documentId}/download`.
5. `partner-reporting` проверяет ownership, статус `PUBLISHED`, version и checksum, затем запрашивает signed download URL в S3/MinIO.
6. Frontend получает metadata и ссылку скачивания; при ошибке отображает локализованный `STR_MNEMO_*`.

## Поток партнера: экспорт
1. Партнер запускает экспорт PDF или XLSX.
2. Frontend вызывает `POST /api/partner-reporting/exports`.
3. Backend создает `partner_report_export`, формирует файл, сохраняет его в S3/MinIO и переводит export job в `READY`.
4. Ответ содержит `exportId`, `rowCount`, `format` и `STR_MNEMO_PARTNER_REPORT_EXPORT_READY`.

## Поток бухгалтера
1. Бухгалтер публикует готовый документ через `POST /api/partner-reporting/finance/documents/{documentId}/publish`.
2. Backend проверяет роль, статус `READY`, обязательный `reasonCode` и checksum файла.
3. Документ получает статус `PUBLISHED`, version, checksum, publishedAt, author и audit record.

## Поток финансового контролера
1. Финансовый контролер открывает finance-view сверки через `GET /api/partner-reporting/finance/reconciliations`.
2. Request обязательно содержит `partnerId`, период и `reason`.
3. Backend читает totals, mismatch reasons, документы, выплаты и сверяет payout status с платежным контуром.
4. При ошибке документа контролер вызывает `POST /api/partner-reporting/finance/documents/{documentId}/revoke`.
5. Backend переводит документ в `REVOKED`, пишет reason code, audit trail и возвращает response с `STR_MNEMO_PARTNER_REPORT_RECONCILIATION_MISMATCH`, если расхождение сохраняется.

## Ошибки и доступ
- Partner endpoints всегда работают в контексте текущего партнера; произвольный `partnerId` в partner API не принимается.
- Finance endpoints требуют роли бухгалтера или финансового контролера и audit reason.
- Backend не передает hardcoded user-facing text во frontend. Все предопределенные сообщения передаются mnemonic-кодами `STR_MNEMO_*`.
- Документы и print-view доступны только при успешной проверке ownership, lifecycle status, version и checksum.

## Версионный baseline на 27.04.2026
- Java 25, Spring Boot 4.0.6, Maven baseline текущего monolith.
- React latest, TypeScript latest, Ant Design latest, Vite latest.
- S3/MinIO latest stable для хранения документов и экспортов.
