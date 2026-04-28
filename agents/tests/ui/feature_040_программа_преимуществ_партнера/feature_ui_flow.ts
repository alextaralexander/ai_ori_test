import { expect, type Page } from '@playwright/test';

export async function runFeature040PartnerBenefitsFlow(page: Page): Promise<void> {
  await page.goto('/test-login?role=partner');
  await expect(page.getByTestId('session-ready')).toContainText('partner');

  await page.goto('/member-benefits');
  await expect(page.getByTestId('partner-benefits-page')).toBeVisible();
  await expect(page.getByTestId('partner-benefits-catalog')).toContainText('CAT-2026-08');
  await expect(page.getByTestId('partner-benefit-welcome')).toBeVisible();
  await expect(page.getByTestId('partner-benefit-free-delivery')).toBeVisible();
  await expect(page.getByTestId('partner-benefit-expiry-warning')).toBeVisible();

  await page.getByTestId('partner-benefit-apply-checkout').click();
  await expect(page.getByTestId('partner-benefit-apply-preview')).toContainText('applicable');

  await page.getByTestId('partner-benefits-tab-referral').click();
  await expect(page.getByTestId('partner-referral-link')).toContainText('REF-CAT-2026-08');
  await expect(page.getByTestId('partner-referral-events')).toContainText('QUALIFIED');

  await page.getByTestId('partner-benefits-tab-rewards').click();
  await expect(page.getByTestId('partner-reward-shop')).toBeVisible();
  await page.getByTestId('partner-reward-card-REWARD-SKINCARE-BOX').click();
  await page.getByTestId('partner-reward-redeem').click();
  await expect(page.getByTestId('partner-reward-redemption-status')).toContainText('RESERVED');

  await page.goto('/test-login?role=partner-support');
  await expect(page.getByTestId('session-ready')).toContainText('partner-support');
  await page.goto('/employee/partner-benefits-support');
  await page.getByTestId('partner-benefits-support-search').fill('PARTNER-040');
  await page.getByTestId('partner-benefits-support-open').click();
  await expect(page.getByTestId('partner-benefits-support-timeline')).toContainText('CORR-040');
  await expect(page.getByTestId('partner-benefits-support-timeline')).toContainText('STR_MNEMO_PARTNER_BENEFITS');
}
