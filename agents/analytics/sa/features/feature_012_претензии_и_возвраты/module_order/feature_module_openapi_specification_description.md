# Feature module OpenAPI description. Feature 012, module_order

## Контракт
Feature #12 расширяет `/api/order` ресурсами `/claims`, `/claims/{claimId}` и `/claims/{claimId}/comments`. Контракт отдает machine-readable статусы, суммы, nextAction, события, вложения и mnemonic-коды для публичных сообщений.

## Основные ответы
- Claim history page используется страницей `/order/claims/claims-history` и поддерживает query/status/resolution filters.
- Claim details используется страницей `/order/claims/claims-history/:claimId` и страницей результата создания.
- Create claim request принимает orderNumber, выбранные items, reasonCode, requestedResolution и comment.

## Ошибки
Ошибки возвращаются через существующий `ErrorResponse` с кодами `STR_MNEMO_ORDER_CLAIM_*`. Backend не возвращает hardcoded user-facing text во frontend.

## Версионная база
Новые endpoints реализуются в текущем Spring Boot monolith без смены технологического baseline. Swagger/OpenAPI для monolith остается runtime-generated через springdoc group module_order; данный YAML является SA-артефактом целевого контракта.