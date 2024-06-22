package zone.cogni.asquare.cube.urigenerator;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.cogni.asquare.cube.spel.TemplateService;
import zone.cogni.asquare.cube.urigenerator.json.UriGenerator;
import zone.cogni.asquare.cube.urigenerator.json.UriGeneratorRoot;
import zone.cogni.asquare.triplestore.RdfStoreServiceAPI;
import zone.cogni.asquare.triplestore.InternalRdfStoreService;
import zone.cogni.sem.jena.template.JenaQueryUtils;

import jakarta.annotation.Nonnull;

import java.io.StringWriter;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class UriGeneratorCalculator {
  private static final Logger log = LoggerFactory.getLogger(UriGeneratorCalculator.class);
  public static final int EXPECTED_ROW_COUNT = 1;

  private final String newUriPrefix;
  private final TemplateService templateService;
  private final UriGeneratorRoot uriGeneratorRoot;
  private final Map<String, Query> preparedStatements = new HashMap<>();

  public UriGeneratorCalculator(
          String newUriPrefix,
          TemplateService templateService,
          URL uriGeneratorRootResource,
          Format format
  ) {
    this.newUriPrefix = newUriPrefix;
    this.templateService = templateService;
    this.uriGeneratorRoot = Utils.load(uriGeneratorRootResource, format);
    initPreparedStatements();
  }

  private void initPreparedStatements() {
    preparedStatements.put("exists-uri", QueryFactory.create("ask { { ?x ?p ?o } union { ?s ?p ?x } } "));
  }

  public Model convert(Model model, Map<String, String> context) {
    try {
      final RdfStoreServiceAPI rdfStore = getRdfStore(model);
      final List<UriGeneratorResult> results = getGeneratorResults(rdfStore);

      int replaceCount = inputValidations(model, results);
      processReplacements(model, rdfStore, replaceCount, context, results);

      validate(model);

      return model;
    } catch (RuntimeException e) {
      throw new RuntimeException("An error occurred during URI generation", e);
    }
  }

  @Nonnull
  private List<UriGeneratorResult> getGeneratorResults(RdfStoreServiceAPI rdfStore) {
    return uriGeneratorRoot.getGenerators()
            .stream()
            .map(generator -> getUriGeneratorResult(rdfStore, generator))
            .collect(Collectors.toList());
  }

  @Nonnull
  private UriGeneratorResult getUriGeneratorResult(RdfStoreServiceAPI rdfStore, UriGenerator generator) {
    UriGeneratorResult uriGeneratorResult = new UriGeneratorResult();
    uriGeneratorResult.setGenerator(generator);
    uriGeneratorResult.setUris(getNewSelectorUris(rdfStore, generator));
    return uriGeneratorResult;
  }

  /**
   * @return number of uris to be replaces
   */
  private int inputValidations(final Model model, final List<UriGeneratorResult> results) {
    final Set<String> incomingUris = getProblemUris(model);

    final Set<String> selectedUris = new HashSet<>();
    final Set<String> duplicates = results.stream()
            .flatMap(result -> result.getUris().stream())
            .filter(uri -> !selectedUris.add(uri))
            .collect(Collectors.toSet());

    if (!duplicates.isEmpty())
      log.error("some uris matched multiple selectors: {}", duplicates);

    final int size = incomingUris.size();
    if (size != selectedUris.size())
      log.error("incoming uris and selected uris do not match up." +
              "\n\t incoming: {}" +
              "\n\t selected: {}", incomingUris, selectedUris);

    if (!duplicates.isEmpty() || size != selectedUris.size())
      throw new RuntimeException("some validations failed when converting new uris, check logs for more info");

    log.info("(uri generator) replacing {} uris", size);
    return size;
  }

  private void processReplacements(final Model model,
                                   final RdfStoreServiceAPI rdfStore,
                                   int replaceCount,
                                   final Map<String, String> context,
                                   final List<UriGeneratorResult> results) {
    int loopCount = 0;
    while (true) {
      int count = calculateReplacementUrisLoop(model, rdfStore, context, results);
      replaceCount -= count;

      log.info("(uri generator) loop {} processed {} uris", ++loopCount, count);

      // stop loop when all uris are processed
      if (replaceCount == 0) break;

      // stop loop when no replacement where processed
      if (count == 0) break;
    }
  }

  private int calculateReplacementUrisLoop(final Model model,
                                           final RdfStoreServiceAPI rdfStore,
                                           final Map<String, String> context,
                                           final List<UriGeneratorResult> results) {
    final AtomicInteger count = new AtomicInteger();

    results.forEach(result -> result.getUris().forEach(uri -> {
      if (result.alreadyReplaced(uri)) return;

      final Optional<String> possibleNewUri = calculateNewUri(rdfStore, context, result, uri);
      if (possibleNewUri.isPresent()) {
        count.addAndGet(1);
        result.addReplacement(uri, possibleNewUri.get());
        ResourceUtils.renameResource(model.getResource(uri), possibleNewUri.get());
      }
    }));

    return count.get();
  }

  private Optional<String> calculateNewUri(final RdfStoreServiceAPI rdfStore,
                                           final Map<String, String> context,
                                           final UriGeneratorResult result,
                                           final String oldUri) {
    if (log.isTraceEnabled()) log.trace("calculate new uri for {}", oldUri);
    traceModel(rdfStore);

    final Map<String, String> variables = new HashMap<>(context);
    variables.put("uri", oldUri);

    // variable template can also NOT exist: then this step is skipped!
    final String variableSelector = result.getGenerator().getVariableSelector();
    if (StringUtils.isNotBlank(variableSelector)) {
      final String variableTemplateQuery = Utils.getPrefixQuery(uriGeneratorRoot.getPrefixes()) + variableSelector;
      final String variableQuery = templateService.processTemplate(variableTemplateQuery, variables);
      if (log.isTraceEnabled()) log.trace("query: {}", variableQuery);

      final Supplier<String> contextSupplier = () -> result.getGenerator().getId();
      final Optional<Map<String, String>> variableMap = getQueryMap(contextSupplier, rdfStore, variableQuery);

      // if one of template variables is still a new URI we should skip calculation for now
      if (variableMap.isEmpty()) return Optional.empty();

      final Map<String, String> map = variableMap.get();
      if (log.isTraceEnabled()) log.trace("query result: {}", map);
      variables.putAll(map);
    }
    if (log.isTraceEnabled()) log.debug("variables: {}", variables);

    final String uriTemplate = result.getGenerator().getUriTemplate();
    final String newUri = templateService.processTemplate(uriTemplate, variables);

    if (existsInModel(rdfStore, newUri))
      throw new RuntimeException("uri overlap found for " + newUri);

    return Optional.of(newUri);
  }

  private void traceModel(RdfStoreServiceAPI rdfStore) {
    if (!log.isTraceEnabled()) return;

    final Model trace = rdfStore.executeConstructQuery("construct {?s ?p ?o} where {?s ?p ?o}");
    final StringWriter out = new StringWriter();
    trace.write(out, "ttl");
    log.trace("model: {}", out);
  }

  private boolean existsInModel(RdfStoreServiceAPI rdfStore, String newUri) {
    final QuerySolutionMap querySolution = new QuerySolutionMap();
    querySolution.add("x", ResourceFactory.createResource(newUri));

    return rdfStore.executeAskQuery(preparedStatements.get("exists-uri"), querySolution);
  }

  /**
   * @return empty optional if one of resources start with a <code>newUriPrefix</code> !
   * else a map of variables to be used in uri template!
   */
  private Optional<Map<String, String>> getQueryMap(Supplier<String> context,
                                                    RdfStoreServiceAPI rdfStore,
                                                    String variableQuery) {
    final List<Map<String, RDFNode>> result1 = rdfStore.executeSelectQuery(variableQuery, JenaQueryUtils::convertToListOfMaps);
    if (result1.size() != EXPECTED_ROW_COUNT)
      throw new RuntimeException("[" + context.get() + "] expected 1 row, found " + result1);

    final Map<String, RDFNode> nodeMap = result1.get(0);
    boolean isBadMatch = nodeMap.values()
            .stream()
            .peek(node -> nonNullCheck(nodeMap, node))
            .anyMatch(node -> node.isURIResource()
                    && node.asResource().getURI().startsWith(newUriPrefix));
    if (isBadMatch) return Optional.empty();

    final Map<String, String> result = new HashMap<>();
    nodeMap.forEach((k, v) -> result.put(k, (v.isResource() ? v.asResource().getURI() : v.asLiteral().getString())));

    return Optional.of(result);
  }

  private void nonNullCheck(Map<String, RDFNode> nodeMap, RDFNode node) {
    if (node == null) throw new RuntimeException("variableSelector result has some null values: " + nodeMap);
  }

  private Set<String> getNewSelectorUris(RdfStoreServiceAPI rdfStore, UriGenerator generator) {
    final String query = Utils.getPrefixQuery(uriGeneratorRoot.getPrefixes()) + generator.getUriSelector();
    return rdfStore
            .executeSelectQuery(query, UriGeneratorCalculator::convertToList).stream()
            .filter(uri -> uri.startsWith(newUriPrefix))
            .collect(Collectors.toSet());
  }

  private static List<String> convertToList(ResultSet resultSet) {
    List<String> result = new ArrayList<>();

    resultSet.forEachRemaining(querySolution ->
            result.add(querySolution.get("uri").asResource().getURI())
    );

    return result;
  }

  private RdfStoreServiceAPI getRdfStore(Model model) {
    return new InternalRdfStoreService(model);
  }

  private void validate(Model model) {
    Set<String> problemUris = getProblemUris(model);

    if (!problemUris.isEmpty()) throw new RuntimeException("some uris could not be replaced: " + problemUris);
  }

  @Nonnull
  private Set<String> getProblemUris(Model model) {
    Set<String> problemUris = new HashSet<>();
    model.listStatements()
            .forEachRemaining(statement -> {
              if (statement.getSubject().getURI().startsWith(newUriPrefix)) {
                problemUris.add(statement.getSubject().getURI());
              }

              if (statement.getObject().isURIResource()
                      && statement.getObject().asResource().getURI().startsWith(newUriPrefix)) {
                problemUris.add(statement.getObject().asResource().getURI());
              }
            });
    return problemUris;
  }

}
