package com.nsr.podcast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.nsr.R;

public class PodcastStreams extends Activity {
	/** description, text, url. */
	public static final String INTENT_KEY_STREAM_INFO = "com.nsr.podcast.StreamData";
	
	private ArrayList<PodcastStreamInfo> podcasts;
	PodcastStreamTask podTask = null;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.podcast_stream_view);
		
		Object last = getLastNonConfigurationInstance();
		if(last == null) {
			podcasts = new ArrayList<PodcastStreamInfo>();
			podTask = new PodcastStreamTask();
			podTask.execute((Void)null);
		}
		else {
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
		if(podTask != null && podTask.getStatus() != AsyncTask.Status.FINISHED)
		{
			podTask.pd.dismiss();
			podTask.cancel(true);
			return null;
		}
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
							
							podcasts.add(thisStream);
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
		
		@Override
		protected void onPostExecute(String[] result) {
			initList();
			pd.dismiss();
		}
	}
}
