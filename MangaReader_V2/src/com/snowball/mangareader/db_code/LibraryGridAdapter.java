package com.snowball.mangareader.db_code;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.snowball.mangareader.MangaReader;
import com.snowball.mangareader.R;
import com.snowball.mangareader.interface_code.TextProgressBar;

public class LibraryGridAdapter extends BaseAdapter {

	private Activity activity;
	private static Cursor cursor;

	public LibraryGridAdapter(Activity activity, Cursor c) {
		super();
		this.activity = activity;
		setCursor(c);
	}

	static class LibraryViewHolder {
		TextView mTitle;
		TextView mAuthor;
		ImageView mCover;
		RatingBar mRating;
		ImageView mDownloading;
		TextProgressBar mProgress;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final LibraryViewHolder viewHolder;

		getCursor().moveToPosition(position);
		if (convertView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			convertView = inflater.inflate(R.layout.main_library_element,
					parent, false);
			viewHolder = new LibraryViewHolder();
			viewHolder.mTitle = (TextView) convertView
					.findViewById(R.id.main_library_element_book_name);
			viewHolder.mAuthor = (TextView) convertView
					.findViewById(R.id.main_library_element_book_author);
			viewHolder.mCover = (ImageView) convertView
					.findViewById(R.id.main_library_element_book_cover);
			viewHolder.mRating = (RatingBar) convertView
					.findViewById(R.id.main_library_element_book_rating);
			viewHolder.mDownloading = (ImageView) convertView.findViewById(R.id.main_library_element_download_btn);
			viewHolder.mProgress = (TextProgressBar) convertView.findViewById(R.id.main_library_element_progress);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (LibraryViewHolder) convertView.getTag();
		}
		
		// Populate GUI
		MangaReader.mChapterCursor = MangaReader.mDbHelper.fetchDownloadingChapters(getCursor().getString(getCursor().getColumnIndex(DbAdapter.KEY_BOOK_TABLE)));
		if (MangaReader.mChapterCursor.moveToFirst()) {
			viewHolder.mDownloading.setVisibility(View.VISIBLE);
			viewHolder.mProgress.setText("Chapter " + MangaReader.mChapterCursor.getString(MangaReader.mChapterCursor.getColumnIndex(DbAdapter.KEY_BOOK_NUM)));
			viewHolder.mProgress.setProgress(MangaReader.mChapterCursor.getInt(MangaReader.mChapterCursor.getColumnIndex(DbAdapter.KEY_BOOK_DOWNLOAD_PERCENT)));
		} else {
			viewHolder.mDownloading.setVisibility(View.GONE);
			viewHolder.mProgress.setText("Download complete");
			viewHolder.mProgress.setProgress(100);
		}
		viewHolder.mProgress.setTextSize(9);
		
		viewHolder.mTitle.setText(getCursor().getString(
				getCursor().getColumnIndex(DbAdapter.KEY_TITLE)));
		viewHolder.mAuthor.setText(getCursor().getString(
				getCursor().getColumnIndex(DbAdapter.KEY_AUTHOR)));
		viewHolder.mRating.setRating(getCursor().getInt(
				getCursor().getColumnIndex(DbAdapter.KEY_RATING)));
		byte[] cover = getCursor().getBlob(
				getCursor().getColumnIndex(DbAdapter.KEY_COVER_THUMB));
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565; // This'll lower memory usage
		viewHolder.mCover.setImageBitmap(BitmapFactory.decodeByteArray(cover,
				0, cover.length));
		return convertView;
	}

	@Override
	public int getCount() {
		return getCursor().getCount();
	}

	@Override
	public Object getItem(int position) {
		getCursor().moveToPosition(position);
		return getCursor().getString(
				getCursor().getColumnIndex(DbAdapter.KEY_TITLE));
	}

	@Override
	public long getItemId(int position) {
		getCursor().moveToPosition(position);
		return getCursor()
				.getLong(getCursor().getColumnIndex(DbAdapter.KEY_ID));
	}

	public static Cursor getCursor() {
		return cursor;
	}

	public void setCursor(Cursor cursor) {
		LibraryGridAdapter.cursor = cursor;
	}

}
