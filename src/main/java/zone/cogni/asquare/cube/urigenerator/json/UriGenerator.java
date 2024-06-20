package zone.cogni.asquare.cube.urigenerator.json;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import lombok.Data;
import zone.cogni.asquare.cube.urigenerator.Vocabulary;

@Data
@OWLClass(iri = Vocabulary.C_IRI_GENERATOR)
public class UriGenerator {

  @Id
  private String id;

  @OWLDataProperty(iri = Vocabulary.P_IRI_SELECTOR)
  private String uriSelector;

  @OWLDataProperty(iri = Vocabulary.P_VARIABLE_SELECTOR)
  private String variableSelector;

  @OWLDataProperty(iri = Vocabulary.P_IRI_TEMPLATE)
  private String uriTemplate;

  public String getFullUriSelector() {
    if (uriSelector == null) return "";
    return uriSelector;
  }

  public String getFullVariableSelector() {
    if (variableSelector == null) return "";
    return variableSelector;
  }
}
