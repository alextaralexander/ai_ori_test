# Архитектура Best Ori Gin

Архитектура остается domain-oriented monolith: backend размещен в `backend/monolith/monolith-app`, frontend в `frontend/monolith/apps/web-shell`. Каждый backend-модуль соблюдает разделение `api`, `domain`, `db`, `impl`.

## Модули
- `public-content` - публичная витрина, новости, FAQ, документы, benefits.
- `catalog` - каталог, поиск, карточка товара, цифровые каталоги.
- `cart` - корзина, промо и дозаказ.
- `order` - checkout, история заказов, претензии.
- `profile` - профиль, настройки и support-доступ.
- `bonus-wallet` - бонусный кошелек и транзакции.
- `partner-onboarding` - invite, регистрация и активация партнера.
- `partner-reporting` - партнерские отчеты, комиссии, документы и финансовая сверка.
- `mlm-structure` - feature #16: MLM dashboard, downline, conversion, team activity, upgrade и карточка партнера.

## Связи
Frontend вызывает backend-модули по HTTP JSON. `mlm-structure` предоставляет `/api/mlm-structure/*` и возвращает только DTO и mnemonic-коды `STR_MNEMO_*` для predefined пользовательских сообщений. Карточка партнера содержит linkedActions на маршруты order, bonus-wallet, partner-reporting и будущие supply-сценарии. Order взаимодействует с Payment, WMS/1C и Delivery по интеграционным API.

## Version baseline
Для feature #16 новые runtime-технологии не добавляются. Используется baseline проекта на 27.04.2026: Java 25, Spring Boot 4.0.6, Maven, XML Liquibase, React/TypeScript/Ant Design/Vite web-shell с latest-зависимостями package.json.