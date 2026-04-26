// Synchronized from agents/tests/. Do not edit this runtime copy manually.
import { expect, test } from '@playwright/test';

test('guest opens business partner registration with active invite context', async ({ page }) => {
  await page.goto('/test-login?role=guest');
  await expect(page.getByTestId('session-ready')).toContainText('guest');
  await page.goto('/invite/business-partner-registration?code=BOG777&campaignId=CMP-2026-05');
  await expect(page.getByTestId('partner-registration-page')).toBeVisible();
  await expect(page.getByTestId('partner-registration-onboarding-type')).toContainText('BUSINESS_PARTNER');
  await expect(page.getByTestId('partner-registration-invite-status')).toContainText('ACTIVE');
  await expect(page.getByTestId('partner-registration-invite-code')).toContainText('BOG777');
  await expect(page.getByTestId('partner-registration-sponsor-context')).toBeVisible();
});

test('guest sees controlled state for invalid invite code', async ({ page }) => {
  await page.goto('/test-login?role=guest');
  await expect(page.getByTestId('session-ready')).toContainText('guest');
  await page.goto('/invite/business-partner-registration?code=UNKNOWN777');
  await expect(page.getByTestId('partner-registration-page')).toBeVisible();
  await expect(page.getByTestId('partner-registration-invite-status')).toContainText('NOT_FOUND');
  await expect(page.getByTestId('partner-registration-message')).toBeVisible();
  await expect(page.getByTestId('partner-registration-private-sponsor-data')).toHaveCount(0);
});

test('guest submits registration application and sees next onboarding action', async ({ page }) => {
  await page.goto('/test-login?role=guest');
  await expect(page.getByTestId('session-ready')).toContainText('guest');
  await page.goto('/invite/business-partner-registration?code=BOG777');
  await page.getByTestId('partner-registration-name').fill('Анна Партнер');
  await page.getByTestId('partner-registration-contact').fill('anna.partner@example.test');
  await page.getByTestId('partner-registration-consent-PARTNER_RULES').check();
  await page.getByTestId('partner-registration-consent-PERSONAL_DATA').check();
  await page.getByTestId('partner-registration-submit').click();
  await expect(page.getByTestId('partner-registration-status')).toContainText(/PENDING_CONTACT_CONFIRMATION|READY_FOR_ACTIVATION/);
  await expect(page.getByTestId('partner-registration-next-action')).toBeVisible();
});

test('invited partner completes activation and receives referral link', async ({ page }) => {
  await page.goto('/test-login?role=invited-partner');
  await expect(page.getByTestId('session-ready')).toContainText('invited-partner');
  await page.goto('/invite/partners-activation?token=ACT-008-001');
  await expect(page.getByTestId('partner-activation-page')).toBeVisible();
  await expect(page.getByTestId('partner-activation-status')).toContainText(/PENDING_CONTACT_CONFIRMATION|READY_FOR_ACTIVATION/);
  await page.getByTestId('partner-activation-code').fill('123456');
  await page.getByTestId('partner-activation-confirm-contact').click();
  await expect(page.getByTestId('partner-activation-contact-confirmed')).toContainText('true');
  await page.getByTestId('partner-activation-consent-PARTNER_RULES').check();
  await page.getByTestId('partner-activation-complete').click();
  await expect(page.getByTestId('partner-activation-status')).toContainText('ACTIVE');
  await expect(page.getByTestId('partner-referral-link')).toBeVisible();
});

test('sponsor manages own invites in sponsor cabinet', async ({ page }) => {
  await page.goto('/test-login?role=sponsor');
  await expect(page.getByTestId('session-ready')).toContainText('sponsor');
  await page.goto('/invite/sponsor-cabinet');
  await expect(page.getByTestId('sponsor-cabinet-page')).toBeVisible();
  await expect(page.getByTestId('sponsor-invite-list')).toBeVisible();
  await expect(page.getByTestId('sponsor-private-candidate-data')).toHaveCount(0);
  await page.getByTestId('sponsor-create-invite').click();
  await page.getByTestId('sponsor-invite-type-business').click();
  await page.getByTestId('sponsor-invite-submit').click();
  await expect(page.getByTestId('sponsor-invite-created-status')).toContainText('CREATED');
  await expect(page.getByTestId('sponsor-invite-target-route')).toContainText('/invite/');
});

test('registration layout is usable on mobile width', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 900 });
  await page.goto('/test-login?role=guest');
  await expect(page.getByTestId('session-ready')).toContainText('guest');
  await page.goto('/invite/beauty-partner-registration?code=BOG777');
  await expect(page.getByTestId('partner-registration-page')).toBeVisible();
  await expect(page.getByTestId('partner-registration-submit')).toBeVisible();
  const horizontalOverflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth);
  expect(horizontalOverflow).toBe(false);
});
