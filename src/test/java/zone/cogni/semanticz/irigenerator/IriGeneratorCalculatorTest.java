package zone.cogni.semanticz.irigenerator;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import zone.cogni.asquare.cube.spel.SpelService;

import java.net.URL;

@Slf4j
public class IriGeneratorCalculatorTest {

  private static final String BASE_URI = "https://example.cogni.zone";

  @ParameterizedTest
  @ValueSource(strings = {
          "/irigenerator/test-case-0",
          "/irigenerator/test-case-1"
  })
  public void testURIConvertedForJsonLD11(final String resourceFolder) {
    testURIConvertedForSyntax(resourceFolder + "/uri-generators.json", Format.JSONLD11, resourceFolder + "/original-model.ttl", resourceFolder + "/expected-model.ttl");
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "/irigenerator/test-case-0",
          "/irigenerator/test-case-1"
  })
  public void testURIConvertedForJson5(final String resourceFolder) {
    testURIConvertedForSyntax(resourceFolder + "/uri-generators.json5", Format.JSON5, resourceFolder + "/original-model.ttl", resourceFolder + "/expected-model.ttl");
  }

  private void testURIConvertedForSyntax(String generatorsResource, Format format, String originalModelResource, String expectedModelResource) {
    final URL uriGeneratorsResource = getClass().getResource(generatorsResource);

    final IriGeneratorCalculator sut = new IriGeneratorCalculator("https://resource",
            new SpelService(),
            uriGeneratorsResource,
            format);

    final Model originalModel = load(originalModelResource);
    final Model expectedModel = load(expectedModelResource);

    final Model convertedModel = sut.convert(originalModel, ImmutableMap.of("baseUri", BASE_URI));

    final boolean isIsomorphic = expectedModel.isIsomorphicWith(convertedModel);

    if (!isIsomorphic) {
      log.error("Models are not isomorphic:");
      log.error("- expected \\ converted:");
      expectedModel.difference(convertedModel).write(System.out, "TTL");
      log.error("- converted \\ expected:");
      convertedModel.difference(expectedModel).write(System.out, "TTL");
    }

    Assertions.assertTrue(isIsomorphic);
  }

  private Model load(final String resource) {
    final URL url = getClass().getResource(resource);
    assert url != null;
    final Model model = ModelFactory.createDefaultModel();
    RDFDataMgr.read(model, url.toString());
    return model;
  }
}
