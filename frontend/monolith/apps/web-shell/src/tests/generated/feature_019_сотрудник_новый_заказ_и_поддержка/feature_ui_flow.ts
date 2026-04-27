// GENERATED FROM agents/tests/. DO NOT EDIT MANUALLY.
import { expect, test } from '@playwright/test';

test('employee opens workspace and finds customer', async ({ page }) => {
  await page.goto('/test-login?role=employee-support');
  await expect(page.getByTestId('session-ready')).toContainText('employee-support');
  await page.goto('/employee?query=CUST-019-001');
  await expect(page.getByTestId('employee-workspace-page')).toBeVisible();
  await expect(page.getByTestId('employee-customer-card')).toContainText('CUST-019-001');
  await expect(page.getByTestId('employee-active-cart')).toContainText('SKU-019-CREAM-001');
});

test('employee creates operator order', async ({ page }) => {
  await page.goto('/test-login?role=employee-support');
  await expect(page.getByTestId('session-ready')).toContainText('employee-support');
  await page.goto('/employee/new-order?customerId=CUST-019-001');
  await expect(page.getByTestId('employee-new-order-page')).toBeVisible();
  await page.getByTestId('employee-create-operator-order').click();
  await expect(page.getByTestId('employee-action-result')).toContainText('STR_MNEMO_EMPLOYEE_OPERATOR_ORDER_CREATED');
  await expect(page.getByTestId('employee-operator-order-card')).toContainText('BOG-ORD-019-NEW');
});

test('employee supports order and records actions', async ({ page }) => {
  await page.goto('/test-login?role=order-support');
  await expect(page.getByTestId('session-ready')).toContainText('order-support');
  await page.goto('/employee/order-support?orderNumber=BOG-ORD-019-001');
  await expect(page.getByTestId('employee-order-support-page')).toBeVisible();
  await expect(page.getByTestId('employee-order-timeline')).toContainText('BOG-ORD-019-001');
  await page.getByTestId('employee-add-note').click();
  await expect(page.getByTestId('employee-action-result')).toContainText('STR_MNEMO_EMPLOYEE_SUPPORT_NOTE_ADDED');
  await page.getByTestId('employee-record-adjustment').click();
  await expect(page.getByTestId('employee-action-result')).toContainText('STR_MNEMO_EMPLOYEE_SUPPORT_ADJUSTMENT_RECORDED');
});

test('supervisor sees support escalations', async ({ page }) => {
  await page.goto('/test-login?role=supervisor');
  await expect(page.getByTestId('session-ready')).toContainText('supervisor');
  await page.goto('/employee/order-support?view=supervisor');
  await expect(page.getByTestId('employee-supervisor-escalations')).toContainText('SLA_AT_RISK');
});

test('partner cannot open employee workspace', async ({ page }) => {
  await page.goto('/test-login?role=partner');
  await expect(page.getByTestId('session-ready')).toContainText('partner');
  await page.goto('/employee');
  await expect(page.getByTestId('employee-access-denied')).toContainText('STR_MNEMO_EMPLOYEE_ACCESS_DENIED');
});