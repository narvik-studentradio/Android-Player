package com.nsr;

import com.nsr.podcast.PodcastStreams;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class Player extends Activity implements OnClickListener {
	private PlayerService playerService;
	private PlayerReceiver receiver;
	private MetadataTracker metadataTracker;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
        ((Button)findViewById(R.id.buttonStart)).setOnClickListener(this);
        ((Button)findViewById(R.id.buttonStop)).setOnClickListener(this);
        ((Button)findViewById(R.id.buttonPodcast)).setOnClickListener(this);
        
        metadataTracker = new MetadataTracker(new Runnable() {
			@Override
			public void run() {
				metadataUpdate();
			}
		});
    }
    
    private void metadataUpdate() {
    	Toast.makeText(Player.this, metadataTracker.getPlaying().title, Toast.LENGTH_SHORT).show();
    }

	@Override
	public void onClick(View v) {
		final Intent serviceIntent = new Intent(getApplicationContext(), PlayerService.class);
		switch(v.getId()) {
		case R.id.buttonStart:
			startService(serviceIntent);
			break;
		case R.id.buttonStop:
			stopService(serviceIntent);
			break;
		case R.id.buttonPodcast:
			startActivity(new Intent(Player.this, com.nsr.podcast.PodcastStreams.class));
			break;
		default:
			break;
		}
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
		metadataTracker.close();
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