package com.snowball.mangareader.db_code;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
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
import com.snowball.mangareader.interface_code.TextProgressBar;

public class DownloadListAdapter extends BaseAdapter {

	public Context context;
	public String[] chapterName;
	public int[] chapterDownloadPercent;
	public int[] chapterDownloading;
	private static Cursor cursor;

	public DownloadListAdapter(Context context, Cursor c) {
		super();
		this.context = context;
		setCursor(c);
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

	static class DownloadViewHolder {
		TextProgressBar progressBar;
		Button startPauseButton;
		Button cancelButton;
		TextView chapterLabel;
		RelativeLayout layout;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final DownloadViewHolder viewHolder;

		getCursor().moveToPosition(position);
		// Grab GUI
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.book_list_element, parent,
					false);

			viewHolder = new DownloadViewHolder();
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

		// Set tags
		Long[] tag = {
				getCursor().getLong(
						getCursor().getColumnIndex(DbAdapter.KEY_DOWNLOAD_MANGA_ID)),
				getCursor().getLong(
						getCursor().getColumnIndex(DbAdapter.KEY_DOWNLOAD_CHAPTER_ID)),
				(long) position};
		viewHolder.startPauseButton.setTag(tag);
		viewHolder.cancelButton.setTag(tag);
		viewHolder.chapterLabel.setTag(tag);
		viewHolder.layout.setTag(tag);
		// Populate GUI
		viewHolder.chapterLabel.setText(getCursor().getString(
				getCursor().getColumnIndex(DbAdapter.KEY_DOWNLOAD_TITLE)));
		viewHolder.progressBar.setText(getCursor().getString(
				getCursor().getColumnIndex(DbAdapter.KEY_DOWNLOAD_TITLE)));


		switch (getCursor().getInt(
				getCursor().getColumnIndex(DbAdapter.KEY_DOWNLOAD_STATE))) {
		case StaticValues.DOWNLOADING:
			setDownloadingView(viewHolder, position);
			break;
		case StaticValues.PAUSED:
			setPausedView(viewHolder, position);
			break;
		case StaticValues.DOWNLOADED:
			setDownloadedView(viewHolder, position);
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
		DownloadListAdapter.cursor = cursor;
	}

	

	
	
	
	
	/******************************************
	 ******************************************
	 **********     VIEW BLOCK    *************
	 ******************************************
	 ******************************************/

	private void setDownloadingView(final DownloadViewHolder viewHolder, int position) {
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

	private void setNotDownloadingView(final DownloadViewHolder viewHolder, int position) {
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
					MangaReader.mDbHelper.addDownload(mangaId, chapterId,title, StaticValues.DOWNLOADING, 0);
					setDownloadingView(viewHolder, position);
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
					MangaReader.mDbHelper.addDownload(mangaId, chapterId,title, StaticValues.DOWNLOADING, 0);
					setDownloadingView(viewHolder, position);
				}
			}
		});
	}

	private void setPausedView(final DownloadViewHolder viewHolder, int position) {
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

	private void setDownloadedView(final DownloadViewHolder viewHolder, int position) {
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
