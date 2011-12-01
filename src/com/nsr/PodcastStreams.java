package com.nsr;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class PodcastStreams extends Activity {
	/** description, text, url. */
	public static final String INTENT_KEY_STREAM_INFO = "com.nsr.podcast.StreamData";
	
	private ArrayList<PodcastStreamInfo> podcasts;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.podcast_stream_view);
		
		Object last = getLastNonConfigurationInstance();
		if(last == null) {
			podcasts = new ArrayList<PodcastStreamInfo>();/*
			PodcastData testData = new PodcastData();
			testData.description = "Testdata";
			PodcastStreamInfo testStream = new PodcastStreamInfo();
			testStream.podcasts.add(testData);
			podcasts.add(testStream);*/
			
			PodcastStreamTask podTask = new PodcastStreamTask();
			podTask.execute((Void)null);
		}
		else {
			Toast.makeText(this, "Saved data", Toast.LENGTH_SHORT).show();
			podcasts = (ArrayList<PodcastStreamInfo>) last;
			initList();
		}
	}
	
	private void initList() {
			ListView list = (ListView)PodcastStreams.this.findViewById(R.id.listViewPodcasts);
			PodcastStreamAdapter adapter = new PodcastStreamAdapter(PodcastStreams.this, podcasts);
			list.setAdapter(adapter);
			
			list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					PodcastStreamInfo stream = ((PodcastStreamItem)view).getStreamData();
					Intent intent = new Intent(PodcastStreams.this, Podcasts.class);
					String[] streamInfo = {stream.description, stream.text, stream.url};
					intent.putExtra(INTENT_KEY_STREAM_INFO, streamInfo);
					startActivity(intent);
				}
			});
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return podcasts;
	}
	
	private class PodcastStreamTask extends AsyncTask<Void, String, String[]> {
		ProgressDialog pd;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = ProgressDialog.show(PodcastStreams.this, "Please wait", "Downloading podcast lists", true, false);
		}

		@Override
		protected void onProgressUpdate(String... values) {
			pd.setMessage(values[0]);
		}

		@Override
		protected String[] doInBackground(Void... arg0) {
			try {
	    		URL url = new URL("http://nsr.samfunnet.no/podcasts.opml");
	    		
				HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
				
				if(httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					
					InputStream in = httpConnection.getInputStream();
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
					Element docElement = doc.getDocumentElement();
					
					publishProgress("Parsing data");
					
					Element body = (Element)docElement.getElementsByTagName("body").item(0);
					NodeList outlines = body.getElementsByTagName("outline");
					for(int i=0; i<1; i++) {//outlines.getLength(); i++) {
						Element outline = (Element) outlines.item(i);
						
						//String title = outline.getAttribute("title");
						
						NodeList subOutlines = outline.getElementsByTagName("outline");
						for(int j=1; j<subOutlines.getLength(); j++) {
							Element subOutline = (Element) subOutlines.item(j);
							
							String subText = subOutline.getAttribute("text");
							String subDesc = subOutline.getAttribute("description");
							String subUrl = subOutline.getAttribute("xmlUrl");
							
							PodcastStreamInfo thisStream = new PodcastStreamInfo();
							thisStream.text = subText;
							thisStream.description = subDesc;
							thisStream.url = subUrl;
							
							//getPodcasts(subUrl, thisStream);
							
							podcasts.add(thisStream);
							
							int k=0;
							k++;
						}
						int j=0;
						j++;
					}
					return new String[]{};
				}
			} catch (DOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			return new String[]{""};
			
			
		}
		/*
		private void getPodcasts(String url, PodcastStreamInfo stream) throws IOException, SAXException, ParserConfigurationException {
			URL url_ = new URL(url);
    		
			HttpURLConnection httpConnection = (HttpURLConnection)url_.openConnection();
			
			if(httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				
				InputStream in = httpConnection.getInputStream();
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
				Element docElement = doc.getDocumentElement();
				
				Element body = (Element)docElement.getElementsByTagName("channel").item(0);
				NodeList outlines = docElement.getElementsByTagName("item");
				
				for(int i=0; i<outlines.getLength(); i++) {
					Element ele = (Element) outlines.item(i);
					
					String eleTitle = ele.getElementsByTagName("title").item(0).getFirstChild().getNodeValue();
					String eleUrl = ele.getElementsByTagName("link").item(0).getFirstChild().getNodeValue();
					String eleDate = ele.getElementsByTagName("pubDate").item(0).getFirstChild().getNodeValue();
					
					PodcastData thisCast = new PodcastData();
					
					thisCast.description = eleTitle;
					thisCast.date = eleDate;
					thisCast.url = eleUrl;
					
					stream.podcasts.add(thisCast);
				}
			}
		}*/
		
		@Override
		protected void onPostExecute(String[] result) {
			initList();
			pd.dismiss();
		}
	}
}
