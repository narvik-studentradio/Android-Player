package com.nsr;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.os.AsyncTask;

public class MetadataTracker implements Closeable {
	private static final String METADATA_URL = "http://nsr-mb.samfunnet.no/xml/metadata";
	private Runnable updateCommand;
	private List<SongData> history;
	private Timer timer;
	private boolean canceled = false;

	public MetadataTracker(Runnable updateCommand) {
		this.updateCommand = updateCommand;
		history = Collections.synchronizedList(new ArrayList<SongData>());
		timer = new Timer();
		fetchMetadata();
	}
	
	public SongData getPlaying() {
		synchronized(history){
			if(history == null || history.size() <= 0)
				return null;
			return history.get(0);
		}
	}
	
	public List<SongData> getHistory() {
		return history;
	}
	
	private void fetchMetadata() {
		MetadataTask mt = new MetadataTask();
		mt.execute();
	}
	
	private void fetchCompleted(boolean result) {
		if(canceled)
			return;
		SongData lastPlayed = getPlaying();
		if(lastPlayed == null)
			return;
		int timeToNext = result ? lastPlayed.remaining * 1000 + 2000 : 10000;
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				fetchMetadata();
			}
		}, timeToNext);
		if(updateCommand != null)
			updateCommand.run();
	}

	@Override
	public void close() {
		canceled = true;
		timer.cancel();
		updateCommand = null;
	}

	private class MetadataTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... arg0) {
			try {
	    		URL url = new URL(METADATA_URL);
				
				HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
				
				if(httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

					InputStream in = httpConnection.getInputStream();
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
					Element docElement = doc.getDocumentElement();
					
					NodeList metadata = docElement.getElementsByTagName("item");
					
					if(metadata.getLength() <= 0)
						return false;
					else
						history.clear();
					
					for(int i=0; i<metadata.getLength(); i++) {
						Element ele = (Element) metadata.item(i);
						
						String eleArtist = ele.getElementsByTagName("artist").item(0).getFirstChild().getNodeValue();
						String eleTitle = ele.getElementsByTagName("title").item(0).getFirstChild().getNodeValue();
						String eleAlbum = ele.getElementsByTagName("album").item(0).getFirstChild().getNodeValue();
						String eleRemaining = ele.getElementsByTagName("remaining").item(0).getFirstChild().getNodeValue();
						String eleDuration = ele.getElementsByTagName("duration").item(0).getFirstChild().getNodeValue();
						String eleType = ele.getElementsByTagName("type").item(0).getFirstChild().getNodeValue();
						
						int remaining;
						try{
							remaining = Integer.parseInt(eleRemaining);
						}catch(NumberFormatException e){remaining = 200;}
						int duration;
						try{
							duration = Integer.parseInt(eleDuration);
						}catch(NumberFormatException e){duration = 200;}
						
						SongData song = new SongData(eleArtist, eleTitle, eleAlbum, duration, remaining, eleType, System.currentTimeMillis());
						history.add(song);
					}
					return true;
				}
				return false;
			} catch (DOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			return false;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			fetchCompleted(result);
		}
	}
}
