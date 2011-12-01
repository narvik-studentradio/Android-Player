package com.nsr;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PodcastItem extends LinearLayout {
	private PodcastData data;
	private TextView textViewTitle;
	private TextView textViewText;

	public PodcastItem(Context context, PodcastData data) {
		super(context);
		this.data = data;
		
		LayoutInflater inf = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inf.inflate(R.layout.podcast_item, this, true);
/*
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li;
		li = (LayoutInflater)getContext().getSystemService(infService);
		li.inflate(R.layout.podcast_stream_item, this, true);
		*/
		textViewTitle = (TextView)findViewById(R.id.textViewTtl);
		textViewText = (TextView)findViewById(R.id.textViewTxt);
		
		textViewTitle.setText(data.date);
		textViewText.setText(data.description);
	}

	public void setTitle(String title) {
		textViewTitle.setText(title);
	}
	
	public void setText(String text) {
		textViewText.setText(text);
	}
	
	public PodcastData getData() {
		return data;
	}
}
