package com.polythinking.mapwidget.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Date;

public class MapWidgetProvider extends AppWidgetProvider {

    private PendingIntent mPendingIntent;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);


        final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        final Calendar TIME = Calendar.getInstance();
        TIME.set(Calendar.MINUTE, 0);
        TIME.set(Calendar.SECOND, 0);
        TIME.set(Calendar.MILLISECOND, 0);

        Intent intent = MapWidgetService.prepareIntent(context, appWidgetIds);

        if (mPendingIntent == null) {
            mPendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        m.setRepeating(
            AlarmManager.RTC,
            TIME.getTime().getTime(),
            AlarmManager.INTERVAL_FIFTEEN_MINUTES / 30,
            mPendingIntent);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        m.cancel(mPendingIntent);
    }

    private void dispatchToService(Context context, int[] appWidgetIds) {
        MapWidgetService.sendRequest(context, appWidgetIds);
    }
}
