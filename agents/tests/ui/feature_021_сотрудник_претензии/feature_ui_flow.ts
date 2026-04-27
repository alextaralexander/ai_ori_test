import { expect, test } from '@playwright/test';

test('employee creates claim from employee workspace route', async ({ page }) => {
  await page.goto('/test-login?role=employee-support');
  await expect(page.getByTestId('session-ready')).toContainText('employee-support');
  await page.goto('/employee/submit-claim?orderNumber=BOG-ORD-021-001');
  await expect(page.getByTestId('employee-submit-claim-page')).toBeVisible();
  await page.getByTestId('employee-claim-support-reason').fill('CUSTOMER_CALL');
  await page.getByTestId('employee-claim-item-SKU-021-001').check();
  await page.getByTestId('employee-claim-problem-type').selectOption('DAMAGED_ITEM');
  await page.getByTestId('employee-claim-requested-resolution').selectOption('REFUND');
  await expect(page.getByTestId('employee-claim-compensation-preview')).toContainText('1250');
  await page.getByTestId('employee-claim-submit').click();
  await expect(page.getByTestId('employee-claim-result')).toContainText('BOG-CLM-021-001');
  await expect(page.getByTestId('employee-claim-result')).toContainText('AT_RISK');
});

test('employee filters employee claims history by sla', async ({ page }) => {
  await page.goto('/test-login?role=employee-support');
  await expect(page.getByTestId('session-ready')).toContainText('employee-support');
  await page.goto('/employee/claims-history?slaState=AT_RISK&claimStatus=IN_REVIEW');
  await expect(page.getByTestId('employee-claims-history-page')).toBeVisible();
  await expect(page.getByTestId('employee-claims-filters')).toBeVisible();
  await expect(page.getByTestId('employee-claims-table')).toContainText('BOG-CLM-021-001');
  await expect(page.getByTestId('employee-claims-table')).toContainText('AT_RISK');
  await expect(page.getByTestId('employee-claims-table')).toContainText('masked');
});

test('employee opens employee claim details and routes claim', async ({ page }) => {
  await page.goto('/test-login?role=employee-support');
  await expect(page.getByTestId('session-ready')).toContainText('employee-support');
  await page.goto('/employee/claims-history/BOG-CLM-021-001');
  await expect(page.getByTestId('employee-claim-details-page')).toBeVisible();
  await expect(page.getByTestId('employee-claim-items')).toContainText('SKU-021-001');
  await expect(page.getByTestId('employee-claim-route-tasks')).toContainText('WAREHOUSE');
  await expect(page.getByTestId('employee-claim-audit')).toContainText('EMPLOYEE_CLAIM_DETAILS_VIEWED');
  await page.getByTestId('employee-claim-transition-finance').click();
  await expect(page.getByTestId('employee-claim-route-tasks')).toContainText('FINANCE');
});

test('supervisor approves claim compensation', async ({ page }) => {
  await page.goto('/test-login?role=supervisor');
  await expect(page.getByTestId('session-ready')).toContainText('supervisor');
  await page.goto('/employee/claims-history/BOG-CLM-021-002');
  await expect(page.getByTestId('employee-claim-details-page')).toBeVisible();
  await page.getByTestId('employee-claim-approve-compensation').click();
  await expect(page.getByTestId('employee-claim-audit')).toContainText('EMPLOYEE_CLAIM_SUPERVISOR_APPROVED');
});

test('partner cannot open employee claims', async ({ page }) => {
  await page.goto('/test-login?role=partner');
  await expect(page.getByTestId('session-ready')).toContainText('partner');
  await page.goto('/employee/claims-history');
  await expect(page.getByTestId('employee-access-denied')).toContainText('STR_MNEMO_EMPLOYEE_ACCESS_DENIED');
});
