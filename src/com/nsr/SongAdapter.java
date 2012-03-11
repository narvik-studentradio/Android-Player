package com.nsr;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class SongAdapter extends BaseAdapter {
	Context context;
	List<SongData> songs;

	public SongAdapter(Context context, List<SongData> songs) {
		super();
		this.context = context;
		this.songs = songs;
	}

	@Override
	public int getCount() {
		return songs.size();
	}

	@Override
	public Object getItem(int arg0) {
		return songs.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SongItem item;
		if(convertView == null) {
			item = new SongItem(context, songs.get(position));
		}
		else {
			item = (SongItem)convertView;
			SongData song = songs.get(position);
			item.setData(song.artist, song.title, song.album);
		}
		return item;
	}
}
