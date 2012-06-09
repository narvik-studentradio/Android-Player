package com.nsr;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ListView;

public class History extends Activity {
	private static final int DIALOG_ERROR = 0;
	private Resources resources;
	private ListView list;
	private UpdateReceiver updateReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view);
		resources = getResources();
		list = (ListView)findViewById(R.id.list);
		updateReceiver = new UpdateReceiver();
		
		populate();
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(updateReceiver);
		super.onPause();
	}

	@Override
	protected void onResume() {
		registerReceiver(updateReceiver, new IntentFilter(PlayerService.INTENT_CALLBACK));
		super.onResume();
	}

	private void populate() {
		List<SongData> history = PlayerService.getHistory();
		if(history==null) {
			showDialog(DIALOG_ERROR);
			return;
		}
		SongAdapter adapter = new SongAdapter(this, history);
		list.setAdapter(adapter);
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		Dialog dialog;
		switch(id) {
		case DIALOG_ERROR :
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(resources.getString(R.string.history_empty))
				   .setCancelable(false)
				   .setPositiveButton(resources.getString(R.string.generic_ok), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						History.this.finish();
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
	
	private class UpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getStringExtra(PlayerService.KEY_MESSAGE).equals(PlayerService.MESSAGE_METADATA_UPDATE))
				populate();
		}
	}
}
