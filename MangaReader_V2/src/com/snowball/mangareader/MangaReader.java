package com.snowball.mangareader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Queue;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	public static Bitmap mCoverImage;

	/** Download manager **/
	public static SharedPreferences preferenceManager;
	public static String DownloadID;
	public static Downloader downloader;
	
	public static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		// Load preferenceManager
		preferenceManager = PreferenceManager.getDefaultSharedPreferences(this);
		// Make storage dirs on SD card
		StaticValues.mangaDirPath = Environment.getExternalStorageDirectory() + "/MangaReader/";
		new File(StaticValues.mangaDirPath).mkdirs();
		// Open database
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
		// Load cover image
		AssetManager mngr = getAssets();
		try {
			InputStream bitmap = mngr.open("cover.jpg");
			mCoverImage = BitmapFactory.decodeStream(bitmap);
		} catch (Exception e) {
			throw new Error("Unable to load cover image");
		}
		// Store application context
		context = getApplicationContext();
		// Enable downloader service
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
