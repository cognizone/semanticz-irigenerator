package zone.cogni.asquare.triplestore.jenamemory;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.ResourcePatternResolver;
import zone.cogni.asquare.triplestore.RdfStoreService;
import zone.cogni.core.spring.ResourceHelper;
import zone.cogni.sem.jena.JenaUtils;
import zone.cogni.sem.jena.template.JenaResultSetHandler;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Supplier;

public class InternalRdfStoreService implements RdfStoreService {

  private static final Logger log = LoggerFactory.getLogger(InternalRdfStoreService.class);

  private final Model model;

  private ResourcePatternResolver resourcePatternResolver;
  private String preLoadLocations;
  private String savePath;

  private File storeFile;
  private File tempStoreFile;

  public InternalRdfStoreService(Model model) {
    this.model = model;
  }

  @PostConstruct
  private void init() {
    if (StringUtils.isNotBlank(savePath)) {
      storeFile = new File(savePath, "store.rdf");
      tempStoreFile = new File(savePath, "temp-store.rdf");
      storeFile.getParentFile().mkdirs();

      if (storeFile.isFile()) JenaUtils.readInto(storeFile, model);
    }

    if (resourcePatternResolver == null || StringUtils.isBlank(preLoadLocations)) return;

    Arrays.stream(StringUtils.split(preLoadLocations, ',')).forEach(location -> {
      log.info("Loading RDF file {}.", location);
      Arrays.stream(ResourceHelper.getResources(resourcePatternResolver, location)).forEach(resource -> {
        try (InputStream inputStream = resource.getInputStream()) {
          model.read(inputStream, null, JenaUtils.getLangByResourceName(location));
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    });
  }

  @Override
  public <R> R executeSelectQuery(Query query, QuerySolutionMap bindings, JenaResultSetHandler<R> resultSetHandler, String context) {
    return executeInLock(Lock.READ, () -> {
      if (log.isTraceEnabled()) log.trace("Select {} - {} \n{}",
                                          context == null ? "" : "--- " + context + " --- ",
                                          bindings,
                                          query);

      try (QueryExecution queryExecution = QueryExecutionFactory.create(query, model, bindings)) {
        ResultSet resultSet = queryExecution.execSelect();
        return resultSetHandler.handle(resultSet);
      }
      catch (RuntimeException e) {
        log.error("Query failed: {}", query);
        throw e;
      }
    });
  }

  @Override
  public boolean executeAskQuery(Query query, QuerySolutionMap bindings) {
    return executeInLock(Lock.READ, () -> {
      try (QueryExecution queryExecution = QueryExecutionFactory.create(query, model, bindings)) {
        return queryExecution.execAsk();
      }
      catch (RuntimeException e) {
        log.error("Query failed: {}", query);
        throw e;
      }
    });
  }

  @Override
  public Model executeConstructQuery(Query query, QuerySolutionMap bindings) {
    return executeInLock(Lock.READ, () -> {
      try (QueryExecution queryExecution = QueryExecutionFactory.create(query, model, bindings)) {
        if (log.isTraceEnabled()) log.trace("Running construct query: \n{}", query);
        return queryExecution.execConstruct();
      }
      catch (RuntimeException e) {
        log.error("Query failed: {}", query);
        throw e;
      }
    });
  }

  private <T> T executeInLock(boolean lock, Supplier<T> executeInLock) {
    model.enterCriticalSection(lock);
    try {
      return executeInLock.get();
    }
    finally {
      model.leaveCriticalSection();
    }
  }

  public Model getModel() {
    return model;
  }

}
