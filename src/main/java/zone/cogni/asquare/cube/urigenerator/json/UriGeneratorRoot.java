package zone.cogni.asquare.cube.urigenerator.json;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import zone.cogni.asquare.cube.urigenerator.Vocabulary;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Data
@OWLClass(iri = Vocabulary.C_GENERATOR_SPECIFICATION)
public class UriGeneratorRoot {

  @Id
  private URI id;

  @OWLObjectProperty(iri = Vocabulary.P_PREFIX)
  private List<Prefix> prefixes;
//  private Map<String, String> prefixes;

  @OWLObjectProperty(iri = Vocabulary.P_GENERATOR)
  private List<UriGenerator> generators;

  public String getPrefixQuery() {
    return prefixes
//            .entrySet()
            .stream()
            .map(e -> "PREFIX "
                    + StringUtils.rightPad(e.getKey() + ":", 8) + " <" + e.getValue() + ">\n")
            .collect(Collectors.joining())
            + "\n";
  }
}
