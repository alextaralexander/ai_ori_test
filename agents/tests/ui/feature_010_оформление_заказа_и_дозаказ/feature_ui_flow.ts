import { expect, test } from '@playwright/test';

test('customer completes main checkout flow', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/cart?seed=checkout-ready');
  await expect(page.getByTestId('cart-page')).toBeVisible();
  await page.getByTestId('cart-checkout').click();
  await expect(page).toHaveURL(/\/order/);
  await expect(page.getByTestId('order-checkout-page')).toBeVisible();
  await expect(page.getByTestId('order-checkout-type')).toContainText('MAIN');
  await page.getByTestId('order-recipient-full-name').fill('Customer 010');
  await page.getByTestId('order-recipient-phone').fill('+79990000010');
  await page.getByTestId('order-step-next').click();
  await page.getByTestId('order-address-saved-ADDR-010-MAIN').click();
  await page.getByTestId('order-step-next').click();
  await page.getByTestId('order-delivery-COURIER').click();
  await page.getByTestId('order-step-next').click();
  await page.getByTestId('order-wallet-amount').fill('300');
  await page.getByTestId('order-payment-ONLINE_CARD').click();
  await page.getByTestId('order-step-next').click();
  await expect(page.getByTestId('order-confirmation-summary')).toBeVisible();
  await page.getByTestId('order-confirm').click();
  await expect(page.getByTestId('order-result')).toBeVisible();
  await expect(page.getByTestId('order-result-number')).toContainText(/ORD-|order/i);
});

test('customer sees controlled validation errors on checkout', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/order?seed=invalid-contact');
  await expect(page.getByTestId('order-checkout-page')).toBeVisible();
  await page.getByTestId('order-recipient-phone').fill('123');
  await page.getByTestId('order-step-next').click();
  await expect(page.getByTestId('order-validation-message')).toBeVisible();
  await expect(page.getByTestId('order-validation-message')).toContainText(/STR_MNEMO_ORDER_CONTACT_INVALID|контакт|contact/i);
});

test('partner completes supplementary checkout separately', async ({ page }) => {
  await page.goto('/test-login?role=partner');
  await expect(page.getByTestId('session-ready')).toContainText('partner');
  await page.goto('/cart/supplementary?seed=checkout-ready');
  await expect(page.getByTestId('supplementary-cart-page')).toBeVisible();
  await page.getByTestId('supplementary-cart-checkout').click();
  await expect(page).toHaveURL(/\/order\/supplementary/);
  await expect(page.getByTestId('order-checkout-type')).toContainText('SUPPLEMENTARY');
  await expect(page.getByTestId('order-partner-context')).toBeVisible();
  await page.getByTestId('order-recipient-full-name').fill('Partner 010');
  await page.getByTestId('order-recipient-phone').fill('+79990000011');
  await page.getByTestId('order-step-next').click();
  await page.getByTestId('order-pickup-PICKUP-010-01').click();
  await page.getByTestId('order-step-next').click();
  await page.getByTestId('order-delivery-PICKUP').click();
  await page.getByTestId('order-step-next').click();
  await page.getByTestId('order-payment-ONLINE_CARD').click();
  await page.getByTestId('order-step-next').click();
  await page.getByTestId('order-confirm').click();
  await expect(page.getByTestId('order-result')).toBeVisible();
  await expect(page.getByTestId('order-result-type')).toContainText('SUPPLEMENTARY');
});

test('partial reserve blocks confirmation with localized reason', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/order?seed=partial-reserve');
  await expect(page.getByTestId('order-checkout-page')).toBeVisible();
  await page.getByTestId('order-confirm').click();
  await expect(page.getByTestId('order-validation-message')).toBeVisible();
  await expect(page.getByTestId('order-validation-message')).toContainText(/STR_MNEMO_ORDER_PARTIAL_RESERVE|резерв|reserve/i);
});

test('checkout layout is usable on mobile width', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 900 });
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/order');
  await expect(page.getByTestId('order-checkout-page')).toBeVisible();
  await expect(page.getByTestId('order-stepper')).toBeVisible();
  await expect(page.getByTestId('order-totals')).toBeVisible();
  const horizontalOverflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth);
  expect(horizontalOverflow).toBe(false);
});
