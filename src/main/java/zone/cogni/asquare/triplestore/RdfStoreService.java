package zone.cogni.asquare.triplestore;


import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.cogni.sem.jena.template.JenaResultSetHandler;

import java.io.Closeable;

public interface RdfStoreService extends Closeable {

  Logger log = LoggerFactory.getLogger(RdfStoreService.class);

  @Override
  default void close() {
    log.info("Closing RdfStoreService ({}) : {}", getClass().getName(), this);
  }

  <R> R executeSelectQuery(Query query, QuerySolutionMap bindings, JenaResultSetHandler<R> resultSetHandler, String context);

  default <R> R executeSelectQuery(String query, JenaResultSetHandler<R> resultSetHandler, String context) {
    Query parsedQuery = QueryFactory.create(query, Syntax.syntaxARQ);
    return executeSelectQuery(parsedQuery, new QuerySolutionMap(), resultSetHandler, context);
  }

  default <R> R executeSelectQuery(String query, JenaResultSetHandler<R> resultSetHandler) {
    return executeSelectQuery(query, resultSetHandler, null);
  }

  boolean executeAskQuery(Query query, QuerySolutionMap bindings);

  Model executeConstructQuery(Query query, QuerySolutionMap bindings);

  default Model executeConstructQuery(String query) {
    Query parsedQuery = QueryFactory.create(query, Syntax.syntaxARQ);
    return executeConstructQuery(parsedQuery, new QuerySolutionMap());
  }
}