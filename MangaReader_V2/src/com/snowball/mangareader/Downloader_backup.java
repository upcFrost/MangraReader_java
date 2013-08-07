package com.snowball.mangareader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.SlidingDrawer;

import com.snowball.mangareader.db_code.DbAdapter;

/**
 * ImageDownload
 * 
 * @author frost
 * @param Integer
 *            [] tag: {Manga ID, Chapter ID}
 * 
 */

public class Downloader_backup extends AsyncTask<Long, Integer, Boolean> {
	private String bookTable;
	private String local_path;
	public static long manga_id;
	public static long chapter_id;
	int lastPage;
	public static int currentDownloading; // It'll be faster to use when paused or smth 

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(Boolean result) {

	}

	/**
	 * onProgressUpdate **
	 * 
	 * @param values
	 *            Integer[] {Download Percent}
	 */
	@Override
	protected void onProgressUpdate(Integer... values) {
		int progress = values[0];
		if (progress == 100) {
			MangaReader.mDbHelper.updateChapter(bookTable, chapter_id, null, null,StaticValues.DOWNLOADED, -1, -1, 100, -1);
			MangaReader.mDbHelper.updateDownload(manga_id, chapter_id, StaticValues.DOWNLOADED, progress, -1);
		} else if (progress != -1) {
			MangaReader.mDbHelper.updateDownload(manga_id, chapter_id, StaticValues.DOWNLOADING, progress, -1);
			MangaReader.mDbHelper.updateChapter(bookTable, chapter_id, null, null,-1, -1, -1, progress, -1);
		}
		Main.notifyAdapters(bookTable);
	}

