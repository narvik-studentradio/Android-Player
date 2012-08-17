package com.nsr.podcast;

class PodcastStreamInfo implements Displayable {
	public String description;
	public String url;
	public String text;
	
	@Override
	public String getTitle() {
		return text;
	}
	
	@Override
	public String getText() {
		return description;
	}
}
