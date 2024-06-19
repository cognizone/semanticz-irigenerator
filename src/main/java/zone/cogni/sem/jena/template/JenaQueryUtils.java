package zone.cogni.sem.jena.template;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;

import java.util.*;


public abstract class JenaQueryUtils {

  public static List<Map<String, RDFNode>> convertToListOfMaps(ResultSet resultSet) {
    final List<Map<String, RDFNode>> result = new ArrayList<>();

    while (resultSet.hasNext()) {
      final QuerySolution querySolution = resultSet.next();
      final List<String> resultVars = resultSet.getResultVars();
      final Map<String, RDFNode> result1 = new HashMap<>();
      resultVars.forEach(var -> result1.put(var, querySolution.get(var)));
      result.add(result1);
    }

    return result;
  }
}
