package com.nsr.podcast;

import com.nsr.R;
import com.nsr.R.id;
import com.nsr.R.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PodcastStreamItem extends LinearLayout {
	private PodcastStreamInfo stream;
	private TextView textViewTitle;
	private TextView textViewText;

	public PodcastStreamItem(Context context, PodcastStreamInfo data) {
		super(context);

		this.stream = data;
		
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li;
		li = (LayoutInflater)getContext().getSystemService(infService);
		li.inflate(R.layout.podcast_stream_item, this, true);
		
		textViewTitle = (TextView)findViewById(R.id.textViewTitle);
		textViewText = (TextView)findViewById(R.id.textViewText);
		
		textViewTitle.setText(data.text);
		textViewText.setText(data.description);
	}

	public void setText(String text) {
		textViewText.setText(text);
	}
	
	public void setTitle(String title) {
		textViewTitle.setText(title);
	}
	
	public PodcastStreamInfo getStreamData() {
		return stream;
	}
}
