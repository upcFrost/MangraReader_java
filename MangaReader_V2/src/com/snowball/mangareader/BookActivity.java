package com.snowball.mangareader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.snowball.mangareader.db_code.ChapterListAdapter;
import com.snowball.mangareader.db_code.DbAdapter;
import com.snowball.mangareader.interface_code.EllipsizingTextView;
import com.snowball.mangareader.interface_code.ExpandableHeightGridView;

public class BookActivity extends Activity {

	private ImageView mCover;
	private TextView mTitle;
	private TextView mAuthor;
	private EllipsizingTextView mAbout;
	private RatingBar mRating;
	private Button mMoreBtn;
	private ProgressBar mProgressBar;
	private TextView mGenres;
	private TextView mChapterNum;
	private TextView mNoImage;
	public static ExpandableHeightGridView mList;
	public static ChapterListAdapter adapter;
	
	private long id;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.book_screen);
		Bundle extras = getIntent().getExtras();
		id = extras.getLong("id");
		// Load cursors from DB 
		MangaReader.mBookCursor = MangaReader.mDbHelper.fetchMangaById(id);
		MangaReader.mBookCursor.moveToFirst();
		MangaReader.mGenresCursor = MangaReader.mDbHelper.fetchMangaGenres(id);
		MangaReader.mGenresCursor.moveToFirst();
		// Grab GUI
		mCover = (ImageView) findViewById(R.id.book_cover);
		mTitle = (TextView) findViewById(R.id.book_title);
		mAuthor = (TextView) findViewById(R.id.book_author);
		mAbout = (EllipsizingTextView) findViewById(R.id.book_about);
		mRating = (RatingBar) findViewById(R.id.book_rating);
		mMoreBtn = (Button) findViewById(R.id.book_more_btn);
		mList = (ExpandableHeightGridView) findViewById(R.id.book_downloads_list);
		mGenres = (TextView) findViewById(R.id.book_genre);
		mProgressBar = (ProgressBar)findViewById(R.id.book_progress_bar);
		mChapterNum = (TextView) findViewById(R.id.book_count);
		mNoImage = (TextView) findViewById(R.id.book_no_image);
		// Get cover integer values from DB
		CoverImageCoord coords = new CoverImageCoord(MangaReader.mBookCursor);
		// Get main bitmap from assets
		Bitmap cover = Bitmap.createBitmap(MangaReader.mCoverImage, coords.x, 
				coords.y, coords.w, coords.h);
		// Set this bitmap to the imageView
		mCover.setImageBitmap(cover);
		mCover.setVisibility(ImageView.VISIBLE);
		mProgressBar.setVisibility(ProgressBar.GONE);
		
		// Set text values from DB to GUI
		mTitle.setText(MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_TITLE)));
		mAuthor.setText(MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_AUTHOR)));
		mAbout.setText(MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_ABOUT)));
		// Set genres array to GUI
		String genres = MangaReader.mGenresCursor.getString(MangaReader.mGenresCursor.getColumnIndex(DbAdapter.KEY_GENRES_NAME));
		while (MangaReader.mGenresCursor.moveToNext()) {
			genres += ", " + MangaReader.mGenresCursor.getString(MangaReader.mGenresCursor.getColumnIndex(DbAdapter.KEY_GENRES_NAME));
		}
		mGenres.setText(genres);
		String chapterNum = MangaReader.mBookCursor.getInt(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_ONGOING)) == 1 ? "Ongoing" : "Completed";
		chapterNum += " (" + MangaReader.mBookCursor.getInt(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_CHAPTER_COUNT)) + ")";
		mChapterNum.setText(chapterNum);
		// Get chapter list from DB and set it to the gridView
		MangaReader.mChapterCursor = MangaReader.mDbHelper.fetchChapters(MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_BOOK_TABLE)));
		adapter = new ChapterListAdapter(this, MangaReader.mChapterCursor, id, MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_BOOK_TABLE)));
		mList.setAdapter(adapter);
		mList.setExpanded(true);
		// Set about field max line count (when ellipsized)
		mAbout.setMaxLines(3);
		mRating.setRating(MangaReader.mBookCursor.getInt(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_RATING)));
		mMoreBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAbout.isEllipsized()) {
					mAbout.setEllipsize(null);
					mAbout.setMaxLines(Integer.MAX_VALUE);
				} else {
					mAbout.setEllipsize(TextUtils.TruncateAt.END);
					mAbout.setMaxLines(3);
				}
			}
		});
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return this.getParent().onKeyDown(keyCode, event);
	}
	
	private class DownloadCover extends AsyncTask<String, Integer, Bitmap> {
		@Override
		protected Bitmap doInBackground(String... params) {
			try {
				URL url = new URL(params[0]);
            	URLConnection connection = url.openConnection();
            	connection.connect();
            	InputStream input = new BufferedInputStream(url.openStream());
            	Bitmap cover = BitmapFactory.decodeStream(input);
            	input.close();
            	return cover;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result != null) {
				mCover.setVisibility(ImageView.VISIBLE);
				mCover.setImageBitmap(result);
				mProgressBar.setVisibility(ProgressBar.GONE);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				result.compress(CompressFormat.PNG, 0, stream);
				MangaReader.mDbHelper.updateManga(id, null, null, null, null, null, null, -1, -1, -1, stream.toByteArray(), -1, -1);
			} else {
				mNoImage.setVisibility(TextView.VISIBLE);
				mProgressBar.setVisibility(ProgressBar.GONE);
			}
		}
		
	}
}
