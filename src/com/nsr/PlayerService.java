package com.nsr;

import java.io.IOException;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.widget.RemoteViews;

public class PlayerService extends Service implements OnPreparedListener, OnErrorListener, OnCompletionListener {
	private MediaPlayer mediaPlayer;
	private MetadataTracker metadataTracker;
	private WidgetCommReceiver wcr = new WidgetCommReceiver();
	private NotificationManager notificationManager;
	private Resources resources;
	private boolean error = false;
	private boolean stopSpam = false; //TODO: Really ugly hack!
	
	private static PlayerService instance;
	public static PlayerService getInstance() {
		return instance;
	}
	public static List<SongData> getHistory() {
		if(instance != null && instance.metadataTracker != null)
			return instance.metadataTracker.getHistory();
		return null;
	}
	
	public static final int NOTIFICATION = 33462;
	//Intent commands
	public static final String INTENT_COMMAND = "com.nsr.playerservice.intent_command";
	public static final String INTENT_CALLBACK = "com.nsr.playerservice.intent_callback";
	//Callback keys
	public static final String KEY_COMMAND = "command";
	public static final String KEY_MESSAGE = "service_message";
	//Command keys
	public static final String COMMAND_REQUEST_METADATA = "request_metadata";
	public static final String COMMAND_STOP_PLAYER = "stop_player";
	//Service message keys
	public static final String MESSAGE_PLAYER_STARTED = "player_started";
	public static final String MESSAGE_METADATA_UPDATE = "metadata_update";
	public static final String MESSAGE_ERROR = "error";
	public static final String MESSAGE_STARTING = "starting";
	//Metadata keys
	public static final String KEY_METADATA_TITLE = "metadata_title";
	public static final String KEY_METADATA_ARTIST = "metadata_artist";
	public static final String KEY_METADATA_ALBUM = "metadata_album";
	public static final String KEY_METADATA_DURATION = "metadata_duration";
	public static final String KEY_METADATA_REMAINING = "metadata_remaining";
	public static final String KEY_METADATA_TYPE = "metadata_type";
	public static final String KEY_METADATA_TIMESTAMP = "metadata_timestamp";
	//Error keys
	public static final String ERROR_OFFLINE = "error_offline";
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
		resources = getResources();
		IntentFilter callbackFilter = new IntentFilter(INTENT_COMMAND);
		registerReceiver(wcr, callbackFilter);
	}
	
	private void startPlayer() {
		if(mediaPlayer != null) {
			if(mediaPlayer.isPlaying())
				return;
			mediaPlayer.release();
		}
		try {
			mediaPlayer = new MediaPlayer();
//			String url = /*"http://stream.sysrq.no:8000/00-nsr.mp3";*/"http://stream.sysrq.no:8000/01-nsr-mobile.mp3";
			String url = getResources().getString(R.string.settings_stream_url);
			mediaPlayer.setDataSource(url);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnPreparedListener(this);
			mediaPlayer.setOnErrorListener(this);
			mediaPlayer.setOnCompletionListener(this);
			mediaPlayer.prepareAsync();
			
		} catch (IOException e) {
			e.printStackTrace();
			sendMessage(MESSAGE_ERROR);
			setWidgetError();
			return;
		}
	}
	
	private void sendRemoteViews(RemoteViews rv) {
		AppWidgetManager awm = AppWidgetManager.getInstance(this);
		ComponentName cname = new ComponentName(this, NsrWidget.class);
		int[] ids = awm.getAppWidgetIds(cname);
		awm.updateAppWidget(ids, rv);
	}
	
	private void setWidgetStarting() {
		RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget_layout);
        rv.setOnClickPendingIntent(R.id.widgetImageView, PendingIntent.getService(this, 0, new Intent(), 0));
        rv.setImageViewResource(R.id.widgetImageView, R.drawable.nsr_widget_green);
        rv.setTextViewText(R.id.widgetTextViewArtist, getResources().getString(R.string.generic_buffering));
        rv.setTextViewText(R.id.widgetTextViewTitle, "");
        
        sendRemoteViews(rv);
	}
	
	private void setWidgetStarted() {
		RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget_layout);
        Intent intent = new Intent(INTENT_COMMAND);
        intent.putExtra(KEY_COMMAND, COMMAND_STOP_PLAYER);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        rv.setOnClickPendingIntent(R.id.widgetImageView, pendingIntent);
        rv.setImageViewResource(R.id.widgetImageView, R.drawable.nsr_widget_pause);
        rv.setTextViewText(R.id.widgetTextViewArtist, getResources().getString(R.string.generic_playing));
        rv.setTextViewText(R.id.widgetTextViewTitle, "");
		
        sendRemoteViews(rv);
	}
	
	private void setWidgetStopped() {
		RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget_layout);
        Intent intent = new Intent(this, PlayerService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        rv.setOnClickPendingIntent(R.id.widgetImageView, pendingIntent);
        rv.setImageViewResource(R.id.widgetImageView, R.drawable.nsr_widget_play);
        rv.setTextViewText(R.id.widgetTextViewArtist, getResources().getString(R.string.widget_title));
        rv.setTextViewText(R.id.widgetTextViewTitle, "");
        
        sendRemoteViews(rv);
	}
	
	private void setWidgetError() {
		RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget_layout);
        Intent intent = new Intent(this, PlayerService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        rv.setOnClickPendingIntent(R.id.widgetImageView, pendingIntent);
        rv.setImageViewResource(R.id.widgetImageView, R.drawable.nsr_widget_red);
        rv.setTextViewText(R.id.widgetTextViewArtist, getResources().getString(R.string.generic_error));
        rv.setTextViewText(R.id.widgetTextViewTitle, "");
        
        sendRemoteViews(rv);
	}
	
	private void sendMessage(String type, String... message) {
		Intent messageIntent = new Intent(INTENT_CALLBACK);
		messageIntent.putExtra(KEY_MESSAGE, type);
		
		if(type.equals(MESSAGE_METADATA_UPDATE) && message.length >= 7) {
			messageIntent.putExtra(KEY_METADATA_ARTIST, message[0]);
			messageIntent.putExtra(KEY_METADATA_TITLE, message[1]);
			messageIntent.putExtra(KEY_METADATA_ALBUM, message[2]);
			messageIntent.putExtra(KEY_METADATA_DURATION, message[3]);
			messageIntent.putExtra(KEY_METADATA_REMAINING, message[4]);
			messageIntent.putExtra(KEY_METADATA_TYPE, message[5]);
			messageIntent.putExtra(KEY_METADATA_TIMESTAMP, message[6]);
			RemoteViews rm = new RemoteViews(getPackageName(), R.layout.widget_layout);
			rm.setTextViewText(R.id.widgetTextViewArtist, message[0]);
			rm.setTextViewText(R.id.widgetTextViewTitle, message[1]);
			sendRemoteViews(rm);
		}
		sendBroadcast(messageIntent);
		if(type.equals(MESSAGE_STARTING))
			setWidgetStarting();
	}
	
	private void sendMetadata() {
		if(metadataTracker == null) {
			metadataTracker = new MetadataTracker(getResources().getString(R.string.settings_metadata_url), new Runnable() {
				@Override
				public void run() {
					sendMetadata();
				}
			});
			return;
		}
		SongData song = metadataTracker.getPlaying();
		if(song == null)
			return;
		sendMessage(MESSAGE_METADATA_UPDATE, song.artist, song.title, song.album,
				Integer.toString(song.duration), Integer.toString(song.remaining),
				song.type, Long.toString(song.timestamp));
		
		notificationManager.notify(NOTIFICATION, prepareNotification());
	}
	
	private Notification prepareNotification() {
		Intent playerIntent = new Intent(getApplicationContext(), Player.class);
		playerIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Notification serviceNotification = new Notification(R.drawable.nsr3, resources.getString(R.string.notification_ticker), 0);
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, playerIntent, 0);
		String notificationText;
		if(metadataTracker != null && metadataTracker.getPlaying() != null)
			notificationText = metadataTracker.getPlaying().artist + 
							   getResources().getString(R.string.generic_separator) + 
							   metadataTracker.getPlaying().title;
		else
			notificationText = resources.getString(R.string.notification_text);
		serviceNotification.setLatestEventInfo(getApplicationContext(), resources.getString(R.string.notification_title), notificationText, pi);
		serviceNotification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
		return serviceNotification;
	}
	
	private class WidgetCommReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(INTENT_COMMAND)) {
				if(intent.getExtras().getString(KEY_COMMAND).equals(COMMAND_STOP_PLAYER))
					PlayerService.this.stopSelf();
				else if(intent.getExtras().getString(KEY_COMMAND).equals(COMMAND_REQUEST_METADATA)) {
					sendMetadata();
				}
			}
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(stopSpam)
			return START_STICKY;
		stopSpam = true;
		sendMessage(MESSAGE_STARTING);
		setWidgetStarting();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					return;
				}
				stopSpam = false;
			}
		}).start();
		
		if(mediaPlayer != null && mediaPlayer.isPlaying())
			return START_STICKY;
		
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		if(audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
			@Override
			public void onAudioFocusChange(int focusChange) {
				switch(focusChange) {
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
					mediaPlayer.stop();
					break;
				case AudioManager.AUDIOFOCUS_GAIN:
					startPlayer();
					break;
				case AudioManager.AUDIOFOCUS_LOSS:
				default:
					stopSelf();
					break;
				}
			}
		}, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
			stopSelf();
		else
			startPlayer();
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(wcr);
		if(error)
			setWidgetError();
		else
			setWidgetStopped();
		if(mediaPlayer != null)
			mediaPlayer.release();
		instance = null;
		if(metadataTracker != null)
			metadataTracker.close();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();
		sendMessage(MESSAGE_PLAYER_STARTED);
		
		startForeground(NOTIFICATION, prepareNotification());

		if(metadataTracker == null) {
			metadataTracker = new MetadataTracker(getResources().getString(R.string.settings_metadata_url), new Runnable() {
				@Override
				public void run() {
					sendMetadata();
				}
			});
		}
		setWidgetStarted();
	}
	
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		sendMessage(MESSAGE_ERROR);
		error = true;
		stopSelf();
		return true;
	}
	@Override
	public void onCompletion(MediaPlayer mp) {
		sendMessage(MESSAGE_ERROR);
		error = true;
		stopSelf();
	}
}