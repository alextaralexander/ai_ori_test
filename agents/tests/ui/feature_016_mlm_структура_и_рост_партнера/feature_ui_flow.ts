import { expect, test } from '@playwright/test';

test('partner leader opens mlm dashboard', async ({ page }) => {
  await page.goto('/test-login?role=partner-leader');
  await expect(page.getByTestId('session-ready')).toContainText('partner-leader');
  await page.goto('/business');
  await expect(page.getByTestId('mlm-dashboard-page')).toBeVisible();
  await expect(page.getByTestId('mlm-dashboard-group-volume')).toBeVisible();
  await expect(page.getByTestId('mlm-dashboard-rank')).toContainText(/SILVER|Gold|GOLD|Серебро/i);
});

test('partner leader reviews beauty community structure', async ({ page }) => {
  await page.goto('/test-login?role=partner-leader');
  await expect(page.getByTestId('session-ready')).toContainText('partner-leader');
  await page.goto('/business/beauty-community');
  await expect(page.getByTestId('mlm-community-page')).toBeVisible();
  await expect(page.getByTestId('mlm-partner-node-BOG-016-002')).toContainText('BOG-016-002');
  await expect(page.getByTestId('mlm-partner-node-BOG-016-002')).toContainText('BRANCH-SKINCARE');
});

test('business manager sees conversion and team activity', async ({ page }) => {
  await page.goto('/test-login?role=business-manager');
  await expect(page.getByTestId('session-ready')).toContainText('business-manager');
  await page.goto('/business/conversion');
  await expect(page.getByTestId('mlm-conversion-page')).toBeVisible();
  await expect(page.getByTestId('mlm-conversion-rate')).toContainText(/%/);
  await page.goto('/business/team-activity?riskOnly=true');
  await expect(page.getByTestId('mlm-team-activity-page')).toBeVisible();
  await expect(page.getByTestId('mlm-activity-risk-BOG-016-003')).toContainText(/RISK_SIGNAL|риск/i);
});

test('partner leader checks upgrade and partner card', async ({ page }) => {
  await page.goto('/test-login?role=partner-leader');
  await expect(page.getByTestId('session-ready')).toContainText('partner-leader');
  await page.goto('/business/upgrade');
  await expect(page.getByTestId('mlm-upgrade-page')).toBeVisible();
  await expect(page.getByTestId('mlm-upgrade-next-rank')).toContainText(/GOLD|Gold|Золото/i);
  await page.goto('/business/partner-card/BOG-016-002');
  await expect(page.getByTestId('mlm-partner-card-page')).toBeVisible();
  await expect(page.getByTestId('mlm-partner-card-BOG-016-002')).toContainText('BOG-016-002');
  await expect(page.getByTestId('mlm-partner-linked-actions')).toContainText(/order|bonus|supply|заказ|бонус/i);
});

test('customer cannot open mlm dashboard', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/business');
  await expect(page.getByTestId('mlm-access-denied')).toContainText(/STR_MNEMO_MLM_STRUCTURE_ACCESS_DENIED|доступ|access/i);
});
