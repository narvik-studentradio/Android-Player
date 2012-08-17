package com.nsr.podcast;

import android.content.Context;
import android.text.Spannable;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nsr.R;

class PodcastItem<T extends Displayable> extends LinearLayout {
	private T data;
	private TextView textViewTitle;
	private TextView textViewText;

	public PodcastItem(Context context, T data) {
		super(context);
		this.data = data;
		
		LayoutInflater inf = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inf.inflate(R.layout.podcast_item, this, true);
		
		textViewTitle = (TextView)findViewById(R.id.podcastItem_title);
		textViewText = (TextView)findViewById(R.id.podcastItem_text);
		
		textViewTitle.setText(data.getTitle());
		textViewText.setText(data.getText());
		
//		DisplayMetrics dm = getResources().getDisplayMetrics();
//		TranslateAnimation ta = new TranslateAnimation(0.0f, -1 * dm.widthPixels, 0.0f, 0.0f);
//		ta.setDuration(10000);
//		ta.setRepeatCount(Animation.INFINITE);
//		textViewText.startAnimation(ta);
	}

	public void setTitle(String title) {
		textViewTitle.setText(title);
	}
	
	public void setText(String text) {
		textViewText.setText(text);
	}
	
	public T getData() {
		return data;
	}
}