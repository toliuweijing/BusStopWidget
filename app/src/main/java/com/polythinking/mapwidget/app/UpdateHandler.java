package com.polythinking.mapwidget.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import fastservice.FastHandler;
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

/**
 * Created by developer on 1/18/15.
 */
public class UpdateHandler extends FastHandler {
  private static final String TAG = UpdateHandler.class.getName();

  private static final String EXTRA_WIDGET_IDS = "extra_widget_ids";

  private final RequestQueue mRequestQueue;
  private final AppWidgetManager mAppWidgetManager;
  private final WidgetDataStore mWidgetDataStore;
  private final RemoteViews mRemoteViews;

  public UpdateHandler(Context context) {
    super(context);

    mRequestQueue = Volley.newRequestQueue(context);
    mAppWidgetManager = AppWidgetManager.getInstance(context);
    mWidgetDataStore = WidgetDataStore.Singleton.getInstance(context);
    mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.activity_main);
  }

  @Override
  public void onDispatch(Intent intent) {
    //TODO: Checkin power button state
    int[] widgetIds = intent.getIntArrayExtra(EXTRA_WIDGET_IDS);

    for (int id : widgetIds) {
      WidgetData data = mWidgetDataStore.get(id);

      if (data == null || data.getMode() == WidgetMode.LOCKED) {
        Log.d(TAG, "widget is disabled or removed, id:" + id);
        continue;
      } else {
        onUpdateWidget(data);
      }
    }
  }

  public static Intent prepareIntent(Context context, int[] widgetIds) {
    Intent intent =  MapWidgetUpdateService.createIntent(context, MapWidgetUpdateService.class, UpdateHandler.class);
    intent.putExtra(EXTRA_WIDGET_IDS, widgetIds);
    return intent;
  }

  public void updateRemoteViews(RemoteViews remoteViews, SiriResponse siriResponse) {
    List<MonitoredStopVisit> stopVisitList = siriResponse
        .siri
        .serviceDelivery
        .stopMonitoringDeliveryConnection.get(0)
        .monitoredStopVisitConnection;

    // --- 0
    if (stopVisitList.isEmpty()) {
      return;
    }
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

  private void onUpdateWidget(final WidgetData widgetData) {
    Log.d(TAG, "Power is on. Start fetching stop visits");
    fetchStopVisits(
        widgetData.getStopCode(),
        widgetData.getLineRef(),
        new FutureCallback<SiriResponse>() {
          @Override
          public void completed(SiriResponse result) {
            Log.d(TAG, "received SiriResponse");
            updateRemoteViews(mRemoteViews, result);
            mAppWidgetManager.updateAppWidget(
                widgetData.getWidgetId(),
                mRemoteViews);
            sendNotificationIfNeeded(widgetData, result);
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

  public void fetchStopVisits(int stopCode, String lineRef, final FutureCallback<SiriResponse> callback) {
    String url = RestApis.Siri.stopMonitoring(
        stopCode,
        lineRef)
        .toString();

    Log.d(TAG, "url=" + url);

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

  public void sendNotificationIfNeeded(WidgetData data, SiriResponse response) {
    if (data.getMode() != WidgetMode.ALERM) {
      return;
    }

    if (response.siri.serviceDelivery.stopMonitoringDeliveryConnection.get(0).monitoredStopVisitConnection.isEmpty()) {
      return;
    }

    if (response
        .siri
        .serviceDelivery
        .stopMonitoringDeliveryConnection.get(0)
        .monitoredStopVisitConnection
        .get(0)
        .monitoredVehicleJourney
        .monitoredCall
        .extensions.distances.stopsFromCall <= 2) {

      Notification noti = new Notification.Builder(mContext.getApplicationContext())
          .setDefaults(Notification.DEFAULT_ALL)
          .build();
      NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
      manager.notify(1, noti);

      CharSequence text = "A B9 bus is approaching";
      int duration = Toast.LENGTH_SHORT;

      Toast toast = Toast.makeText(mContext, text, duration);
      toast.show();
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

}
