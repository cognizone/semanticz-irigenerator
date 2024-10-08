/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package zone.cogni.asquare.triplestore;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.Lock;
import zone.cogni.sem.jena.template.JenaResultSetHandler;

import java.io.Closeable;
import java.util.function.Supplier;

@Slf4j
public class InternalRdfStoreService implements RdfStoreServiceAPI, Closeable {

  private final Model model;

  public InternalRdfStoreService(Model model) {
    this.model = model;
  }

  @Override
  public boolean executeAskQuery(Query query, QuerySolutionMap bindings) {
    trace(query);
    return executeInLock(() -> {
      try (QueryExecution queryExecution = QueryExecution.create()
              .query(query)
              .model(model)
              .initialBinding(bindings)
              .build()) {
        return queryExecution.execAsk();
      } catch (RuntimeException e) {
        error(query);
        throw e;
      }
    });
  }

  public <R> R executeSelectQuery(String query, JenaResultSetHandler<R> resultSetHandler) {
    Query parsedQuery = QueryFactory.create(query, Syntax.syntaxARQ);
    trace(parsedQuery);
    QuerySolutionMap bindings = new QuerySolutionMap();
    return executeInLock(() -> {
      try (QueryExecution queryExecution = QueryExecution.create()
              .query(parsedQuery)
              .model(model)
              .initialBinding(bindings)
              .build()) {
        ResultSet resultSet = queryExecution.execSelect();
        return resultSetHandler.handle(resultSet);
      } catch (RuntimeException e) {
        error(parsedQuery);
        throw e;
      }
    });
  }

  public Model executeConstructQuery(String query) {
    Query parsedQuery = QueryFactory.create(query, Syntax.syntaxARQ);
    trace(parsedQuery);
    return executeInLock(() -> {
      try (QueryExecution queryExecution = QueryExecution.create()
              .query(parsedQuery)
              .model(model)
              .build()
      ) {
        return queryExecution.execConstruct();
      } catch (RuntimeException e) {
        error(parsedQuery);
        throw e;
      }
    });
  }

  private void trace(final Query query) {
    log.trace("Running query: {}", query);
  }

  private void error(final Query query) {
    log.error("Query failed: {}", query);
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
