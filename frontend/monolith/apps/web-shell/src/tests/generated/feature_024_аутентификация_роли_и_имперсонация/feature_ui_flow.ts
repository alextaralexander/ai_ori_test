// GENERATED FROM agents/tests/ui/feature_024_аутентификация_роли_и_имперсонация/feature_ui_flow.ts - DO NOT EDIT MANUALLY.
import { expect, test } from '@playwright/test';

test('auth restores partner session and switches active partner', async ({ page }) => {
  await page.goto('/test-login?role=partner-leader&invitationCode=INV-024-SPONSOR');
  await expect(page.getByTestId('session-ready')).toContainText('partner-leader');

  await page.goto('/auth/session');
  await expect(page.getByTestId('auth-role-router')).toContainText('/business');
  await expect(page.getByTestId('auth-invitation-code-state')).toContainText('VALID');

  await page.getByTestId('auth-partner-search-input').fill('024');
  await page.getByTestId('auth-partner-search-submit').click();
  await expect(page.getByTestId('auth-partner-search')).toContainText('PART-024-001');

  await page.getByTestId('auth-active-partner-select-PART-024-001').click();
  await expect(page.getByTestId('auth-active-partner-switcher')).toContainText('PART-024-001');
});

test('customer is denied employee route through role router', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');

  await page.goto('/employee');
  await expect(page.getByTestId('auth-route-forbidden')).toContainText('STR_MNEMO_AUTH_ACCESS_DENIED');
});

test('supervisor starts and finishes impersonation with visible banner', async ({ page }) => {
  await page.goto('/test-login?role=supervisor');
  await expect(page.getByTestId('session-ready')).toContainText('supervisor');

  await page.goto('/auth/impersonation');
  await expect(page.getByTestId('auth-impersonation-panel')).toBeVisible();
  await page.getByTestId('auth-impersonation-target-user-id').fill('USR-024-PARTNER');
  await page.getByTestId('auth-impersonation-target-role').fill('partner');
  await page.getByTestId('auth-impersonation-reason-code').fill('SUPPORT_CASE_REVIEW');
  await page.getByTestId('auth-impersonation-reason-text').fill('Проверка обращения партнера');
  await page.getByTestId('auth-impersonation-duration').fill('20');
  await page.getByTestId('auth-impersonation-start').click();
  await expect(page.getByTestId('auth-impersonation-banner')).toContainText('USR-024-PARTNER');

  await page.getByTestId('auth-impersonation-finish').click();
  await expect(page.getByTestId('auth-impersonation-banner')).toBeHidden();
});

test('logout clears auth context', async ({ page }) => {
  await page.goto('/test-login?role=partner');
  await expect(page.getByTestId('session-ready')).toContainText('partner');

  await page.goto('/auth/session');
  await page.getByTestId('auth-logout-button').click();
  await expect(page.getByTestId('auth-role-router')).toContainText('guest');
});
