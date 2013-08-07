package com.snowball.mangareader.db_code;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.snowball.mangareader.Downloader;
import com.snowball.mangareader.ImageActivity;
import com.snowball.mangareader.Main;
import com.snowball.mangareader.MangaReader;
import com.snowball.mangareader.R;
import com.snowball.mangareader.StaticValues;
import com.snowball.mangareader.db_code.DownloadListAdapter.DownloadViewHolder;
import com.snowball.mangareader.interface_code.TextProgressBar;

public class ChapterListAdapter extends BaseAdapter {

	public Context context;
	public String[] chapterName;
	public int[] chapterDownloadPercent;
	public int[] chapterDownloading;
	private static Cursor cursor;
	public long mangaId;
	public long chapterId;
	private String bookTable;

	public ChapterListAdapter(Context context, Cursor c, long bookId,
			String bookTable) {
		super();
		this.context = context;
		setCursor(c);
		this.mangaId = bookId;
		this.bookTable = bookTable;
	}

	@Override
	public int getCount() {
		return getCursor().getCount();
	}

	@Override
	public Object getItem(int position) {
		getCursor().moveToPosition(position);
		return getCursor().getString(
				getCursor().getColumnIndex(DbAdapter.KEY_BOOK_TITLE));
	}

	@Override
	public long getItemId(int position) {
		getCursor().moveToPosition(position);
		return getCursor().getLong(
				getCursor().getColumnIndex(DbAdapter.KEY_BOOK_ID));
	}

	static class ChapterViewHolder {
		TextProgressBar progressBar;
		Button startPauseButton;
		Button cancelButton;
		TextView chapterLabel;
		RelativeLayout layout;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ChapterViewHolder viewHolder;

		getCursor().moveToPosition(position);
		// if (convertView == null) {
		// Grab GUI
		LayoutInflater inflater = LayoutInflater.from(context);
		convertView = inflater.inflate(R.layout.book_list_element, parent,
				false);

		viewHolder = new ChapterViewHolder();
		viewHolder.progressBar = (TextProgressBar) convertView
				.findViewById(R.id.book_list_element_progress);
		viewHolder.startPauseButton = (Button) convertView
				.findViewById(R.id.book_list_element_start_button);
		viewHolder.chapterLabel = (TextView) convertView
				.findViewById(R.id.book_list_element_chapter_name);
		viewHolder.cancelButton = (Button) convertView
				.findViewById(R.id.book_list_element_cancel_button);
		viewHolder.layout = (RelativeLayout) convertView
				.findViewById(R.id.book_list_element_layout);
		convertView.setTag(viewHolder);
		// } else {
		// viewHolder = (ChapterViewHolder) convertView.getTag();
		// }

		// Set tags
		chapterId = getCursor().getLong(
				getCursor().getColumnIndex(DbAdapter.KEY_BOOK_ID));
		Long[] tag = {mangaId, chapterId, (long) position};
		viewHolder.startPauseButton.setTag(tag);
		viewHolder.cancelButton.setTag(tag);
		viewHolder.chapterLabel.setTag(tag);
		viewHolder.layout.setTag(tag);
		// Populate GUI
		viewHolder.chapterLabel.setText(getCursor().getString(
				getCursor().getColumnIndex(DbAdapter.KEY_BOOK_TITLE)));
		viewHolder.progressBar.setText(getCursor().getString(
				getCursor().getColumnIndex(DbAdapter.KEY_BOOK_TITLE)));

		switch (getCursor().getInt(
				getCursor().getColumnIndex(DbAdapter.KEY_BOOK_STATE))) {
		case StaticValues.DOWNLOADED:
			setDownloadedView(viewHolder, position);
			break;
		case StaticValues.DOWNLOADING:
			setDownloadingView(viewHolder, position);
			break;
		case StaticValues.PAUSED:
			setPausedView(viewHolder, position);
			break;
		case StaticValues.NOT_DOWNLOADING:
			setNotDownloadingView(viewHolder, position);
			break;
		default:
			break;
		}

		return convertView;
	}

	public static Cursor getCursor() {
		return cursor;
	}

	public void setCursor(Cursor cursor) {
		ChapterListAdapter.cursor = cursor;
	}
	
	
	
	
	
	
	
	/******************************************
	 ******************************************
	 **********     VIEW BLOCK    *************
	 ******************************************
	 ******************************************/

