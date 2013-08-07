package com.snowball.mangareader.db_code;

import java.io.InputStream;

import android.app.Activity;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.snowball.mangareader.CoverImageCoord;
import com.snowball.mangareader.MangaReader;
import com.snowball.mangareader.R;

public class SearchGridAdapter extends BaseAdapter {

	private Activity activity;
	private static Cursor cursor;

	public SearchGridAdapter(Activity activity, Cursor c) {
		super();
		this.activity = activity;
		setCursor(c);
	}
	
	// ViewHolder for the search grid (title, author, cover image)	
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
		
		// Get title string from DB
		viewHolder.mTitle.setText(getCursor().getString(
				getCursor().getColumnIndex(DbAdapter.KEY_TITLE)));
		// Get author string from DB
		viewHolder.mAuthor.setText(getCursor().getString(
				getCursor().getColumnIndex(DbAdapter.KEY_AUTHOR)));
		// Get cover integer values from DB
		CoverImageCoord coords = new CoverImageCoord(getCursor());
		// Get main bitmap from assets
		Bitmap cover = Bitmap.createBitmap(MangaReader.mCoverImage, coords.x, 
				coords.y, coords.w, coords.h);
		// Set this bitmap to the imageView
		viewHolder.mCover.setImageBitmap(cover);

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
