# Module partner-reporting ER description

## Назначение модуля
`partner-reporting` хранит полную модель отчетных периодов партнера, строк заказов для комиссионной сверки, удержаний и корректировок, выплат, документов по выплатам и экспортных заданий после feature #15. Модуль принадлежит backend monolith и должен использовать package ownership `api/domain/db/impl`: JPA entities и repositories размещаются в `domain`, XML Liquibase changesets в `db`, controllers/services/config/security/validators/mappers в role-specific подпакетах `impl`.

## Таблица `partner_report_period`
Главная агрегирующая таблица отчетного периода партнера.

- `report_period_id uuid` - primary key.
- `partner_id varchar` - идентификатор партнера-владельца отчета, обязательный.
- `catalog_id varchar` - каталог или кампания продаж, обязательный.
- `bonus_program_id varchar` - бонусная или компенсационная программа.
- `period_from date`, `period_to date` - границы отчетного периода, обязательные.
- `gross_sales_amount numeric(19,2)` - общий объем продаж.
- `commission_base_amount numeric(19,2)` - база расчета комиссии.
- `accrued_commission_amount numeric(19,2)` - начисленная комиссия.
- `withheld_amount numeric(19,2)` - удержания.
- `payable_amount numeric(19,2)` - сумма к выплате.
- `paid_amount numeric(19,2)` - фактически выплаченная сумма.
- `currency_code varchar(3)` - ISO currency, обязательный.
- `reconciliation_status varchar` - `MATCHED`, `MISMATCH`, `PENDING`.
- `public_mnemo varchar` - публичный mnemonic `STR_MNEMO_*` для статуса или расхождения.
- `correlation_id varchar` - трассировка расчета.
- `created_at timestamp`, `updated_at timestamp` - audit timestamps.

Уникальность: `partner_id + catalog_id + bonus_program_id + period_from + period_to`. Индексы: `partner_id, period_from, period_to`, `catalog_id`, `reconciliation_status`.

## Таблица `partner_report_order_line`
Строка заказа, влияющая на комиссию партнера.

- `report_order_line_id uuid` - primary key.
- `report_period_id uuid` - foreign key на `partner_report_period`.
- `order_number varchar` - номер заказа.
- `order_source varchar` - `SELF`, `STRUCTURE`, `OFFLINE_CLIENT`, `SUPPLEMENTARY`.
- `structure_level integer` - уровень структуры, nullable для личного заказа.
- `customer_ref varchar` - обезличенная ссылка на покупателя или структуру.
- `order_status varchar` - публичный статус заказа.
- `ordered_at timestamp` - дата заказа.
- `order_amount numeric(19,2)` - сумма заказа.
- `commission_base_amount numeric(19,2)` - база комиссии по строке.
- `commission_rate_percent numeric(7,4)` - примененный процент.
- `commission_amount numeric(19,2)` - начисленная комиссия.
- `calculation_status varchar` - `PENDING`, `READY`, `PAID`, `HELD`, `REVERSED`.
- `payout_reference varchar` - ссылка на выплату.

Индексы: `report_period_id`, `order_number`, `calculation_status`, `payout_reference`.

## Таблица `partner_commission_adjustment`
Удержание или ручная корректировка комиссии.

- `adjustment_id uuid` - primary key.
- `report_period_id uuid` - foreign key на отчетный период.
- `report_order_line_id uuid` - nullable foreign key на строку заказа.
- `adjustment_type varchar` - `RETURN`, `CANCEL`, `TAX`, `MANUAL_ADJUSTMENT`, `CHARGEBACK`.
- `reason_code varchar` - служебная причина.
- `source_ref varchar` - ссылка на возврат, претензию, платеж или ручную операцию.
- `amount numeric(19,2)` - сумма корректировки, положительная или отрицательная по бизнес-смыслу типа.
- `currency_code varchar(3)` - валюта.
- `actor_user_id varchar` - инициатор ручной операции.
- `audit_recorded boolean` - флаг записи аудита.
- `created_at timestamp` - дата создания.

