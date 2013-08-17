package com.snowball.mangareader.db_code;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;

import com.snowball.mangareader.R;

public class GenreGridAdapter extends BaseAdapter {
	
	public Context context;
	private static Cursor cursor;
	
	public GenreGridAdapter(Context context, Cursor c) {
		super();
		this.context = context;
		setCursor(c);
	}

	public static Cursor getCursor() {
		return cursor;
	}

	public void setCursor(Cursor cursor) {
		GenreGridAdapter.cursor = cursor;
	}

	@Override
	public int getCount() {
		return getCursor().getCount();
	}

	@Override
	public Object getItem(int position) {
		getCursor().moveToPosition(position);
		return getCursor().getString(
				getCursor().getColumnIndex(DbAdapter.KEY_GENRES_NAME));
	}

	@Override
	public long getItemId(int position) {
		getCursor().moveToPosition(position);
		return getCursor()
				.getLong(getCursor().getColumnIndex(DbAdapter.KEY_GENRES_ID));
	}

	public static class GenreViewHolder {
		RadioButton genreButton;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final GenreViewHolder viewHolder;

		getCursor().moveToPosition(position);
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.main_search_advanced_grid_element, parent,
					false);
			viewHolder = new GenreViewHolder();
			viewHolder.genreButton = (RadioButton)convertView.findViewById(R.id.main_search_advanced_gridRadio);
			viewHolder.genreButton.setTag(position);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (GenreViewHolder) convertView.getTag();
		}
		
		viewHolder.genreButton.setText(getCursor().getString(getCursor().getColumnIndex(DbAdapter.KEY_GENRES_NAME)));
		return convertView;
	}

}
