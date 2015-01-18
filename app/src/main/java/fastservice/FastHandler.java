package fastservice;

import android.content.Context;
import android.content.Intent;

public abstract class FastHandler {
  public abstract void onDispatch(Context context, Intent intent);
}
