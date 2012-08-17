package com.nsr.podcast;

class PodcastData implements Displayable {
	public String url;
	public String description;
	public String title;
	
	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public String getText() {
		return description;
	}
}