	private void setDownloadingView(final ChapterViewHolder viewHolder, int position) {
		getCursor().moveToPosition(position);
		viewHolder.progressBar.setProgress(getCursor()
				.getInt(getCursor().getColumnIndex(
						DbAdapter.KEY_BOOK_DOWNLOAD_PERCENT)));
		viewHolder.progressBar.setTextStyle("Arial", true, false);
		viewHolder.progressBar.setTextSize(14);
		viewHolder.progressBar.setTextColor(Color.WHITE);
		viewHolder.progressBar.setVisibility(TextProgressBar.VISIBLE);
		viewHolder.startPauseButton.setBackgroundResource(R.drawable.pause);
		viewHolder.startPauseButton.setVisibility(Button.VISIBLE);
		viewHolder.startPauseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				long mangaId = ((Long[]) v.getTag())[0];
				long chapterId = ((Long[]) v.getTag())[1];
				int position = (int) (long) ((Long[]) v.getTag())[2];
				if (Downloader.manga_id == mangaId && Downloader.chapter_id == chapterId) {
					Downloader.currentDownloading = StaticValues.PAUSED;
				}
				setPausedView(viewHolder, position);
			}
		});
		viewHolder.cancelButton.setVisibility(Button.VISIBLE);
		viewHolder.cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				long mangaId = ((Long[]) v.getTag())[0];
				long chapterId = ((Long[]) v.getTag())[1];
				int position = (int) (long) ((Long[]) v.getTag())[2];
				MangaReader.mDbHelper.deleteDownload(mangaId, chapterId);
				if (Downloader.manga_id == mangaId && Downloader.chapter_id == chapterId) {
					Downloader.currentDownloading = StaticValues.NOT_DOWNLOADING;
				}
				MangaReader.mBookCursor = MangaReader.mDbHelper.fetchMangaById(mangaId);
				MangaReader.mBookCursor.moveToFirst();
				String bookTable = MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_BOOK_TABLE));
				MangaReader.mDbHelper.updateChapter(bookTable, chapterId, null, null, StaticValues.NOT_DOWNLOADING, -1, -1, 0, -1);
				// TODO Recursive delete dir
				setNotDownloadingView(viewHolder, position);
				Main.notifyAdapters(bookTable);
			}
		});
		viewHolder.chapterLabel.setVisibility(TextView.GONE);
	}

	private void setNotDownloadingView(final ChapterViewHolder viewHolder, int position) {
		viewHolder.startPauseButton.setBackgroundResource(R.drawable.download);
		viewHolder.startPauseButton.setVisibility(Button.VISIBLE);
		viewHolder.startPauseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				long mangaId = ((Long[]) v.getTag())[0];
				long chapterId = ((Long[]) v.getTag())[1];
				int position = (int) (long) ((Long[]) v.getTag())[2];
				MangaReader.mDownloadsCursor = MangaReader.mDbHelper.fetchDownloadById(mangaId, chapterId);
				if (!MangaReader.mDownloadsCursor.moveToFirst()) {
					MangaReader.mBookCursor = MangaReader.mDbHelper.fetchMangaById(mangaId);
					MangaReader.mBookCursor.moveToFirst();
					String bookTable = MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_BOOK_TABLE));
					MangaReader.mChapterCursor = MangaReader.mDbHelper.fetchChapterById(bookTable, chapterId);
					MangaReader.mChapterCursor.moveToFirst();
					String title = MangaReader.mChapterCursor.getString(MangaReader.mChapterCursor.getColumnIndex(DbAdapter.KEY_BOOK_TITLE));
					MangaReader.mDbHelper.updateChapter(bookTable, chapterId, null, null,StaticValues.DOWNLOADING, -1, -1, -1, -1);
					MangaReader.mDbHelper.updateManga(mangaId, null, null, null, null, null, null, -1, -1, -1, null, 1, -1);
					MangaReader.mDbHelper.addDownload(mangaId, chapterId,title, StaticValues.DOWNLOADING, 0);
					setDownloadingView(viewHolder, position);
					Main.notifyAdapters(bookTable);
				}
			}
		});
		viewHolder.progressBar.setVisibility(TextProgressBar.GONE);
		viewHolder.chapterLabel.setTextColor(Color.BLACK);
		viewHolder.chapterLabel.setTextSize(14);
		viewHolder.chapterLabel.setVisibility(TextView.VISIBLE);
		viewHolder.cancelButton.setVisibility(Button.GONE);
		viewHolder.layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				long mangaId = ((Long[]) v.getTag())[0];
				long chapterId = ((Long[]) v.getTag())[1];
				int position = (int) (long) ((Long[]) v.getTag())[2];
				MangaReader.mDownloadsCursor = MangaReader.mDbHelper.fetchDownloadById(mangaId, chapterId);
				if (!MangaReader.mDownloadsCursor.moveToFirst()) {
					MangaReader.mBookCursor = MangaReader.mDbHelper.fetchMangaById(mangaId);
					MangaReader.mBookCursor.moveToFirst();
					String bookTable = MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_BOOK_TABLE));
					MangaReader.mChapterCursor = MangaReader.mDbHelper.fetchChapterById(bookTable, chapterId);
					MangaReader.mChapterCursor.moveToFirst();
					String title = MangaReader.mChapterCursor.getString(MangaReader.mChapterCursor.getColumnIndex(DbAdapter.KEY_BOOK_TITLE));
					MangaReader.mDbHelper.updateChapter(bookTable, chapterId, null, null,StaticValues.DOWNLOADING, -1, -1, -1, -1);
					MangaReader.mDbHelper.updateManga(mangaId, null, null, null, null, null, null, -1, -1, -1, null, 1, -1);
					MangaReader.mDbHelper.addDownload(mangaId, chapterId,title, StaticValues.DOWNLOADING, 0);
					setDownloadingView(viewHolder, position);
					Main.notifyAdapters(bookTable);
					
					// Use download manager to grab chapter
					// TODO: Make it work
