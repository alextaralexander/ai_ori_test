// Synchronized from agents/tests/. Do not edit this runtime copy manually.
import { expect, test } from '@playwright/test';

test('guest opens personalized beauty benefits landing and starts registration', async ({ page }) => {
  await page.goto('/test-login?role=guest');
  await expect(page.getByTestId('session-ready')).toContainText('guest');
  await page.goto('/beauty-benefits/BOG777');
  await expect(page.getByTestId('benefit-landing-page')).toBeVisible();
  await expect(page.getByTestId('benefit-landing-type')).toContainText('BEAUTY');
  await expect(page.getByTestId('benefit-referral-status')).toContainText('ACTIVE');
  await expect(page.getByTestId('benefit-referral-code')).toContainText('BOG777');
  await page.getByTestId('benefit-cta-register').click();
  await expect(page).toHaveURL(/\/register/);
  await expect(page).toHaveURL(/code=BOG777/);
});

test('guest opens business benefits landing and navigates to catalog', async ({ page }) => {
  await page.goto('/test-login?role=guest');
  await expect(page.getByTestId('session-ready')).toContainText('guest');
  await page.goto('/business-benefits');
  await expect(page.getByTestId('benefit-landing-page')).toBeVisible();
  await expect(page.getByTestId('benefit-landing-type')).toContainText('BUSINESS');
  await expect(page.getByTestId('benefit-blocks')).toBeVisible();
  await page.getByTestId('benefit-cta-open-catalog').click();
  await expect(page).toHaveURL(/\/search/);
});

test('unknown referral code shows localized controlled state', async ({ page }) => {
  await page.goto('/test-login?role=guest');
  await expect(page.getByTestId('session-ready')).toContainText('guest');
  await page.goto('/beauty-benefits/UNKNOWN777');
  await expect(page.getByTestId('benefit-referral-status')).toContainText('NOT_FOUND');
  await expect(page.getByTestId('benefit-referral-message')).toBeVisible();
  await expect(page.getByTestId('benefit-referral-message')).toContainText(/код|code|referral/i);
  await expect(page.getByTestId('benefit-private-sponsor-data')).toHaveCount(0);
});

test('app benefits landing exposes install app action', async ({ page }) => {
  await page.goto('/test-login?role=guest');
  await expect(page.getByTestId('session-ready')).toContainText('guest');
  await page.goto('/the-new-oriflame-app');
  await expect(page.getByTestId('benefit-landing-type')).toContainText('APP');
  await expect(page.getByTestId('benefit-cta-install-app')).toBeVisible();
});
