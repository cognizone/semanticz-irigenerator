package zone.cogni.sem.jena.template;


import org.apache.jena.rdf.model.RDFNode;

import java.util.List;
import java.util.Map;

public final class JenaResultSetHandlers {

  public static final JenaResultSetHandler<List<Map<String, RDFNode>>> listOfMapsResolver = JenaQueryUtils::convertToListOfMaps;

  private JenaResultSetHandlers() {
    throw new AssertionError("Should not be initialized!");
  }
}
