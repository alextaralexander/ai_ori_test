# OpenAPI-описание feature 016 для module_mlm-structure

Модуль публикует REST API под `/api/mlm-structure`. Все контроллеры находятся в `com.bestorigin.monolith.mlmstructure.impl.controller`, поэтому runtime Swagger группы `mlm-structure` должен автоматически включать endpoints модуля и иметь канонические URL `/v3/api-docs/mlm-structure` и `/swagger-ui/mlm-structure`.

## Endpoints
- `GET /api/mlm-structure/dashboard` - dashboard текущей кампании лидера: объемы, активные партнеры, rank, qualificationPercent, nextActions, publicMnemo.
- `GET /api/mlm-structure/community` - список downline с фильтрами `campaignId`, `level`, `branchId`, `status`.
- `GET /api/mlm-structure/conversion` - funnel invite-to-first-order: sent, accepted, registered, activated, firstOrder, conversionRatePercent.
- `GET /api/mlm-structure/team-activity` - активности команды, включая riskOnly.
- `GET /api/mlm-structure/upgrade` - условия следующего ранга, deadline и блокирующие требования.
- `GET /api/mlm-structure/partners/{personNumber}` - карточка партнера и переходы к заказам, бонусам и supply.

## DTOs
DTOs объявляются в `MlmStructureDtos`: `MlmVolume`, `MlmDashboardResponse`, `MlmPartnerNodeResponse`, `MlmCommunityResponse`, `MlmConversionFunnelResponse`, `MlmTeamActivityResponse`, `MlmUpgradeResponse`, `MlmPartnerCardResponse`, `MlmStructureErrorResponse`.

## Validations and STR_MNEMO
- `STR_MNEMO_MLM_STRUCTURE_DASHBOARD_READY` - dashboard готов.
- `STR_MNEMO_MLM_STRUCTURE_ACCESS_DENIED` - роль не имеет доступа.
- `STR_MNEMO_MLM_STRUCTURE_FILTER_INVALID` - неверный фильтр.
- `STR_MNEMO_MLM_STRUCTURE_PARTNER_NOT_FOUND` - personNumber не найден.
- `STR_MNEMO_MLM_STRUCTURE_UPGRADE_READY` - условия апгрейда рассчитаны.
- `STR_MNEMO_MLM_STRUCTURE_PARTNER_CARD_READY` - карточка партнера готова.

Backend не передает hardcoded user-facing text во frontend, только mnemonic-коды.