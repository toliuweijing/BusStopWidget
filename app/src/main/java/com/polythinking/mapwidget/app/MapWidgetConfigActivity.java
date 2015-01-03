package com.polythinking.mapwidget.app;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import network.RestApis;

public class MapWidgetConfigActivity extends Activity {

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

      WidgetData data = new WidgetData(
          mAppWidgetId,
          RestApis.SAMPLE_STOP_CODE,
          RestApis.SAMPLE_LINE_REF);
      WidgetDataStore.Singleton.getInstance(this).set(data);
    }

    Intent resultValue = new Intent();
    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
    setResult(RESULT_OK, resultValue);
    finish();
  }
}
