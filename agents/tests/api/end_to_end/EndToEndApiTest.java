package com.bestorigin.tests.endtoend;

import com.bestorigin.tests.feature001.FeatureApiTest;
import org.junit.jupiter.api.Test;

public class EndToEndApiTest {

    @Test
    void publicMarketplaceGreenPathAggregatesImplementedFeatureTests() throws Exception {
        new FeatureApiTest().assertFeatureGreenPath();
        new com.bestorigin.tests.feature002.FeatureApiTest().assertFeatureGreenPath();
        new com.bestorigin.tests.feature003.FeatureApiTest().assertFeatureGreenPath();
        new com.bestorigin.tests.feature004.FeatureApiTest().assertFeatureGreenPath();
        new com.bestorigin.tests.feature005.FeatureApiTest().assertFeatureGreenPath();
        new com.bestorigin.tests.feature006.FeatureApiTest().assertFeatureGreenPath();
        new com.bestorigin.tests.feature007.FeatureApiTest().assertFeatureGreenPath();
        new com.bestorigin.tests.feature008.FeatureApiTest().assertFeatureGreenPath();
        new com.bestorigin.tests.feature009.FeatureApiTest().assertFeatureGreenPath();
        new com.bestorigin.tests.feature010.FeatureApiTest().assertFeatureGreenPath();
        new com.bestorigin.tests.feature011.FeatureApiTest().assertFeatureGreenPath();
        new com.bestorigin.tests.feature012.FeatureApiTest().assertFeatureGreenPath();
        new com.bestorigin.tests.feature013.FeatureApiTest().assertFeatureGreenPath();
        new com.bestorigin.tests.feature014.FeatureApiTest().assertFeatureGreenPath();
        new com.bestorigin.tests.feature015.FeatureApiTest().assertFeatureGreenPath();
    }
}
