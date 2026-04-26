package com.bestorigin.tests.endtoend;

import com.bestorigin.tests.feature001.FeatureApiTest;
import org.junit.jupiter.api.Test;

public class EndToEndApiTest {

    @Test
    void publicMarketplaceGreenPathAggregatesImplementedFeatureTests() throws Exception {
        new FeatureApiTest().assertFeatureGreenPath();
        new com.bestorigin.tests.feature002.FeatureApiTest().assertFeatureGreenPath();
    }
}
