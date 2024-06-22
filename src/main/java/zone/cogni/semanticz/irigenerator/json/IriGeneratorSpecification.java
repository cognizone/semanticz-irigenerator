package zone.cogni.semanticz.irigenerator.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import zone.cogni.semanticz.irigenerator.PrefixDeserializer;
import zone.cogni.semanticz.irigenerator.Vocabulary;

import java.net.URI;
import java.util.List;

@Data
@Accessors(chain = true)
@OWLClass(iri = Vocabulary.C_IRI_GENERATOR_SPECIFICATION)
public class IriGeneratorSpecification {

  @Id
  private URI id;

  @OWLObjectProperty(iri = Vocabulary.P_PREFIX)
  @JsonDeserialize(using = PrefixDeserializer.class)
  private List<Prefix> prefixes;

  @OWLObjectProperty(iri = Vocabulary.P_GENERATOR)
  private List<IriGenerator> generators;
}
