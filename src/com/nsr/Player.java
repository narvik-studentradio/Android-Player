package com.nsr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.NotActiveException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.CharsetDecoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Player extends Activity {
	private PlayerService playerService;
	private PlayerReceiver receiver;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		final Intent serviceIntent = new Intent(getApplicationContext(), PlayerService.class);
        
        Button btn = (Button)findViewById(R.id.button1);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startService(serviceIntent);
			}
		});
        
        Button btnStop = (Button)findViewById(R.id.button2);
        btnStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopService(serviceIntent);
			}
		});
        
        ((ImageView)findViewById(R.id.imageViewLargeIcon)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Player.this, PodcastStreams.class));
			}
		});
    }
    
    @Override
	protected void onPause() {
    	if(receiver != null)
    		unregisterReceiver(receiver);
		super.onPause();
	}

	@Override
	protected void onResume() {
		if(receiver == null)
			receiver = new PlayerReceiver();
		IntentFilter filter = new IntentFilter(PlayerService.INTENT_CALLBACK);
		registerReceiver(receiver, filter);
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
    
    private class PlayerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(PlayerService.INTENT_CALLBACK)) {
				Toast.makeText(Player.this, intent.getExtras().getString(PlayerService.KEY_MESSAGE), Toast.LENGTH_SHORT).show();
				if(intent.getExtras().getString(PlayerService.KEY_MESSAGE).equals(PlayerService.MESSAGE_METADATA_UPDATE))
					Toast.makeText(Player.this, intent.getExtras().getString(PlayerService.KEY_METADATA_TITLE), Toast.LENGTH_SHORT).show();
			}
		}
    }
}