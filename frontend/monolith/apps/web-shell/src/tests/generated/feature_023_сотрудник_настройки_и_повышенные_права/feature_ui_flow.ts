// GENERATED FROM agents\tests\ui\feature_023_сотрудник_настройки_и_повышенные_права\feature_ui_flow.ts - DO NOT EDIT MANUALLY.
import { expect, test } from '@playwright/test';

test('employee edits profile settings and security sections', async ({ page }) => {
  await page.goto('/test-login?role=employee-support');
  await expect(page.getByTestId('session-ready')).toContainText('employee-support');
  await page.goto('/employee/profile-settings');
  await expect(page.getByTestId('employee-profile-settings-page')).toBeVisible();

  await page.goto('/employee/profile-settings/general');
  await expect(page.getByTestId('employee-profile-general-form')).toBeVisible();
  await page.getByTestId('employee-profile-display-name').fill('Employee 023');
  await page.getByTestId('employee-profile-timezone').fill('Europe/Moscow');
  await page.getByTestId('employee-profile-general-save').click();
  await expect(page.getByTestId('employee-profile-general-form')).toContainText('Employee 023');

  await page.goto('/employee/profile-settings/contacts');
  await expect(page.getByTestId('employee-profile-contacts-list')).toContainText('maskedValue');
  await page.goto('/employee/profile-settings/addresses');
  await expect(page.getByTestId('employee-profile-addresses-list')).toBeVisible();
  await page.goto('/employee/profile-settings/documents');
  await expect(page.getByTestId('employee-profile-documents-list')).toContainText('fileReferenceId');
  await page.goto('/employee/profile-settings/security');
  await expect(page.getByTestId('employee-profile-security-panel')).toContainText('mfaEnabled');
});

test('employee requests elevated mode and sees active banner after approval', async ({ page }) => {
  await page.goto('/test-login?role=employee-support');
  await expect(page.getByTestId('session-ready')).toContainText('employee-support');
  await page.goto('/employee/super-user');
  await expect(page.getByTestId('employee-super-user-page')).toBeVisible();
  await expect(page.getByTestId('employee-super-user-policy-list')).toContainText('EMPLOYEE_ELEVATED_SUPPORT_OPERATIONS');

  await page.getByTestId('employee-super-user-policy-code').fill('EMPLOYEE_ELEVATED_SUPPORT_OPERATIONS');
  await page.getByTestId('employee-super-user-reason-code').fill('SUPPORT_ESCALATION');
  await page.getByTestId('employee-super-user-reason-text').fill('Проверка проблемного заказа');
  await page.getByTestId('employee-super-user-duration').fill('20');
  await page.getByTestId('employee-super-user-request-submit').click();
  await expect(page.getByTestId('employee-super-user-request-form')).toContainText('PENDING_SUPERVISOR_APPROVAL');

  await page.goto('/test-login?role=supervisor');
  await page.goto('/employee/super-user');
  await page.getByTestId('employee-super-user-approve-first').click();
  await expect(page.getByTestId('employee-elevated-session-banner')).toContainText('ACTIVE');
});

test('guest cannot open employee profile settings or super user', async ({ page }) => {
  await page.goto('/test-login?role=guest');
  await expect(page.getByTestId('session-ready')).toContainText('guest');
  await page.goto('/employee/profile-settings');
  await expect(page.getByTestId('employee-access-denied')).toContainText('STR_MNEMO_EMPLOYEE_ACCESS_DENIED');
  await page.goto('/employee/super-user');
  await expect(page.getByTestId('employee-access-denied')).toContainText('STR_MNEMO_EMPLOYEE_SUPER_USER_FORBIDDEN');
});
