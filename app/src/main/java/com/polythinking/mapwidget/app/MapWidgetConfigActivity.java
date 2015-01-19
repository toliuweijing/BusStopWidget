package com.polythinking.mapwidget.app;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import model.SiriResponse;
import model.SiriResponse.Siri.ServiceDelivery.StopMonitoringDelivery.MonitoredStopVisit;
import network.RestApis;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;

public class MapWidgetConfigActivity extends Activity {
  private static final String TAG = MapWidgetConfigActivity.class.getName();

  private EditText mStopCodeText;
  private EditText mLineRefText;
  private Button mGetLinesButton;
  private Button mSubscribe;
  private TextView mLinesText;

  private int mAppWidgetId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    Bundle extras = intent.getExtras();

    if (extras == null) {
      finish();
      return;
    }

    setContentView(R.layout.config_activity);
    configureViews();

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

  private void configureViews() {
    mStopCodeText = (EditText) findViewById(R.id.stop_code);
    mLineRefText = (EditText) findViewById(R.id.line_ref);
    mGetLinesButton = (Button) findViewById(R.id.get_lines_button);
    mSubscribe = (Button) findViewById(R.id.subscribe_button);
    mLinesText = (TextView) findViewById(R.id.lines_text);

    mGetLinesButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            String stopCode = mStopCodeText.getText().toString();
            fetchAndUpdateLinesText(stopCode);
          }
        });
    mSubscribe.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            String stopCode = mStopCodeText.getText().toString();
            String lineRef = mLineRefText.getText().toString();

            if (stopCode != null && lineRef != null) {
              setComplete(Integer.valueOf(stopCode), lineRef);
            }
          }
        });
  }

  private void fetchAndUpdateLinesText(String stopCode) {
    String url = RestApis.Siri.stopMonitoring(Integer.valueOf(stopCode)).toString();

    JsonObjectRequest request = new JsonObjectRequest(
        url,
        null,
        new Response.Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {
            try {
              SiriResponse siriResponse = SiriResponse.read(response);
              setLinesText(siriResponse);
            } catch (IOException e) {
              Log.w(TAG, "failed to parse response", e);
            }
          }},
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.w(TAG, "failed to retrieve response", error);
          }
        });
    Volley.newRequestQueue(this).add(request);
  }

  private void setLinesText(SiriResponse siriResponse) {
    Set<String> refs = Sets.newHashSet();
    List<MonitoredStopVisit> stopVisitList =
        siriResponse.siri.serviceDelivery.stopMonitoringDeliveryConnection.get(0).monitoredStopVisitConnection;
    for (MonitoredStopVisit stopVisit : stopVisitList) {
      refs.add(stopVisit.monitoredVehicleJourney.lineRef);
    }
    String oneLiner = "";
    for (String r : refs) {
      oneLiner += r + "\n\n";
    }

    mLinesText.setText(oneLiner);
  }

  private void setComplete(int stopCode, String lineRef) {
    WidgetData data = new WidgetData(
        mAppWidgetId,
        stopCode,
        lineRef,
        WidgetMode.POWER_ON);
    WidgetDataStore.Singleton.getInstance(this).set(data);

    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
    MapWidgetProvider.configureAlermManager(this, appWidgetManager);
    MapWidgetProvider.configurePowerButtonBroadcast(this, new int[] {mAppWidgetId});

    Intent resultValue = new Intent();
    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
    setResult(RESULT_OK, resultValue);
    finish();
  }
}
