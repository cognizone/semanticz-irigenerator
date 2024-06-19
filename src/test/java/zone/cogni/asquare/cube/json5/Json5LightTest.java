package zone.cogni.asquare.cube.json5;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Json5LightTest {

  @Test
  public void read_json() throws IOException {
    // given
    URL resource = getClass().getResource("/json5/read_json.json5");
    ObjectMapper json5Mapper = Json5Light.getJson5Mapper();

    // when
    JsonNode root = json5Mapper.readTree(Objects.requireNonNull(resource).openStream());

    // then
    assertTrue(root.has("id"));
    assertTrue(root.has("someNumber"));
    assertTrue(root.has("multiLine"));
  }
}