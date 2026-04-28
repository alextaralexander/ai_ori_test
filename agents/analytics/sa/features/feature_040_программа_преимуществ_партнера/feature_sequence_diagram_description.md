# Sequence description. Feature #40. Программа преимуществ партнера

## Основной runtime-поток
Партнер открывает `/member-benefits`. Frontend восстанавливает session context через auth module, получает роли и active partner, затем вызывает `GET /api/partner-benefits/me/summary`. Модуль `partner-benefits` проверяет `PARTNER_BENEFITS_VIEW`, ownership active partner и собирает данные из собственных таблиц, `catalog`, `admin-pricing`, `bonus-wallet` и `admin-referral`. Ответ содержит структурированные DTO: benefits, progress, referral link, reward balance, reward availability и retention offers. Frontend локализует все пользовательские тексты через i18n dictionaries; backend возвращает только data и mnemonic-коды.

## Применение выгоды
При переходе в cart/checkout frontend отправляет в `partner-benefits` apply-preview, чтобы показать партнеру применимость. Preview не является финальным применением. Checkout перед подтверждением заказа вызывает backend validation по `benefitId`, owner, status, catalogId, expiry, target и compatibility rules. Только подтвержденная backend выгода участвует в totals. Ошибки возвращаются как `STR_MNEMO_PARTNER_BENEFITS_EXPIRED`, `STR_MNEMO_PARTNER_BENEFITS_OWNER_MISMATCH`, `STR_MNEMO_PARTNER_BENEFITS_CATALOG_MISMATCH` или другой код семейства `STR_MNEMO_PARTNER_BENEFITS_*`.

## Referral
Frontend получает персональную referral link и QR payload через `GET /me/referral-link`. Код не ротируется при обычном чтении. `admin-referral` и `mlm-structure` определяют attribution policy и ownership. Когда привлеченный пользователь выполняет qualifying action, referral event приходит в `partner-benefits`, создается benefit grant типа `RECOMMENDATION_DISCOUNT`, отправляется notification event и пишется audit event с correlationId.

## Reward redemption
Reward shop возвращает только разрешенную витрину наград. При redemption обязательны `Idempotency-Key` и optimistic locking. `partner-benefits` проверяет баланс, доступность reward, регион, склад, catalogId, срок действия и лимиты, затем резервирует или списывает reward balance через `bonus-wallet` и публикует fulfillment event. Повторный запрос с тем же idempotency key не создает дубль. Конфликт версии или недостаточный баланс возвращает HTTP 409 и mnemonic-код.

## Support view
Сотрудник поддержки открывает timeline по `partnerNumber`. `partner-benefits` проверяет `PARTNER_BENEFITS_SUPPORT_VIEW`, маскирует PII, показывает только разрешенные действия и пишет audit event просмотра. Любое ручное действие поддержки должно иметь reasonCode, actorUserId, sourceSystem и correlationId.

## Версионная база
Sequence artifact создан 28.04.2026 для baseline Java 25, Spring Boot 4.0.6, React/TypeScript/Ant Design и runtime OpenAPI через springdoc-openapi. Фича не вводит новый технологический стек; compatibility fallback должен быть документирован отдельно.
