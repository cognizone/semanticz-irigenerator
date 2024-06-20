package zone.cogni.asquare.cube.urigenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import zone.cogni.asquare.cube.json5.Json5Light;
import zone.cogni.asquare.cube.urigenerator.json.UriGeneratorRoot;

import java.io.IOException;
import java.net.URL;

public class Utils {
  public static UriGeneratorRoot load(URL resource, Format format) {
    try {
      ObjectMapper mapper = format.equals(Format.JSON5) ? Json5Light.getJson5Mapper() : JsonLdObjectMapperFactory.getJsonLdMapper();
      return mapper.readValue(resource.openStream(), UriGeneratorRoot.class);
    } catch (IOException e) {
      throw new RuntimeException("Unable to load uri generator configuration." + resource, e);
    }
  }
}