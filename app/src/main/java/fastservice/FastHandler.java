package fastservice;

import android.content.Context;
import android.content.Intent;

public abstract class FastHandler {
  protected final Context mContext;

  protected FastHandler(Context context) {
    mContext = context;
  }

  public abstract void onDispatch(Intent intent);
}
