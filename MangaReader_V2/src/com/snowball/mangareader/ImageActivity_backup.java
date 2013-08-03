package com.snowball.mangareader;

import java.io.File;

import com.polites.android.GestureImageView;
import com.snowball.mangareader.db_code.DbAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.FeatureInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Environment;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

//public class ImageActivity extends Activity implements OnTouchListener {
public class ImageActivity_backup extends Activity {
	private GestureImageView picture;
	private ImageButton leftButton;
	private ImageButton rightButton;
	private ImageButton gotoButton;
	
	private String bookTable;
	private String dirName;
	private long mangaId;
	private long chapterId;
	private int pictureViewHeight;
	private int pictureViewWidth;
	private int currentPage;
	private float imageHeight;
	private float imageWidth;
	private float scale;

	// These matrices will be used to move and zoom image
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	private static final String TAG = "Touch";
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

		Bundle extras = getIntent().getExtras();
		mangaId = extras.getLong("genreId");
		chapterId = extras.getLong("chapterId");
		
		MangaReader.mBookCursor = MangaReader.mDbHelper.fetchMangaById(mangaId);
		MangaReader.mBookCursor.moveToFirst();
		bookTable = MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_BOOK_TABLE));

		MangaReader.mChapterCursor = MangaReader.mDbHelper.fetchChapterById(
				bookTable, chapterId);
		MangaReader.mChapterCursor.moveToFirst();
		dirName = MangaReader.mBookCursor.getString(MangaReader.mBookCursor.getColumnIndex(DbAdapter.KEY_LOCAL_PATH)) + "/" + MangaReader.mChapterCursor.getInt(MangaReader.mChapterCursor.getColumnIndex(DbAdapter.KEY_BOOK_NUM)) + "/";
		
		currentPage = MangaReader.mChapterCursor.getInt(MangaReader.mChapterCursor.getColumnIndex(DbAdapter.KEY_BOOK_LAST_READ_PAGE));
		//pageSet(currentPage, 0, true);
		scale = 1;
		
		pageSet(currentPage);
		
		//picture.setOnTouchListener(this);
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
		gotoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(ImageActivity_backup.this);

				alert.setTitle("Go to page");

				// Set an EditText view to get user input 
				final EditText input = new EditText(ImageActivity_backup.this);
				input.setPadding(5, 0, 5, 0);
				alert.setView(input);

				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

				  // Do something with value!
				  }
				});

				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
				    // Canceled.
				  }
				});

				alert.show();
			}
		});
	}
	
	/** Set new image **/
	private boolean pageSet(int image) {		
		File imgFile = new File(dirName+"/"+ image +".jpg");
		if (imgFile.exists()) {
			Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
			picture.setImageBitmap(bitmap);
			return true;
		}
		return false;
	}
	
