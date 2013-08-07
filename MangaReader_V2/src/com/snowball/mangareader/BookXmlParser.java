package com.snowball.mangareader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class BookXmlParser {
	// Vars
	public ArrayList<Page> pageArray;
	// XML parsers
	private XmlPullParserFactory factory;
	private XmlPullParser xpp; 
	// Consts
	private static final String CHAPTER_ID = "chapter_id";
	private static final String PAGE = "page";
	private static final String PAGE_ID = "page_id";
	private static final String PAGE_WIDTH = "width";
	private static final String PAGE_HEIGHT = "height";
	private static final String PAGE_OFFSET_X = "offset_x";
	private static final String PAGE_OFFSET_Y = "offset_y";

	public BookXmlParser() throws XmlPullParserException, IOException {
		// Initialize XML parsers
		factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		xpp = factory.newPullParser();
		pageArray = new ArrayList<Page>();
	}
	
	public ArrayList<Page> getPageArray(File filename, String chapterTitle) throws IOException, XmlPullParserException {
		// Open xml file
//		BufferedReader buf = new BufferedReader(new FileReader(filename));
		InputStream buf = MangaReader.getContext().getAssets().open("test.xml");
		// Set input for XML parser
		xpp.setInput(buf, null);
		boolean thisChapter = false;
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if(eventType == XmlPullParser.START_TAG) {
				// Check if we're reading the right chapter
				if (xpp.getName().equals(CHAPTER_ID)) {
					xpp.require(XmlPullParser.START_TAG, null, CHAPTER_ID);
					if (readText(xpp).equals(chapterTitle)) {
						thisChapter = true;
					} else {
						thisChapter = false;
					}
					xpp.require(XmlPullParser.END_TAG, null, CHAPTER_ID);
				}
				// Add page if we began reading the new page, else - read content
				if (xpp.getName().equals(PAGE) && thisChapter) {
					pageArray.add(new Page());
	  			} else if (!pageArray.isEmpty()) {
					analyzeTag(xpp, pageArray.get(pageArray.size()-1));
				}
			}
	        eventType = xpp.next();
		}
		return pageArray;
	}

	private void analyzeTag(XmlPullParser xpp, Page page) throws XmlPullParserException, IOException {
		// Analyze what tag we're currently have
		if (xpp.getName().equals(PAGE_OFFSET_X)) {
			// Check that we have start tag
			xpp.require(XmlPullParser.START_TAG, null, PAGE_OFFSET_X);
			// Set offset for current page
			page.setOffset_x(Integer.valueOf(readText(xpp)));
			// Check that we have end tag
			xpp.require(XmlPullParser.END_TAG, null, PAGE_OFFSET_X);
		} else if (xpp.getName().equals(PAGE_OFFSET_Y)) {
			xpp.require(XmlPullParser.START_TAG, null, PAGE_OFFSET_Y);
			page.setOffset_y(Integer.valueOf(readText(xpp)));
			xpp.require(XmlPullParser.END_TAG, null, PAGE_OFFSET_Y);
		} else if (xpp.getName().equals(PAGE_WIDTH)) {
			xpp.require(XmlPullParser.START_TAG, null, PAGE_WIDTH);
			page.setWidth(Integer.valueOf(readText(xpp)));
			xpp.require(XmlPullParser.END_TAG, null, PAGE_WIDTH);
		} else if (xpp.getName().equals(PAGE_HEIGHT)) {
			xpp.require(XmlPullParser.START_TAG, null, PAGE_HEIGHT);
			page.setHeight(Integer.valueOf(readText(xpp)));
			xpp.require(XmlPullParser.END_TAG, null, PAGE_HEIGHT);
		} else if (xpp.getName().equals(PAGE_ID)) {
			xpp.require(XmlPullParser.START_TAG, null, PAGE_ID);
			page.setId(Integer.valueOf(readText(xpp)));
			xpp.require(XmlPullParser.END_TAG, null, PAGE_ID);
		}
	}
	
	// For the tags title and summary, extracts their text values.
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
	    String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    return result;
	}

}
