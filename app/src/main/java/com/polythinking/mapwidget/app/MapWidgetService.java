package com.polythinking.mapwidget.app;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import network.Request;

public class MapWidgetService extends Service {

    public static final String EXTRA_WIDGET_IDS = "extra_widget_ids";

    private RequestQueue mRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();

        mRequestQueue = Volley.newRequestQueue(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = getApplicationContext();

        AppWidgetManager appWidgetManager = AppWidgetManager
                .getInstance(context);

        int[] appWidgetIds = intent.getIntArrayExtra(EXTRA_WIDGET_IDS);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.activity_main);
        appWidgetManager.updateAppWidget(appWidgetIds[0], views);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void sendRequest(Context context, int[] widgetIds) {
        Intent intent = new Intent(context.getApplicationContext(), MapWidgetService.class);
        intent.putExtra(EXTRA_WIDGET_IDS, widgetIds);
        context.startService(intent);
    }
}
