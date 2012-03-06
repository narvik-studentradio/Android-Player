package com.nsr.podcast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.nsr.Player;
import com.nsr.R;
import com.nsr.R.id;
import com.nsr.R.layout;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class Podcasts extends Activity {
	private ArrayList<PodcastData> data;
	PodcastTask podTask = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.podcast_view);
		
		Object retained = getLastNonConfigurationInstance();
		if(retained == null) {
			Intent intent = getIntent();
			String[] streamInfo = intent.getStringArrayExtra(PodcastStreams.INTENT_KEY_STREAM_INFO);
			podTask = new PodcastTask();
			podTask.execute(new String[]{streamInfo[2]});
		}
		else {
			data = (ArrayList<PodcastData>) retained;
			initList();
		}
	}

	private void initList() {
		ListView list = (ListView)Podcasts.this.findViewById(R.id.listView);
		PodcastAdapter adapter = new PodcastAdapter(this, data);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				PodcastItem item = (PodcastItem) view;
				
				File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
				downloadDir.mkdirs();
				
				Uri uri = Uri.parse(item.getData().url);
				
				DownloadManager mgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
				DownloadManager.Request req = new DownloadManager.Request(uri);
				req.setTitle("NSR download");
				req.setDescription(item.getData().description);
				req.setDestinationInExternalPublicDir(Environment.DIRECTORY_PODCASTS, item.getData().url.substring(item.getData().url.lastIndexOf("/")));
				mgr.enqueue(req);
				
				Intent backIntent = new Intent(Podcasts.this, Player.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(backIntent);
			}
		});
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		if(podTask.getStatus() != AsyncTask.Status.FINISHED) {
			podTask.pd.dismiss();
			podTask.cancel(true);
			return null;
		}
		return data;
	}
	
	private class PodcastTask extends AsyncTask<String, String, ArrayList<PodcastData>> {
		private ProgressDialog pd;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = ProgressDialog.show(Podcasts.this, "Please wait", "Downloading podcast list", true, false);
		}

		@Override
		protected void onProgressUpdate(String... values) {
			pd.setMessage(values[0]);
		}

		@Override
		protected ArrayList<PodcastData> doInBackground(String... address) {
			try {
				URL url = new URL(address[0]);
				HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
				
				if(httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
	
					InputStream in = httpConnection.getInputStream();
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
					Element docElement = doc.getDocumentElement();
					
					publishProgress("Parsing data");
					
					NodeList outlines = docElement.getElementsByTagName("item");
					
					ArrayList<PodcastData> result = new ArrayList<PodcastData>();
					
					for(int i=0; i<outlines.getLength(); i++) {
						Element ele = (Element) outlines.item(i);
						
						String eleTitle = ele.getElementsByTagName("title").item(0).getFirstChild().getNodeValue();
						Element enclosure = (Element)ele.getElementsByTagName("enclosure").item(0);
						String eleUrl = enclosure.getAttribute("url");
						String eleDate = ele.getElementsByTagName("pubDate").item(0).getFirstChild().getNodeValue();
						
						PodcastData thisCast = new PodcastData();
						
						thisCast.description = eleTitle;
						thisCast.date = eleDate;
						thisCast.url = eleUrl;
						
						result.add(thisCast);
					}
					return result;
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(ArrayList<PodcastData> result) {
			data = result;
			initList();
			pd.dismiss();
		}
	}
}
