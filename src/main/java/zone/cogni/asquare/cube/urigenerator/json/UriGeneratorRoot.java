package zone.cogni.asquare.cube.urigenerator.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import zone.cogni.asquare.cube.json5.Json5Light;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UriGeneratorRoot {

  public static UriGeneratorRoot load(URL resource) {
    try {
      ObjectMapper mapper = Json5Light.getJson5Mapper();
      return mapper.readValue(resource.openStream(), UriGeneratorRoot.class);
    } catch (IOException e) {
      String extra;
      try {
        extra = " URL " + resource.toURI();
      } catch (URISyntaxException ex) {
        throw new RuntimeException(ex);
      }
      throw new RuntimeException("Unable to load uri generator configuration." + extra, e);
    }
  }

  private Map<String, String> prefixes;
  private List<UriGenerator> generators;

  public UriGeneratorRoot() {
  }

  public Map<String, String> getPrefixes() {
    return prefixes;
  }

  public void setPrefixes(Map<String, String> prefixes) {
    this.prefixes = prefixes;
  }

  public List<UriGenerator> getGenerators() {
    return generators;
  }

  public void setGenerators(List<UriGenerator> generators) {
    this.generators = generators;
  }

  public String getPrefixQuery() {
    return prefixes.entrySet()
            .stream()
            .map(e -> "PREFIX "
                    + StringUtils.rightPad(e.getKey() + ":", 8) + " <" + e.getValue() + ">\n")
            .collect(Collectors.joining())
            + "\n";
  }

}
