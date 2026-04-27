# ER-описание feature 016 для module_mlm-structure

Модуль `mlm-structure` хранит и отдает агрегированную модель MLM-структуры партнера для текущей каталожной кампании. В runtime первой итерации используется in-memory repository, а Liquibase XML фиксирует целевую реляционную структуру.

## Таблица mlm_partner_node
- `partner_node_id uuid PK` - технический идентификатор узла.
- `person_number varchar(32) UK NOT NULL` - публичный номер партнера для маршрута `/business/partner-card/:personNumber`.
- `sponsor_person_number varchar(32)` - номер sponsor/upline.
- `branch_id varchar(64) NOT NULL` - ветка downline.
- `structure_level int NOT NULL` - уровень в структуре относительно лидера.
- `partner_role varchar(32) NOT NULL` - LEADER, CONSULTANT, MANAGER, ANALYST_VIEW.
- `partner_status varchar(32) NOT NULL` - ACTIVE, AT_RISK, NEW, BLOCKED.
- `joined_at timestamptz NOT NULL` - дата входа в структуру.

Индексы: unique `person_number`, non-unique `sponsor_person_number`, `branch_id`, `structure_level`.

## Таблица mlm_campaign_metric
- `metric_id uuid PK`.
- `person_number varchar(32) FK -> mlm_partner_node.person_number`.
- `campaign_id varchar(32) NOT NULL`.
- `personal_volume numeric(14,2) NOT NULL`.
- `group_volume numeric(14,2) NOT NULL`.
- `active_downline_count int NOT NULL`.
- `risk_score numeric(5,2) NOT NULL`.

Unique constraint: `(person_number, campaign_id)`.

## Таблица mlm_qualification_progress
- `progress_id uuid PK`.
- `person_number varchar(32) FK`.
- `campaign_id varchar(32) NOT NULL`.
- `current_rank varchar(32) NOT NULL`.
- `next_rank varchar(32) NOT NULL`.
- `completion_percent numeric(5,2) NOT NULL`.
- `deadline_at timestamptz NOT NULL`.

## Таблица mlm_team_activity
- `activity_id uuid PK`.
- `person_number varchar(32) FK`.
- `campaign_id varchar(32) NOT NULL`.
- `activity_type varchar(48) NOT NULL` - ORDER_PLACED, INVITE_ACCEPTED, FIRST_ORDER, RISK_SIGNAL, UPGRADE_PROGRESS.
- `activity_status varchar(32) NOT NULL` - DONE, OPEN, BLOCKED.
- `occurred_at timestamptz NOT NULL`.

## Package ownership
- `api` - DTOs and enums.
- `domain` - `MlmStructureSnapshot` and `MlmStructureRepository` only.
- `db` - dedicated XML Liquibase changelog.
- `impl/controller` - REST controller.
- `impl/service` - service, in-memory repository and role/validation exceptions.
- `impl/config` - module metadata for OpenAPI grouping.