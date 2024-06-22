package zone.cogni.semanticz.irigenerator.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import zone.cogni.semanticz.irigenerator.Vocabulary;

@Data
@Accessors(chain = true)
@OWLClass(iri = Vocabulary.C_IRI_GENERATOR)
public class IriGenerator {

  @Id
  private String id;

  @OWLDataProperty(iri = Vocabulary.P_IRI_SELECTOR)
  // TODO for legacy compatibility with the old JSON5 format
  @JsonProperty("uriSelector")
  private String iriSelector;

  @OWLDataProperty(iri = Vocabulary.P_VARIABLE_SELECTOR)
  private String variableSelector;

  @OWLDataProperty(iri = Vocabulary.P_IRI_TEMPLATE)
  // TODO for legacy compatibility with the old JSON5 format
  @JsonProperty("uriTemplate")
  private String iriTemplate;
}
