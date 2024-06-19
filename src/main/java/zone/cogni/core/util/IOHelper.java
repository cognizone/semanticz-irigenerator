package zone.cogni.core.util;


import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

public class IOHelper {
  public static <X extends Flushable & Closeable> void flushAndClose(X closeFlusher) {
    if (closeFlusher == null) return;

    try {
      closeFlusher.flush();
    }
    catch (IOException ignore) {
    }

    try {
      closeFlusher.close();
    }
    catch (IOException ignore) {
    }
  }
}
