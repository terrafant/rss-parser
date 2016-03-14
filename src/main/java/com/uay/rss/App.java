package com.uay.rss;

import com.rometools.rome.feed.synd.SyndFeed;
import com.uay.rss.model.Feed;
import com.uay.rss.parser.RssJavaxParser;
import com.uay.rss.parser.RssRomeParser;

public class App {

    public static final String URL_TO_PARSE = "http://dou.ua/lenta/articles/feed";

    public static void main(String[] args) {
        RssJavaxParser rssJavaxParser = new RssJavaxParser();
        Feed feed = rssJavaxParser.parseRssFeed(URL_TO_PARSE);

        RssRomeParser rssRomeParser = new RssRomeParser();
        SyndFeed syndFeed = rssRomeParser.parseRssFeed(URL_TO_PARSE);
    }
}
