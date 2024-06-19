package zone.cogni.sem.jena.template;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;

import java.util.*;


public abstract class JenaQueryUtils {

  public static List<Map<String, RDFNode>> convertToListOfMaps(ResultSet resultSet) {
    List<Map<String, RDFNode>> result = new ArrayList<>();


    while (resultSet.hasNext()) {
      QuerySolution querySolution = resultSet.next();
      result.add(convertRowToMap(resultSet.getResultVars(), querySolution));
    }

    return result;
  }

  // note: please keep resultVars !!
  private static Map<String, RDFNode> convertRowToMap(List<String> resultVars, QuerySolution querySolution) {
    Map<String, RDFNode> result = new HashMap<>();
    resultVars.forEach(var -> result.put(var, querySolution.get(var)));

    return result;
  }

  @Deprecated
  public static Map<String, RDFNode> convertRowToMap(QuerySolution querySolution) {
    Map<String, RDFNode> result = new HashMap<>();

    Iterator varNames = querySolution.varNames();
    while (varNames.hasNext()) {
      String varName = (String) varNames.next();
      result.put(varName, querySolution.get(varName));
    }

    return result;
  }
}
