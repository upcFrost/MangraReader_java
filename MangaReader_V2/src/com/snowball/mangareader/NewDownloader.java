package com.snowball.mangareader;

import java.util.ArrayList;

import com.snowball.mangareader.db_code.DbAdapter;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

public class NewDownloader extends AsyncTask<Long, Integer, Boolean> {
	private DownloadManager mgr;
	private Cursor downloads;
	private Cursor dmRow;
	private Cursor mangaCursor;

	public NewDownloader() {
		MangaReader.getContext();
		mgr = (DownloadManager) MangaReader.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
	}
	
	@Override
	protected Boolean doInBackground(Long... params) {
		while (true) {
			// Get current query
			DownloadManager.Query q = new DownloadManager.Query();
			// Get downloads (with manga/chapter IDs) from DB
			downloads = MangaReader.mDbHelper.fetchAllDownloads();
			for(downloads.moveToFirst(); !downloads.isAfterLast(); downloads.moveToNext()) {
				q.setFilterById(downloads.getLong(downloads.getColumnIndex(DbAdapter.KEY_DOWNLOAD_DM_ID)));
				dmRow = mgr.query(q);
				dmRow.moveToFirst();
				// Calculate progress
				int bytes_downloaded = dmRow.getInt(dmRow.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
				int total_size = dmRow.getInt(dmRow.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
				int progress = (int) bytes_downloaded/total_size;
				// Get manga and chapter IDs from DB
				long manga_id = downloads.getLong(downloads.getColumnIndex(DbAdapter.KEY_DOWNLOAD_MANGA_ID));
				long chapter_id = downloads.getLong(downloads.getColumnIndex(DbAdapter.KEY_DOWNLOAD_CHAPTER_ID));
				// Update download status
				int status;
				switch (dmRow.getInt(dmRow.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
				case DownloadManager.STATUS_PENDING:
						break;
				
				case DownloadManager.STATUS_RUNNING:
						break;
				
				case DownloadManager.STATUS_PAUSED:
						break;
				
				case DownloadManager.STATUS_SUCCESSFUL:
						break;
				}
				MangaReader.mDbHelper.updateDownload(manga_id, chapter_id, StaticValues.DOWNLOADING, progress, -1);
				mangaCursor = MangaReader.mDbHelper.fetchMangaById(manga_id);
				// Get book table name, update chapter info and notify all adapters about changes
				String bookTable = mangaCursor.getString(mangaCursor.getColumnIndex(DbAdapter.KEY_BOOK_TABLE));
				MangaReader.mDbHelper.updateChapter(bookTable, chapter_id, null, null, -1, -1, -1, progress, -1);
				Main.notifyAdapters(bookTable);
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
