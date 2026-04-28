// Generated from agents/tests/ui. Do not edit this runtime copy manually.
// Managed feature #38 admin bonus UI test entrypoint.
import { test } from '@playwright/test';

import { runFeature038AdminBonusFlow } from './feature_ui_flow';

test('admin bonus program compensation plan and payout batch green path', async ({ page }) => {
  await runFeature038AdminBonusFlow(page);
});