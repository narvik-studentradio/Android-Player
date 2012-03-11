package com.nsr;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.ListView;

public class History extends Activity {
	private static final int DIALOG_ERROR = 0;
	ListView list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_view);
		
		list = (ListView)findViewById(R.id.list);
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
			builder.setMessage("No history available.")
				   .setPositiveButton("Ok", new OnClickListener() {
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
}
