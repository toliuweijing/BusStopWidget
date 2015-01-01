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
import com.android.volley.toolbox.Volley;
import model.SiriResponse;
import model.SiriResponse.Siri.ServiceDelivery.StopMonitoringDelivery.MonitoredStopVisit;
import model.SiriResponse.Siri.ServiceDelivery.StopMonitoringDelivery.MonitoredStopVisit.MonitoredVehicleJourney;
import network.RestApis;
import org.apache.http.concurrent.FutureCallback;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MapWidgetUpdateService extends Service {
  private static final String TAG = MapWidgetUpdateService.class.getName();

  public static final String EXTRA_WIDGET_IDS = "extra_widget_ids";

  public static final String EXTRA_USER_ACTION = "extra_action";
  public static final int USER_ACTION_POWER_BUTTON_CLICKED = 0;

  private RequestQueue mRequestQueue;

  private static boolean mIsPowerOn = true;

  @Override
  public void onCreate() {
    super.onCreate();

    mRequestQueue = Volley.newRequestQueue(this);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i(TAG, "onStartCommend");

    final RemoteViews remoteViews = new RemoteViews(
        getApplicationContext().getPackageName(),
        R.layout.activity_main);

    updatePowerButtonIfNeeded(remoteViews, intent);

    Context context = getApplicationContext();
    final AppWidgetManager appWidgetManager = AppWidgetManager
        .getInstance(context);
    final int[] appWidgetIds = intent.getIntArrayExtra(EXTRA_WIDGET_IDS);

    if (mIsPowerOn) {
      Log.d(TAG, "Power is on. Start fetching stop visits");
      fetchStopVisits(
          RestApis.SAMPLE_STOP_CODE,
          RestApis.SAMPLE_LINE_REF,
          new FutureCallback<SiriResponse>() {
            @Override
            public void completed(SiriResponse result) {
              Log.d(TAG, "received SiriResponse");
              updateRemoteViews(remoteViews, result);
              appWidgetManager.updateAppWidget(appWidgetIds[0], remoteViews);
            }

            @Override
            public void failed(Exception ex) {
              // no-op
            }

            @Override
            public void cancelled() {
              // no-op
            }
          });
    }

    appWidgetManager.updateAppWidget(appWidgetIds[0], remoteViews);
    return START_NOT_STICKY;
  }

  public void fetchStopVisits(int stopCode, String lineRef, final FutureCallback<SiriResponse> callback) {
    String url = RestApis.Siri.stopMonitoring(
        stopCode,
        lineRef)
        .toString();

    JsonObjectRequest request = new JsonObjectRequest(
        url,
        null,
        new Response.Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {
            try {
              SiriResponse siriResponse = SiriResponse.read(response);
              callback.completed(siriResponse);
            } catch (IOException e) {
              Log.w(TAG, "failed to parse response", e);
              callback.failed(e);
            }
          }},
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.w(TAG, "failed to retrieve response", error);
            callback.failed(error);
          }
        });
    mRequestQueue.add(request);
  }


  private void updatePowerButtonIfNeeded(RemoteViews views, Intent intent) {
    if (intent.hasExtra(EXTRA_USER_ACTION)) {
      int action = intent.getIntExtra(EXTRA_USER_ACTION, -1);
      if (action == USER_ACTION_POWER_BUTTON_CLICKED) {
        mIsPowerOn = !mIsPowerOn;
        if (mIsPowerOn) {
          views.setImageViewResource(R.id.power_button, android.R.drawable.ic_lock_power_off);
        } else {
          views.setImageViewResource(R.id.power_button, android.R.drawable.ic_lock_lock);
        }
      }
    }
  }

  public void updateRemoteViews(RemoteViews remoteViews, SiriResponse siriResponse) {
    List<MonitoredStopVisit> stopVisitList = siriResponse
        .siri
        .serviceDelivery
        .stopMonitoringDeliveryConnection.get(0)
        .monitoredStopVisitConnection;

    // --- 0
    MonitoredVehicleJourney j0 = stopVisitList.get(0).monitoredVehicleJourney;
    remoteViews.setTextViewText(R.id.icon, j0.publishedLineName);
    remoteViews.setTextViewText(R.id.direction_text, j0.destinationName);
    remoteViews.setTextViewText(R.id.presentable_distance_text, buildPresentableDistance(j0));

    // ---- 1
    if (stopVisitList.size() >= 2) {
      MonitoredVehicleJourney j1 = stopVisitList.get(1).monitoredVehicleJourney;
      remoteViews.setTextViewText(R.id.presentable_distance_text2, buildPresentableDistance(j1));
    }
  }

  public String buildPresentableDistance(MonitoredVehicleJourney j) {
    boolean hasDepartureTime =
        j.originAimedDepartureTime != null
        && j.originAimedDepartureTime.length() > 0;

    String presentableDistance = j.monitoredCall.extensions.distances.presentableDistance;
    if (hasDepartureTime) {
      Date date = parseDate(j.originAimedDepartureTime);
      long departureIntervalMin = formatToRelateiveOffsetInMinutes(date);
      if (departureIntervalMin < 1) {
        return presentableDistance + ", departing now";
      } else {
        return presentableDistance
            + ", depart in "
            + String.valueOf(formatToRelateiveOffsetInMinutes(date))
            + " mins";
      }
    }
    return presentableDistance;
  }

  public Date parseDate(String text) {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    try {
      return format.parse(text);
    } catch (ParseException e) {
      return null;
    }
  }

  public long formatToRelateiveOffsetInMinutes(Date date) {
    long milliseconds = date.getTime() - System.currentTimeMillis();
    return TimeUnit.MILLISECONDS.toMinutes(milliseconds);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  public static Intent prepareIntent(Context context, int[] widgetIds) {
    Intent intent = new Intent(context.getApplicationContext(), MapWidgetUpdateService.class);
    intent.putExtra(EXTRA_WIDGET_IDS, widgetIds);
    return intent;
  }

  public static Intent prepareIntent(Context context, int[] widgetIds, int userAction) {
    Intent intent = new Intent(context.getApplicationContext(), MapWidgetUpdateService.class);
    intent.putExtra(EXTRA_WIDGET_IDS, widgetIds);
    intent.putExtra(EXTRA_USER_ACTION, userAction);
    return intent;
  }
}
