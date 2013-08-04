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
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.snowball.mangareader.db_code.ChapterListAdapter;
import com.snowball.mangareader.db_code.DbAdapter;
import com.snowball.mangareader.db_code.GenreGridAdapter;
import com.snowball.mangareader.db_code.SearchGridAdapter;
import com.snowball.mangareader.interface_code.EllipsizingTextView;
import com.snowball.mangareader.interface_code.ExpandableHeightGridView;

public class SearchActivity extends Activity {
	
	private Button mAdvancedButton;
	private RelativeLayout mAdvancedLayout;
	private Button mSearchButton;
	private EditText mSearchText;
	public TextView mPopupGenres;
	public TextView mPopupChapterNum;
	public static ExpandableHeightGridView mGrid;
	public static SearchGridAdapter adapter;
	public static ExpandableHeightGridView mPopupList;
	public static ChapterListAdapter adapter_popup;
	 
	
	private TextView mPopupTitle;
	private TextView mPopupNoImage;
	private ImageView mPopupCover;
	private RatingBar mPopupRatingBar;
	private EllipsizingTextView mPopupAbout;
	private TextView mPopupAuthor;
	private Button mPopupMoreInfo;
	private ScrollView mScroll;
	private ProgressBar mPopupProgressBar;
	private ExpandableHeightGridView mAdvancedGrid;
	private RadioButton mAdvancedNamePart;
	private RadioButton mAdvancedNameExact;
	private RadioButton mAdvancedAuthorPart;
	private RadioButton mAdvancedAuthorExact;
	private PopupWindow popupWindow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_search_tab);
		
		// Get interface
		mGrid = (ExpandableHeightGridView)findViewById(R.id.main_search_list);
		mAdvancedLayout = (RelativeLayout) findViewById(R.id.main_search_advanced_outer);
		mAdvancedButton = (Button) findViewById(R.id.main_search_advanced_btn);
		mSearchButton = (Button) findViewById(R.id.main_search_search_btn);
		mSearchText = (EditText) findViewById(R.id.main_search_textfield);
		mAdvancedGrid = (ExpandableHeightGridView) findViewById(R.id.main_search_advanced_genresGrid);
		mAdvancedNamePart = (RadioButton) findViewById(R.id.main_search_advanced_radioNamePart);
		mAdvancedNameExact = (RadioButton) findViewById(R.id.main_search_advanced_radioNameExact);
		mAdvancedAuthorPart = (RadioButton) findViewById(R.id.main_search_advanced_radioAuthorPart);
		mAdvancedAuthorExact = (RadioButton) findViewById(R.id.main_search_advanced_radioAuthorExact);
		// Set expanded to true for GridView
		mGrid.setExpanded(true);
		// Get cursor from database
		MangaReader.mBaseCursor = MangaReader.mDbHelper.fetchSearchBase();
		MangaReader.mBaseCursor.moveToFirst();
		// Create DB adapter
		adapter = new SearchGridAdapter(this, MangaReader.mBaseCursor);
		mGrid.setAdapter(adapter);
		// Set onClick Listener that'll open a popUp if any field is pressed
		mGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				createPopup(id);
			}
		});
		mSearchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAdvancedLayout.getVisibility() == View.GONE) {
					MangaReader.mBaseCursor = MangaReader.mDbHelper.fetchSearchBaseByName(mSearchText.getText().toString());
					adapter.setCursor(MangaReader.mBaseCursor);
					adapter.notifyDataSetChanged();
				} else {
					boolean namePart = mAdvancedNamePart.isChecked() ? true : false;
					boolean authorPart = mAdvancedAuthorPart.isChecked() ? true : false;
				}
			}
		});
		mAdvancedButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAdvancedLayout.getVisibility() == View.GONE) {
					mAdvancedLayout.setVisibility(View.VISIBLE);
					mAdvancedButton.setBackgroundDrawable(
							getResources().getDrawable(R.drawable.button_advanced_background_pressed));
					mAdvancedButton.setTextColor(Color.parseColor("#4abcde"));
					
				} else {
					mAdvancedLayout.setVisibility(View.GONE);
					mAdvancedButton.setBackgroundDrawable(
							getResources().getDrawable(R.drawable.button_advanced_background_normal));
					mAdvancedButton.setTextColor(getResources().getColorStateList(R.drawable.button_text_selector));
				}
			}
		});
		MangaReader.mGenresCursor = MangaReader.mDbHelper.fetchGenres();
		GenreGridAdapter mGenreAdapter = new GenreGridAdapter(this, MangaReader.mGenresCursor);
		mAdvancedGrid.setAdapter(mGenreAdapter);
		mAdvancedGrid.setExpanded(true);
		mAdvancedGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				((RadioButton) parent.findViewWithTag(position)).setChecked(true);
				for (int i = 0; i < parent.getChildCount(); i++) {
					if (i != position) {
						((RadioButton) parent.findViewWithTag(i)).setChecked(false);
					}
				}
			}
		});
		// Recreate popup on orientation change (WARNING, 100ms IS JUST A GUESS)
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (StaticValues.search_popup_id != -1) {
					createPopup(StaticValues.search_popup_id);
				}
			}
		}, 100);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return this.getParent().onKeyDown(keyCode, event);
	}
	
	private class DownloadCover extends AsyncTask<String, Integer, Bitmap> {
		
		long id;
		
		public DownloadCover(long id) {
			this.id = id;
		}
		
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
				mPopupCover.setImageBitmap(result);
				mPopupCover.setVisibility(ImageView.VISIBLE);
				mPopupProgressBar.setVisibility(ProgressBar.GONE);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				result.compress(CompressFormat.PNG, 0, stream);
				MangaReader.mDbHelper.updateManga(id, null, null, null, null, null, null, -1, -1, -1, stream.toByteArray(), -1, -1);
			} else {
				mPopupNoImage.setVisibility(TextView.VISIBLE);
				mPopupProgressBar.setVisibility(ProgressBar.GONE);
			}
		}	
	}
	
	private void createPopup(long id) {
		// Set restore flags
		StaticValues.search_popup_id = id;
		// Grab GUI
		LayoutInflater popupInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View popupView = popupInflater.inflate(R.layout.main_search_popup, null);
		mPopupTitle = (TextView) popupView.findViewById(R.id.main_search_popup_title);
		mPopupCover = (ImageView) popupView.findViewById(R.id.main_search_popup_book_cover);
		mPopupRatingBar = (RatingBar) popupView.findViewById(R.id.main_search_popup_rating);
		mPopupList = (ExpandableHeightGridView) popupView.findViewById(R.id.main_search_popup_downloads_list);
		mPopupAbout = (EllipsizingTextView) popupView.findViewById(R.id.main_search_popup_about);
		mPopupAuthor = (TextView) popupView.findViewById(R.id.main_search_popup_author);
		mPopupMoreInfo = (Button) popupView.findViewById(R.id.main_search_popup_more_btn);
		mPopupProgressBar = (ProgressBar) popupView.findViewById(R.id.main_search_popup_progress_bar);
		mScroll = (ScrollView)popupView.findViewById(R.id.main_search_popup_shadow);
		mPopupGenres = (TextView)popupView.findViewById(R.id.main_search_popup_genre);
		mPopupChapterNum = (TextView)popupView.findViewById(R.id.main_search_popup_count);
		mPopupNoImage = (TextView) popupView.findViewById(R.id.main_search_popup_no_image);
		// Grab DB adapters for book data and genre data
		MangaReader.mBookCursor = MangaReader.mDbHelper.fetchMangaById(id);
		MangaReader.mBookCursor.moveToFirst();
		MangaReader.mGenresCursor = MangaReader.mDbHelper.fetchMangaGenres(id);
		MangaReader.mGenresCursor.moveToFirst();
		// Set GUI text fields
		mPopupTitle.setText(MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_TITLE)));
		mPopupAuthor.setText(MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_AUTHOR)));
		mPopupRatingBar.setRating(MangaReader.mBookCursor.getFloat(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_TITLE)));
		String chapterNum = MangaReader.mBookCursor.getInt(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_ONGOING)) == 1 ? "Ongoing" : "Completed";
		chapterNum += " (" + MangaReader.mBookCursor.getInt(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_CHAPTER_COUNT)) + ")";
		mPopupChapterNum.setText(chapterNum);
		// Get cover integer values from DB
		CoverImageCoord coords = new CoverImageCoord(MangaReader.mBookCursor);
		// Get main bitmap from assets
		Bitmap cover = Bitmap.createBitmap(MangaReader.mCoverImage, coords.x,
				coords.y, coords.w, coords.h);
		mPopupProgressBar.setVisibility(ProgressBar.GONE);
		mPopupCover.setVisibility(ImageView.VISIBLE);
		mPopupCover.setImageBitmap(cover);
		// Set genres string
		String genres = "";
		try {
			genres = MangaReader.mGenresCursor.getString(MangaReader.mGenresCursor.getColumnIndex(DbAdapter.KEY_GENRES_NAME));
			while (MangaReader.mGenresCursor.moveToNext()) {
				genres += ", " + MangaReader.mGenresCursor.getString(MangaReader.mGenresCursor.getColumnIndex(DbAdapter.KEY_GENRES_NAME));
			}
		} catch (Exception e) {}
		mPopupGenres.setText(genres);
		// Get chapters from DB
		MangaReader.mChapterCursor = MangaReader.mDbHelper.fetchChapters(MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_BOOK_TABLE)));
		adapter_popup = new ChapterListAdapter(popupView.getContext(), MangaReader.mChapterCursor, id, MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_BOOK_TABLE)));
		mPopupList.setAdapter(adapter_popup);
		mPopupList.setExpanded(true);
		mPopupAbout.setMaxLines(3); // Ellipsizing doesn't work other way, screw the xml O_o
		mPopupAbout.setText(MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_ABOUT)));
		mPopupMoreInfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPopupAbout.isEllipsized()) {
					mPopupAbout.setEllipsize(null);
					mPopupAbout.setMaxLines(Integer.MAX_VALUE);
				} else {
					mPopupAbout.setEllipsize(TextUtils.TruncateAt.END);
					mPopupAbout.setMaxLines(3);
				}
			}
		});
		// Make window
		popupWindow = new PopupWindow(popupView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT,true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				StaticValues.search_popup_id = -1;
			}
		});
		popupWindow.setOutsideTouchable(true);
		popupWindow.showAtLocation(popupView, Gravity.LEFT | Gravity.TOP, 0, 0);
		// ScrollView to top (some strange bug)
		mScroll.post(new Runnable() {
			@Override
			public void run() {
				mScroll.scrollTo(0, 0);
			}
		});
	}
	
}
