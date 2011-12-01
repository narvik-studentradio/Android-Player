package com.nsr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;

public class DownloadService extends Service {
	public static final String KEY_URL = "com.nsr.DownloadService.KEY_URL";
	
	private static DownloadService instance;
	
	private ArrayList<DownloadTask> downloads;

	@Override
	public void onCreate() {
		super.onCreate();
		downloads = new ArrayList<DownloadService.DownloadTask>();
		instance = this;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		DownloadTask task = new DownloadTask();
		task.execute(intent.getStringExtra(KEY_URL));
		return START_NOT_STICKY;
	}
	
	public static void downloadFile(String url) {
		if(instance == null) {
			
		}
	}
	
	@Override
	public void onDestroy() {
		instance = null;
		for(DownloadTask task : downloads)
			task.cancel(true);
		super.onDestroy();
	}

	public static DownloadService getInstance() {
		return instance;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private class DownloadTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
			//http://www.androidsnippets.com/download-an-http-file-to-sdcard-with-progress-notification
			try {
				//set the download URL, a url that points to a file on the internet
				//this is the file to be downloaded
				URL url = new URL(params[0]);

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
			return null;
		}
	}
}
