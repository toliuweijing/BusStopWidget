package com.polythinking.mapwidget.app;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import fastservice.FastHandler;

/**
 * Created by developer on 1/18/15.
 */
public class PowerButtonClickHandler extends FastHandler {
  private final String TAG = PowerButtonClickHandler.class.getName();

  private static final String EXTRA_WIDGET_ID = "extra_widget_id";

  private final AppWidgetManager mAppWidgetManager;
  private final WidgetDataStore mWidgetDataStore;
  private final RemoteViews mRemoteViews;

  public PowerButtonClickHandler(Context context) {
    super(context);

    mAppWidgetManager = AppWidgetManager.getInstance(context);
    mWidgetDataStore = WidgetDataStore.Singleton.getInstance(context);
    mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_main_layout);
  }

  @Override
  public void onDispatch(Intent intent) {
    int widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, -1);
    WidgetData data = mWidgetDataStore.get(widgetId);

    if (data != null) {
      onClick(data);
    }
  }

  private void onClick(WidgetData data) {
    Log.d(TAG, "power button clicked");
    data.setWidgetMode(mWidgetDataStore, data.getMode().nextMode());
    updatePowerButton(data);
  }

  public void updatePowerButton(WidgetData data) {
    mRemoteViews.setImageViewResource(R.id.power_button, data.getMode().getDrawableId());
    mAppWidgetManager.updateAppWidget(
        data.getWidgetId(),
        mRemoteViews);
  }

  public static Intent prepareIntent(Context context, int widgetId) {
    Intent intent =  MapWidgetUpdateService.createIntent(
        context,
        MapWidgetUpdateService.class,
        PowerButtonClickHandler.class);
    intent.putExtra(EXTRA_WIDGET_ID, widgetId);
    return intent;
  }
}
