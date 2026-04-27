# Acceptance criteria feature 016. MLM-структура и рост партнера

## Функциональные критерии
1. `/business` доступен партнерским и управленческим ролям после логина и показывает dashboard с campaignId, личным объемом, групповым объемом, количеством активных партнеров, текущим рангом, следующим рангом и статусом квалификации.
2. `/business/beauty-community` показывает структуру downline по уровням и веткам, включая personNumber, имя, роль, статус, личный объем, групповой объем и риск просадки.
3. `/business/conversion` показывает funnel приглашений: отправлено invite, принято, зарегистрировано, активировано, сделан первый заказ, а также conversionRatePercent.
4. `/business/team-activity` показывает список событий команды и партнеров с флагами риска, последней активностью и drill-down ссылкой в карточку партнера.
5. `/business/upgrade` показывает текущий и целевой ранг, deadline текущей кампании, выполненные и невыполненные условия апгрейда.
6. `/business/partner-card/:personNumber` возвращает карточку партнера по `personNumber` и отображает role, status, structureLevel, sponsorPersonNumber, personalVolume, groupVolume, qualificationProgress и переходы к заказам/бонусам/поставкам.
7. API поддерживает фильтры `campaignId`, `level`, `branchId`, `status`, `riskOnly` без разрушения зеленого пути, а некорректные фильтры возвращают mnemonic-код `STR_MNEMO_MLM_STRUCTURE_FILTER_INVALID`.
8. При отсутствии доступа backend возвращает только mnemonic-код `STR_MNEMO_MLM_STRUCTURE_ACCESS_DENIED`; hardcoded пользовательский текст из backend во frontend не передается.
9. Все новые frontend-строки добавлены в `resources_ru.ts` и `resources_en.ts`; компоненты используют только `t(...)`.
10. Swagger/OpenAPI runtime должен автоматически включать контроллеры модуля `mlm-structure` через пакет `com.bestorigin.monolith.mlmstructure.impl` и канонический путь `/v3/api-docs/mlm-structure`.

## Data/API критерии
- Backend-модуль соблюдает разделение `api`, `domain`, `db`, `impl`; runtime-классы находятся в role-specific подпакетах `impl/controller`, `impl/service`, `impl/config`.
- Liquibase changeset хранится отдельным XML-файлом `feature_016_mlm_structure.xml` в changelog-папке owning module.
- Тестовые данные покрывают минимум одну структуру с лидером, двумя ветками, тремя уровнями, карточкой партнера `BOG-016-002`, квалификацией и рисковым партнером.

## UI критерии
- На desktop dashboard, структура, conversion, activity, upgrade и partner card отображаются без перекрытий и используют стабильные test id.
- На mobile ключевые блоки перестраиваются в одну колонку, карточки партнеров остаются читаемыми, кнопки переходов не выходят за контейнеры.
- Состояния загрузки, пустых данных, доступа запрещен и готовности квалификации отображаются через i18n.

## Тестовые критерии
- Managed API test начинается с логина роли и проверяет dashboard, структуру, conversion, upgrade, карточку партнера и запрет доступа.
- Managed UI test начинается с `/test-login` и проходит маршруты `/business`, `/business/beauty-community`, `/business/conversion`, `/business/team-activity`, `/business/upgrade`, `/business/partner-card/BOG-016-002`.
- End-to-end managed tests агрегируют реальный feature #16 test, а не содержат placeholder assertion.