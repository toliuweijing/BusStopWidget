package com.polythinking.mapwidget.app;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.google.common.collect.Lists;
import fastservice.FastHandler;
import fastservice.FastService;

import java.util.List;

public class MapWidgetUpdateService extends FastService {
  private static final String TAG = MapWidgetUpdateService.class.getName();

  public static final String EXTRA_WIDGET_IDS = "extra_widget_ids";

  public static final String EXTRA_USER_ACTION = "extra_action";
  public static final int USER_ACTION_POWER_BUTTON_CLICKED = 0;

  private List<? extends FastHandler> mHandlers;

  @Override
  public void onCreate() {
    super.onCreate();

    mHandlers = Lists.newArrayList(
        new UpdateHandler(this),
        new PowerButtonClickHandler(this));
  }

  private boolean stopSelfIfNeeded() {
    if (!MapWidgetProvider.isEnabled(this)) {
      stopSelf();
      return true;
    }
    return false;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i(TAG, "on run");
    if (stopSelfIfNeeded()) {
      Log.d(TAG, "stop self since there's no widget enabled");
      return START_NOT_STICKY;
    }

    dispatch(intent);

    return START_NOT_STICKY;
  }

  @Override
  protected List<? extends FastHandler> getHandlers() {
    return mHandlers;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
