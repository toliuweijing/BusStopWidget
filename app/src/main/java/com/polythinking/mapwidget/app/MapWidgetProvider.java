package com.polythinking.mapwidget.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.polythinking.mapwidget.app.MapWidgetUpdateService.USER_ACTION_POWER_BUTTON_CLICKED;

public class MapWidgetProvider extends AppWidgetProvider {

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    super.onUpdate(context, appWidgetManager, appWidgetIds);

    configureAlermManager(context, appWidgetIds);
    configurePowerButtonBroadcast(context, appWidgetIds);
  }

  private void configureAlermManager(Context context, int[] appWidgetIds) {
    final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    PendingIntent pendingIntent = PendingIntentStore.get(
        context,
        appWidgetIds,
        PendingIntentStore.TYPE_ALERM_MANAGER);

    m.setRepeating(
        AlarmManager.RTC,
        System.currentTimeMillis(),
        AlarmManager.INTERVAL_FIFTEEN_MINUTES / 30,
        pendingIntent);
  }

  private void configurePowerButtonBroadcast(Context context, int[] appWidgetIds) {
    PendingIntent pendingIntent = PendingIntentStore.get(
        context,
        appWidgetIds,
        PendingIntentStore.TYPE_POWER_BUTTON_CLICKED);

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
    m.cancel(PendingIntentStore.get(context, null, PendingIntentStore.TYPE_ALERM_MANAGER));
  }

  private static class PendingIntentStore {
    private static final int TYPE_ALERM_MANAGER = 0;
    private static final int TYPE_POWER_BUTTON_CLICKED = 1;

    private static PendingIntent get(
        Context context,
        int[] appWidgetIds,
        int type) {
      Intent intent = null;
      if (type == TYPE_ALERM_MANAGER) {
        intent = MapWidgetUpdateService.prepareIntent(context, appWidgetIds);
      } else if (type == TYPE_POWER_BUTTON_CLICKED) {
        intent = MapWidgetUpdateService.prepareIntent(context, appWidgetIds, USER_ACTION_POWER_BUTTON_CLICKED);
      }

      // PendingIntent is globally cached and reused if there's a match.
      // We pass in @type as a part of its key, which assures PendingIntent
      // is reused only within the same @type.
      PendingIntent pendingIntent =
          PendingIntent.getService(context, type, intent, PendingIntent.FLAG_CANCEL_CURRENT);
      return pendingIntent;
    }
  }
}
