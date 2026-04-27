// GENERATED FROM agents/tests/. Do not edit this runtime copy manually.
import { expect, test, type Page } from '@playwright/test';

export async function runFeature028AdminReferralFlow(page: Page) {
  await page.goto('/test-login?role=marketing-admin');
  await expect(page.getByTestId('session-ready')).toContainText('marketing-admin');

  await page.goto('/admin/referrals');
  await expect(page.getByTestId('admin-referral-page')).toBeVisible();
  await expect(page.getByTestId('admin-referral-landing-table')).toBeVisible();

  await page.getByTestId('admin-referral-create-landing').click();
  await page.getByTestId('admin-referral-field-landing-type').selectOption('BUSINESS');
  await page.getByTestId('admin-referral-field-locale').selectOption('ru');
  await page.getByTestId('admin-referral-field-slug').fill('business-partner-spring');
  await page.getByTestId('admin-referral-field-name').fill('Business spring');
  await page.getByTestId('admin-referral-field-campaign-code').fill('BIZ-SPRING-2026');
  await page.getByTestId('admin-referral-add-block-hero').click();
  await page.getByTestId('admin-referral-add-block-benefit').click();
  await page.getByTestId('admin-referral-add-block-cta').click();
  await page.getByTestId('admin-referral-add-block-legal').click();
  await page.getByTestId('admin-referral-landing-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_REFERRAL_LANDING_SAVED');

  await page.getByTestId('admin-referral-landing-preview-open').click();
  await expect(page.getByTestId('admin-referral-landing-preview')).toBeVisible();
  await page.getByTestId('admin-referral-landing-activate').click();
  await expect(page.getByTestId('admin-referral-landing-table')).toContainText('ACTIVE');

  await page.goto('/test-login?role=crm-admin');
  await expect(page.getByTestId('session-ready')).toContainText('crm-admin');
  await page.goto('/admin/referrals/funnels');
  await page.getByTestId('admin-referral-create-funnel').click();
  await page.getByTestId('admin-referral-field-funnel-code').fill('business-partner-default');
  await page.getByTestId('admin-referral-field-funnel-scenario').selectOption('BUSINESS_PARTNER');
  await page.getByTestId('admin-referral-funnel-add-consent-personal-data').click();
  await page.getByTestId('admin-referral-funnel-add-consent-partner-terms').click();
  await page.getByTestId('admin-referral-funnel-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_REFERRAL_FUNNEL_SAVED');

  await page.goto('/admin/referrals/codes');
  await page.getByTestId('admin-referral-generate-code').click();
  await page.getByTestId('admin-referral-field-code-type').selectOption('CAMPAIGN_MULTI_USE');
  await page.getByTestId('admin-referral-field-code-campaign').fill('BIZ-SPRING-2026');
  await page.getByTestId('admin-referral-field-code-max-usage').fill('1000');
  await page.getByTestId('admin-referral-code-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_REFERRAL_CODE_GENERATED');

  await page.goto('/admin/referrals/analytics');
  await expect(page.getByTestId('admin-referral-conversion-report')).toBeVisible();
  await expect(page.getByTestId('admin-referral-conversion-report')).toContainText('LANDING_VIEWED');
  await expect(page.getByTestId('admin-referral-conversion-report')).toContainText('PARTNER_ACTIVATED');

  await page.goto('/admin/referrals/audit');
  await expect(page.getByTestId('admin-referral-audit-table')).toBeVisible();
  await expect(page.getByTestId('admin-referral-audit-table')).toContainText('correlationId');
}

test('admin referral manages landing, funnel, referral code and analytics', async ({ page }) => {
  await runFeature028AdminReferralFlow(page);
});

test('admin referral validates missing legal notice and forbidden access', async ({ page }) => {
  await page.goto('/test-login?role=marketing-admin');
  await expect(page.getByTestId('session-ready')).toContainText('marketing-admin');
  await page.goto('/admin/referrals');
  await page.getByTestId('admin-referral-create-landing').click();
  await page.getByTestId('admin-referral-field-landing-type').selectOption('BUSINESS');
  await page.getByTestId('admin-referral-field-slug').fill('business-without-legal');
  await page.getByTestId('admin-referral-add-block-hero').click();
  await page.getByTestId('admin-referral-add-block-cta').click();
  await page.getByTestId('admin-referral-landing-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_REFERRAL_LANDING_LEGAL_NOTICE_REQUIRED');

  await page.goto('/test-login?role=employee-support');
  await expect(page.getByTestId('session-ready')).toContainText('employee-support');
  await page.goto('/admin/referrals');
  await expect(page.getByTestId('admin-referral-forbidden')).toContainText('STR_MNEMO_ADMIN_REFERRAL_FORBIDDEN');
});
