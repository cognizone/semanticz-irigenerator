package zone.cogni.asquare.triplestore;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.cogni.sem.jena.template.JenaResultSetHandler;

import java.io.Closeable;
import java.util.function.Supplier;

public class InternalRdfStoreService implements RdfStoreServiceAPI, Closeable {

  private static final Logger log = LoggerFactory.getLogger(InternalRdfStoreService.class);

  private final Model model;

  public InternalRdfStoreService(Model model) {
    this.model = model;
  }

  @Override
  public boolean executeAskQuery(Query query, QuerySolutionMap bindings) {
    return executeInLock(() -> {
      try (QueryExecution queryExecution = QueryExecution.create()
              .query(query)
              .model(model)
              .initialBinding(bindings)
              .build()) {
        return queryExecution.execAsk();
      } catch (RuntimeException e) {
        log.error("Query failed: {}", query);
        throw e;
      }
    });
  }

  public <R> R executeSelectQuery(String query, JenaResultSetHandler<R> resultSetHandler) {
    Query parsedQuery = QueryFactory.create(query, Syntax.syntaxARQ);
    QuerySolutionMap bindings = new QuerySolutionMap();
    return executeInLock(() -> {
      if (log.isTraceEnabled()) log.trace("Select {} \n{}",
              bindings,
              parsedQuery);

      try (QueryExecution queryExecution = QueryExecution.create()
              .query(parsedQuery)
              .model(model)
              .initialBinding(bindings)
              .build()) {
        ResultSet resultSet = queryExecution.execSelect();
        return resultSetHandler.handle(resultSet);
      } catch (RuntimeException e) {
        log.error("Query failed: {}", parsedQuery);
        throw e;
      }
    });
  }

  public Model executeConstructQuery(String query) {
    Query parsedQuery = QueryFactory.create(query, Syntax.syntaxARQ);
    return executeInLock(() -> {

      try (QueryExecution queryExecution = QueryExecution.create()
              .query(parsedQuery)
              .model(model)
              .build()
      ) {
        if (log.isTraceEnabled()) log.trace("Running construct query: \n{}", parsedQuery);
        return queryExecution.execConstruct();
      } catch (RuntimeException e) {
        log.error("Query failed: {}", parsedQuery);
        throw e;
      }
    });
  }

  @Override
  public void close() {
    log.info("Closing RdfStoreService ({}) : {}", getClass().getName(), this);
  }

  private <T> T executeInLock(Supplier<T> executeInLock) {
    model.enterCriticalSection(Lock.READ);
    try {
      return executeInLock.get();
    } finally {
      model.leaveCriticalSection();
    }
  }
}
