package com.nsr.podcast;

class PodcastData implements Displayable {
	public String url;
	public String description;
	public String date;
	
	@Override
	public String getTitle() {
		return date;
	}
	
	@Override
	public String getText() {
		return description;
	}
}
