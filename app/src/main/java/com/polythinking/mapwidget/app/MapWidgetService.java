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

    MonitoredVehicleJourney vehicleJourney = stopVisitList.get(0).monitoredVehicleJourney;
    views.setTextViewText(R.id.icon, vehicleJourney.publishedLineName);
    views.setTextViewText(R.id.direction_text, vehicleJourney.destinationName);
    views.setTextViewText(
        R.id.presentable_distance_text,
        vehicleJourney.monitoredCall.extensions.distances.presentableDistance);

    return views;
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
