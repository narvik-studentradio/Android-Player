package com.nsr.podcast;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class PodcastStreamAdapter extends BaseAdapter {
	Context context;
	ArrayList<PodcastStreamInfo> streams;

	public PodcastStreamAdapter(Context context, ArrayList<PodcastStreamInfo> streams) {
		super();
		this.context = context;
		this.streams = streams;
	}

	@Override
	public int getCount() {
		return streams.size();
	}

	@Override
	public Object getItem(int arg0) {
		return streams.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PodcastStreamItem item;
		if(convertView == null)
			item = new PodcastStreamItem(context, streams.get(position));
		else {
			item = (PodcastStreamItem) convertView;
			item.setTitle(streams.get(position).text);
			item.setText(streams.get(position).description);
		}
		
		return item;
	}
}