//					String URL = MangaReader.mChapterCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_BOOK_URL));
//					String Title = MangaReader.mChapterCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_BOOK_TITLE));
//					DownloadManager.Request request = new DownloadManager.Request(Uri.parse(URL));
//					request.setTitle(Title);
//					DownloadManager manager = (DownloadManager) MangaReader.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
//					long downloadId = manager.enqueue(request);
//					MangaReader.mDbHelper.addDownload(mangaId, chapterId, downloadId);
//					Main.notifyAdapters(bookTable);
				}
			}
		});
	}

	private void setPausedView(final ChapterViewHolder viewHolder, int position) {
		getCursor().moveToPosition(position);
		viewHolder.progressBar.setProgress(getCursor()
				.getInt(getCursor().getColumnIndex(
						DbAdapter.KEY_BOOK_DOWNLOAD_PERCENT)));
		viewHolder.progressBar.setTextStyle("Arial", true, false);
		viewHolder.progressBar.setTextSize(14);
		viewHolder.progressBar.setTextColor(Color.WHITE);
		viewHolder.startPauseButton.setBackgroundResource(R.drawable.download_2);
		viewHolder.startPauseButton.setVisibility(Button.VISIBLE);
		viewHolder.startPauseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				long mangaId = ((Long[]) v.getTag())[0];
				long chapterId = ((Long[]) v.getTag())[1];
				int position = (int) (long) ((Long[]) v.getTag())[2];
				MangaReader.mDbHelper.updateDownload(mangaId, chapterId, StaticValues.DOWNLOADING, -1, -1);
				MangaReader.mBookCursor = MangaReader.mDbHelper.fetchMangaById(mangaId);
				MangaReader.mBookCursor.moveToFirst();
				String bookTable = MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_BOOK_TABLE));
				MangaReader.mDbHelper.updateChapter(bookTable, chapterId, null, null, StaticValues.DOWNLOADING, -1, -1, 0, -1);
				setDownloadingView(viewHolder, position);
			}
		});
		viewHolder.cancelButton.setVisibility(Button.VISIBLE);
		viewHolder.cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				long mangaId = ((Long[]) v.getTag())[0];
				long chapterId = ((Long[]) v.getTag())[1];
				int position = (int) (long) ((Long[]) v.getTag())[2];
				MangaReader.mDbHelper.deleteDownload(mangaId, chapterId);
				MangaReader.mBookCursor = MangaReader.mDbHelper.fetchMangaById(mangaId);
				MangaReader.mBookCursor.moveToFirst();
				String bookTable = MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_BOOK_TABLE));
				MangaReader.mDbHelper.updateChapter(bookTable, chapterId, null, null, StaticValues.NOT_DOWNLOADING, -1, -1, 0, -1);
				// TODO Recursive delete dir
				setNotDownloadingView(viewHolder, position);
				Main.notifyAdapters(bookTable);
			}
		});
		viewHolder.progressBar.setVisibility(TextProgressBar.VISIBLE);
		viewHolder.chapterLabel.setVisibility(TextView.GONE);
	}

	private void setDownloadedView(final ChapterViewHolder viewHolder, int position) {
		viewHolder.startPauseButton.setVisibility(Button.GONE);
		viewHolder.progressBar.setVisibility(TextProgressBar.GONE);
		viewHolder.chapterLabel.setTextColor(Color.BLACK);
		viewHolder.chapterLabel.setTextSize(14);
		viewHolder.chapterLabel.setVisibility(TextView.VISIBLE);
		viewHolder.layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Long[] tag = (Long[]) v.getTag(); // {genreId, chapterId}
				Intent i = new Intent(context, ImageActivity.class);
				i.putExtra("mangaId", tag[0]);
				i.putExtra("chapterId", tag[1]);
				context.startActivity(i);
			}
		});
		viewHolder.cancelButton.setVisibility(Button.VISIBLE);
		viewHolder.cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				long mangaId = ((Long[]) v.getTag())[0];
				long chapterId = ((Long[]) v.getTag())[1];
				int position = (int) (long) ((Long[]) v.getTag())[2];
				MangaReader.mDbHelper.deleteDownload(mangaId, chapterId);
				MangaReader.mBookCursor = MangaReader.mDbHelper.fetchMangaById(mangaId);
				MangaReader.mBookCursor.moveToFirst();
				String bookTable = MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_BOOK_TABLE));
				MangaReader.mDbHelper.updateChapter(bookTable, chapterId, null, null, StaticValues.NOT_DOWNLOADING, -1, -1, 0, -1);
				// TODO Recursive delete dir
				setNotDownloadingView(viewHolder, position);
				Main.notifyAdapters(bookTable);
			}
		});
	}
	
	
}
