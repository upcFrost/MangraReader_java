package com.snowball.mangareader.interface_code;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class TextProgressBar extends ProgressBar {
	private String text;
	private Paint textPaint;

	public TextProgressBar(Context context) {
		super(context);
		text = "HP";
		textPaint = new Paint();
		textPaint.setColor(Color.BLACK);
	}

	public TextProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		text = "HP";
		textPaint = new Paint();
		textPaint.setColor(Color.BLACK);
	}

	public TextProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		text = "HP";
		textPaint = new Paint();
		textPaint.setColor(Color.BLACK);
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		// First draw the regular progress bar, then custom draw our text
		super.onDraw(canvas);
		Rect bounds = new Rect();
		textPaint.getTextBounds(text, 0, text.length(), bounds);
		int x = getWidth() / 2 - bounds.centerX();
		int y = getHeight() / 2 - bounds.centerY();
		canvas.drawText(text, x, y, textPaint);
	}

	public synchronized void setText(String text) {
		this.text = text;
		drawableStateChanged();
	}
	
	public void setTextStyle(String familyName, boolean bold, boolean italic) {
		int style;
		if (bold && !italic) {
			style = Typeface.BOLD;
		} else if (bold && italic) {
			style = Typeface.BOLD_ITALIC;
		} else if (!bold && italic){
			style = Typeface.ITALIC;
		} else {
			style = Typeface.NORMAL;
		}
		Typeface tf = Typeface.create(familyName, style);
		textPaint.setTypeface(tf);
		drawableStateChanged();
	}
	
	public void setTextSize(float textSize) {
		textPaint.setTextSize(textSize);
		drawableStateChanged();
	}

	public void setTextColor(int color) {
		textPaint.setColor(color);
		drawableStateChanged();
	}
}