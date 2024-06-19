package zone.cogni.asquare.triplestore;


import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import zone.cogni.sem.jena.template.JenaResultSetHandler;

public interface RdfStoreServiceAPI {

  <R> R executeSelectQuery(String query, JenaResultSetHandler<R> resultSetHandler);

  boolean executeAskQuery(Query query, QuerySolutionMap bindings);

  Model executeConstructQuery(String query);
}