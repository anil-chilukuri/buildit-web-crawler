package com.buildit.assignment;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

public class CrawlerResponseTest {

	@Test
    public void testModelCreation(){
        List<Future<CrawlerResponse>> actual = Collections.emptyList();
        int currentDepth = 10;
        String actualURL = "someURL";
        String actualError = "actualError";
        CrawlerResponse model = CrawlerResponse.builder()
                .currentURL(actualURL)
                .currentDepth(currentDepth)
                .childrenFutures(actual)
                .error(actualError)
                .build();
        Assert.assertNotNull(model);
        Assert.assertEquals(actualURL, model.getCurrentURL());
        Assert.assertEquals(currentDepth, model.getCurrentDepth());
        Assert.assertEquals(actual, model.getChildrenFutures());
        Assert.assertEquals(actualError, model.getError());
    }

}
