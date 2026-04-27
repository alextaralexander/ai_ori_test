# Feature module ER description. Feature 022. Module employee

## Назначение модели
Feature #22 расширяет employee module read-side данными для карточки партнера и отчета истории заказов партнера. Модель не дублирует master-data партнера, заказов, бонусов и WMS, а фиксирует employee audit trail и расчетные snapshot-агрегаты, необходимые для воспроизводимого отчета.

## employee_partner_card_audit
Таблица фиксирует просмотры карточки партнера, отчета и переходы в связанные flows.

Поля:
- `audit_event_id uuid` - первичный ключ события.
- `actor_user_id varchar(128)` - идентификатор сотрудника или регионального менеджера.
- `actor_role varchar(64)` - роль в момент действия: `backoffice`, `employee-support`, `supervisor`, `regional-manager`.
- `support_reason_code varchar(64)` - основание просмотра, например `EMPLOYEE_PARTNER_CARD_VIEW`.
- `source_route varchar(256)` - frontend route, из которого пришло действие.
- `target_entity_type varchar(64)` - `PARTNER`, `PARTNER_REPORT`, `ORDER`, `CLAIM`, `SUPPORT_CASE`.
- `target_entity_id varchar(128)` - идентификатор целевой сущности.
- `partner_id varchar(64)` - внутренний идентификатор партнера.
- `person_number varchar(64)` - партнерский person number.
- `correlation_id varchar(128)` - сквозной идентификатор запроса/перехода.
- `occurred_at timestamptz` - время события.

Ограничения и индексы:
- PK `pk_employee_partner_card_audit` по `audit_event_id`.
- Индекс `idx_employee_partner_card_audit_partner_time` по `partner_id, occurred_at desc`.
- Индекс `idx_employee_partner_card_audit_actor_time` по `actor_user_id, occurred_at desc`.
- Check constraint на непустые `actor_user_id`, `support_reason_code`, `target_entity_type`, `target_entity_id`.

## employee_partner_report_snapshot
Таблица хранит расчетный снимок агрегатов отчета партнера на момент генерации. Источники фактов: order history, claims, bonus wallet, WMS/delivery events.

Поля:
- `report_snapshot_id uuid` - первичный ключ snapshot.
- `partner_id varchar(64)` - идентификатор партнера.
- `person_number varchar(64)` - person number партнера.
- `campaign_code varchar(32)` - каталог/кампания отчета.
- `region_code varchar(32)` - региональный scope.
- `total_orders integer` - количество заказов в выборке.
- `total_amount numeric(19,2)` - сумма всех заказов.
- `paid_amount numeric(19,2)` - оплаченная сумма.
- `returned_amount numeric(19,2)` - сумма возвратов/компенсаций.
- `personal_volume numeric(19,2)` - персональный объем.
- `group_volume numeric(19,2)` - групповой объем.
- `open_claim_count integer` - количество открытых претензий.
- `delayed_delivery_count integer` - количество заказов с задержкой доставки.
- `generated_at timestamptz` - момент генерации snapshot.

Ограничения и индексы:
- PK `pk_employee_partner_report_snapshot` по `report_snapshot_id`.
- Индекс `idx_employee_partner_report_snapshot_partner_campaign` по `partner_id, campaign_code, generated_at desc`.
- Check constraints для неотрицательных счетчиков и денежных/volume полей.

## Связи
- `employee_partner_card_audit.partner_id/person_number` логически связывает аудит с партнером и snapshot отчета.
- `employee_partner_card_audit` связывается с `employee_support_snapshot` по контексту сотрудника, target entity и correlation id на уровне сервиса, без жесткого FK к операционным таблицам других feature.

## Версионная база
Фича не вводит новые технологии и использует baseline задачи на 27.04.2026: Java/Spring Boot/Maven monolith, PostgreSQL, Liquibase XML, Hibernate-compatible numeric/date types, package policy `api/domain/db/impl`.