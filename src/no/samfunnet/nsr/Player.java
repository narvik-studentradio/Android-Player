package no.samfunnet.nsr;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Player extends Activity implements OnClickListener {
	private PlayerReceiver receiver;
	private Timer progressTimer;
	private ProgressBar progressBar;
	private TextView txtArtist;
	private TextView txtTitle;
	private SongData playing;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
        ((Button)findViewById(R.id.buttonStart)).setOnClickListener(this);
        ((Button)findViewById(R.id.buttonStop)).setOnClickListener(this);
        ((Button)findViewById(R.id.buttonPodcast)).setOnClickListener(this);
        ((Button)findViewById(R.id.buttonHistory)).setOnClickListener(this);
        txtArtist = (TextView)findViewById(R.id.appTxtArtist);
        txtTitle = (TextView)findViewById(R.id.appTxtTitle);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
    }
    
    private void updateProgress() {
    	if(playing == null) {
            Intent mdReq = new Intent(PlayerService.INTENT_COMMAND);
            mdReq.putExtra(PlayerService.KEY_COMMAND, PlayerService.COMMAND_REQUEST_METADATA);
        	sendBroadcast(mdReq);
    		return;
    	}
    	int passed = (int) (System.currentTimeMillis() - playing.timestamp) / 1000;
    	progressBar.setProgress(playing.duration - playing.remaining + passed);
    }
    
    private void metadataUpdate(SongData song) {
    	playing = song;
    	txtArtist.setText(song.artist);
    	txtTitle.setText(song.title);
    	progressBar.setMax(song.duration);
    	updateProgress();
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
			progressBar.setProgress(0);
			txtArtist.setText("");
			txtTitle.setText("");
			break;
		case R.id.buttonPodcast:
			startActivity(new Intent(Player.this, no.samfunnet.nsr.podcast.PodcastStreams.class));
			break;
		case R.id.buttonHistory:
			startActivity(new Intent(Player.this, History.class));
			break;
		default:
			break;
		}
	}
    
    @Override
	protected void onPause() {
    	if(receiver != null)
    		unregisterReceiver(receiver);
    	
		progressTimer.cancel();
		progressBar.setProgress(0);
		txtArtist.setText("");
		txtTitle.setText("");
		
		super.onPause();
	}

	@Override
	protected void onResume() {
        Intent mdReq = new Intent(PlayerService.INTENT_COMMAND);
        mdReq.putExtra(PlayerService.KEY_COMMAND, PlayerService.COMMAND_REQUEST_METADATA);
    	sendBroadcast(mdReq);
		if(receiver == null)
			receiver = new PlayerReceiver();
		IntentFilter filter = new IntentFilter(PlayerService.INTENT_CALLBACK);
		registerReceiver(receiver, filter);

        progressTimer = new Timer("progress_timer");
        progressTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if(PlayerService.getInstance() != null)
					progressBar.post(new Runnable() {
						@Override
						public void run() {
							updateProgress();
						}
					});
			}
		}, 1000, 1000);
        updateProgress();
        
		super.onResume();
	}

	private class PlayerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(PlayerService.INTENT_CALLBACK)) {
				String message = intent.getExtras().getString(PlayerService.KEY_MESSAGE);
				if(message.equals(PlayerService.MESSAGE_METADATA_UPDATE)) {
					Bundle extras = intent.getExtras();
					String artist = extras.getString(PlayerService.KEY_METADATA_ARTIST);
					String title = extras.getString(PlayerService.KEY_METADATA_TITLE);
					String album = extras.getString(PlayerService.KEY_METADATA_ALBUM);
					int duration = Integer.parseInt(extras.getString(PlayerService.KEY_METADATA_DURATION));
					int remaining = Integer.parseInt(extras.getString(PlayerService.KEY_METADATA_REMAINING));
					String type = extras.getString(PlayerService.KEY_METADATA_TYPE);
					long timestamp = Long.parseLong(extras.getString(PlayerService.KEY_METADATA_TIMESTAMP));
					SongData song = new SongData(artist, title, album, duration, remaining, type, timestamp);
					metadataUpdate(song);
				}
				else if(message.equals(PlayerService.MESSAGE_ERROR)) {
					txtArtist.setText(R.string.generic_error);
					txtTitle.setText("");
				}
			}
		}
    }
}