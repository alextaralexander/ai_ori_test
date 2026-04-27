// Synchronized from agents/tests/. Do not edit this generated runtime copy manually.
import { expect, test } from '@playwright/test';

test('customer opens bonus wallet summary and transaction list', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/profile/transactions/all');
  await expect(page.getByTestId('bonus-wallet-page')).toBeVisible();
  await expect(page.getByTestId('bonus-wallet-balance-CASHBACK')).toBeVisible();
  await expect(page.getByTestId('bonus-wallet-balance-REFERRAL_DISCOUNT')).toBeVisible();
  await expect(page.getByTestId('bonus-wallet-transaction-TXN-014-CASHBACK-ACCRUAL')).toBeVisible();
});

test('customer filters cashback transactions and opens details', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await page.goto('/profile/transactions/cashback?orderNumber=ORD-011-MAIN');
  await expect(page.getByTestId('bonus-wallet-page')).toBeVisible();
  await expect(page.getByTestId('bonus-wallet-transaction-TXN-014-CASHBACK-ACCRUAL')).toContainText(/ORD-011-MAIN|Cashback|Кешбэк/i);
  await page.getByTestId('bonus-wallet-open-TXN-014-CASHBACK-ACCRUAL').click();
  await expect(page.getByTestId('bonus-wallet-transaction-details')).toContainText('TXN-014-CASHBACK-ACCRUAL');
  await expect(page.getByTestId('bonus-wallet-linked-order')).toHaveAttribute('href', '/order/order-history/ORD-011-MAIN');
});

test('partner sees referral bucket', async ({ page }) => {
  await page.goto('/test-login?role=partner');
  await page.goto('/profile/transactions/referral');
  await expect(page.getByTestId('bonus-wallet-page')).toBeVisible();
  await expect(page.getByTestId('bonus-wallet-balance-REFERRAL_DISCOUNT')).toBeVisible();
  await expect(page.getByTestId('bonus-wallet-transaction-TXN-014-REFERRAL-ACCRUAL')).toContainText(/REFERRAL|Referral|Рефераль/i);
});

test('customer exports wallet history', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await page.goto('/profile/transactions/all');
  await page.getByTestId('bonus-wallet-export').click();
  await expect(page.getByTestId('bonus-wallet-export-result')).toContainText(/STR_MNEMO_BONUS_WALLET_EXPORT_READY|экспорт/i);
});

test('finance access is not shown to customer', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await page.goto('/profile/transactions/finance/customer-014-other');
  await expect(page.getByTestId('bonus-wallet-access-denied')).toContainText(/STR_MNEMO_BONUS_WALLET_ACCESS_DENIED|access|доступ/i);
});
