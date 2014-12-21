package com.polythinking.mapwidget.app;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class MapWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        dispatchToService(context, appWidgetIds);

    }

    private void dispatchToService(Context context, int[] appWidgetIds) {
        MapWidgetService.sendRequest(context, appWidgetIds);
    }
}
