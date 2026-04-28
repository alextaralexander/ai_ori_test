// Managed feature #39 admin fulfillment UI test entrypoint.
import { test } from '@playwright/test';

import { runFeature039AdminFulfillmentFlow } from './feature_ui_flow';

test('admin fulfillment conveyor delivery and pickup point green path', async ({ page }) => {
  await runFeature039AdminFulfillmentFlow(page);
});
