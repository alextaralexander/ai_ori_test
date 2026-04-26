import { expect, test } from '@playwright/test';

test('customer opens product card and adds product to cart', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/product/BOG-CREAM-001');
  await expect(page.getByTestId('product-card-page')).toBeVisible();
  await expect(page.getByTestId('product-card-title')).toBeVisible();
  await expect(page.getByTestId('product-card-gallery')).toBeVisible();
  await expect(page.getByTestId('product-card-information')).toBeVisible();
  await expect(page.getByTestId('product-card-recommendations')).toBeVisible();
  await page.getByTestId('product-card-quantity').fill('2');
  await page.getByTestId('product-card-add-to-cart').click();
  await expect(page.getByTestId('cart-summary-count')).toContainText('2');
  await expect(page.getByTestId('product-card-cart-message')).toContainText(/добавлен|added/i);
  await page.getByTestId('product-card-checkout').click();
  await expect(page).toHaveURL(/\/checkout/);
});

test('partner adds product from card with partner context', async ({ page }) => {
  await page.goto('/test-login?role=partner');
  await expect(page.getByTestId('session-ready')).toContainText('partner');
  await page.goto('/product/BOG-CREAM-001');
  await expect(page.getByTestId('product-card-partner-context')).toBeVisible();
  await page.getByTestId('product-card-add-to-cart').click();
  await expect(page.getByTestId('cart-summary-count')).toContainText('1');
  await expect(page.getByTestId('product-card-cart-message')).toContainText(/добавлен|added/i);
});

test('unknown product card shows localized not found state', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/product/UNKNOWN-PRODUCT');
  await expect(page.getByTestId('product-card-not-found')).toBeVisible();
  await expect(page.getByTestId('product-card-not-found')).toContainText(/не найден|not found/i);
  await expect(page.getByTestId('product-card-back-to-search')).toBeVisible();
});

test('unavailable product cannot be added from card', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/product/BOG-SOLDOUT-001');
  await expect(page.getByTestId('product-card-page')).toBeVisible();
  await expect(page.getByTestId('product-card-availability')).toContainText(/недоступ|unavailable|out of stock/i);
  await page.getByTestId('product-card-add-to-cart').click();
  await expect(page.getByTestId('product-card-cart-message')).toContainText(/недоступ|unavailable|out of stock/i);
});
