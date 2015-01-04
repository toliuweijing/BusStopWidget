package com.polythinking.mapwidget.app;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import network.RestApis;

public class MapWidgetConfigActivity extends Activity {
  private static final String TAG = MapWidgetConfigActivity.class.getName();

  private int mAppWidgetId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    Bundle extras = intent.getExtras();
    if (extras != null) {
      mAppWidgetId = extras.getInt(
          AppWidgetManager.EXTRA_APPWIDGET_ID,
          AppWidgetManager.INVALID_APPWIDGET_ID);

      Log.d(TAG, "receive WidgetId " + mAppWidgetId);

      WidgetData data = new WidgetData(
          mAppWidgetId,
          RestApis.SAMPLE_STOP_CODE,
          RestApis.SAMPLE_LINE_REF,
          WidgetMode.POWER_ON);
      WidgetDataStore.Singleton.getInstance(this).set(data);
    }

    Intent resultValue = new Intent();
    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
    setResult(RESULT_OK, resultValue);
    finish();
  }
}
