package com.nsr;

import java.io.File;
import java.io.FileOutputStream;
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
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class Podcasts extends Activity {
	private ArrayList<PodcastData> data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.podcast_view);
		
		Object retained = getLastNonConfigurationInstance();
		if(retained == null) {
			Intent intent = getIntent();
			String[] streamInfo = intent.getStringArrayExtra(PodcastStreams.INTENT_KEY_STREAM_INFO);
			PodcastTask task = new PodcastTask();
			task.execute(new String[]{streamInfo[2]});
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
				try {
					//set the download URL, a url that points to a file on the internet
					//this is the file to be downloaded
					URL url = new URL(item.getData().url);

					//create the new connection
					HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

					//set up some things on the connection
					urlConnection.setRequestMethod("GET");
					urlConnection.setDoOutput(true);

					//and connect!
					urlConnection.connect();

					//set the path where we want to save the file
					//in this case, going to save it on the root directory of the
					//sd card.
					File SDCardRoot = Environment.getExternalStorageDirectory();
					//create a new file, specifying the path, and the filename
					//which we want to save the file as.
					File file = new File(SDCardRoot,"nsrDownload.mp3");

					//this will be used to write the downloaded data into the file we created
					FileOutputStream fileOutput = new FileOutputStream(file);

					//this will be used in reading the data from the internet
					InputStream inputStream = urlConnection.getInputStream();

					//this is the total size of the file
					int totalSize = urlConnection.getContentLength();
					//variable to store total downloaded bytes
					int downloadedSize = 0;

					//create a buffer...
					byte[] buffer = new byte[1024];
					int bufferLength = 0; //used to store a temporary size of the buffer

					//now, read through the input buffer and write the contents to the file
					while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
						//add the data in the buffer to the file in the file output stream (the file on the sd card
						fileOutput.write(buffer, 0, bufferLength);
						//add up the size so we know how much is downloaded
						downloadedSize += bufferLength;

					}
					//close the output stream when done
					fileOutput.close();

				//catch some possible errors...
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// see http://androidsnippets.com/download-an-http-file-to-sdcard-with-progress-notification
			}
		});
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
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
