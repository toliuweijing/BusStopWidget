package com.polythinking.mapwidget.app;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public enum WidgetMode {
  LOCKED, POWER_ON, ALERM;

  private static final Map<WidgetMode, Integer> drawableMap =
      Maps.newHashMap(ImmutableMap.of(
          LOCKED,
          android.R.drawable.ic_lock_lock,
          POWER_ON,
          android.R.drawable.ic_lock_power_off,
          ALERM,
          android.R.drawable.ic_lock_silent_mode_off));

  public WidgetMode nextMode() {
    List<WidgetMode> buttons = Lists.newArrayList(values());
    int nextIndex = (buttons.indexOf(this) + 1) % buttons.size();
    return buttons.get(nextIndex);
  }

  public int getDrawableId() {
    return drawableMap.get(this);
  }
}
