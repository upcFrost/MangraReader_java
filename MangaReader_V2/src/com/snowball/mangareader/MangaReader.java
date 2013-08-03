package com.snowball.mangareader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Queue;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.snowball.mangareader.db_code.DbAdapter;

public class MangaReader extends Application {

	/** Database handlers **/
	public static Cursor mBaseCursor;
	public static Cursor mBookCursor;
	public static Cursor mChapterCursor;
	public static Cursor mGenresCursor;
	public static Cursor mDownloadsCursor;
	public static DbAdapter mDbHelper;

	/** Download manager **/
	public static SharedPreferences preferenceManager;
	public static String DownloadID;
	public static Downloader downloader;
	
	public static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		preferenceManager = PreferenceManager.getDefaultSharedPreferences(this);
		StaticValues.mangaDirPath = Environment.getExternalStorageDirectory() + "/MangaReader/";
		new File(StaticValues.mangaDirPath).mkdirs();

		mDbHelper = new DbAdapter(this);
		try {
			mDbHelper.createDataBase();
		} catch (IOException ioe) {
			throw new Error("Unable to create database");
		}
		try {
			mDbHelper.openDataBase();
		} catch (SQLException sqle) {
			throw sqle;
		}
		context = getApplicationContext();
		
		downloader = new Downloader();
		downloader.execute();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		mDbHelper.close();
		mBaseCursor.close();
		mBookCursor.close();
		mChapterCursor.close();
	}
	
	public static Context getContext(){
		return context;
	}
}
