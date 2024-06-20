package zone.cogni.asquare.cube.urigenerator;

import com.google.common.collect.ImmutableMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import zone.cogni.asquare.cube.spel.SpelService;

import java.net.URL;

public class UriGeneratorCalculatorTest {


  @Test
  public void testURIConvertedForJsonLD11() {
    testURIConvertedForSyntax("/urigenerator/uri-generators.json", Format.JSONLD11);
  }

  @Test
  public void testURIConvertedForJson5() {
    testURIConvertedForSyntax("/urigenerator/uri-generators.json5", Format.JSON5);
  }

  private void testURIConvertedForSyntax(String generatorsResource, Format format) {
    final URL uriGeneratorsResource = getClass().getResource(generatorsResource);

    final UriGeneratorCalculator sut = new UriGeneratorCalculator("http://resource",
            new SpelService(),
            uriGeneratorsResource,
            format);

    final URL modelUrl = getClass().getResource("/urigenerator/model.ttl");
    assert modelUrl != null;

    //given
    final Model model = ModelFactory.createDefaultModel();
    RDFDataMgr.read(model, modelUrl.toString());
    //when
    final Model converted = sut.convert(model, ImmutableMap.of("baseUri", "http://asquare.cogni.zone"));
    //then
    Assertions.assertTrue(converted.containsResource(ResourceFactory.createResource("http://asquare.cogni.zone/5")));
    Assertions.assertTrue(converted.containsResource(ResourceFactory.createResource("http://asquare.cogni.zone/2021/0005")));
  }
}
