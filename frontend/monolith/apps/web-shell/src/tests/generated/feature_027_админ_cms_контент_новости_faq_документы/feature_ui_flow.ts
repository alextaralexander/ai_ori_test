// GENERATED FROM agents/tests/. Do not edit this runtime copy manually.
import { expect, test } from '@playwright/test';

test('admin CMS manages material lifecycle and publication', async ({ page }) => {
  await page.goto('/test-login?role=content-admin');
  await expect(page.getByTestId('session-ready')).toContainText('content-admin');

  await page.goto('/admin/cms');
  await expect(page.getByTestId('admin-cms-page')).toBeVisible();
  await expect(page.getByTestId('admin-cms-material-table')).toBeVisible();

  await page.getByTestId('admin-cms-create-material').click();
  await page.getByTestId('admin-cms-material-form').getByTestId('admin-cms-field-title').fill('Spring campaign editorial');
  await page.getByTestId('admin-cms-field-slug').fill('spring-campaign-editorial');
  await page.getByTestId('admin-cms-field-material-type').selectOption('NEWS');
  await page.getByTestId('admin-cms-field-language').selectOption('ru');
  await page.getByTestId('admin-cms-add-block-rich-text').click();
  await page.getByTestId('admin-cms-editor-blocks').getByTestId('admin-cms-block-rich-text-input').fill('Campaign body');
  await page.getByTestId('admin-cms-seo-panel').getByTestId('admin-cms-seo-title').fill('Spring campaign');
  await page.getByTestId('admin-cms-seo-description').fill('Best Ori Gin campaign');
  await page.getByTestId('admin-cms-material-save').click();

  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_CMS_MATERIAL_SAVED');
  await expect(page.getByTestId('admin-cms-material-table')).toContainText('spring-campaign-editorial');

  await page.getByTestId('admin-cms-preview-open').click();
  await expect(page.getByTestId('admin-cms-preview')).toBeVisible();
  await expect(page.getByTestId('admin-cms-preview')).toContainText('Spring campaign editorial');

  await page.getByTestId('admin-cms-submit-review').click();
  await expect(page.getByTestId('admin-cms-review-queue')).toContainText('IN_REVIEW');

  await page.goto('/test-login?role=legal-reviewer');
  await expect(page.getByTestId('session-ready')).toContainText('legal-reviewer');
  await page.goto('/admin/cms/review');
  await page.getByTestId('admin-cms-review-open-spring-campaign-editorial').click();
  await page.getByTestId('admin-cms-review-comment').fill('Approved for publication');
  await page.getByTestId('admin-cms-review-approve').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_CMS_REVIEW_APPROVED');

  await page.goto('/test-login?role=content-admin');
  await page.goto('/admin/cms');
  await page.getByTestId('admin-cms-material-table').getByText('spring-campaign-editorial').click();
  await page.getByTestId('admin-cms-publication-schedule').getByTestId('admin-cms-publish-now').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_CMS_MATERIAL_PUBLISHED');
  await expect(page.getByTestId('admin-cms-material-table')).toContainText('PUBLISHED');
});

test('admin CMS validates duplicate slug and document metadata', async ({ page }) => {
  await page.goto('/test-login?role=cms-editor');
  await expect(page.getByTestId('session-ready')).toContainText('cms-editor');

  await page.goto('/admin/cms');
  await page.getByTestId('admin-cms-create-material').click();
  await page.getByTestId('admin-cms-field-title').fill('Duplicate material');
  await page.getByTestId('admin-cms-field-slug').fill('spring-campaign-editorial');
  await page.getByTestId('admin-cms-field-material-type').selectOption('NEWS');
  await page.getByTestId('admin-cms-material-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_CMS_SLUG_CONFLICT');

  await page.getByTestId('admin-cms-field-material-type').selectOption('DOCUMENT');
  await page.getByTestId('admin-cms-field-slug').fill('terms-of-sale-invalid');
  await page.getByTestId('admin-cms-document-type').fill('TERMS_OF_SALE');
  await page.getByTestId('admin-cms-document-version-label').fill('2026.04');
  await page.getByTestId('admin-cms-material-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_CMS_DOCUMENT_INVALID');
});

test('admin CMS audit and forbidden state are visible', async ({ page }) => {
  await page.goto('/test-login?role=content-admin');
  await expect(page.getByTestId('session-ready')).toContainText('content-admin');
  await page.goto('/admin/cms/audit');
  await expect(page.getByTestId('admin-cms-audit-table')).toBeVisible();
  await page.getByTestId('admin-cms-audit-action-filter').fill('ADMIN_CMS_MATERIAL_PUBLISHED');
  await page.getByTestId('admin-cms-audit-search').click();
  await expect(page.getByTestId('admin-cms-audit-table')).toContainText('ADMIN_CMS_MATERIAL_PUBLISHED');
  await expect(page.getByTestId('admin-cms-audit-table')).toContainText('correlationId');

  await page.goto('/test-login?role=employee-support');
  await expect(page.getByTestId('session-ready')).toContainText('employee-support');
  await page.goto('/admin/cms');
  await expect(page.getByTestId('admin-cms-forbidden')).toContainText('STR_MNEMO_ADMIN_CMS_ACCESS_DENIED');
});