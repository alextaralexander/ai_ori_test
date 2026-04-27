import { expect, test } from '@playwright/test';

test('customer creates claim from delivered order', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/order/claims/claim-create?orderNumber=ORD-011-MAIN');
  await expect(page.getByTestId('claim-create-page')).toBeVisible();
  await page.getByTestId('claim-item-100-011').check();
  await page.getByTestId('claim-reason').selectOption('DAMAGED_ITEM');
  await page.getByTestId('claim-resolution').selectOption('REFUND');
  await page.getByTestId('claim-comment').fill('photo attached');
  await page.getByTestId('claim-submit').click();
  await expect(page.getByTestId('claim-result')).toContainText(/CLM-012-001|STR_MNEMO_ORDER_CLAIM_CREATED/);
});

test('customer searches claim history and opens details', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/order/claims/claims-history');
  await expect(page.getByTestId('claim-history-page')).toBeVisible();
  await page.getByTestId('claim-history-search').fill('CLM-012-001');
  await page.getByTestId('claim-history-submit').click();
  await expect(page.getByTestId('claim-history-card-CLM-012-001')).toBeVisible();
  await page.getByTestId('claim-history-details-CLM-012-001').click();
  await expect(page).toHaveURL(/\/order\/claims\/claims-history\/CLM-012-001/);
  await expect(page.getByTestId('claim-details-page')).toBeVisible();
  await expect(page.getByTestId('claim-details-items')).toBeVisible();
  await expect(page.getByTestId('claim-details-events')).toBeVisible();
});

test('claim validation reports unavailable item', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await page.goto('/order/claims/claim-create?orderNumber=ORD-011-MAIN&sku=GIFT-011');
  await expect(page.getByTestId('claim-create-page')).toBeVisible();
  await page.getByTestId('claim-item-GIFT-011').check();
  await page.getByTestId('claim-reason').selectOption('DAMAGED_ITEM');
  await page.getByTestId('claim-resolution').selectOption('REFUND');
  await page.getByTestId('claim-submit').click();
  await expect(page.getByTestId('claim-error')).toContainText(/STR_MNEMO_ORDER_CLAIM_ITEM_UNAVAILABLE|claim/i);
});

test('foreign claim is not exposed', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await page.goto('/order/claims/claims-history/CLM-012-OTHER');
  await expect(page.getByTestId('claim-access-denied')).toContainText(/STR_MNEMO_ORDER_CLAIM_ACCESS_DENIED|access/i);
  await expect(page.getByTestId('claim-details-page')).toHaveCount(0);
});