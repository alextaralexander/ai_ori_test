import { expect, type Page, test } from '@playwright/test';

const baseUrl = process.env.BESTORIGIN_FRONTEND_URL ?? 'http://localhost:5173';

async function loginAs(page: Page, role: 'customer' | 'partner') {
  await page.goto(`${baseUrl}/test-login?role=${role}`);
  await expect(page.locator('[data-testid="session-ready"]')).toHaveText(role);
}

test.describe('Feature 001 public home and navigation', () => {
  test('guest opens public home and navigates to catalog', async ({ page }) => {
    await page.goto(`${baseUrl}/`);

    await expect(page.getByRole('banner')).toBeVisible();
    await expect(page.getByRole('main')).toBeVisible();
    await expect(page.getByTestId('home-hero')).toBeVisible();
    await expect(page.getByTestId('home-promo-section')).toBeVisible();
    await expect(page.getByRole('link', { name: /каталог/i }).first()).toBeVisible();
    await expect(page.getByRole('link', { name: /community/i }).first()).toBeVisible();

    await page.getByRole('link', { name: /каталог/i }).first().click();
    await expect(page).toHaveURL(/\/catalog/);
  });

  test('guest opens community and sees shared navigation', async ({ page }) => {
    await page.goto(`${baseUrl}/community`);

    await expect(page.getByRole('banner')).toBeVisible();
    await expect(page.getByRole('main')).toBeVisible();
    await expect(page.getByTestId('community-overview')).toBeVisible();
    await expect(page.getByRole('contentinfo')).toBeVisible();
    await expect(page.getByRole('link', { name: /регистрация/i }).first()).toBeVisible();
  });

  test('customer sees personal entry points with public navigation', async ({ page }) => {
    await loginAs(page, 'customer');
    await page.goto(`${baseUrl}/home`);

    await expect(page.getByTestId('entry-profile')).toBeVisible();
    await expect(page.getByTestId('entry-cart')).toBeVisible();
    await expect(page.getByTestId('entry-orders')).toBeVisible();
    await expect(page.getByRole('link', { name: /каталог/i }).first()).toBeVisible();
    await expect(page.getByRole('link', { name: /community/i }).first()).toBeVisible();
  });

  test('partner can open partner office entry point', async ({ page }) => {
    await loginAs(page, 'partner');
    await page.goto(`${baseUrl}/`);

    await expect(page.getByTestId('entry-partner-office')).toBeVisible();
    await page.getByTestId('entry-partner-office').click();
    await expect(page).toHaveURL(/\/partner-office/);
  });
});
