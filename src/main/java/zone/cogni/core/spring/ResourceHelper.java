package zone.cogni.core.spring;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;

public class ResourceHelper {

  public static Resource[] getResources(ResourcePatternResolver applicationContext, String locationPattern) {
    try {
      return applicationContext.getResources(locationPattern);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

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
