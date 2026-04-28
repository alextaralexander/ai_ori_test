import { expect, type Page } from '@playwright/test';

export async function runFeature038AdminBonusFlow(page: Page): Promise<void> {
  await page.goto('/test-login?role=bonus-admin');
  await expect(page.getByTestId('session-ready')).toContainText('bonus-admin');

  await page.goto('/admin/bonus-program');
  await expect(page.getByTestId('admin-bonus-program-page')).toBeVisible();

  await page.getByTestId('bonus-rule-create').click();
  await page.getByTestId('bonus-rule-code').fill('ORDER_BONUS_038');
  await page.getByTestId('bonus-rule-currency').fill('RUB');
  await page.getByTestId('bonus-rule-rate').fill('7.5');
  await page.getByTestId('bonus-rule-priority').fill('38');
  await page.getByTestId('bonus-rule-save').click();
  await expect(page.getByTestId('bonus-rule-status')).toContainText('DRAFT');

  await page.getByTestId('bonus-rule-preview').click();
  await expect(page.getByTestId('bonus-preview-result')).toContainText('expectedAmount');
  await page.getByTestId('bonus-rule-activate').click();
  await expect(page.getByTestId('bonus-rule-status')).toContainText('ACTIVE');

  await page.goto('/test-login?role=mlm-manager');
  await expect(page.getByTestId('session-ready')).toContainText('mlm-manager');
  await page.goto('/admin/bonus-program/calculations');
  await page.getByTestId('bonus-calculation-period').fill('2026-04');
  await page.getByTestId('bonus-calculation-run').click();
  await expect(page.getByTestId('bonus-calculation-status')).toContainText('correlationId');

  await page.goto('/test-login?role=finance-manager');
  await expect(page.getByTestId('session-ready')).toContainText('finance-manager');
  await page.goto('/admin/bonus-program/payout-batches');
  await page.getByTestId('payout-batch-period').fill('2026-04');
  await page.getByTestId('payout-batch-currency').fill('RUB');
  await page.getByTestId('payout-batch-create').click();
  await expect(page.getByTestId('payout-batch-status')).toContainText('DRAFT');
  await page.getByTestId('payout-batch-approve').click();
  await expect(page.getByTestId('payout-batch-status')).toContainText('APPROVED');
  await page.getByTestId('payout-batch-send').click();
  await expect(page.getByTestId('payout-batch-status')).toContainText('SENT');
}
