package com.nsr;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class NsrWidget extends AppWidgetProvider{
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

     
            Intent activityIntent = new Intent(context, Player.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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
