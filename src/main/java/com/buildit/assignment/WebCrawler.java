package com.buildit.assignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WebCrawler {

	private int depthLimit;
    private int threadPoolLimit;
    private boolean isExternalCrawlingAllowed;
    
    private static Logger logger = Logger.getLogger(WebCrawler.class.getCanonicalName());

    
	@SuppressWarnings("rawtypes")
	public void crawlSite(String siteURL, BiConsumer<Map, Optional<String>> consumer) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolLimit);
        Map<String, List> result = new HashMap<>();
        List<String> errorList = new ArrayList<>();
        Optional<String> errorMsg = Optional.empty();
        try {
            CrawlerCallable masterCallable = CrawlerCallable.builder()
                    .currentDepth(0)
                    .depthLimit(depthLimit)
                    .executorService(executorService)
                    .crawledSites(new ConcurrentSkipListSet<>())
                    .currentURL(siteURL)
                    .isExternalCrawlingAllowed(isExternalCrawlingAllowed)
                    .build();
            List<Map> resultList = handleRecursiveCrawls(Collections.singletonList(executorService.submit(masterCallable)), errorList);
            result.put(siteURL, resultList);
        } catch (Exception ie) {
            errorMsg = Optional.of(ie.getMessage());
            logger.log(Level.SEVERE, ie.getMessage(), ie);
            // only the top most one??
        } finally {
            executorService.shutdown();
            while (!executorService.isTerminated()) {
                // just wait in mainthread for executors to complete
            }
            if(!errorList.isEmpty()) {
                errorMsg = Optional.of(errorList.toString());
                logger.severe(errorList.toString());
            }
            logger.info("Invoking consumer with data");
            consumer.accept(result, errorMsg);
        }
    }


    @SuppressWarnings("rawtypes")
	private List<Map> handleRecursiveCrawls(List<Future<CrawlerResponse>> futures, List<String> errorList) {
        //logger.info("Starting handleRecursiveCrawls.");
        List<Map> parents = new ArrayList<>();
        for (Future<CrawlerResponse> future : futures) {
            Map<String, List<Map>> parentResponse = new HashMap<>();
            try {
                CrawlerResponse crawlerResponse = future.get();
                List<Map> children = new ArrayList<>();
                logger.info("crawlerResponse.getCurrentURL():::" + crawlerResponse.getCurrentURL());
                List<Future<CrawlerResponse>> childrenFutures = crawlerResponse.getChildrenFutures();
                if(!childrenFutures.isEmpty()) {
                    children.addAll(handleRecursiveCrawls(childrenFutures, errorList));
                }
                parentResponse.put(crawlerResponse.getCurrentURL(), children);
                parents.add(parentResponse);
            } catch (Exception ie){
                logger.log(Level.SEVERE, ie.getMessage());
                // construct a nested error map
                errorList.add(ie.getMessage());
            }
        }
        //logger.info("Ending handleRecursiveCrawls.");
        return parents;
    }
    
    public static void main(String args[]) {
    	String seedUrl = args[0];
    	int maxDepth = Integer.parseInt(args[1]);
    	//boolean isExternalCrawlingAllowed = Boolean.parseBoolean(args[2]);
    	//String seedUrl = "http://wiprodigital.com";
    	//int maxDepth = 3;
    	boolean isExternalCrawlingAllowed = Boolean.FALSE;
    	
    	crawl(seedUrl, maxDepth, isExternalCrawlingAllowed,(r, e) -> {
            if(e.isPresent()) {
                System.out.println("Error::"+e.get());
            } else {
                System.out.println("Crawled successfully. Crawled sites:");
                System.out.println(Collections.singletonList(r).toString());
            }
        });
    }
    
    @SuppressWarnings("rawtypes")
	static void crawl(String s, int depth, boolean isExternalCrawlingAllowed, BiConsumer<Map, Optional<String>> consumer){
        WebCrawler crawler = new WebCrawler(depth, 5, isExternalCrawlingAllowed);
        crawler.crawlSite(s, consumer);
    }

}