	@Override
	protected Boolean doInBackground(Long... params) {
		ConnectivityManager cm =
		        (ConnectivityManager) MangaReader.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo;
		while (true) {
			boolean queue = false;
			while (!queue) {
				try {
					netInfo = cm.getActiveNetworkInfo();
					MangaReader.mDownloadsCursor = MangaReader.mDbHelper.fetchActiveDownloads();
					if (MangaReader.mDownloadsCursor.getCount() != 0 && netInfo != null && netInfo.isConnected()) {
						queue = true;
						currentDownloading = StaticValues.DOWNLOADING;
					} else {
						Thread.sleep(2000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			MangaReader.mDownloadsCursor.moveToFirst();
			manga_id = MangaReader.mDownloadsCursor.getLong(MangaReader.mDownloadsCursor.getColumnIndex(DbAdapter.KEY_DOWNLOAD_MANGA_ID));
			chapter_id = MangaReader.mDownloadsCursor.getLong(MangaReader.mDownloadsCursor.getColumnIndex(DbAdapter.KEY_DOWNLOAD_CHAPTER_ID));
			lastPage = MangaReader.mDownloadsCursor.getInt(MangaReader.mDownloadsCursor.getColumnIndex(DbAdapter.KEY_DOWNLOAD_LAST_PAGE));
			StringBuilder page = new StringBuilder();
			Matcher m;
			Pattern pic = Pattern.compile(".*<img src=\"(.*?goodmanga.net/images/manga.*?)[0-9]*.jpg.*?alt=.*?Page (.*?)\".*?>");
			Pattern total = Pattern.compile("</select>.*?<span>of ([0-9]*?)</span>");
			String line = null;
			String fullText = null;
			String commonURL = null;
			int pageTotal = 1;
			Cursor mangaCursor = MangaReader.mDbHelper.fetchMangaById(manga_id);
			mangaCursor.moveToFirst();
			Cursor chapterCursor = MangaReader.mDbHelper.fetchChapterById(mangaCursor.getString(mangaCursor.getColumnIndex(DbAdapter.KEY_BOOK_TABLE)),chapter_id);
			chapterCursor.moveToFirst();

			// Populate database and add manga to favorites
			local_path = StaticValues.mangaDirPath + StaticValues.escapeRE(mangaCursor.getString(mangaCursor.getColumnIndex(DbAdapter.KEY_BOOK_TABLE)));
			MangaReader.mDbHelper.updateManga(manga_id, null, null, null, null,	local_path, null, -1, -1, -1, null, 1, -1);
			bookTable = mangaCursor.getString(mangaCursor.getColumnIndex(DbAdapter.KEY_BOOK_TABLE));
			MangaReader.mDbHelper.updateChapter(bookTable, chapter_id, null,null, StaticValues.DOWNLOADING, -1, -1, -1, -1);
			//publishProgress(0);

			// Initialize download of the first page
			String url = chapterCursor.getString(chapterCursor.getColumnIndex(DbAdapter.KEY_BOOK_URL));
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet();
				request.setURI(new URI(url));
				HttpResponse response = client.execute(request);
				InputStream in = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				// Downloading the page EXCEPT linebrakes (because of using
				// "dot")
				while ((line = reader.readLine()) != null) {
					if (currentDownloading == StaticValues.DOWNLOADING) {
						page.append(line);
					} else {
						break;
					}					
				}
				// Check if was not canceled
				if (currentDownloading != StaticValues.DOWNLOADING) {
					MangaReader.mDbHelper.updateChapter(bookTable, chapter_id,null, null, currentDownloading, -1, -1,-1, -1);
					continue;
				}
				fullText = page.toString();

				// Now analyzing what we've downloaded
				m = total.matcher(fullText);
				if (m.find()) {
					pageTotal = Integer.decode(m.group(1));
					MangaReader.mDbHelper.updateChapter(bookTable, chapter_id,null, null, -1, -1, pageTotal, -1, -1);
					//publishProgress(0);
				} else {
					System.out.println("Downloader: 'total' pattern failed");
					MangaReader.mDbHelper.updateChapter(bookTable, chapter_id,null, null, StaticValues.NOT_DOWNLOADING, -1, -1,	-1, -1);
					publishProgress(0);
					continue;
				}

				m = pic.matcher(fullText);
				if (m.find()) {
					commonURL = m.group(1);
				} else {
					System.out.println("Downloader: 'pic' pattern failed");
					MangaReader.mDbHelper.updateChapter(bookTable, chapter_id,null, null, StaticValues.NOT_DOWNLOADING, -1, -1,	-1, -1);
					publishProgress(0);
					continue;
				}

				String filePath = local_path+ "/"+ chapterCursor.getInt(chapterCursor.getColumnIndex(DbAdapter.KEY_BOOK_NUM)) + "/";

				boolean downloaded = true;
				for (int pageNumber = lastPage; pageNumber <= pageTotal; pageNumber++) {
					String downloadURL = commonURL + pageNumber + ".jpg";
					String fileName = pageNumber + ".jpg";
					if (!downloadURL(downloadURL, pageTotal, pageNumber, filePath,fileName) || currentDownloading != StaticValues.DOWNLOADING) {
						downloaded = false;
						break;
					}
					if (currentDownloading == StaticValues.DOWNLOADING) {
						MangaReader.mDbHelper.updateDownload(manga_id, chapter_id, StaticValues.DOWNLOADING, -1, pageNumber);
					}
				}
				// Check if was not canceled
				if (currentDownloading != StaticValues.DOWNLOADING) {
					MangaReader.mDbHelper.updateChapter(bookTable, chapter_id,null, null, currentDownloading, -1, -1,-1, -1);
					MangaReader.mDbHelper.updateDownload(manga_id, chapter_id, currentDownloading, -1, -1);
					continue;
				}
				// Check if failed
				if (downloaded) {
					publishProgress(100);
				} else {
					MangaReader.mDbHelper.updateChapter(bookTable, chapter_id,null, null, StaticValues.NOT_DOWNLOADING, -1, -1,	-1, -1);
					publishProgress(0);
				}
				continue;

			} catch (Exception e) {
				e.printStackTrace();
				MangaReader.mDbHelper.updateChapter(bookTable, chapter_id,
						null, null, StaticValues.NOT_DOWNLOADING, -1, -1, -1,
						-1);
				publishProgress(0);
				continue;
			}
		}
	}

	private void notifyAdapters() {
		if (LibraryActivity.adapter != null) {
			MangaReader.mBaseCursor = MangaReader.mDbHelper.fetchLibraryBase();
			MangaReader.mBaseCursor.moveToFirst();
			LibraryActivity.adapter.setCursor(MangaReader.mBaseCursor);
			LibraryActivity.adapter.notifyDataSetChanged();
		}
		if (SearchActivity.adapter != null) {
			MangaReader.mBaseCursor = MangaReader.mDbHelper.fetchSearchBase();
			MangaReader.mBaseCursor.moveToFirst();
			SearchActivity.adapter.setCursor(MangaReader.mBaseCursor);
			SearchActivity.adapter.notifyDataSetChanged();
		}
		if (SearchActivity.adapter_popup != null) {
			MangaReader.mChapterCursor = MangaReader.mDbHelper
					.fetchChapters(bookTable);
			SearchActivity.adapter_popup.setCursor(MangaReader.mChapterCursor);
			SearchActivity.adapter_popup.notifyDataSetChanged();
		}
		if (BookActivity.adapter != null) {
			MangaReader.mChapterCursor = MangaReader.mDbHelper
					.fetchChapters(bookTable);
			BookActivity.adapter.setCursor(MangaReader.mChapterCursor);
			BookActivity.adapter.notifyDataSetChanged();
		}
		if (Main.adapter != null) {
			MangaReader.mDownloadsCursor = MangaReader.mDbHelper.fetchAllDownloads();
			Main.adapter.setCursor(MangaReader.mDownloadsCursor);
			Main.adapter.notifyDataSetChanged();
		}
	}

	/**
	 * Download file from URL
	 * 
	 * @param downloadURL
	 *            String, file URL
	 * @param downloadTitle
	 *            String, chapter title
	 * @param downloadPage
	 *            String, description like "Page 1 of 3"
	 * @param path
	 *            String, file dir path
	 * @param fileName
	 *            String, file name
	 */
	public boolean downloadURL(String downloadURL, int totalPage, int page,
			String path, String fileName) {
		try {
			URL url = new URL(downloadURL);
			URLConnection connection = url.openConnection();
			connection.connect();
			// download the file
			InputStream input = new BufferedInputStream(url.openStream());
			new File(path).mkdirs();
			OutputStream output = new FileOutputStream(path + fileName);

			byte data[] = new byte[1024];
			int count;
			while ((count = input.read(data)) != -1) {
				if (currentDownloading == StaticValues.DOWNLOADING) {
					output.write(data, 0, count);
				} else {
					MangaReader.mDbHelper.updateChapter(bookTable, chapter_id, null,
							null, currentDownloading, -1, -1, -1, -1);
					return false;
				}
			}

			output.flush();
			output.close();
			input.close();
			if ((int) 100*page/totalPage != 100) {
				publishProgress((int) (100 * page / totalPage));
			}
			return true;
		} catch (Exception e) {
			System.out.println(e);
			MangaReader.mDbHelper.updateChapter(bookTable, chapter_id, null,
					null, StaticValues.NOT_DOWNLOADING, -1, -1, -1, -1);
			publishProgress(0);
			return false;
		}
	}
}
