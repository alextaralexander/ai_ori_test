import { expect, type Page, test } from '@playwright/test';

const baseUrl = process.env.BESTORIGIN_FRONTEND_URL ?? 'http://localhost:5173';

async function loginAs(page: Page, role: 'customer') {
  await page.goto(`${baseUrl}/test-login?role=${role}`);
  await expect(page.locator('[data-testid="session-ready"]')).toHaveText(role);
}

export async function assertFeature002GreenPath(page: Page) {
  await page.goto(`${baseUrl}/news`);
  await expect(page.getByRole('main')).toBeVisible();
  await expect(page.getByTestId('news-feed')).toBeVisible();
  await expect(page.getByTestId('news-card-spring-collection')).toBeVisible();

  await page.getByTestId('news-card-spring-collection').click();
  await expect(page).toHaveURL(/\/content\/brand-care-guide/);
  await expect(page.getByTestId('content-page')).toBeVisible();
  await expect(page.getByTestId('content-section-rich-text')).toBeVisible();
  await expect(page.getByTestId('content-attachment-pdf')).toBeVisible();

  await page.goto(`${baseUrl}/offer/spring-offer`);
  await expect(page.getByTestId('offer-page')).toBeVisible();
  await expect(page.getByTestId('offer-conditions')).toBeVisible();
  await page.getByTestId('offer-primary-cta').click();
  await expect(page).toHaveURL(/\/catalog/);
}

test.describe('Feature 002 content pages and news', () => {
  test('guest opens news feed and content page', async ({ page }) => {
    await page.goto(`${baseUrl}/news`);

    await expect(page.getByRole('banner')).toBeVisible();
    await expect(page.getByRole('main')).toBeVisible();
    await expect(page.getByTestId('news-feed')).toBeVisible();
    await expect(page.getByTestId('news-card-spring-collection')).toBeVisible();

    await page.getByTestId('news-card-spring-collection').click();
    await expect(page).toHaveURL(/\/content\/brand-care-guide/);
    await expect(page.getByTestId('content-page')).toBeVisible();
    await expect(page.getByTestId('content-section-rich-text')).toBeVisible();
    await expect(page.getByTestId('content-attachment-pdf')).toBeVisible();
    await expect(page.getByRole('link', { name: /каталог/i }).first()).toBeVisible();
  });

  test('guest opens offer and follows catalog CTA', async ({ page }) => {
    await page.goto(`${baseUrl}/offer/spring-offer`);

    await expect(page.getByRole('banner')).toBeVisible();
    await expect(page.getByTestId('offer-page')).toBeVisible();
    await expect(page.getByTestId('offer-conditions')).toBeVisible();
    await page.getByTestId('offer-primary-cta').click();
    await expect(page).toHaveURL(/\/catalog/);
  });

  test('customer sees personal CTA on content page', async ({ page }) => {
    await loginAs(page, 'customer');
    await page.goto(`${baseUrl}/content/brand-care-guide`);

    await expect(page.getByTestId('content-page')).toBeVisible();
    await expect(page.getByTestId('content-cta-customer-cart')).toBeVisible();
    await expect(page.getByRole('link', { name: /каталог/i }).first()).toBeVisible();
  });

  test('unpublished content shows unavailable state', async ({ page }) => {
    await page.goto(`${baseUrl}/content/expired-material`);

    await expect(page.getByTestId('content-unavailable')).toBeVisible();
    await expect(page.getByRole('link', { name: /новости/i }).first()).toBeVisible();
    await expect(page.getByRole('link', { name: /каталог/i }).first()).toBeVisible();
  });
});
