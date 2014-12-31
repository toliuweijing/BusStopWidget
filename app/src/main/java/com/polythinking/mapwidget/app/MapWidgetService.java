package com.polythinking.mapwidget.app;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import model.SiriResponse;
import model.SiriResponse.Siri.ServiceDelivery.StopMonitoringDelivery.MonitoredStopVisit;
import model.SiriResponse.Siri.ServiceDelivery.StopMonitoringDelivery.MonitoredStopVisit.MonitoredVehicleJourney;
import network.RestApis;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    Log.i("jing", "onStartCommend");

    Context context = getApplicationContext();

    final AppWidgetManager appWidgetManager = AppWidgetManager
        .getInstance(context);

    final int[] appWidgetIds = intent.getIntArrayExtra(EXTRA_WIDGET_IDS);

    String url = RestApis.Siri.stopMonitoring(
        RestApis.SAMPLE_STOP_CODE,
        RestApis.SAMPLE_LINE_REF).toString();

    Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
      @Override
      public void onResponse(JSONObject response) {
        try {
          SiriResponse siriResponse = SiriResponse.read(response);
          RemoteViews remoteViews = createRemoteViews(siriResponse);
          appWidgetManager.updateAppWidget(appWidgetIds[0], remoteViews);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }};

    JsonObjectRequest request = new JsonObjectRequest(url, null, listener, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.d("jing", error.toString());
      }
    });
    mRequestQueue.add(request);


    return START_NOT_STICKY;
  }

  public RemoteViews createRemoteViews(SiriResponse siriResponse) {
    RemoteViews views = new RemoteViews(
        getApplicationContext().getPackageName(),
        R.layout.activity_main);

    List<MonitoredStopVisit> stopVisitList = siriResponse
        .siri
        .serviceDelivery
        .stopMonitoringDeliveryConnection.get(0)
        .monitoredStopVisitConnection;

    // --- 0
    MonitoredVehicleJourney j0 = stopVisitList.get(0).monitoredVehicleJourney;
    views.setTextViewText(R.id.icon, j0.publishedLineName);
    views.setTextViewText(R.id.direction_text, j0.destinationName);
    views.setTextViewText(R.id.presentable_distance_text, buildPresentableDistance(j0));

    // ---- 1
    MonitoredVehicleJourney j1 = stopVisitList.get(1).monitoredVehicleJourney;
    views.setTextViewText(R.id.presentable_distance_text2, buildPresentableDistance(j1));

    return views;
  }

  public String buildPresentableDistance(MonitoredVehicleJourney j) {
    boolean hasDepartureTime =
        j.originAimedDepartureTime != null
        && j.originAimedDepartureTime.length() > 0;

    if (hasDepartureTime) {
      Date date = parseDate(j.originAimedDepartureTime);
      return "at terminal, depart at " + formatToDepartureTime(date);
    }
    return j.monitoredCall.extensions.distances.presentableDistance;
  }

  public Date parseDate(String text) {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    try {
      return format.parse(text);
    } catch (ParseException e) {
      return null;
    }
  }

  public String formatToDepartureTime(Date date) {
    SimpleDateFormat format = new SimpleDateFormat("HH:mm");
    return format.format(date);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  public static Intent prepareIntent(Context context, int[] widgetIds) {
    Intent intent = new Intent(context.getApplicationContext(), MapWidgetService.class);
    intent.putExtra(EXTRA_WIDGET_IDS, widgetIds);
    return intent;
  }

  public static void sendRequest(Context context, int[] widgetIds) {
    Intent intent = new Intent(context.getApplicationContext(), MapWidgetService.class);
    intent.putExtra(EXTRA_WIDGET_IDS, widgetIds);
    context.startService(intent);
  }
}
