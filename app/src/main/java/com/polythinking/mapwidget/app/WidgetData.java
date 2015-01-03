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

  public WidgetData() {
  }

  public WidgetData(int widgetId, int stopCode, String lineRef) {
    this.widgetId = widgetId;
    this.stopCode = stopCode;
    this.lineRef = lineRef;
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

  @Override
  public String toString() {
    return "" + widgetId + stopCode + lineRef;
  }
}
