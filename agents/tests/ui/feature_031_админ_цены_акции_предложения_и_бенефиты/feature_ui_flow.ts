import { expect, test, type Page } from '@playwright/test';

export async function runFeature031AdminPricingFlow(page: Page) {
  await page.goto('/test-login?role=pricing-manager');
  await expect(page.getByTestId('session-ready')).toContainText('pricing-manager');

  await page.goto('/admin/pricing');
  await expect(page.getByTestId('admin-pricing-page')).toBeVisible();
  await expect(page.getByTestId('admin-pricing-price-list-table')).toBeVisible();

  await page.getByTestId('admin-pricing-create-price-list').click();
  await page.getByTestId('admin-pricing-price-list-code').fill('PL-RU-2026-05');
  await page.getByTestId('admin-pricing-price-list-name').fill('Цены майской кампании');
  await page.getByTestId('admin-pricing-campaign-id').fill('CAM-2026-05');
  await page.getByTestId('admin-pricing-price-list-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PRICING_PRICE_LIST_SAVED');

  await page.getByTestId('admin-pricing-tab-prices').click();
  await page.getByTestId('admin-pricing-price-sku').fill('BOG-SERUM-001');
  await page.getByTestId('admin-pricing-base-price').fill('1890.00');
  await page.getByTestId('admin-pricing-price-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PRICING_PRICE_SAVED');

  await page.getByTestId('admin-pricing-promo-price-add').click();
  await page.getByTestId('admin-pricing-promo-price-sku').fill('BOG-SERUM-001');
  await page.getByTestId('admin-pricing-promo-price-value').fill('1590.00');
  await page.getByTestId('admin-pricing-promo-price-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PRICING_PROMO_PRICE_SAVED');

  await page.getByTestId('admin-pricing-tab-segment-rules').click();
  await page.getByTestId('admin-pricing-segment-code').fill('PARTNER');
  await page.getByTestId('admin-pricing-role-code').fill('PARTNER');
  await page.getByTestId('admin-pricing-segment-rule-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PRICING_SEGMENT_RULE_SAVED');

  await page.goto('/test-login?role=promotions-manager');
  await expect(page.getByTestId('session-ready')).toContainText('promotions-manager');
  await page.goto('/admin/pricing');
  await page.getByTestId('admin-pricing-tab-promotions').click();
  await page.getByTestId('admin-pricing-create-promotion').click();
  await page.getByTestId('admin-pricing-promotion-code').fill('MAY-BEAUTY-BUNDLE');
  await page.getByTestId('admin-pricing-promotion-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PRICING_PROMOTION_SAVED');

  await page.getByTestId('admin-pricing-offer-code').fill('MAY-SERUM-CREAM-BUNDLE');
  await page.getByTestId('admin-pricing-offer-type').selectOption('BUNDLE');
  await page.getByTestId('admin-pricing-offer-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PRICING_OFFER_SAVED');

  await page.getByTestId('admin-pricing-gift-sku').fill('BOG-GIFT-MASK-001');
  await page.getByTestId('admin-pricing-gift-threshold').fill('5000.00');
  await page.getByTestId('admin-pricing-gift-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PRICING_GIFT_RULE_SAVED');

  await page.goto('/test-login?role=business-admin');
  await expect(page.getByTestId('session-ready')).toContainText('business-admin');
  await page.goto('/admin/pricing');
  await page.getByTestId('admin-pricing-tab-publish').click();
  await page.getByTestId('admin-pricing-publish-preflight').click();
  await expect(page.getByTestId('admin-pricing-preflight-summary')).toContainText('valid');
  await page.getByTestId('admin-pricing-publish-start').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PRICING_PUBLISHED');

  await page.getByTestId('admin-pricing-pause-offer').click();
  await page.getByTestId('admin-pricing-pause-reason').fill('INCORRECT_DISCOUNT');
  await page.getByTestId('admin-pricing-pause-confirm').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PRICING_OFFER_PAUSED');

  await page.getByTestId('admin-pricing-tab-audit').click();
  await expect(page.getByTestId('admin-pricing-audit-table')).toContainText('SHOPPING_OFFER_PAUSED');
}

test('admin pricing publishes prices promotions offers and gifts', async ({ page }) => {
  await runFeature031AdminPricingFlow(page);
});

test('admin pricing rejects forbidden user', async ({ page }) => {
  await page.goto('/test-login?role=employee-support');
  await expect(page.getByTestId('session-ready')).toContainText('employee-support');
  await page.goto('/admin/pricing');
  await expect(page.getByTestId('admin-pricing-forbidden')).toContainText('STR_MNEMO_ADMIN_PRICING_FORBIDDEN');
});