Индексы: `report_period_id`, `report_order_line_id`, `adjustment_type`, `source_ref`.

## Таблица `partner_payout`
Выплата партнеру за отчетный период.

- `payout_id uuid` - primary key.
- `report_period_id uuid` - foreign key на отчетный период.
- `partner_id varchar` - партнер.
- `payout_reference varchar` - внешний или внутренний reference выплаты, уникальный.
- `payout_method varchar` - способ выплаты.
- `payout_status varchar` - `SCHEDULED`, `PAID`, `FAILED`, `REVERSED`.
- `scheduled_at timestamp`, `paid_at timestamp` - даты планирования и фактической выплаты.
- `amount numeric(19,2)` - сумма.
- `currency_code varchar(3)` - валюта.
- `payment_reference varchar` - ссылка платежного контура.
- `failure_mnemo varchar` - `STR_MNEMO_*` при ошибке выплаты.

Индексы: `report_period_id`, `partner_id`, `payout_reference`, `payout_status`.

## Таблица `partner_report_document`
Документ партнера по выплате или сверке.

- `document_id uuid` - primary key.
- `report_period_id uuid` - foreign key на отчетный период.
- `payout_id uuid` - nullable foreign key на выплату.
- `document_type varchar` - `ACT`, `RECEIPT`, `CERTIFICATE`, `PAYOUT_STATEMENT`, `TAX_NOTE`, `RECONCILIATION_REPORT`.
- `document_status varchar` - `DRAFT`, `READY`, `PUBLISHED`, `REVOKED`.
- `version_number integer` - версия документа.
- `checksum_sha256 varchar` - checksum опубликованного файла.
- `storage_key varchar` - ключ файла в S3/MinIO.
- `print_view_key varchar` - ключ печатной формы.
- `published_at timestamp`, `revoked_at timestamp` - даты lifecycle.
- `author_user_id varchar` - бухгалтер или финансовый контролер.
- `reason_code varchar` - причина публикации, отзыва или корректировки.
- `public_mnemo varchar` - публичный mnemonic статуса.

Уникальность: `report_period_id + document_type + version_number`. Индексы: `report_period_id`, `payout_id`, `document_type`, `document_status`.

## Таблица `partner_report_export`
Экспорт отчетности партнера.

- `export_id uuid` - primary key.
- `report_period_id uuid` - foreign key на отчетный период.
- `partner_id varchar` - партнер-владелец.
- `export_format varchar` - `PDF` или `XLSX`.
- `export_status varchar` - `REQUESTED`, `READY`, `FAILED`, `EXPIRED`.
- `row_count integer` - количество строк выгрузки.
- `storage_key varchar` - ключ результата в S3/MinIO.
- `public_mnemo varchar` - например `STR_MNEMO_PARTNER_REPORT_EXPORT_READY`.
- `requested_by_user_id varchar` - инициатор.
- `requested_at timestamp`, `ready_at timestamp` - даты lifecycle.

Индексы: `report_period_id`, `partner_id`, `export_status`, `requested_at`.

## Связи и ограничения
- `partner_report_period` является корнем агрегата и каскадно связывается с заказными строками, корректировками, выплатами, документами и экспортами.
- Удаление отчетных данных не используется для бизнес-операций; ошибочные документы переводятся в `REVOKED`, а выплаты в `REVERSED`.
- Для финансовых операций обязательны `correlation_id`, `reason_code` или audit metadata в зависимости от типа операции.
- Backend не хранит предопределенные пользовательские тексты в payload; для frontend используются только `STR_MNEMO_*`.

## Версионный baseline на 27.04.2026
- Java 25, Spring Boot 4.0.6, Maven baseline текущего monolith.
- Hibernate latest stable в рамках выбранного Spring Boot baseline.
- Liquibase XML changeset `feature_015_partner_reporting.xml` в owning module `partner-reporting`.
