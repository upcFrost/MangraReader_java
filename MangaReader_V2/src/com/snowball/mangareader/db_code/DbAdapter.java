package com.snowball.mangareader.db_code;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DbAdapter extends SQLiteOpenHelper {
	// Main table fields
	public static final String KEY_ID = "_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_URL_HOME = "url_home";
	public static final String KEY_AUTHOR = "author";
	public static final String KEY_BOOK_TABLE = "book_table";
	public static final String KEY_LOCAL_PATH = "local_path";
	public static final String KEY_ABOUT = "about";
	public static final String KEY_RATING = "rating";
	public static final String KEY_CHAPTER_COUNT = "chapter_count";
	public static final String KEY_ONGOING = "ongoing";
	public static final String KEY_COVER = "cover";
	public static final String KEY_COVER_THUMB = "cover_thumb";
	public static final String KEY_COVER_URL = "cover_url";
	public static final String KEY_FAVORITE = "favorite";
	public static final String KEY_VERSION = "version";
	// Book table fields
	public static final String KEY_BOOK_ID = "_id";
	public static final String KEY_BOOK_URL = "chapter_url";
	public static final String KEY_BOOK_TITLE = "chapter_name";
	public static final String KEY_BOOK_STATE = "chapter_state";
	public static final String KEY_BOOK_NUM = "chapter_num";
	public static final String KEY_BOOK_PAGENUM = "chapter_page_num";
	public static final String KEY_BOOK_DOWNLOAD_PERCENT = "chapter_download_percent";
	public static final String KEY_BOOK_LAST_READ_PAGE = "chapter_last_page";
	public static final String KEY_BOOK_VERSION = "version";
	// Genres table fields
	public static final String KEY_GENRES_ID = "_id";
	public static final String KEY_GENRES_NAME = "genre";
	public static final String KEY_GENRES_VERSION = "version";
	// Genres_link table fields
	public static final String KEY_GENRES_LINK_GENRE = "genre_id";
	public static final String KEY_GENRES_LINK_MANGA = "manga_id";
	public static final String KEY_GENRES_LINK_VERSION = "version";
	// Downloads table
	public static final String KEY_DOWNLOAD_ID = "_id";
	public static final String KEY_DOWNLOAD_MANGA_ID = "manga_id";
	public static final String KEY_DOWNLOAD_CHAPTER_ID = "chapter_id";
	public static final String KEY_DOWNLOAD_TITLE = "chapter_name";
	public static final String KEY_DOWNLOAD_STATE = "chapter_state";
	public static final String KEY_DOWNLOAD_PERCENT = "chapter_download_percent";
	public static final String KEY_DOWNLOAD_LAST_PAGE = "last_page";
	// About DB
	private static final String DATABASE_NAME = "MangaBase";
	private static final String DATABASE_MAIN_TABLE = "MangaReader_Main";
	private static final String DATABASE_GENRES_TABLE = "MangaReader_Genres";
	private static final String DATABASE_GENRES_LINK_TABLE = "MangaReader_Genres_Link";
	private static final String DATABASE_DOWNLOADS_TABLE = "MangaReader_Downloads";
	private static final int DATABASE_VERSION = 1;
	// DB File Handlers
	private static final String DB_PATH = "/data/data/com.snowball.mangareader/databases/";
	private static final String DB_NAME = "MangaBase";
	private SQLiteDatabase mDb;
	private final Context mCtx;
	// Common SQL
	private static final String MAIN_TABLE_CREATE = "CREATE TABLE `MangaReader_Main` (`_id` INTEGER NOT NULL primary key autoincrement,`title` varchar(255) NOT NULL,`url_home` text NOT NULL,`author` text,`book_table` text,`local_path` text,`about` text,`rating` INTEGER   NULL,`chapter_count` INTEGER   NULL,`ongoing` INTEGER   NULL,`cover` longblob,`favorite` INTEGER   '0',`version` INTEGER NOT NULL);";
	private static final String BOOK_DATABASE_CREATE_TEMP = "CREATE TABLE `{mTableName}`(`_id` INTEGER NOT NULL primary key autoincrement,`chapter_name` varchar(255) NOT NULL,`chapter_url` text NOT NULL,`chapter_num` float   NULL,`chapter_state` INTEGER   '0', `chapter_page_num` INTEGER '0', `chapter_download_percent` INTEGER '0',`version` INTEGER NOT NULL);";
	private static final String GENRES_TABLE_CREATE = "CREATE TABLE `MangaReader_Genres` (`_id` INTEGER NOT NULL primary key autoincrement,`genre` varchar(255) NOT NULL,`version` INTEGER NOT NULL);";
	private static final String GENRES_LINK_TABLE_CREATE = "CREATE TABLE `MangaReader_Genres_Link` (`genre_id` INTEGER   NULL,`manga_id` INTEGER   NULL,`version` INTEGER NOT NULL);";
	private static final String DOWNLOADS_TABLE_CREATE = "CREATE TABLE `MangaReader_Downloads` (`_id` INTEGER NOT NULL primary key autoincrement, `manga_id` INTEGER NULL, `chapter_id` INTEGER NULL, `chapter_name` TEXT NOT NULL, `chapter_state` INTEGER '0', `chapter_download_percent` INTEGER '0', `last_page` INTEGER '1');";

	/*****************************
	 ***************************** 
	 ****** ADAPTER BLOCK ******
	 ***************************** 
	 *****************************/

	public DbAdapter(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
		this.mCtx = context;
	}

	/**
	 * Creates a empty database on the system and rewrites it with your own
	 * database.
	 * */
	public void createDataBase() throws IOException {

		boolean dbExist = checkDataBase();

		if (dbExist) {
			// do nothing - database already exist
		} else {

			// By calling this method and empty database will be created into
			// the default system path
			// of your application so we are gonna be able to overwrite that
			// database with our database.
			this.getWritableDatabase();

			try {

				copyDataBase();

			} catch (IOException e) {

				throw new Error("Error copying database");

			}
		}

	}

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase() {

		SQLiteDatabase checkDB = null;

		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.NO_LOCALIZED_COLLATORS);

		} catch (SQLiteException e) {

			// database does't exist yet.

		}

		if (checkDB != null) {

			checkDB.close();

		}

		return checkDB != null ? true : false;
	}

	/**
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 * */
	private void copyDataBase() throws IOException {

		// Open your local db as the input stream
		InputStream myInput = mCtx.getAssets().open(DB_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	public void openDataBase() throws SQLException {

		// Open the database
		String myPath = DB_PATH + DB_NAME;
		mDb = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.NO_LOCALIZED_COLLATORS);

	}

	@Override
	public synchronized void close() {

		if (mDb != null)
			mDb.close();

		super.close();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	/*****************************
	 ***************************** 
	 ******** ADD BLOCK ********
	 ***************************** 
	 *****************************/

	/** Add new book: 
	 *  @param id 
	 *  	should be -1 if not set
	 *  
	 *  @param cover 
	 *  	byteArray 
	 *  **/
	public long addManga(long id, String title, String url, String author, String table,
			String local_path, String about, int rating, int chapter_count,
			int ongoing, byte[] cover, int favorite, int version) {
		ContentValues cv = new ContentValues();
		if (id != -1)
			cv.put(KEY_ID, id);
		cv.put(KEY_TITLE, title);
		cv.put(KEY_URL_HOME, url);
		cv.put(KEY_AUTHOR, author);
		cv.put(KEY_BOOK_TABLE, table);
		if (local_path != null)
			cv.put(KEY_LOCAL_PATH, local_path);
		cv.put(KEY_ABOUT, about);
		cv.put(KEY_RATING, rating);
		cv.put(KEY_CHAPTER_COUNT, chapter_count);
		cv.put(KEY_ONGOING, ongoing);
		cv.put(KEY_COVER, cover);
		cv.put(KEY_FAVORITE, favorite);
		cv.put(KEY_VERSION, version);
		return mDb.insert(DATABASE_MAIN_TABLE, null, cv);
	}

	/** Add new chapter **/
	public long addChapter(String table, long id, String title, String url,
			int state, int num, int pagenum, int downloadpercent, int version) {
		ContentValues cv = new ContentValues();
		if (id != -1)
			cv.put(KEY_BOOK_ID, id);
		cv.put(KEY_BOOK_TITLE, title);
		cv.put(KEY_BOOK_URL, url);
		cv.put(KEY_BOOK_STATE, state);
		cv.put(KEY_BOOK_NUM, num);
		cv.put(KEY_BOOK_PAGENUM, num);
		cv.put(KEY_BOOK_DOWNLOAD_PERCENT, num);
		cv.put(KEY_BOOK_VERSION, version);
		return mDb.insert(table, null, cv);
	}

	/** Add genre **/
	public long addGenre(long id, String name, int version) {
		ContentValues cv = new ContentValues();
		if (id != -1)
			cv.put(KEY_GENRES_ID, id);
		cv.put(KEY_GENRES_NAME, name);
		cv.put(KEY_GENRES_VERSION, version);
		return mDb.insert(DATABASE_GENRES_TABLE, null, cv);
	}

	/** Add manga -> genre link **/
	public long addLinkMangaGenre(long manga_id, long genre_id) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_GENRES_LINK_GENRE, genre_id);
		cv.put(KEY_GENRES_LINK_MANGA, manga_id);
		return mDb.insert(DATABASE_GENRES_LINK_TABLE, null, cv);
	}

	public void addChapterTable(String tableName) {
		mDb.execSQL(BOOK_DATABASE_CREATE_TEMP
				.replace("{mTableName}", tableName));
	}
	
	/** Add download **/
	public long addDownload(long manga_id, long chapter_id,
			String title, int state, int downloadpercent) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_DOWNLOAD_MANGA_ID, manga_id);
		cv.put(KEY_DOWNLOAD_CHAPTER_ID, chapter_id);
		cv.put(KEY_DOWNLOAD_TITLE, title);
		cv.put(KEY_DOWNLOAD_STATE, state);
		cv.put(KEY_DOWNLOAD_PERCENT, downloadpercent);
		return mDb.insert(DATABASE_DOWNLOADS_TABLE, null, cv);
	}

	/*****************************
	 ***************************** 
	 ****** UPDATE BLOCK *******
	 ***************************** 
	 *****************************/

	/** Update general info about book. All params should be null if not set, except
	 * @param rating
	 * 		-1 if not set
	 * @param chapter_count
	 * 		-1 if not set
	 * @param ongoing
	 * 		-1 if not set
	 * @param favorite
	 * 		-1 if not set
	 * @param version
	 * 		-1 if not set **/
	public boolean updateManga(long id, String title, String url, String author, String table,
			String local_path, String about, int rating, int chapter_count,
			int ongoing, byte[] cover, int favorite, int version) {
		ContentValues cv = new ContentValues();
		if (title != null)
			cv.put(KEY_TITLE, title);
		if (url != null)
			cv.put(KEY_URL_HOME, url);
		if (author != null)
			cv.put(KEY_AUTHOR, author);
		if (table != null)
			cv.put(KEY_BOOK_TABLE, table);
		if (local_path != null)
			cv.put(KEY_LOCAL_PATH, local_path);
		if (about != null)
			cv.put(KEY_ABOUT, about);
		if (rating != -1)
			cv.put(KEY_RATING, rating);
		if (chapter_count != -1)
			cv.put(KEY_CHAPTER_COUNT, chapter_count);
		if (ongoing != -1)
			cv.put(KEY_ONGOING, ongoing);
		if (cover != null)
			cv.put(KEY_COVER, cover);
		if (favorite != -1)
			cv.put(KEY_FAVORITE, favorite);
		if (version != -1)
			cv.put(KEY_VERSION, version);
		return mDb.update(DATABASE_MAIN_TABLE, cv, KEY_ID +"=" + id, null) > 0;
	}
	
	/** Update chapter info **/
	public boolean updateChapter(String table, long id, String title, String url,
			int state, int num, int pagenum, int downloadpercent, int version) {
		ContentValues cv = new ContentValues();
		if (title != null) {
			cv.put(KEY_BOOK_TITLE, title);
		}
		if (url != null) {
			cv.put(KEY_BOOK_URL, url);
		}
		if (state != -1) {
			cv.put(KEY_BOOK_STATE, state);
		}
		if (num != -1) {
			cv.put(KEY_BOOK_NUM, num);
		}
		if (pagenum != -1) {
			cv.put(KEY_BOOK_PAGENUM, pagenum);
		}
		if (downloadpercent != -1) {
			cv.put(KEY_BOOK_DOWNLOAD_PERCENT, downloadpercent);
		}
		if (version != -1) {
			cv.put(KEY_BOOK_VERSION, version);
		}
		return mDb.update("`"+table+"`", cv, KEY_ID +"=" + id, null) > 0;
	}
	
	/** Update download status **/
	public boolean updateDownload(long manga_id, long chapter_id, 
			int state, int downloadpercent, int lastPage) {
		ContentValues cv = new ContentValues();
		if (downloadpercent != -1) {
			cv.put(KEY_DOWNLOAD_PERCENT, downloadpercent);
		}
		if (lastPage != -1) {
			cv.put(KEY_DOWNLOAD_LAST_PAGE, lastPage);
		}
		cv.put(KEY_DOWNLOAD_STATE, state);
		return mDb.update(DATABASE_DOWNLOADS_TABLE, cv, "manga_id = "+manga_id+" AND chapter_id = "+chapter_id, null) > 0;
	}
	
	/** Delete download **/
	public boolean deleteDownload(long manga_id, long chapter_id) {
		return mDb.delete(DATABASE_DOWNLOADS_TABLE, "manga_id = "+manga_id+" AND chapter_id = "+chapter_id, null) > 0;
	}
	
	/*****************************
	 ***************************** 
	 ******* FETCH BLOCK *******
	 ***************************** 
	 *****************************/

	/** Fetch main database for search **/
	public Cursor fetchSearchBase() {
		return mDb.rawQuery("SELECT DISTINCT _id, title, author, cover_thumb FROM MangaReader_Main ORDER BY title ASC", null);
	}
	
	public Cursor fetchSearchBaseByName(String key) {
		return mDb.rawQuery("SELECT DISTINCT _id, title, author, cover_thumb FROM MangaReader_Main WHERE title LIKE '%"+ key +"%' ORDER BY title ASC", null);
	}
	
	public Cursor fetchLibraryBase() {
		return mDb.rawQuery("SELECT DISTINCT * FROM MangaReader_Main WHERE favorite = 1 ORDER BY title ASC;", null);
	}
	
	public Cursor fetchGenres() {
		return mDb.rawQuery("SELECT DISTINCT * FROM MangaReader_Genres ORDER BY genre ASC", null);
	}

	public Cursor fetchMangaGenres(long manga_id) {
		return mDb.rawQuery("SELECT DISTINCT * FROM MangaReader_Genres JOIN MangaReader_Genres_Link ON MangaReader_Genres_Link.genre_id = MangaReader_Genres._id WHERE MangaReader_Genres_Link.manga_id ="+ manga_id + " ORDER BY genre ASC", null);
	}

	public Cursor fetchMangaById(long id) {
		return mDb.rawQuery("SELECT DISTINCT * FROM MangaReader_Main WHERE _id = {mKeyValue};".replace("{mKeyValue}", Long.toString(id)), null);
	}
	
	public Cursor fetchChapters(String table) {
		return mDb.rawQuery("SELECT DISTINCT * FROM `{mTable}` ORDER BY chapter_num ASC;".replace("{mTable}", table), null);
	}
	
	public Cursor fetchDownloadingChapters(String table) {
		return mDb.rawQuery("SELECT DISTINCT * FROM `{mTable}` WHERE chapter_state = 1 ORDER BY chapter_download_percent DESC;".replace("{mTable}", table), null);
	}
	
	public Cursor fetchChapterById(String table, long id) {
		return mDb.rawQuery(("SELECT DISTINCT * FROM `{mTable}` WHERE _id = "+id+";").replace("{mTable}", table), null);
	}
	
	public Cursor fetchAllDownloads() {
		return mDb.rawQuery("SELECT DISTINCT * FROM `MangaReader_Downloads` ORDER BY chapter_state ASC, chapter_download_percent DESC, _id DESC;", null);
	}
	
	public Cursor fetchActiveDownloads() {
		return mDb.rawQuery("SELECT DISTINCT * FROM `MangaReader_Downloads` WHERE chapter_state=1 ORDER BY _id ASC;", null);
	}
	
	public Cursor fetchDownloadById(long mangaId, long chapterId) {
		return mDb.rawQuery("SELECT DISTINCT * FROM `MangaReader_Downloads` WHERE manga_id = "+mangaId+" AND chapter_id = "+chapterId+";", null);
	}
}
