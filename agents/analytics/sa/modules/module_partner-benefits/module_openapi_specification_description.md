# Module OpenAPI description. partner-benefits

## Swagger group
`partner-benefits` публикуется как отдельная monolith OpenAPI group:
- `/v3/api-docs/partner-benefits`
- `/swagger-ui/partner-benefits`

`MonolithModule.moduleKey()` должен возвращать `partner-benefits`, package prefix должен быть `com.bestorigin.monolith.partnerbenefits`. Controllers размещаются в `impl/controller`; manual endpoint registries запрещены.

## Endpoint groups
- `/me/summary`: персональная сводка выгод, referral, rewards и retention offers.
- `/me/benefits/{benefitId}/apply-preview`: предварительная проверка применимости выгоды. Финальная проверка остается за checkout backend.
- `/me/referral-link` и `/me/referral-events`: referral link, QR payload и маскированная activity.
- `/me/rewards` и `/me/rewards/{rewardId}/redemptions`: reward shop и idempotent redemption.
- `/support/accounts/{partnerNumber}/timeline`: support timeline с маскировкой персональных данных.

## Security scopes
`PARTNER_BENEFITS_VIEW`, `PARTNER_BENEFITS_REFERRAL_VIEW`, `PARTNER_BENEFITS_REWARD_VIEW`, `PARTNER_BENEFITS_REWARD_REDEEM`, `PARTNER_BENEFITS_SUPPORT_VIEW`, `PARTNER_BENEFITS_SUPPORT_ACTION`, `PARTNER_BENEFITS_AUDIT_VIEW`.

## Message contract
Backend возвращает только structured data и mnemonic-коды:
- `STR_MNEMO_PARTNER_BENEFITS_ACCESS_DENIED`
- `STR_MNEMO_PARTNER_BENEFITS_VALIDATION_FAILED`
- `STR_MNEMO_PARTNER_BENEFITS_NOT_FOUND`
- `STR_MNEMO_PARTNER_BENEFITS_EXPIRED`
- `STR_MNEMO_PARTNER_BENEFITS_ALREADY_CONSUMED`
- `STR_MNEMO_PARTNER_BENEFITS_OWNER_MISMATCH`
- `STR_MNEMO_PARTNER_BENEFITS_CATALOG_MISMATCH`
- `STR_MNEMO_PARTNER_BENEFITS_REWARD_UNAVAILABLE`
- `STR_MNEMO_PARTNER_BENEFITS_REWARD_BALANCE_INSUFFICIENT`
- `STR_MNEMO_PARTNER_BENEFITS_VERSION_CONFLICT`
- `STR_MNEMO_PARTNER_BENEFITS_REFERRAL_REJECTED`
- `STR_MNEMO_PARTNER_BENEFITS_SUPPORT_ACTION_FORBIDDEN`

Frontend обязан локализовать эти коды через dictionaries всех поддерживаемых языков.

## Integration contracts
Модуль интегрируется с `auth`, `catalog`, `admin-pricing`, `cart`, `order`, `bonus-wallet`, `admin-bonus`, `admin-referral`, `mlm-structure`, `delivery`, `admin-fulfillment`, `platform-experience` и `admin-platform`. Внешним потребителям не передаются hardcoded user-facing тексты.

## Version baseline
Baseline на 28.04.2026: Java 25, Spring Boot 4.0.6, springdoc-openapi, Maven, Liquibase XML, PostgreSQL, MapStruct, Lombok. Compatibility fallback допускается только с документированной причиной и follow-up задачей.
