// Синхронизировано из agents/tests. Не редактировать вручную.
import { expect, test } from '@playwright/test';

test('customer opens cart and manages product quantity', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/product/BOG-CREAM-001');
  await page.getByTestId('product-card-quantity').fill('2');
  await page.getByTestId('product-card-add-to-cart').click();
  await page.goto('/cart');
  await expect(page.getByTestId('cart-page')).toBeVisible();
  await expect(page.getByTestId('cart-line-BOG-CREAM-001')).toBeVisible();
  await expect(page.getByTestId('cart-line-BOG-CREAM-001-quantity')).toContainText('2');
  await page.getByTestId('cart-line-BOG-CREAM-001-increase').click();
  await expect(page.getByTestId('cart-line-BOG-CREAM-001-quantity')).toContainText('3');
  await expect(page.getByTestId('cart-totals')).toBeVisible();
});

test('customer applies shopping offer from main cart', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/cart');
  await expect(page.getByTestId('cart-page')).toBeVisible();
  await page.goto('/cart/shopping-offers');
  await expect(page.getByTestId('cart-shopping-offers-page')).toBeVisible();
  await expect(page.getByTestId('cart-offer-SET-GLOW-001')).toBeVisible();
  await page.getByTestId('cart-offer-SET-GLOW-001-apply').click();
  await expect(page.getByTestId('cart-offer-SET-GLOW-001-status')).toContainText(/APPLIED|AVAILABLE/);
  await expect(page.getByTestId('cart-offer-message')).toBeVisible();
});

test('unavailable cart line blocks checkout with controlled message', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/cart?seed=removed-item');
  await expect(page.getByTestId('cart-page')).toBeVisible();
  await expect(page.getByTestId('cart-line-BOG-REMOVED-003')).toBeVisible();
  await expect(page.getByTestId('cart-line-BOG-REMOVED-003-availability')).toContainText(/UNAVAILABLE|REMOVED_FROM_CAMPAIGN/);
  await expect(page.getByTestId('cart-checkout')).toBeDisabled();
  await expect(page.getByTestId('cart-validation-message')).toBeVisible();
});

test('partner manages supplementary order separately', async ({ page }) => {
  await page.goto('/test-login?role=partner');
  await expect(page.getByTestId('session-ready')).toContainText('partner');
  await page.goto('/cart/supplementary');
  await expect(page.getByTestId('supplementary-cart-page')).toBeVisible();
  await expect(page.getByTestId('supplementary-cart-type')).toContainText('SUPPLEMENTARY');
  await page.getByTestId('supplementary-add-BOG-CREAM-001').click();
  await expect(page.getByTestId('supplementary-line-BOG-CREAM-001')).toBeVisible();
  await page.goto('/cart/supplementary/shopping-offers');
  await expect(page.getByTestId('supplementary-shopping-offers-page')).toBeVisible();
  await expect(page.getByTestId('supplementary-offer-list')).toBeVisible();
  await expect(page.getByTestId('main-cart-line-BOG-CREAM-001')).toHaveCount(0);
});

test('order support can open permitted cart support view', async ({ page }) => {
  await page.goto('/test-login?role=order-support');
  await expect(page.getByTestId('session-ready')).toContainText('order-support');
  await page.goto('/support/carts/customer-009?cartType=MAIN');
  await expect(page.getByTestId('cart-support-view')).toBeVisible();
  await expect(page.getByTestId('cart-support-lines')).toBeVisible();
  await expect(page.getByTestId('cart-support-payment-token')).toHaveCount(0);
});

test('cart layout is usable on mobile width', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 900 });
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/cart');
  await expect(page.getByTestId('cart-page')).toBeVisible();
  await expect(page.getByTestId('cart-totals')).toBeVisible();
  await expect(page.getByTestId('cart-checkout')).toBeVisible();
  const horizontalOverflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth);
  expect(horizontalOverflow).toBe(false);
});
