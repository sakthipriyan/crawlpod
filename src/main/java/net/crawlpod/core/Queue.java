package net.crawlpod.core;

import java.util.List;

public interface Queue {
	
	boolean enqueue(List<CrawlRequest> requests);

	CrawlRequest dequeue();

	boolean failed(CrawlRequest requset, CrawlResponse response);

	long crawled();

	long size();

	boolean shutdown();
}
