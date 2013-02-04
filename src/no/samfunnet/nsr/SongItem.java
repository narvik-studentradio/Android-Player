package no.samfunnet.nsr;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SongItem extends LinearLayout {
	SongData song;
	TextView txtArtist;
	TextView txtTitle;
	TextView txtAlbum;

	public SongItem(Context context, SongData song) {
		super(context);
		this.song = song;
		inflate(context, R.layout.song_item, this);
		
		txtArtist = (TextView)findViewById(R.id.songTxtArtist);
		txtTitle = (TextView)findViewById(R.id.songTxtTitle);
		txtAlbum = (TextView)findViewById(R.id.songTxtAlbum);
		txtArtist.setText(song.artist);
		txtTitle.setText(song.title);
		txtAlbum.setText(song.album);
	}
	
	public void setData(String artist, String title, String album) {
		txtArtist.setText(artist);
		txtTitle.setText(title);
		txtAlbum.setText(album);
	}
}