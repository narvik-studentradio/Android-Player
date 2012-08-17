package com.nsr;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.TextView;

public class ScrollingTextView extends TextView {

	public ScrollingTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setStuff();
	}

	public ScrollingTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setStuff();
	}

	public ScrollingTextView(Context context) {
		super(context);
		setStuff();
	}
	
	private void setStuff() {
		setSingleLine(true);
		setEllipsize(TruncateAt.MARQUEE);
		setMarqueeRepeatLimit(-1);
		setHorizontallyScrolling(true);
	}
	
	@Override
	protected void onFocusChanged(boolean focused, int direction,
			Rect previouslyFocusedRect) {
		if(focused)
			super.onFocusChanged(focused, direction, previouslyFocusedRect);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if(hasWindowFocus)
			super.onWindowFocusChanged(hasWindowFocus);
	}
	
	@Override
	public boolean isFocused() {
		return true;
	}
}
