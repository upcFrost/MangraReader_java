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
import android.widget.TextView;

import com.snowball.mangareader.R;

public class SearchGridAdapter extends BaseAdapter {

	private Activity activity;
	private static Cursor cursor;

	public SearchGridAdapter(Activity activity, Cursor c) {
		super();
		this.activity = activity;
		setCursor(c);
	}

	static class SearchViewHolder {
		TextView mTitle;
		TextView mAuthor;
		ImageView mCover;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final SearchViewHolder viewHolder;

		getCursor().moveToPosition(position);
		if (convertView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			convertView = inflater.inflate(R.layout.main_search_element,
					parent, false);
			viewHolder = new SearchViewHolder();
			viewHolder.mTitle = (TextView) convertView
					.findViewById(R.id.main_search_element_book_name);
			viewHolder.mCover = (ImageView) convertView
					.findViewById(R.id.main_search_element_book_cover);
			viewHolder.mAuthor = (TextView) convertView
					.findViewById(R.id.main_search_element_book_author);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (SearchViewHolder) convertView.getTag();
		}
		byte[] cover = getCursor().getBlob(
				getCursor().getColumnIndex(DbAdapter.KEY_COVER_THUMB));
		viewHolder.mTitle.setText(getCursor().getString(
				getCursor().getColumnIndex(DbAdapter.KEY_TITLE)));
		viewHolder.mAuthor.setText(getCursor().getString(
				getCursor().getColumnIndex(DbAdapter.KEY_AUTHOR)));
		
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
		SearchGridAdapter.cursor = cursor;
	}

}
