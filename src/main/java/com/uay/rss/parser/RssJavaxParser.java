package com.uay.rss.parser;

import com.uay.rss.model.Feed;
import com.uay.rss.model.FeedMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * StAX RSS parser
 */
public class RssJavaxParser {

    private static final Logger logger = LoggerFactory.getLogger(RssJavaxParser.class);

    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String LANGUAGE = "language";
    private static final String COPYRIGHT = "copyright";
    private static final String LINK = "link";
    private static final String AUTHOR = "author";
    private static final String CREATOR = "creator";
    private static final String ITEM = "item";
    private static final String PUB_DATE = "pubDate";
    private static final String LAST_BUILD_DATE = "lastBuildDate";
    private static final String GUID = "guid";

    private String description;
    private String title;
    private String link;
    private String language;
    private String copyright;
    private String author;
    private String pubdate;
    private String guid;

    private XMLEventReader eventReader;
    private boolean isFeedHeader;

    public Feed parseRssFeed(String urlToParse) {
        long start = System.currentTimeMillis();
        initFields(urlToParse);
        Feed feed = null;
        try {
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                feed = processXmlEvent(event, feed);
            }
        } catch (XMLStreamException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info(urlToParse + "[title: '" + feed.getTitle() + "'; size: " + feed.getMessages().size() + "}");
        return feed;
    }

    private void initFields(String urlToParse) {
        try {
            eventReader = constructXmlEventReader(urlToParse);
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
        isFeedHeader = true;
        description = "";
        title = "";
        link = "";
        language = "";
        copyright = "";
        author = "";
        pubdate = "";
        guid = "";
    }


    private Feed processXmlEvent(XMLEvent event, Feed feed) throws XMLStreamException {
        if (event.isStartElement()) {
            parseEventData(event);
            if (feed == null && !isFeedHeader) {
                feed = new Feed(title, link, description, language, copyright, pubdate);
            }
        } else if (event.isEndElement()) {
            if (ITEM.equals(event.asEndElement().getName().getLocalPart())) {
                feed.getMessages().add(constructFeedMessage());
                eventReader.nextEvent();
            }
        }
        return feed;
    }

    private FeedMessage constructFeedMessage() {
        FeedMessage message = new FeedMessage();
        message.setAuthor(author);
        message.setDescription(description);
        message.setGuid(guid);
        message.setLink(link);
        message.setTitle(title);
        return message;
    }

    private void parseEventData(XMLEvent event) throws XMLStreamException {
        switch (event.asStartElement().getName().getLocalPart()) {
            case ITEM:
                isFeedHeader = false;
                eventReader.nextEvent();
                break;
            case TITLE:
                title = getCharacterData(eventReader);
                break;
            case DESCRIPTION:
                description = getCharacterData(eventReader);
                break;
            case LINK:
                //to skip tags with prefix (e.g. atom:link)
                if (StringUtils.isEmpty(event.asStartElement().getName().getPrefix())) {
                    link = getCharacterData(eventReader);
                }
                break;
            case GUID:
                guid = getCharacterData(eventReader);
                break;
            case LANGUAGE:
                language = getCharacterData(eventReader);
                break;
            case AUTHOR:
            case CREATOR:
                author = getCharacterData(eventReader);
                break;
            case PUB_DATE:
            case LAST_BUILD_DATE:
                pubdate = getCharacterData(eventReader);
                break;
            case COPYRIGHT:
                copyright = getCharacterData(eventReader);
                break;
        }
    }

    private XMLEventReader constructXmlEventReader(String urlToParse) throws XMLStreamException {
        InputStream inputStream = createInputStream(urlToParse);

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.IS_COALESCING, true); // to handle special HTML characters like & etc.
        return inputFactory.createXMLEventReader(inputStream);
    }

    private String getCharacterData(XMLEventReader eventReader)
            throws XMLStreamException {
        String result = "";
        XMLEvent event = eventReader.nextEvent();
        if (event instanceof Characters) {
            result = event.asCharacters().getData();
        }
        return result;
    }

    private InputStream createInputStream(String urlToParse) {
        try {
            URL url = new URL(urlToParse);
            return url.openStream();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}