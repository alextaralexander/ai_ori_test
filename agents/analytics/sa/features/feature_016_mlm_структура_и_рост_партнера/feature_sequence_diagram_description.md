# Sequence-описание feature 016

Пользователь входит в web-shell через `/test-login` с ролью `partner-leader`, `business-manager` или `mlm-analyst`. Frontend определяет маршрут `/business*`, загружает публичный shell и вызывает API модуля `mlm-structure`.

Основной поток:
1. `/business` вызывает `GET /api/mlm-structure/dashboard?campaignId=CAT-2026-05` и получает KPI, rank, qualificationPercent, nextActions и mnemonic готовности.
2. `/business/beauty-community` вызывает `GET /api/mlm-structure/community` с фильтрами branch/level/status и получает список downline.
3. `/business/conversion` вызывает `GET /api/mlm-structure/conversion` и отображает invite funnel.
4. `/business/team-activity` вызывает `GET /api/mlm-structure/team-activity` и показывает события команды и риски.
5. `/business/upgrade` вызывает `GET /api/mlm-structure/upgrade` и отображает условия следующего ранга.
6. `/business/partner-card/:personNumber` вызывает `GET /api/mlm-structure/partners/{personNumber}` и получает карточку партнера с linkedActions в modules order, bonus-wallet, partner-reporting и future supply.

Если роль не разрешена, backend возвращает `STR_MNEMO_MLM_STRUCTURE_ACCESS_DENIED`. Если `personNumber` отсутствует, возвращается `STR_MNEMO_MLM_STRUCTURE_PARTNER_NOT_FOUND`. Frontend переводит эти коды через i18n.