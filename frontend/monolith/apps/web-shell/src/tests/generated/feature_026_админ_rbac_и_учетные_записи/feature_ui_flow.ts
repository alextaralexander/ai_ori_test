// GENERATED FROM agents/tests/ui/feature_026_админ_rbac_и_учетные_записи/feature_ui_flow.ts - DO NOT EDIT MANUALLY.
import { expect, test } from '@playwright/test';

test('admin RBAC manages account roles and permission preview', async ({ page }) => {
  await page.goto('/test-login?role=super-admin');
  await expect(page.getByTestId('session-ready')).toContainText('super-admin');

  await page.goto('/admin/rbac');
  await expect(page.getByTestId('admin-rbac-page')).toBeVisible();
  await expect(page.getByTestId('admin-rbac-account-table')).toBeVisible();

  await page.getByTestId('admin-rbac-create-account').click();
  await page.getByTestId('admin-rbac-account-form-full-name').fill('Feature 026 Employee');
  await page.getByTestId('admin-rbac-account-form-email').fill('employee026@bestorigin.test');
  await page.getByTestId('admin-rbac-account-form-department').fill('SUPPORT');
  await page.getByTestId('admin-rbac-account-form-position').fill('Support operator');
  await page.getByTestId('admin-rbac-account-form-save').click();

  await expect(page.getByTestId('admin-rbac-account-table')).toContainText('employee026@bestorigin.test');
  await page.getByTestId('admin-rbac-role-assignment').click();
  await page.getByTestId('admin-rbac-role-employee-support').check();
  await page.getByTestId('admin-rbac-permission-set-support-base').check();
  await page.getByTestId('admin-rbac-permission-preview-button').click();
  await expect(page.getByTestId('admin-rbac-permission-preview')).toContainText('EMPLOYEE_SUPPORT_BASE');
  await page.getByTestId('admin-rbac-role-assignment-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_RBAC_ACCESS_UPDATED');
});

test('HR admin receives localized forbidden state outside scope', async ({ page }) => {
  await page.goto('/test-login?role=hr-admin');
  await expect(page.getByTestId('session-ready')).toContainText('hr-admin');

  await page.goto('/admin/rbac');
  await page.getByTestId('admin-rbac-account-table').getByText('employee026@bestorigin.test').click();
  await page.getByTestId('admin-rbac-role-assignment').click();
  await page.getByTestId('admin-rbac-permission-set-admin-full').check();
  await page.getByTestId('admin-rbac-role-assignment-save').click();

  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_RBAC_SCOPE_DENIED');
});

test('security admin updates policies and rotates service account secret', async ({ page }) => {
  await page.goto('/test-login?role=security-admin');
  await expect(page.getByTestId('session-ready')).toContainText('security-admin');

  await page.goto('/admin/rbac/security');
  await expect(page.getByTestId('admin-rbac-security-policy')).toBeVisible();
  await page.getByTestId('admin-rbac-mfa-required-admin').check();
  await page.getByTestId('admin-rbac-security-policy-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_RBAC_POLICY_UPDATED');

  await page.goto('/admin/rbac/service-accounts');
  await expect(page.getByTestId('admin-rbac-service-account-table')).toBeVisible();
  await page.getByTestId('admin-rbac-service-account-rotate-SVC-026-WMS').click();
  await expect(page.getByTestId('admin-rbac-service-account-one-time-secret')).toBeVisible();
  await expect(page.getByTestId('admin-rbac-service-account-one-time-secret')).toContainText('STR_MNEMO_ADMIN_RBAC_SECRET_SHOWN_ONCE');
});

test('auditor searches immutable RBAC audit events', async ({ page }) => {
  await page.goto('/test-login?role=auditor');
  await expect(page.getByTestId('session-ready')).toContainText('auditor');

  await page.goto('/admin/rbac/audit');
  await expect(page.getByTestId('admin-rbac-audit-table')).toBeVisible();
  await page.getByTestId('admin-rbac-audit-action-filter').fill('ADMIN_ROLE_ASSIGNED');
  await page.getByTestId('admin-rbac-audit-search').click();
  await expect(page.getByTestId('admin-rbac-audit-table')).toContainText('ADMIN_ROLE_ASSIGNED');
  await expect(page.getByTestId('admin-rbac-audit-table')).toContainText('correlationId');
});

test('employee without admin RBAC rights is forbidden', async ({ page }) => {
  await page.goto('/test-login?role=employee-support');
  await expect(page.getByTestId('session-ready')).toContainText('employee-support');

  await page.goto('/admin/rbac');
  await expect(page.getByTestId('admin-rbac-forbidden')).toContainText('STR_MNEMO_ADMIN_RBAC_ACCESS_DENIED');
});
