package zone.cogni.asquare.cube.urigenerator.json;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import zone.cogni.asquare.cube.urigenerator.Vocabulary;

import java.net.URI;

@Data
@Accessors(chain = true)
@OWLClass(iri = Vocabulary.C_PREFIX)
public class Prefix {

  @Id
  private URI id;

  @OWLDataProperty(iri = Vocabulary.P_PREFIX_NAME)
  private String key;

  @OWLDataProperty(iri = Vocabulary.P_NAMESPACE)
  private String value;
}
