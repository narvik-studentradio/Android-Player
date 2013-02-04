package no.samfunnet.nsr.podcast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import no.samfunnet.nsr.R;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class PodcastStreams extends Activity {
	/** description, text, url. */
	public static final String INTENT_KEY_STREAM_INFO = "no.samfunnet.nsr.podcast.StreamData";
	public static final int DIALOG_ERROR = 0;
	
	private ArrayList<PodcastStreamInfo> podcasts;
	private Resources resources;
	private PodcastStreamTask podTask = null;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view);
		resources = getResources();
		
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
		if(podcasts == null) {
			showDialog(DIALOG_ERROR);
			return;
		}
		ListView list = (ListView)PodcastStreams.this.findViewById(R.id.list);
		PodcastAdapter adapter = new PodcastAdapter(PodcastStreams.this, podcasts);
		list.setAdapter(adapter);
		
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				@SuppressWarnings("unchecked")
				PodcastStreamInfo stream = ((PodcastItem<PodcastStreamInfo>)view).getData();
				Intent intent = new Intent(PodcastStreams.this, Podcasts.class);
				String[] streamInfo = {stream.description, stream.text, stream.url};
				intent.putExtra(INTENT_KEY_STREAM_INFO, streamInfo);
				startActivity(intent);
			}
		});
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
		case DIALOG_ERROR :
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(resources.getString(R.string.podcasts_empty))
				   .setCancelable(false)
				   .setPositiveButton(resources.getString(R.string.generic_ok), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						PodcastStreams.this.finish();
					}
				});
			dialog = builder.create();
			break;
		default :
			dialog = null;
			break;
		}
		return dialog;
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
	
	private class PodcastStreamTask extends AsyncTask<Void, String, ArrayList<PodcastStreamInfo>> {
		ProgressDialog pd;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = ProgressDialog.show(PodcastStreams.this, resources.getString(R.string.generic_wait),
					resources.getString(R.string.podcasts_streams_downloading), true, false);
		}

		@Override
		protected void onProgressUpdate(String... values) {
			pd.setMessage(values[0]);
		}

		@Override
		protected ArrayList<PodcastStreamInfo> doInBackground(Void... arg0) {
			try {
	    		URL url = new URL(resources.getString(R.string.settings_podcast_opml));
				HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
				
				if(httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					
					InputStream in = httpConnection.getInputStream();
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
					Element docElement = doc.getDocumentElement();
					
					publishProgress(resources.getString(R.string.podcasts_parsing));
					ArrayList<PodcastStreamInfo> streams = new ArrayList<PodcastStreamInfo>();
					
					Element body = (Element)docElement.getElementsByTagName("body").item(0);
					NodeList outlines = body.getElementsByTagName("outline");
					for(int i=0; i<1; i++) {
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
							
							streams.add(thisStream);
						}
					}
					return streams;
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
			return null;
		}
		
		@Override
		protected void onPostExecute(ArrayList<PodcastStreamInfo> result) {
			podcasts = result;
			initList();
			pd.dismiss();
		}
	}
}
