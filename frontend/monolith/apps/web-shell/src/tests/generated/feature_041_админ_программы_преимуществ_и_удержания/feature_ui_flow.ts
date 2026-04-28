// AUTO-GENERATED from agents/tests/. Do not edit this synchronized runtime copy manually.
import { expect, type Page } from '@playwright/test';

export async function runFeature041AdminBenefitProgramFlow(page: Page): Promise<void> {
  await page.goto('/test-login?role=admin-benefit-program-manager');
  await expect(page.getByTestId('session-ready')).toContainText('admin-benefit-program-manager');

  await page.goto('/admin/benefit-programs');
  await expect(page.getByTestId('admin-benefit-programs-page')).toBeVisible();
  await expect(page.getByTestId('admin-benefit-programs-table')).toContainText('CAT-2026-08-CASHBACK');

  await page.getByTestId('admin-benefit-program-create').click();
  await page.getByTestId('benefit-program-code').fill('CAT-2026-08-CASHBACK');
  await page.getByTestId('benefit-program-type').click();
  await page.getByRole('option', { name: 'CASHBACK' }).click();
  await page.getByTestId('benefit-program-catalog-id').fill('CAT-2026-08');
  await page.getByTestId('benefit-program-owner-role').fill('CRM');
  await page.getByTestId('benefit-program-eligibility-min-order').fill('3000');
  await page.getByTestId('benefit-program-compatibility-priority').fill('40');
  await page.getByTestId('benefit-program-save').click();
  await expect(page.getByTestId('benefit-program-status')).toContainText('DRAFT');

  await page.getByTestId('benefit-program-dry-run-tab').click();
  await page.getByTestId('dry-run-partner-number').fill('PARTNER-041');
  await page.getByTestId('dry-run-cart-id').fill('CART-041-001');
  await page.getByTestId('dry-run-run').click();
  await expect(page.getByTestId('dry-run-result')).toContainText('applicable');
  await expect(page.getByTestId('dry-run-result')).toContainText('correlationId');

  await page.getByTestId('benefit-program-finance-tab').click();
  await page.getByTestId('budget-total').fill('500000');
  await page.getByTestId('budget-cashback-limit').fill('3000');
  await page.getByTestId('budget-save').click();
  await expect(page.getByTestId('budget-status')).toContainText('RUB');

  await page.getByTestId('benefit-program-publish-tab').click();
  await page.getByTestId('program-status-target').click();
  await page.getByRole('option', { name: 'SCHEDULED' }).click();
  await page.getByTestId('program-status-reason').fill('CATALOG_2026_08_APPROVED');
  await page.getByTestId('program-status-submit').click();
  await expect(page.getByTestId('benefit-program-status')).toContainText('SCHEDULED');

  await page.getByTestId('benefit-program-audit-tab').click();
  await expect(page.getByTestId('benefit-program-audit-table')).toContainText('DRY_RUN');
  await expect(page.getByTestId('benefit-program-integration-table')).toContainText('PARTNER_BENEFITS');
}
