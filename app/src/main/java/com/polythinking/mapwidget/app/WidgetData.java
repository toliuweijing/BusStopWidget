package com.polythinking.mapwidget.app;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE
)
public class WidgetData {
  private int widgetId;

  private int stopCode;

  private String lineRef;

  private WidgetMode mode;

  // Needed for Jackson deserialization.
  public WidgetData() {
  }

  public WidgetData(
      int widgetId,
      int stopCode,
      String lineRef,
      WidgetMode mode) {
    this.widgetId = widgetId;
    this.stopCode = stopCode;
    this.lineRef = lineRef;
    this.mode = mode;
  }

  @Override
  public String toString() {
    return "" + widgetId + stopCode + lineRef;
  }

  public String getLineRef() {
    return lineRef;
  }

  public int getStopCode() {
    return stopCode;
  }

  public int getWidgetId() {
    return widgetId;
  }

  public WidgetMode getMode() {
    return mode;
  }

  public void setWidgetMode(WidgetDataStore dataStore, WidgetMode mode) {
    this.mode = mode;
    dataStore.set(this);
  }
}
