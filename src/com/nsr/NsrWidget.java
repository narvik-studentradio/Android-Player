package com.nsr;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Toast;

public class NsrWidget extends AppWidgetProvider{
	//public static final String WIDGET_CALLBACK = "widget_callback";
	//public static final String WIDGET_REQUEST_METADATA = "widget_request_metadata";
	private static final String STARTUP_ACTIVITY = "startup_activity";


	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

     
            Intent activityIntent = new Intent(context, Player.class);
            PendingIntent activityPendingIntent = PendingIntent.getActivity(context, 0, activityIntent, 0);
            
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setOnClickPendingIntent(R.id.widgetTextViewArtist, activityPendingIntent);
            views.setTextViewText(R.id.widgetTextViewArtist, "NSR Widget");
            
            if(PlayerService.getInstance() == null) {
	            Intent startIntent = new Intent(context, PlayerService.class);
	            PendingIntent startPendingIntent = PendingIntent.getService(context, 0, startIntent, 0);
            	views.setOnClickPendingIntent(R.id.widgetImageView, startPendingIntent);
            }
            else {
	            Intent startIntent = new Intent(PlayerService.INTENT_COMMAND);
	            startIntent.putExtra(PlayerService.KEY_COMMAND, PlayerService.COMMAND_STOP_PLAYER);
	            PendingIntent startPendingIntent = PendingIntent.getBroadcast(context, 0, startIntent, 0);
            	views.setOnClickPendingIntent(R.id.widgetImageView, startPendingIntent);
            	views.setImageViewResource(R.id.widgetImageView, R.drawable.nsr_widget_pause);
            	
            	Intent metadataIntent = new Intent(PlayerService.INTENT_COMMAND);
            	metadataIntent.putExtra(PlayerService.KEY_COMMAND, PlayerService.COMMAND_REQUEST_METADATA);
            	context.sendBroadcast(metadataIntent);
            }

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
	}

/*
	@Override
	public void onReceive(Context context, Intent intent) {
		String pn = NsrWidget.class.getName();
		RemoteViews views = new RemoteViews(pn, R.layout.widget_layout);
        Intent startIntent = new Intent(context, PlayerService.class);
        PendingIntent startPendingIntent = PendingIntent.getService(context, 0, startIntent, 0);
    	views.setOnClickPendingIntent(R.id.widgetImageView, startPendingIntent);
    	views.setImageViewResource(R.id.widgetImageView, R.drawable.nsr_watermark1);
    	ComponentName cn = new ComponentName(context, NsrWidget.class);
    	AppWidgetManager.getInstance(context).updateAppWidget(cn, views);
		super.onReceive(context, intent);
	}*/
	
}
