package com.polythinking.mapwidget.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import static com.polythinking.mapwidget.app.MapWidgetUpdateService.USER_ACTION_POWER_BUTTON_CLICKED;

public class MapWidgetProvider extends AppWidgetProvider {
  private static final String TAG = MapWidgetProvider.class.getName();

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    super.onUpdate(context, appWidgetManager, appWidgetIds);

    Log.d(TAG, "onUpdate:" + appWidgetIds.toString());
    configureAlermManager(context, appWidgetManager);
    configurePowerButtonBroadcast(context, appWidgetIds);
  }

  @Override
  public void onDeleted(Context context, int[] appWidgetIds) {
    super.onDeleted(context, appWidgetIds);
    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
    configureAlermManager(context, appWidgetManager);
  }

  @Override
  public void onDisabled(Context context) {
    super.onDisabled(context);
    final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    m.cancel(PendingIntentStore.onAlermManagerUpdate(context, null));
  }

  public static boolean isEnabled(Context context) {
    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
    int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, MapWidgetProvider.class));
    return ids != null && ids.length > 0;
  }

  private void configureAlermManager(Context context, AppWidgetManager appWidgetManager) {
    final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, this.getClass()));

    Log.d(TAG, "ConfigureAlermManager start");
    for (int id : ids) {
      Log.d(TAG, ""+id);
    }
    Log.d(TAG, "ConfigureAlermManager end");

    PendingIntent pendingIntent = PendingIntentStore.onAlermManagerUpdate(
        context,
        ids);

    m.setRepeating(
        AlarmManager.RTC,
        System.currentTimeMillis(),
        AlarmManager.INTERVAL_FIFTEEN_MINUTES / 30,
        pendingIntent);
  }

  private void configurePowerButtonBroadcast(Context context, int[] appWidgetIds) {
    for(int id : appWidgetIds) {
      Log.d(TAG, "configure power button " + id);

      PendingIntent pendingIntent = PendingIntentStore.onPowerButtonClicked(
          context,
          id);

      RemoteViews views = new RemoteViews(
          context.getPackageName(),
          R.layout.activity_main);

      final AppWidgetManager appWidgetManager = AppWidgetManager
          .getInstance(context);
      views.setOnClickPendingIntent(R.id.power_button, pendingIntent);
      appWidgetManager.updateAppWidget(id, views);
    }
  }


  private static class PendingIntentStore {
    private static final int TYPE_ALERM_MANAGER = 0;

    private static PendingIntent onAlermManagerUpdate(
        Context context,
        int[] appWidgetIds) {
      Intent intent = MapWidgetUpdateService.prepareIntent(context, appWidgetIds);

      // TYPE_ALERM_MAANAGER will be the unique id for this PendingIntent.
      // This is the only PendingIntent instance triggered by AlermManager every 30s.
      PendingIntent pendingIntent = PendingIntent.getService(
          context,
          TYPE_ALERM_MANAGER,
          intent,
          PendingIntent.FLAG_CANCEL_CURRENT);
      return pendingIntent;
    }

    private static PendingIntent onPowerButtonClicked(
        Context context,
        int widgetid) {
      Intent intent = MapWidgetUpdateService.prepareIntent(
          context,
          new int[]{widgetid},
          USER_ACTION_POWER_BUTTON_CLICKED);
      return PendingIntent.getService(context, widgetid, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
  }
}
