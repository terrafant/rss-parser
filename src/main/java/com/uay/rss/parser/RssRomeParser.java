package com.uay.rss.parser;

import com.rometools.fetcher.FeedFetcher;
import com.rometools.fetcher.FetcherEvent;
import com.rometools.fetcher.FetcherListener;
import com.rometools.fetcher.impl.HashMapFeedInfoCache;
import com.rometools.fetcher.impl.HttpURLFeedFetcher;
import com.rometools.rome.feed.synd.SyndFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Rome RSS parser
 */
public class RssRomeParser {

    private static final Logger logger = LoggerFactory.getLogger(RssRomeParser.class);

    public SyndFeed parseRssFeed(String urlToParse) {
        SyndFeed feed = null;
        try {
            final URL feedUrl = new URL(urlToParse);
            final FeedFetcher fetcher = new HttpURLFeedFetcher(HashMapFeedInfoCache.getInstance());
            final FetcherEventListenerImpl listener = new FetcherEventListenerImpl();
            fetcher.addFetcherEventListener(listener);

            logger.info("Retrieving feed " + feedUrl);
            feed = fetcher.retrieveFeed(feedUrl);
            logger.info(urlToParse + "[title: '" + feed.getTitle() + "'; size: " + feed.getEntries().size() + "}");
        } catch (final Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return feed;
    }

    static class FetcherEventListenerImpl implements FetcherListener {
        public void fetcherEvent(final FetcherEvent event) {
            final String eventType = event.getEventType();
            if (FetcherEvent.EVENT_TYPE_FEED_POLLED.equals(eventType)) {
                logger.debug("EVENT: Feed Polled. URL = " + event.getUrlString());
            } else if (FetcherEvent.EVENT_TYPE_FEED_RETRIEVED.equals(eventType)) {
                logger.debug("EVENT: Feed Retrieved. URL = " + event.getUrlString());
            } else if (FetcherEvent.EVENT_TYPE_FEED_UNCHANGED.equals(eventType)) {
                logger.debug("EVENT: Feed Unchanged. URL = " + event.getUrlString());
            }
        }
    }

}
