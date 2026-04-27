// Synchronized from agents/tests/. Do not edit this generated runtime copy manually.
import { expect, test } from '@playwright/test';

test('customer opens profile overview and sees readiness', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/profile-settings');
  await expect(page.getByTestId('profile-settings-page')).toBeVisible();
  await expect(page.getByTestId('profile-section-general')).toBeVisible();
  await expect(page.getByTestId('profile-section-contacts')).toBeVisible();
  await expect(page.getByTestId('profile-readiness-checkout')).toBeVisible();
});

test('customer updates general profile section', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await page.goto('/profile-settings/general');
  await expect(page.getByTestId('profile-general-page')).toBeVisible();
  await page.getByTestId('profile-first-name').fill('Анна');
  await page.getByTestId('profile-last-name').fill('Иванова');
  await page.getByTestId('profile-preferred-language').selectOption('ru');
  await page.getByTestId('profile-general-save').click();
  await expect(page.getByTestId('profile-general-result')).toContainText(/Иванова|STR_MNEMO_PROFILE_GENERAL_UPDATED/);
});

test('customer adds contact and starts verification', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await page.goto('/profile-settings/contacts');
  await expect(page.getByTestId('profile-contacts-page')).toBeVisible();
  await page.getByTestId('profile-contact-type').selectOption('EMAIL');
  await page.getByTestId('profile-contact-value').fill('customer013@example.test');
  await page.getByTestId('profile-contact-primary').check();
  await page.getByTestId('profile-contact-save').click();
  await expect(page.getByTestId('profile-contact-list')).toContainText(/customer013|masked|EMAIL/i);
  await page.getByTestId('profile-contact-verify').click();
  await expect(page.getByTestId('profile-contact-verification-status')).toContainText(/REQUIRES_VERIFICATION|STR_MNEMO_PROFILE_CONTACT_REQUIRES_VERIFICATION/);
});

test('customer adds default address and sees checkout readiness', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await page.goto('/profile-settings/addresses');
  await expect(page.getByTestId('profile-addresses-page')).toBeVisible();
  await page.getByTestId('profile-address-city').fill('Москва');
  await page.getByTestId('profile-address-street').fill('Тверская');
  await page.getByTestId('profile-address-house').fill('1');
  await page.getByTestId('profile-address-postal-code').fill('101000');
  await page.getByTestId('profile-address-default').check();
  await page.getByTestId('profile-address-save').click();
  await expect(page.getByTestId('profile-address-list')).toContainText('Москва');
  await expect(page.getByTestId('profile-readiness-checkout')).toContainText(/ready|Готов|CHECKOUT/i);
});

test('documents are shown as masked values', async ({ page }) => {
  await page.goto('/test-login?role=partner');
  await page.goto('/profile-settings/documents');
  await expect(page.getByTestId('profile-documents-page')).toBeVisible();
  await page.getByTestId('profile-document-type').selectOption('PASSPORT');
  await page.getByTestId('profile-document-number').fill('4510123456');
  await page.getByTestId('profile-document-save').click();
  await expect(page.getByTestId('profile-document-list')).toContainText(/PASSPORT|Паспорт/);
  await expect(page.getByTestId('profile-document-list')).not.toContainText('4510123456');
});

test('weak password shows localized mnemonic error', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await page.goto('/profile-settings/security');
  await expect(page.getByTestId('profile-security-page')).toBeVisible();
  await page.getByTestId('profile-current-password').fill('current-password');
  await page.getByTestId('profile-new-password').fill('123');
  await page.getByTestId('profile-password-save').click();
  await expect(page.getByTestId('profile-security-error')).toContainText(/STR_MNEMO_PROFILE_PASSWORD_WEAK|пароль/i);
});

test('foreign profile is not exposed', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await page.goto('/profile-settings/support/USR-013-OTHER?reason=PROFILE_HELP');
  await expect(page.getByTestId('profile-access-denied')).toContainText(/STR_MNEMO_PROFILE_ACCESS_DENIED|access/i);
  await expect(page.getByTestId('profile-documents-page')).toHaveCount(0);
});
