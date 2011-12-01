package com.nsr;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.IBinder;
import android.widget.RemoteViews;

public class PlayerService extends Service {
	private MediaPlayer mediaPlayer;
	private Metadataz metadata;
	
	private static PlayerService instance;
	public static PlayerService getInstance() {
		return instance;
	}
	public static final int NOTIFICATION = 33462;
	public static final String INTENT_COMMAND = "com.nsr.playerservice.intent_command";
	public static final String KEY_COMMAND = "command";
	public static final String COMMAND_REQUEST_METADATA = "request_metadata";
	public static final String COMMAND_STOP_PLAYER = "stop_player";
	public static final String INTENT_CALLBACK = "com.nsr.playerservice.intent_callback";
	public static final String KEY_MESSAGE = "service_message";
	public static final String KEY_METADATA_TITLE = "metadata_title";
	public static final String KEY_METADATA_ARTIST = "metadata_artist";
	public static final String KEY_METADATA_URL = "metadata_url";
	public static final String MESSAGE_PLAYER_STARTED = "player_started";
	public static final String MESSAGE_METADATA_UPDATE = "metadata_update";
	public static final String MESSAGE_ERROR = "error";
	public static final String ERROR_OFFLINE = "error_offline";
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		WidgetCommReceiver wcr = new WidgetCommReceiver();
		IntentFilter callbackFilter = new IntentFilter(INTENT_COMMAND);
		registerReceiver(wcr, callbackFilter);
	}
	
	private void startPlayer() {
		if(mediaPlayer != null) {/*
			setWidgetStarted();
			if(metadata == null) {
				MetadataTask metaTask = new MetadataTask();
				metaTask.execute((Void)null);
			}
			else {
				sendMessage(MESSAGE_METADATA_UPDATE, metadata.artist, metadata.title, metadata.url);
			}*/
			return;
		}
		try {
			mediaPlayer = new MediaPlayer();
			String url = "http://stream.sysrq.no:8000/01-nsr-mobile.mp3";
			mediaPlayer.setDataSource(url);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					mp.start();
					sendMessage(MESSAGE_PLAYER_STARTED);
				}
			});
			mediaPlayer.prepareAsync();
			
			Intent playerIntent = new Intent(getApplicationContext(), Player.class);
			playerIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_SINGLE_TOP);
			Notification serviceNotification = new Notification(R.drawable.nsr3, "Service running", 5000);
			PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, playerIntent, 0);
			serviceNotification.setLatestEventInfo(getApplicationContext(), "NSR", "NSR spiller nå", pi);
			startForeground(NOTIFICATION, serviceNotification);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		MetadataTask mt = new MetadataTask();
		mt.execute((Void)null);
		setWidgetStarted();
	}
	
	private void sendRemoteViews(RemoteViews rv) {
		AppWidgetManager awm = AppWidgetManager.getInstance(this);
		ComponentName cname = new ComponentName(this, NsrWidget.class);
		int[] ids = awm.getAppWidgetIds(cname);
		awm.updateAppWidget(ids, rv);
	}
	
	private void setWidgetStarted() {
		RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget_layout);
        Intent intent = new Intent(INTENT_COMMAND);
        intent.putExtra(KEY_COMMAND, COMMAND_STOP_PLAYER);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        rv.setOnClickPendingIntent(R.id.widgetImageView, pendingIntent);
        rv.setImageViewResource(R.id.widgetImageView, R.drawable.nsr_widget_pause);
		
        sendRemoteViews(rv);
	}
	
	private void setWidgetStopped() {
		
		RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget_layout);
        Intent intent = new Intent(this, PlayerService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        rv.setOnClickPendingIntent(R.id.widgetImageView, pendingIntent);
        rv.setImageViewResource(R.id.widgetImageView, R.drawable.nsr_widget_play);
        rv.setTextViewText(R.id.widgetTextViewArtist, "NSR Widget");
        rv.setTextViewText(R.id.widgetTextViewTitle, "");
        
        sendRemoteViews(rv);
        
        Intent testIntent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
        sendBroadcast(testIntent);
	}
	
	private void sendMessage(String type, String... message) {
		Intent messageIntent = new Intent(INTENT_CALLBACK);
		messageIntent.putExtra(KEY_MESSAGE, type);
		if(type.equals(MESSAGE_METADATA_UPDATE) && message.length >= 3) {
			messageIntent.putExtra(KEY_METADATA_ARTIST, message[0]);
			messageIntent.putExtra(KEY_METADATA_TITLE, message[1]);
			messageIntent.putExtra(KEY_METADATA_URL, message[2]);
		}
		sendBroadcast(messageIntent);
		
		if(type.equals(MESSAGE_METADATA_UPDATE) && message.length >= 2) {
			RemoteViews rm = new RemoteViews(getPackageName(), R.layout.widget_layout);
			rm.setTextViewText(R.id.widgetTextViewArtist, message[0]);
			rm.setTextViewText(R.id.widgetTextViewTitle, message[1]);
			sendRemoteViews(rm);
		}
	}
	
	private class WidgetCommReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(INTENT_COMMAND)) {
				if(intent.getExtras().getString(KEY_COMMAND).equals(COMMAND_STOP_PLAYER))
					PlayerService.this.stopSelf();
				else if(intent.getExtras().getString(KEY_COMMAND).equals(COMMAND_REQUEST_METADATA)) {
					if(metadata == null) {
						MetadataTask metaTask = new MetadataTask();
						metaTask.execute((Void)null);
					}
					else
						sendMessage(MESSAGE_METADATA_UPDATE, metadata.artist, metadata.title, metadata.url);
				}
			}
		}
	}
	
	private class MetadataTask extends AsyncTask<Void, Void, String[]> {
		@Override
		protected String[] doInBackground(Void... arg0) {
			try {
	    		URL url = new URL("http://stream.sysrq.no:8000/status2.xsl");
				
				HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
				
				if(httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					
					InputStream in = httpConnection.getInputStream();
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
					Element docElement = doc.getDocumentElement();
					
					String metadata = docElement.getFirstChild().getNodeValue();
					String[] metadataz = metadata.split(",");
					String title = metadataz[16];
					return new String[]{title};
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
			if(metadata == null)
				metadata = new Metadataz();
			metadata.artist = result[0];
			metadata.title = result[0];
			metadata.url = result[0];
			sendMessage(MESSAGE_METADATA_UPDATE, result[0], result[0], result[0]);
		}
	}
	
	private class Metadataz {
		public String artist;
		public String title;
		public String url;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startPlayer();
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		setWidgetStopped();
		mediaPlayer.release();
		instance = null;
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
