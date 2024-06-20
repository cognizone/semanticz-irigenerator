package zone.cogni.asquare.cube.urigenerator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.jackson.JsonLdModule;
import cz.cvut.kbss.jsonld.jackson.serialization.SerializationConstants;

import static cz.cvut.kbss.jsonld.ConfigParam.ASSUME_TARGET_TYPE;

public class JsonLdObjectMapperFactory {

  public static ObjectMapper getJsonLdMapper() {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    final JsonLdModule module = new JsonLdModule();
    module.configure(ASSUME_TARGET_TYPE, Boolean.TRUE.toString());
    module.configure(ConfigParam.SCAN_PACKAGE, "zone.cogni.asquare.cube.urigenerator.json");
    module.configure(SerializationConstants.FORM, SerializationConstants.FORM_COMPACT_WITH_CONTEXT);
    objectMapper.registerModule(module);
    return objectMapper;
  }
}