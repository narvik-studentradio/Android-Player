package com.nsr.podcast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.nsr.Player;
import com.nsr.R;

public class Podcasts extends Activity {
	private static final int DIALOG_ERROR = 0;
	private Resources resources;
	private ArrayList<PodcastData> data;
	private PodcastTask podTask = null;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view);
		resources = getResources();
		
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
		if(data == null) {
			showDialog(DIALOG_ERROR);
			return;
		}
		ListView list = (ListView)Podcasts.this.findViewById(R.id.list);
		PodcastAdapter adapter = new PodcastAdapter(this, data);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				@SuppressWarnings("unchecked")
				PodcastItem<PodcastData> item = (PodcastItem<PodcastData>) view;
				
				File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
				downloadDir.mkdirs();
				
				Uri uri = Uri.parse(item.getData().url);
				
				DownloadManager mgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
				DownloadManager.Request req = new DownloadManager.Request(uri);
				req.setTitle(resources.getString(R.string.podcasts_downloadmanager_title));
				req.setDescription(item.getData().description);
				req.setDestinationInExternalPublicDir(Environment.DIRECTORY_PODCASTS, item.getData().url.substring(item.getData().url.lastIndexOf("/")));
				mgr.enqueue(req);
				
				Intent backIntent = new Intent(Podcasts.this, Player.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(backIntent);
			}
		});
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		Dialog dialog;
		switch(id) {
		case DIALOG_ERROR :
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(resources.getString(R.string.podcasts_empty))
				   .setCancelable(false)
				   .setPositiveButton(resources.getString(R.string.generic_ok), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Podcasts.this.finish();
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
			pd = ProgressDialog.show(Podcasts.this, resources.getString(R.string.generic_wait),
					resources.getString(R.string.podcasts_downloading), true, false);
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
					
					publishProgress(resources.getString(R.string.podcasts_parsing));
					
					NodeList outlines = docElement.getElementsByTagName("item");
					
					ArrayList<PodcastData> result = new ArrayList<PodcastData>();
					
					for(int i=0; i<outlines.getLength(); i++) {
						Element ele = (Element) outlines.item(i);
						
						String eleTitle = ele.getElementsByTagName("title").item(0).getFirstChild().getNodeValue();
						Element enclosure = (Element)ele.getElementsByTagName("enclosure").item(0);
						if(enclosure == null)
							continue;
						String eleUrl = enclosure.getAttribute("url");
						if(eleUrl.equals(""))
							continue;
						String eleDesc = ele.getElementsByTagName("description").item(0).getFirstChild().getNodeValue();
						
						PodcastData thisCast = new PodcastData();
						
						thisCast.description = eleDesc;
						thisCast.title = eleTitle;
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
