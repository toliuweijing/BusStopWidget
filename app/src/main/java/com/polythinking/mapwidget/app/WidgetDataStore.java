package com.polythinking.mapwidget.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Used to read/write widget data with light-weight Apis.
 */
public class WidgetDataStore {
  private static final String TAG = WidgetDataStore.class.getName();

  private final SharedPreferences mSharedPreferences;
  private final ObjectMapper mObjectMapper;

  private WidgetDataStore(SharedPreferences sharedPreferences, ObjectMapper objectMapper) {
    mSharedPreferences = sharedPreferences;
    mObjectMapper = objectMapper;
  }

  public WidgetData get(int id) {
    String value = mSharedPreferences.getString(String.valueOf(id), null);
//    Log.d(TAG, value);
    if (value == null) {
      return null;
    }
    return deserilize(value, WidgetData.class);
  }

  public void set(WidgetData data) {
    String value = serialize(data);
    Log.d(TAG, value);
    if (value != null) {
      mSharedPreferences.edit().putString(
          String.valueOf(data.getWidgetId()),
          value)
          .apply();
    }
  }

  private String serialize(WidgetData data) {
    String value = null;

    try {
      value = mObjectMapper.writeValueAsString(data);
    } catch (JsonProcessingException e) {
      Log.w(TAG, "failed to serialize", e);
    }

    return value;
  }

  private <T> T deserilize(String value, Class<T> klass) {
    T model = null;

    try {
      model = mObjectMapper.readValue(value, klass);
    } catch (IOException e) {
      Log.w(TAG, "failed to deserialize", e);
    }

    return model;
  }

  public static abstract class Singleton {
    private static WidgetDataStore mInstance;
    public static WidgetDataStore getInstance(Context context) {
      if (mInstance == null) {
        SharedPreferences sharedPreferences =
            context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        mInstance = new WidgetDataStore(sharedPreferences, new ObjectMapper());
      }
      return mInstance;
    }
  }
}
