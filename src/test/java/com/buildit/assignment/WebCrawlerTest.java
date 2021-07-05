package com.buildit.assignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.BiConsumer;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BiConsumer.class, WebCrawler.class, Executors.class})
public class WebCrawlerTest {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
    public void testCrawlService() throws Exception {
        ThreadPoolExecutor mockExecutor = Mockito.mock(ThreadPoolExecutor.class);

        FutureTask<CrawlerResponse> mockFuture = Mockito.mock(FutureTask.class);
        String childUrl = "http://www.xyz.com/";
        CrawlerResponse response = CrawlerResponse.builder()
                .childrenFutures(new ArrayList<>())
                .currentDepth(1)
                .currentURL(childUrl)
                .build();

        String siteURL = "http://www.abc.com/";
        Mockito.when(mockFuture.get()).thenReturn(response);
        Mockito.when(mockExecutor.submit(Mockito.any(CrawlerCallable.class))).thenReturn(mockFuture);
        Mockito.when(mockExecutor.isTerminated()).thenReturn(true);

        PowerMockito.mockStatic(Executors.class);
        PowerMockito.when(Executors.newFixedThreadPool(Mockito.eq(1))).thenReturn(mockExecutor);

        WebCrawler crawlerService = new WebCrawler(1, 1, false);

        BiConsumer<Map, Optional<String>> consumer = new BiConsumer<Map, Optional<String>>() {
            @Override
            public void accept(Map map, Optional<String> s) {
                Assert.assertFalse(s.isPresent());
                Assert.assertEquals(1, map.size());
                Assert.assertTrue(map.containsKey(siteURL));
                List<Map> list = (List<Map>) map.get(siteURL);
                Assert.assertTrue(list.get(0).containsKey(childUrl));
            }
        };

        crawlerService.crawlSite(siteURL, consumer);


        Mockito.verify(mockExecutor).submit(Mockito.any(CrawlerCallable.class));
        PowerMockito.verifyStatic();
        Executors.newFixedThreadPool(Mockito.anyInt());

        Mockito.verify(mockExecutor).submit(Mockito.any(CrawlerCallable.class));
        Mockito.verify(mockFuture).get();
    }


}
