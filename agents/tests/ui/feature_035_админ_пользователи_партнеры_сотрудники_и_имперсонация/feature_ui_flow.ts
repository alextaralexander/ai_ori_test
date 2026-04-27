import { expect, test, type Page } from '@playwright/test';

export async function runFeature035AdminIdentityFlow(page: Page) {
  await page.goto('/test-login?role=master-data-admin');
  await expect(page.getByTestId('session-ready')).toContainText('master-data-admin');

  await page.goto('/admin/identity');
  await expect(page.getByTestId('admin-identity-page')).toBeVisible();
  await expect(page.getByTestId('admin-identity-subject-table')).toBeVisible();

  await page.getByTestId('admin-identity-search').fill('USR-035-1001');
  await page.getByTestId('admin-identity-subject-type').selectOption('USER');
  await page.getByTestId('admin-identity-status').selectOption('ACTIVE');
  await page.getByTestId('admin-identity-search-submit').click();
  await expect(page.getByTestId('admin-identity-subject-table')).toContainText('USR-035-1001');

  await page.getByTestId('admin-identity-open-card-USR-035-1001').click();
  await expect(page.getByTestId('admin-identity-subject-card')).toBeVisible();
  await expect(page.getByTestId('admin-identity-eligibility-panel')).toBeVisible();
  await expect(page.getByTestId('admin-identity-audit-trail')).toBeVisible();
  await expect(page.getByTestId('admin-identity-pii-mask')).toBeVisible();

  await page.getByTestId('admin-identity-change-status').click();
  await page.getByTestId('admin-identity-new-status').selectOption('SUSPENDED');
  await page.getByTestId('admin-identity-status-reason').selectOption('MANUAL_RISK_REVIEW');
  await page.getByTestId('admin-identity-status-submit').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_IDENTITY_STATUS_CHANGED');

  await page.goto('/test-login?role=partner-ops-admin');
  await expect(page.getByTestId('session-ready')).toContainText('partner-ops-admin');
  await page.goto('/admin/identity/partners/PTR-035-1001');
  await expect(page.getByTestId('admin-identity-partner-panel')).toBeVisible();
  await page.getByTestId('admin-identity-transfer-sponsor').click();
  await page.getByTestId('admin-identity-new-sponsor').fill('SP-035-B');
  await page.getByTestId('admin-identity-sponsor-effective-from').fill('2026-05-01');
  await page.getByTestId('admin-identity-sponsor-reason').selectOption('PARTNER_TRANSFER_APPROVED');
  await page.getByTestId('admin-identity-sponsor-preview').click();
  await expect(page.getByTestId('admin-identity-sponsor-impact-preview')).toBeVisible();
  await page.getByTestId('admin-identity-sponsor-submit').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_IDENTITY_SPONSOR_CHANGED');

  await page.goto('/test-login?role=security-admin');
  await expect(page.getByTestId('session-ready')).toContainText('security-admin');
  await page.goto('/admin/identity/impersonation');
  await expect(page.getByTestId('admin-identity-impersonation-page')).toBeVisible();
  await page.getByTestId('admin-identity-policy-code').fill('SUPPORT_READ_ONLY');
  await page.getByTestId('admin-identity-policy-target-type').selectOption('USER');
  await page.getByTestId('admin-identity-policy-duration').fill('30');
  await page.getByTestId('admin-identity-policy-forbidden-actions').fill('PAYMENT,BONUS_WITHDRAWAL,PASSWORD_CHANGE,PROFILE_EDIT');
  await page.getByTestId('admin-identity-policy-submit').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_IDENTITY_POLICY_SAVED');

  await page.getByTestId('admin-identity-impersonation-target').fill('USR-035-1007');
  await page.getByTestId('admin-identity-impersonation-reason').selectOption('SUPPORT_DIAGNOSTICS');
  await page.getByTestId('admin-identity-impersonation-start').click();
  await expect(page.getByTestId('admin-identity-impersonation-banner')).toContainText('USR-035-1007');

  await page.getByTestId('admin-identity-forbidden-password-change').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_IDENTITY_IMPERSONATION_ACTION_FORBIDDEN');

  await page.goto('/admin/identity/audit');
  await expect(page.getByTestId('admin-identity-audit-table')).toBeVisible();
  await page.getByTestId('admin-identity-audit-subject').fill('USR-035-1007');
  await page.getByTestId('admin-identity-audit-submit').click();
  await expect(page.getByTestId('admin-identity-audit-table')).toContainText('correlationId');
}

test('admin identity manages subjects partners employees and impersonation', async ({ page }) => {
  await runFeature035AdminIdentityFlow(page);
});

test('admin identity rejects forbidden user', async ({ page }) => {
  await page.goto('/test-login?role=content-admin');
  await expect(page.getByTestId('session-ready')).toContainText('content-admin');
  await page.goto('/admin/identity');
  await expect(page.getByTestId('admin-identity-forbidden')).toContainText('STR_MNEMO_ADMIN_IDENTITY_FORBIDDEN_ACTION');
});
