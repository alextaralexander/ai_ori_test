// Managed feature #40 partner benefits UI test entrypoint.
import { test } from '@playwright/test';

import { runFeature040PartnerBenefitsFlow } from './feature_ui_flow';

test('partner benefits referral reward shop and support green path', async ({ page }) => {
  await runFeature040PartnerBenefitsFlow(page);
});
