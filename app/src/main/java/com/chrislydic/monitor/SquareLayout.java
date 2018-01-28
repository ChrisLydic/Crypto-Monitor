package com.chrislydic.monitor;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Force recycler view's grid layout to be squares.
 * https://stackoverflow.com/questions/32063565/how-to-make-recyclerviews-grid-item-to-have-equal-height-and-width
 */
public class SquareLayout extends LinearLayout {

	public SquareLayout(Context context) {
		super(context);
	}

	public SquareLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SquareLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int width, int height) {
		super.onMeasure(width, width);
	}

}
