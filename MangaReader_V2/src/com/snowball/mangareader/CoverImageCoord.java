package com.snowball.mangareader;

import android.database.Cursor;

import com.snowball.mangareader.db_code.DbAdapter;

public class CoverImageCoord {
	public int x;
	public int y;
	public int w;
	public int h;
	
	public CoverImageCoord(Cursor c) {
		this.x = c.getInt(c.getColumnIndex(DbAdapter.KEY_COVER_X));
		this.y = c.getInt(c.getColumnIndex(DbAdapter.KEY_COVER_Y));
		this.w = c.getInt(c.getColumnIndex(DbAdapter.KEY_COVER_W));
		this.h = c.getInt(c.getColumnIndex(DbAdapter.KEY_COVER_H));
	}
}
