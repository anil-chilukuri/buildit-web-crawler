package com.buildit.assignment;

import java.util.List;
import java.util.concurrent.Future;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CrawlerResponse {

	private String currentURL;
	private List<Future<CrawlerResponse>> childrenFutures;
	private int currentDepth;
	private String error;

}
