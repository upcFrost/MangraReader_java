package com.snowball.mangareader;

import java.util.regex.Pattern;


public class StaticValues {
	public static boolean libraryDetailShow = false;
	public static boolean isMenuShowing = false;
	public static int numColumnsLibrary = 2; // Fuck it, they've done it only in API 11
	public static int numColumnsSearch = 1;
	
	public static final int NOT_DOWNLOADING = 0;
	public static final int DOWNLOADING = 1;
	public static final int PAUSED = 2;
	public static final int DOWNLOADED = 3;
	
	static Pattern escaper = Pattern.compile("([^a-zA-z0-9])");
	
	public static String mangaDirPath;
	
	public static String escapeRE(String str) {
	    return escaper.matcher(str).replaceAll("_");
	}
	
	public static long search_popup_id = -1;
	public static boolean download_popup = false;
}
