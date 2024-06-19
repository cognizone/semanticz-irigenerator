package zone.cogni.core.spring;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamSource;

import java.io.IOException;
import java.io.InputStream;

public class ResourceHelper {

  public static String toString(InputStreamSource resource) {
    return toString(resource, "UTF-8");
  }

  public static String toString(InputStreamSource resource, String encoding) {
    try (InputStream inputStream = resource.getInputStream()) {
      return IOUtils.toString(inputStream, encoding);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T extends InputStreamSource> InputStream getInputStream(T resource) {
    try {
      return resource.getInputStream();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private ResourceHelper() {
  }
}
