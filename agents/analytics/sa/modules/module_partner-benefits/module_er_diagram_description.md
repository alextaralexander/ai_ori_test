# Module ER description. partner-benefits

## Назначение
`partner-benefits` является owning module пользовательской программы преимуществ партнера Best Ori Gin. Модуль хранит персональные выгоды, прогресс условий, referral-ссылку и события привлечения, reward shop runtime-витрину, redemption, удерживающие офферы и audit trail. Административная настройка правил остается в `admin-pricing`, `admin-referral`, `admin-bonus` и CRM/marketing контурах.

## Package ownership
- `api`: внешние DTO и enum-контракты REST API.
- `domain`: JPA entities и repository interfaces.
- `db`: только Liquibase XML changelog files.
- `impl/controller`: Spring MVC controllers.
- `impl/service`: eligibility, summary, referral, reward redemption, support timeline.
- `impl/security`: RBAC scopes, active partner ownership, support masking policy.
- `impl/mapper`: MapStruct преобразования entity/DTO.
- `impl/event`: domain events для checkout, wallet, fulfillment, notification и audit.
- `impl/client`: adapters к catalog, pricing, cart/order, wallet, referral, fulfillment и platform-experience.

## Таблицы
- `partner_benefit_account`: root aggregate по partner/user. Unique по `partner_id`, `user_id`, `partner_number`; optimistic locking через `version`.
- `partner_benefit_grant`: персональная выгода с типом, статусом, source reference, catalog window, application target, amount и mnemonic state.
- `partner_benefit_progress`: условия выполнения конкретной выгоды; unique `(benefit_id, condition_code)`.
- `partner_referral_link`: персональная referral link и QR payload hash; `referral_code` уникален и не ротируется при чтении.
- `partner_referral_event`: маскированная referral activity и qualifying action results.
- `partner_reward_catalog_item`: runtime-витрина наград, доступная партнерскому контуру.
- `partner_reward_redemption`: idempotent redemption с optimistic locking и ссылкой на fulfillment.
- `partner_retention_offer`: персональный удерживающий offer с audience/risk reason/priority.
- `partner_benefit_audit_event`: immutable audit событий программы преимуществ.

## Ключевые индексы
- `partner_benefit_grant(account_id, benefit_status, expires_at)` для кабинета выгод и warning banners.
- `partner_benefit_grant(account_id, catalog_id)` для текущего и следующего каталога.
- `partner_referral_event(referral_link_id, occurred_at)` для referral timeline.
- `partner_reward_redemption(account_id, created_at)` и unique `idempotency_key` для защиты от дублей.
- `partner_benefit_audit_event(account_id, occurred_at)`, `(action_code, occurred_at)`, `(correlation_id)` для support и audit investigations.

## Связи с другими модулями
`partner-benefits` читает catalog windows, pricing/offers compatibility, referral attribution policy, wallet balance, checkout validation context, delivery/fulfillment availability и platform notification contracts через APIs/adapters. Модуль не дублирует товары, цены, заказы, платежи, MLM-дерево или административные справочники.

## Версионная база
Baseline зафиксирован на 28.04.2026: Java 25, Spring Boot 4.0.6, Hibernate, MapStruct, Lombok, PostgreSQL, Liquibase XML. При runtime fallback из-за совместимости монолита должно быть создано architecture decision с причиной и upgrade follow-up.