//	@Override
//	public void onWindowFocusChanged(boolean hasFocus) {
//		float[] values = new float[9];
//		super.onWindowFocusChanged(hasFocus);
//		// Checking the first image if it fits the screen
//		pictureViewWidth = picture.getWidth();
//  	  	pictureViewHeight = picture.getHeight();
//		matrix.getValues(values);
//		values = checkImagePosition(values);
//		matrix.setValues(values);
//		picture.setImageMatrix(matrix);
//	}
//
//	@Override
//	protected void onDestroy() {
//		super.onDestroy();
//	}
//
//	public boolean onTouch(View v, MotionEvent event) {
//		float[] values;		
//		ImageView view = (ImageView) v;
//		
//	      // Dump touch event to log (debug only)
//	      //dumpEvent(event);
//
//	      // Handle touch events here...
//	      switch (event.getAction() & MotionEvent.ACTION_MASK) {
//	      case MotionEvent.ACTION_DOWN:
//	         savedMatrix.set(matrix);
//	         start.set(event.getX(), event.getY());
//	         Log.d(TAG, "mode=DRAG");
//	         mode = DRAG;
//	         break;
//	         
//	      case MotionEvent.ACTION_POINTER_DOWN:
//	         oldDist = spacing(event);
//	         Log.d(TAG, "oldDist=" + oldDist);
//	         if (oldDist > 10f) {
//	            savedMatrix.set(matrix);
//	            midPoint(mid, event);
//	            mode = ZOOM;
//	            Log.d(TAG, "mode=ZOOM");
//	         }
//	         break;
//	         
//	      case MotionEvent.ACTION_UP:
//	    	  /** Return/change image when not pressed **/
//	    	  pictureViewWidth = view.getWidth();
//	    	  pictureViewHeight = view.getHeight();
//	    	  values = new float[9];
//		      matrix.getValues(values);
//		      checkImageChange(values);
//		      values = checkImagePosition(values);
//		      matrix.setValues(values);
//		      break;
//		      
//	      case MotionEvent.ACTION_POINTER_UP:
//	         mode = NONE;
//	         Log.d(TAG, "mode=NONE");
//	         break;
//	         
//	      case MotionEvent.ACTION_MOVE:
//	         if (mode == DRAG) {
//	            matrix.set(savedMatrix);
//	            matrix.postTranslate(event.getX() - start.x,
//	                  event.getY() - start.y);
//	         }
//	         else if (mode == ZOOM) {
//	            float newDist = spacing(event);
//	            Log.d(TAG, "newDist=" + newDist);
//	            if (newDist > 10f) {
//	               matrix.set(savedMatrix);
//	               float addScale = newDist / oldDist;
//	               scale *= addScale;
//	               matrix.postScale(addScale, addScale, mid.x, mid.y);
//	            }
//	         }
//	         break;
//	      }
//
//	      view.setImageMatrix(matrix);
//	      return true; // indicate event was handled
//	   }
//
//
//	/** Show an event in the LogCat view, for debugging */
//	private void dumpEvent(MotionEvent event) {
//	   String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
//	      "POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
//	   StringBuilder sb = new StringBuilder();
//	   int action = event.getAction();
//	   int actionCode = action & MotionEvent.ACTION_MASK;
//	   sb.append("event ACTION_" ).append(names[actionCode]);
//	   if (actionCode == MotionEvent.ACTION_POINTER_DOWN
//	         || actionCode == MotionEvent.ACTION_POINTER_UP) {
//	      sb.append("(pid " ).append(
//	      action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
//	      sb.append(")" );
//	   }
//	   sb.append("[" );
//	   for (int i = 0; i < event.getPointerCount(); i++) {
//	      sb.append("#" ).append(i);
//	      sb.append("(pid " ).append(event.getPointerId(i));
//	      sb.append(")=" ).append((int) event.getX(i));
//	      sb.append("," ).append((int) event.getY(i));
//	      if (i + 1 < event.getPointerCount())
//	         sb.append(";" );
//	   }
//	   sb.append("]" );
//	   Log.d(TAG, sb.toString());
//	}
//	
//	/** Determine the space between the first two fingers */
//	private float spacing(MotionEvent event) {
//	   float x = event.getX(0) - event.getX(1);
//	   float y = event.getY(0) - event.getY(1);
//	   return FloatMath.sqrt(x * x + y * y);
//	}
//
//	/** Calculate the mid point of the first two fingers */
//	private void midPoint(PointF point, MotionEvent event) {
//	   float x = event.getX(0) + event.getX(1);
//	   float y = event.getY(0) + event.getY(1);
//	   point.set(x / 2, y / 2);
//	}
//
//	/** Set new image **/
//	private boolean pageSet(int image, float currentX, boolean newFromRight) {		
//		File imgFile = new File(dirName+"/"+ image +".jpg");
//		if (imgFile.exists()) {
//			Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//			imageHeight = bitmap.getHeight() * scale;
//			imageWidth = bitmap.getWidth() * scale;
//			TranslateAnimation slide;
//			if (newFromRight) {
//				slide = new TranslateAnimation(imageWidth, 0, 0, 0);
//			} else {
//				slide = new TranslateAnimation(-imageWidth - currentX, 0, 0, 0);
//			}
//			slide.setDuration(500);
//			picture.startAnimation(slide);
//			picture.setImageBitmap(bitmap);
//			return true;
//		}
//		return false;
//	}
//	
//	
//	/** Change image when moved by more then 1/2 screen out **/
//	private void checkImageChange(float[] values) {
//		if (values[2] < (pictureViewWidth/2 - imageWidth)) {
//			if (pageSet(currentPage+1, values[2], true)) {
//				values[2] = 0;
//				currentPage++;
//			}
//		} else if (values[2] > pictureViewWidth/2) {
//			if (pageSet(currentPage-1, values[2], false)) {
//				currentPage--;
//			}
//		}
//	}
//	
//	/** Return image to screen if out **/
//	private float[] checkImagePosition(float[] values) {
//		if (imageWidth > pictureViewWidth) {
//			if (values[2] > 0) {
//				values[2] = 0;
//			} else if (values[2] < (pictureViewWidth - imageWidth)) {
//				values[2] = pictureViewWidth - imageWidth;
//			}
//		} else {
//			values[2] = (pictureViewWidth - imageWidth)/2;
//		}
//		
//		if (imageHeight > pictureViewHeight) {
//			if (values[5] > 0) {
//				values[5] = 0;
//			} else if (values[5] < (pictureViewHeight - imageHeight)) {
//				values[5] = pictureViewHeight - imageHeight;
//			} 
//		} else {
//			values[5] = (pictureViewHeight - imageHeight)/2;
//		}
//
//		return values;
//	}
}
