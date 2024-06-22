package zone.cogni.asquare.cube.urigenerator.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import zone.cogni.asquare.cube.urigenerator.PrefixDeserializer;
import zone.cogni.asquare.cube.urigenerator.Vocabulary;

import java.net.URI;
import java.util.List;

@Data
@Accessors(chain = true)
@OWLClass(iri = Vocabulary.C_GENERATOR_SPECIFICATION)
public class UriGeneratorRoot {

  @Id
  private URI id;

  @OWLObjectProperty(iri = Vocabulary.P_PREFIX)
  @JsonDeserialize(using = PrefixDeserializer.class)
  private List<Prefix> prefixes;

  @OWLObjectProperty(iri = Vocabulary.P_GENERATOR)
  private List<UriGenerator> generators;
}
