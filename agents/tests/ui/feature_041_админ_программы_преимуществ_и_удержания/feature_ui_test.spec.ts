// Managed feature #41 admin benefit program UI test entrypoint.
import { test } from '@playwright/test';

import { runFeature041AdminBenefitProgramFlow } from './feature_ui_flow';

test('admin benefit program dry run publish budget and audit green path', async ({ page }) => {
  await runFeature041AdminBenefitProgramFlow(page);
});
