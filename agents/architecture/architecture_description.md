# Architecture description after feature 012

## Контекст
Платформа Best Ori Gin использует frontend web-shell и Spring Boot monolith. Feature #12 добавляет пользовательский контур претензий и возвратов после заказа. Основной owning backend module - `order`.

## Компоненты
- `OrderClaimsView` отвечает за создание претензии, историю претензий и детали кейса на маршрутах `/order/claims/claim-create`, `/order/claims/claims-history`, `/order/claims/claims-history/:claimId`.
- `Order claims API` расширяет module_order ресурсами `/api/order/claims`.
- `OrderClaimService` валидирует заказ и позиции, рассчитывает компенсацию, ведет audit trail и маршрутизирует проверку в склад, логистику или платежи.
- `i18n resources` содержат все новые пользовательские строки и mnemonic-коды для русского и английского языков.

## Пакетная ownership-модель
Backend сохраняет `api/domain/db/impl`: DTO in `order/api`, domain snapshots/repository in `order/domain`, runtime classes in `order/impl/controller` and `order/impl/service`, dedicated Liquibase XML in order changelog resources.

## Версионная база
Новые технологии не вводятся. Реализация использует текущие Java, Spring Boot, Maven, React, TypeScript and Ant Design-compatible patterns repository baseline. При последующем upgrade нужно обновить этот раздел в task artifacts.