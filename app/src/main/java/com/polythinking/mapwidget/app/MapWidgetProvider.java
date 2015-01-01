package com.polythinking.mapwidget.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.util.Calendar;

import static com.polythinking.mapwidget.app.MapWidgetUpdateService.EXTRA_ACTION;
import static com.polythinking.mapwidget.app.MapWidgetUpdateService.VALUE_ACTION_POWER_BUTTON_CLICKED;

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

        Intent intent = MapWidgetUpdateService.prepareIntent(context, appWidgetIds);

        if (mPendingIntent == null) {
            mPendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        m.setRepeating(
            AlarmManager.RTC,
            TIME.getTime().getTime(),
            AlarmManager.INTERVAL_FIFTEEN_MINUTES / 30,
            mPendingIntent);

        configurePowerButtonBroadcast(context, appWidgetIds);

    }

    private void configurePowerButtonBroadcast(Context context, int[] appWidgetIds) {
        Intent intent = MapWidgetUpdateService.prepareIntent(context, appWidgetIds);
        intent.putExtra(EXTRA_ACTION, VALUE_ACTION_POWER_BUTTON_CLICKED);
        PendingIntent pendingIntent =
            PendingIntent.getService(context, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        RemoteViews views = new RemoteViews(
            context.getPackageName(),
            R.layout.activity_main);

        final AppWidgetManager appWidgetManager = AppWidgetManager
            .getInstance(context);
        views.setOnClickPendingIntent(R.id.power_button, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds[0], views);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        m.cancel(mPendingIntent);
    }
}
