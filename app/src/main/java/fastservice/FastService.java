package fastservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public abstract class FastService extends Service {
  private static final String EXTRA_HANDLER_CLASS_NAME = "extra_handler_class_name";

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    dispatch(intent);
    return START_NOT_STICKY;
  }

  private void dispatch(Intent intent) {
    List<FastHandler> handlers = getHandlers();
    String handlerName = intent.getStringExtra(EXTRA_HANDLER_CLASS_NAME);
    for (FastHandler handler : handlers) {
      if (handler.getClass().getName().equals(handlerName)) {
        handler.onDispatch(this, intent);
      }
    }
  }

  protected abstract List<FastHandler> getHandlers();

  public static Intent createIntent(
      Context context,
      Class<? extends  FastService> serviceClass,
      Class<? extends  FastHandler> handlerClass) {
    Intent intent = new Intent(context.getApplicationContext(), serviceClass);
    intent.putExtra(EXTRA_HANDLER_CLASS_NAME, handlerClass.getName());
    return intent;
  }
}
