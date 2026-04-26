// Synchronized from agents/tests/. Do not edit this runtime copy manually.
import { expect, test } from '@playwright/test';

test('guest searches catalog with filters and sorting', async ({ page }) => {
  await page.goto('/search');
  await expect(page.getByTestId('catalog-search-page')).toBeVisible();
  await page.getByTestId('catalog-search-input').fill('cream');
  await page.getByTestId('catalog-category-filter').selectOption('face-care');
  await page.getByTestId('catalog-availability-filter').selectOption('inStock');
  await page.getByTestId('catalog-promo-filter').check();
  await page.getByTestId('catalog-sort-select').selectOption('popular');
  await page.getByTestId('catalog-search-submit').click();
  await expect(page).toHaveURL(/q=cream/);
  await expect(page.getByTestId('catalog-card-BOG-CREAM-001')).toBeVisible();
  await expect(page.getByTestId('catalog-card-add-BOG-CREAM-001')).toBeVisible();
});

test('guest sees empty search state with recommendations', async ({ page }) => {
  await page.goto('/search?q=missing%20product');
  await expect(page.getByTestId('catalog-empty-state')).toBeVisible();
  await expect(page.getByTestId('catalog-empty-state')).toContainText(/ничего не найдено|No products found/i);
  await expect(page.getByTestId('catalog-recommendations')).toBeVisible();
  await expect(page.getByTestId('catalog-card-BOG-CREAM-001')).toBeVisible();
});

test('customer adds available product to cart from search result', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/search?q=cream&availability=inStock');
  await page.getByTestId('catalog-card-add-BOG-CREAM-001').click();
  await expect(page.getByTestId('cart-summary-count')).toContainText('1');
  await expect(page.getByTestId('catalog-cart-message')).toContainText(/добавлен|added/i);
});

test('partner sees partner badges and adds product with partner context', async ({ page }) => {
  await page.goto('/test-login?role=partner');
  await expect(page.getByTestId('session-ready')).toContainText('partner');
  await page.goto('/search?promo=true');
  await expect(page.getByTestId('catalog-partner-context')).toBeVisible();
  await expect(page.getByTestId('catalog-card-BOG-CREAM-001')).toContainText(/партнер|partner/i);
  await page.getByTestId('catalog-card-add-BOG-CREAM-001').click();
  await expect(page.getByTestId('cart-summary-count')).toContainText('1');
});
