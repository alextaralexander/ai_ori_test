import { expect, test } from '@playwright/test';

test('platform experience shows localized notification and offline recovery', async ({ context, page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');

  await page.goto('/checkout');
  await expect(page.getByTestId('platform-notification-root')).toBeVisible();

  await page.getByTestId('platform-demo-success-notification').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_PLATFORM_NOTIFICATION_DEMO_SUCCESS');

  await context.setOffline(true);
  await expect(page.getByTestId('platform-offline-popup')).toBeVisible();
  await expect(page.getByTestId('platform-offline-popup')).toContainText('STR_MNEMO_PLATFORM_OFFLINE');

  await context.setOffline(false);
  await expect(page.getByTestId('platform-reconnect-notification')).toBeVisible();
});

test('language switcher applies i18n to platform messages', async ({ page }) => {
  await page.goto('/test-login?role=partner');
  await expect(page.getByTestId('session-ready')).toContainText('partner');

  await page.goto('/business');
  await page.getByTestId('platform-language-switcher').click();
  await page.getByTestId('platform-language-option-en').click();

  await page.getByTestId('platform-demo-success-notification').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_PLATFORM_NOTIFICATION_DEMO_SUCCESS');
  await expect(page.getByTestId('platform-i18n-current-locale')).toContainText('en');
});

test('consent panel blocks marketing pixel and allows analytics pageview', async ({ page }) => {
  const analyticsRequests: string[] = [];
  await page.route('**/analytics/**', async (route) => {
    analyticsRequests.push(route.request().url());
    await route.fulfill({ status: 202, body: '{"accepted":true}' });
  });

  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');

  await page.goto('/privacy/consent');
  await expect(page.getByTestId('platform-consent-panel')).toBeVisible();
  await page.getByTestId('platform-consent-analytics').check();
  await page.getByTestId('platform-consent-marketing').uncheck();
  await page.getByTestId('platform-consent-save').click();

  await page.goto('/product/PRD-025-001');
  await expect(page.getByTestId('platform-consent-panel-state')).toContainText('analytics');
  expect(analyticsRequests.some((url) => url.includes('pageview') || url.includes('analytics-events'))).toBeTruthy();
  expect(analyticsRequests.some((url) => url.includes('mindbox'))).toBeFalsy();
});

test('tracking admin opens diagnostics and customer is forbidden', async ({ page }) => {
  await page.goto('/test-login?role=tracking-admin');
  await expect(page.getByTestId('session-ready')).toContainText('tracking-admin');

  await page.goto('/admin/analytics-diagnostics');
  await expect(page.getByTestId('platform-analytics-diagnostics')).toBeVisible();
  await expect(page.getByTestId('platform-analytics-diagnostics')).toContainText('YANDEX_METRIKA');
  await expect(page.getByTestId('platform-analytics-diagnostics')).toContainText('MINDBOX');
  await expect(page.getByTestId('platform-analytics-diagnostics')).toContainText('HYBRID_PIXEL');

  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/admin/analytics-diagnostics');
  await expect(page.getByTestId('platform-diagnostics-forbidden')).toContainText('STR_MNEMO_ANALYTICS_DIAGNOSTICS_FORBIDDEN');
});
