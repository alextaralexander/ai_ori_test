// AUTO-GENERATED from agents/tests/. Do not edit this synchronized runtime copy manually.
import { test } from '@playwright/test';

import '../feature_001_публичная_главная_и_навигация/feature_ui_flow';
import '../feature_002_контентные_страницы_и_новости/feature_ui_flow';
import '../feature_003_faq_инфо_и_документы/feature_ui_flow';
import '../feature_004_каталог_и_поиск_товаров/feature_ui_flow';
import '../feature_005_карточка_товара/feature_ui_flow';
import '../feature_006_цифровые_каталоги_и_pdf_материалы/feature_ui_flow';
import '../feature_007_бьюти_и_бизнес_бенефиты/feature_ui_flow';
import '../feature_008_регистрация_и_инвайты_партнеров/feature_ui_flow';
import '../feature_009_корзина_и_промо_предложения/feature_ui_flow';
import '../feature_010_оформление_заказа_и_дозаказ/feature_ui_flow';
import '../feature_011_история_и_детали_заказов/feature_ui_flow';
import '../feature_012_претензии_и_возвраты/feature_ui_flow';
import '../feature_013_профиль_и_настройки_пользователя/feature_ui_flow';
import '../feature_014_бонусный_кошелек_и_транзакции/feature_ui_flow';
import '../feature_015_партнерские_отчеты_комиссии_и_документы/feature_ui_flow';
import '../feature_016_mlm_структура_и_рост_партнера/feature_ui_flow';
import '../feature_017_партнерские_офлайн_продажи_и_заказы_клиентов/feature_ui_flow';
import '../feature_018_партнерский_офис_поставки_и_логистика/feature_ui_flow';
import '../feature_019_сотрудник_новый_заказ_и_поддержка/feature_ui_flow';
import '../feature_020_сотрудник_история_заказов/feature_ui_flow';
import '../feature_021_сотрудник_претензии/feature_ui_flow';
import '../feature_022_сотрудник_карточка_партнера_и_отчеты/feature_ui_flow';
import '../feature_023_сотрудник_настройки_и_повышенные_права/feature_ui_flow';
import '../feature_024_аутентификация_роли_и_имперсонация/feature_ui_flow';
import '../feature_025_уведомления_офлайн_i18n_и_аналитика/feature_ui_flow';
import '../feature_026_админ_rbac_и_учетные_записи/feature_ui_flow';
import '../feature_027_админ_cms_контент_новости_faq_документы/feature_ui_flow';
import '../feature_028_админ_лендинги_инвайты_и_рефералы/feature_ui_flow';
import '../feature_029_админ_pim_каталог_товары_категории_медиа/feature_ui_flow';
import '../feature_030_админ_каталоги_кампаний_и_pdf/feature_ui_flow';
import '../feature_031_админ_цены_акции_предложения_и_бенефиты/feature_ui_flow';

import { runFeature032AdminWmsFlow } from '../feature_032_админ_wms_1c_остатки_склады_и_поставки/feature_ui_flow';
import { runFeature033AdminOrdersFlow } from '../feature_033_админ_заказы_платежи_и_дозаказы/feature_ui_flow';
import { runFeature034AdminServiceFlow } from '../feature_034_админ_претензии_возвраты_и_сервис/feature_ui_flow';
import { runFeature035AdminIdentityFlow } from '../feature_035_админ_пользователи_партнеры_сотрудники_и_имперсонация/feature_ui_flow';
import { runFeature036AdminPlatformFlow } from '../feature_036_админ_kpi_аудит_и_интеграции_платформы/feature_ui_flow';
import { runFeature037DeliveryFlow } from '../feature_037_доставка_отслеживание_и_точки_выдачи/feature_ui_flow';
import { runFeature038AdminBonusFlow } from '../feature_038_админ_бонусная_система_и_компенсационный_план/feature_ui_flow';
import { runFeature039AdminFulfillmentFlow } from '../feature_039_админ_конвейер_сборки_доставка_и_точки_выдачи/feature_ui_flow';

test('admin WMS green path participates in product e2e flow', async ({ page }) => {
  await runFeature032AdminWmsFlow(page);
});

test('admin orders and payments green path participates in product e2e flow', async ({ page }) => {
  await runFeature033AdminOrdersFlow(page);
});

test('admin service claims returns replacements and SLA green path participates in product e2e flow', async ({ page }) => {
  await runFeature034AdminServiceFlow(page);
});

test('admin identity users partners employees and impersonation green path participates in product e2e flow', async ({ page }) => {
  await runFeature035AdminIdentityFlow(page);
});


test('admin platform KPI audit and integrations green path participates in product e2e flow', async ({ page }) => {
  await runFeature036AdminPlatformFlow(page);
});

test('delivery tracking and pickup points green path participates in product e2e flow', async ({ page }) => {
  await runFeature037DeliveryFlow(page);
});

test('admin bonus compensation plan and payout green path participates in product e2e flow', async ({ page }) => {
  await runFeature038AdminBonusFlow(page);
});

test('admin fulfillment conveyor delivery and pickup points green path participates in product e2e flow', async ({ page }) => {
  await runFeature039AdminFulfillmentFlow(page);
});
