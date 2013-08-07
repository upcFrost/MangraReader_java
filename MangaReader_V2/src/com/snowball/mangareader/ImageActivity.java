package com.snowball.mangareader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.polites.android.GestureImageView;
import com.snowball.mangareader.db_code.DbAdapter;

//public class ImageActivity extends Activity implements OnTouchListener {
public class ImageActivity extends Activity {
	// GUI
	private GestureImageView picture;
	private ImageButton leftButton;
	private ImageButton rightButton;
	private ImageButton gotoButton;
	private Button pageNumButton;
	// Vars
	private String bookTable;
	private String dirName;
	private long mangaId;
	private long chapterId;
	private int currentPage;
	private int totalPageNum; 
	// Array list for page data
	private ArrayList<Page> pageArray;
	// For zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;
	// Main picture
	private static Bitmap mainPicture;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.picview);
		// Grab GUI
		picture = (GestureImageView) findViewById(R.id.picview_picture);
		leftButton = (ImageButton) findViewById(R.id.picview_left_arrow);
		rightButton = (ImageButton) findViewById(R.id.picview_right_arrow);
		gotoButton = (ImageButton) findViewById(R.id.picview_goto);
		pageNumButton = (Button) findViewById(R.id.picview_pic_num_button);
		// Get manga and chapter IDs from intent 
		Bundle extras = getIntent().getExtras();
		mangaId = extras.getLong("mangaId");
		chapterId = extras.getLong("chapterId");
		// Get book table from DB using manga ID
		MangaReader.mBookCursor = MangaReader.mDbHelper.fetchMangaById(mangaId);
		MangaReader.mBookCursor.moveToFirst();
		bookTable = MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_BOOK_TABLE));
		// Now fetch chapter from DB using book table name and chapter ID
		MangaReader.mChapterCursor = MangaReader.mDbHelper.fetchChapterById(bookTable, chapterId);
		MangaReader.mChapterCursor.moveToFirst();
		dirName = MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_LOCAL_PATH)) + "/" + MangaReader.mChapterCursor.getInt(MangaReader.mChapterCursor.getColumnIndex(DbAdapter.KEY_BOOK_NUM)) + "/";
		// Set main picture
		// TODO Rename main image file
		mainPicture = BitmapFactory.decodeFile(new File(dirName+"/1.jpg").getAbsolutePath());
		// Initiate XML parser
		String bookName = MangaReader.mChapterCursor.getString(MangaReader.mChapterCursor.getColumnIndex(DbAdapter.KEY_BOOK_TITLE));
		pageArray = new ArrayList<Page>();
		try {
			BookXmlParser parser = new BookXmlParser();
			pageArray = parser.getPageArray(new File("text.xml"), bookName);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Get total number of pages and last read page
		totalPageNum = MangaReader.mChapterCursor.getInt(MangaReader.mChapterCursor.getColumnIndex(DbAdapter.KEY_BOOK_PAGENUM));
//		currentPage = MangaReader.mChapterCursor.getInt(MangaReader.mChapterCursor.getColumnIndex(DbAdapter.KEY_BOOK_LAST_READ_PAGE));
		currentPage = 1;
		pageSet(pageArray.get(currentPage));
		// Bring page number button to front (or it'll stay under the imageview)
		pageNumButton.bringToFront();
		
		leftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (pageSet(pageArray.get(currentPage-1))) {
					currentPage--;
				}	
			}
		});
		rightButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (pageSet(pageArray.get(currentPage+1))) {
					currentPage++;
				}	
			}
		});
		gotoButton.setOnClickListener(gotoPageListener);
		pageNumButton.setOnClickListener(gotoPageListener);
	}
	
	/** Set new image **/
	private boolean pageSet(Page page) {
		// Set image from main big picture subset
		picture.setImageBitmap(Bitmap.createBitmap(mainPicture, page.offset_x, page.offset_y, page.width, page.height));
		// Set info text with page number
		pageNumButton.setText("Page " + page.id + " of " + totalPageNum);
		// Clean memory from previous bitmap
		System.gc();
		return true;
		// TODO Check and clean
//		File imgFile = new File(dirName+"/"+ pageNum +".jpg");
//		if (imgFile.exists()) {
//			Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//			picture.setImageBitmap(bitmap);
//			pageNumButton.setText("Page " + pageNum + " of " + totalPageNum);
//			return true;
//		}
//		return false;
	}
	
	
	@Override
	protected void onDestroy() {
		// Clean main image
		mainPicture.recycle();
		System.gc();
		super.onDestroy();
	}


	Button.OnClickListener gotoPageListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			final AlertDialog alert = new AlertDialog.Builder(ImageActivity.this).create();
			// Set title with maximum page number specified
			alert.setTitle("Go to page (1 - " +totalPageNum+ ")");
			// Set an EditText view to get user input 
			final EditText input = new EditText(ImageActivity.this);
			input.setText(String.valueOf(currentPage));
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			alert.setView(input,10,0,10,0);
			// Create OK and Cancel buttons
			alert.setButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				try {
					int newPage = Integer.decode(input.getText().toString());
					if (newPage <= totalPageNum && newPage > 0) {
						pageSet(pageArray.get(newPage));
						currentPage = newPage;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			  }
			});

			alert.setButton2("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
				  
			  }
			});

			alert.show();
		}
	}; 
}
