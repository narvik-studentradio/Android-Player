package com.nsr.podcast;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class PodcastAdapter extends BaseAdapter {
	private Context context;
	private List<? extends Displayable> data;

	public PodcastAdapter(Context context, List<? extends Displayable> data) {
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

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PodcastItem<Displayable> item;
		if(convertView == null) {
			item = new PodcastItem<Displayable>(context, data.get(position));
		}
		else {
			item = (PodcastItem<Displayable>) convertView;
			item.setText(data.get(position).getText());
			item.setTitle(data.get(position).getTitle());
		}
		return item;
	}
}
