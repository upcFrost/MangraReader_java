package com.snowball.mangareader;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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
	private GestureImageView picture;
	private ImageButton leftButton;
	private ImageButton rightButton;
	private ImageButton gotoButton;
	private Button pageNumButton;
	
	private String bookTable;
	private String dirName;
	private long mangaId;
	private long chapterId;
	private int currentPage;
	private int totalPageNum;
	// These matrices will be used to move and zoom image
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// For zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.picview);

		picture = (GestureImageView) findViewById(R.id.picview_picture);
		leftButton = (ImageButton) findViewById(R.id.picview_left_arrow);
		rightButton = (ImageButton) findViewById(R.id.picview_right_arrow);
		gotoButton = (ImageButton) findViewById(R.id.picview_goto);
		pageNumButton = (Button) findViewById(R.id.picview_pic_num_button);

		Bundle extras = getIntent().getExtras();
		mangaId = extras.getLong("mangaId");
		chapterId = extras.getLong("chapterId");
		
		MangaReader.mBookCursor = MangaReader.mDbHelper.fetchMangaById(mangaId);
		MangaReader.mBookCursor.moveToFirst();
		bookTable = MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_BOOK_TABLE));

		MangaReader.mChapterCursor = MangaReader.mDbHelper.fetchChapterById(
				bookTable, chapterId);
		MangaReader.mChapterCursor.moveToFirst();
		dirName = MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_LOCAL_PATH)) + "/" + MangaReader.mChapterCursor.getInt(MangaReader.mChapterCursor.getColumnIndex(DbAdapter.KEY_BOOK_NUM)) + "/";
		
		totalPageNum = MangaReader.mChapterCursor.getInt(MangaReader.mChapterCursor.getColumnIndex(DbAdapter.KEY_BOOK_PAGENUM));
		currentPage = MangaReader.mChapterCursor.getInt(MangaReader.mChapterCursor.getColumnIndex(DbAdapter.KEY_BOOK_LAST_READ_PAGE));
		pageSet(currentPage);
		
		pageNumButton.bringToFront();
		
		leftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (pageSet(currentPage-1)) {
					currentPage--;
				}	
			}
		});
		rightButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (pageSet(currentPage+1)) {
					currentPage++;
				}	
			}
		});
		gotoButton.setOnClickListener(gotoPageListener);
		pageNumButton.setOnClickListener(gotoPageListener);
	}
	
	/** Set new image **/
	private boolean pageSet(int pageNum) {		
		File imgFile = new File(dirName+"/"+ pageNum +".jpg");
		if (imgFile.exists()) {
			Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
			picture.setImageBitmap(bitmap);
			pageNumButton.setText("Page " + pageNum + " of " + totalPageNum);
			return true;
		}
		return false;
	}
	
	
	Button.OnClickListener gotoPageListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			final AlertDialog alert = new AlertDialog.Builder(ImageActivity.this).create();

			alert.setTitle("Go to page (1 - " +totalPageNum+ ")");

			// Set an EditText view to get user input 
			final EditText input = new EditText(ImageActivity.this);
			input.setText(String.valueOf(currentPage));
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			alert.setView(input,10,0,10,0);

			alert.setButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				try {
					int newPage = Integer.decode(input.getText().toString());
					if (newPage <= totalPageNum && newPage > 0) {
						pageSet(newPage);
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
