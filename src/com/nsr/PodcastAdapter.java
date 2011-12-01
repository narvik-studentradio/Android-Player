package com.nsr;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class PodcastAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<PodcastData> data;

	public PodcastAdapter(Context context, ArrayList<PodcastData> data) {
		super();
		this.context = context;
		this.data = data;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PodcastItem item;
		if(convertView == null) {
			item = new PodcastItem(context, data.get(position));
		}
		else {
			item = (PodcastItem) convertView;
			item.setText(data.get(position).description);
			item.setTitle(data.get(position).date);
		}
		return item;
	}
}
