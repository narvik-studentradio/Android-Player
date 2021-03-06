package no.samfunnet.nsr;

public class SongData {
	public String artist;
	public String title;
	public String album;
	public int duration;
	public int remaining;
	public String type;
	public long timestamp;
	
	public SongData(String artist, String title, String album, int duration,
			int remaining, String type, long timestamp) {
		super();
		this.artist = artist;
		this.title = title;
		this.album = album;
		this.duration = duration;
		this.remaining = remaining;
		this.type = type;
		this.timestamp = timestamp;
	}
}
