package zone.cogni.semanticz.irigenerator;

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
import zone.cogni.semanticz.irigenerator.json.IriGenerator;
import zone.cogni.semanticz.irigenerator.json.IriGeneratorSpecification;
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

public class IriGeneratorCalculator {
  private static final Logger log = LoggerFactory.getLogger(IriGeneratorCalculator.class);
  public static final int EXPECTED_ROW_COUNT = 1;

  private final String newIriPrefix;
  private final TemplateService templateService;
  private final IriGeneratorSpecification iriGeneratorSpecification;
  private final Map<String, Query> preparedStatements = new HashMap<>();

  public IriGeneratorCalculator(
          String newIriPrefix,
          TemplateService templateService,
          URL iriGeneratorSpecificationResource,
          Format format
  ) {
    this.newIriPrefix = newIriPrefix;
    this.templateService = templateService;
    this.iriGeneratorSpecification = Utils.load(iriGeneratorSpecificationResource, format);
    initPreparedStatements();
  }

  private void initPreparedStatements() {
    preparedStatements.put("exists-uri", QueryFactory.create("ask { { ?x ?p ?o } union { ?s ?p ?x } } "));
  }

  public Model convert(Model model, Map<String, String> context) {
    try {
      final RdfStoreServiceAPI rdfStore = getRdfStore(model);
      final List<IriGeneratorResult> results = getGeneratorResults(rdfStore);

      int replaceCount = inputValidations(model, results);
      processReplacements(model, rdfStore, replaceCount, context, results);

      validate(model);

      return model;
    } catch (RuntimeException e) {
      throw new RuntimeException("An error occurred during IRI generation", e);
    }
  }

  @Nonnull
  private List<IriGeneratorResult> getGeneratorResults(RdfStoreServiceAPI rdfStore) {
    return iriGeneratorSpecification.getGenerators()
            .stream()
            .map(generator -> getIriGeneratorResult(rdfStore, generator))
            .collect(Collectors.toList());
  }

  @Nonnull
  private IriGeneratorResult getIriGeneratorResult(RdfStoreServiceAPI rdfStore, IriGenerator generator) {
    IriGeneratorResult iriGeneratorResult = new IriGeneratorResult();
    iriGeneratorResult.setGenerator(generator);
    iriGeneratorResult.setUris(getNewSelectorIris(rdfStore, generator));
    return iriGeneratorResult;
  }

  /**
   * @return number of IRIs to be replaces
   */
  private int inputValidations(final Model model, final List<IriGeneratorResult> results) {
    final Set<String> incomingIris = getProblemIris(model);

    final Set<String> selectedIris = new HashSet<>();
    final Set<String> duplicates = results.stream()
            .flatMap(result -> result.getUris().stream())
            .filter(iri -> !selectedIris.add(iri))
            .collect(Collectors.toSet());

    if (!duplicates.isEmpty())
      log.error("some IRIs matched multiple selectors: {}", duplicates);

    final int size = incomingIris.size();
    if (size != selectedIris.size())
      log.error("incoming IRIs and selected IRIs do not match up." +
              "\n\t incoming: {}" +
              "\n\t selected: {}", incomingIris, selectedIris);

    if (!duplicates.isEmpty() || size != selectedIris.size())
      throw new RuntimeException("some validations failed when converting new IRIs, check logs for more info");

    log.info("(IRI generator) replacing {} IRIs", size);
    return size;
  }

  private void processReplacements(final Model model,
                                   final RdfStoreServiceAPI rdfStore,
                                   int replaceCount,
                                   final Map<String, String> context,
                                   final List<IriGeneratorResult> results) {
    int loopCount = 0;
    while (true) {
      int count = calculateReplacementIrisLoop(model, rdfStore, context, results);
      replaceCount -= count;

      log.info("(IRI generator) loop {} processed {} IRIs", ++loopCount, count);

      // stop loop when all IRIs are processed
      if (replaceCount == 0) break;

      // stop loop when no replacement where processed
      if (count == 0) break;
    }
  }

  private int calculateReplacementIrisLoop(final Model model,
                                           final RdfStoreServiceAPI rdfStore,
                                           final Map<String, String> context,
                                           final List<IriGeneratorResult> results) {
    final AtomicInteger count = new AtomicInteger();

    results.forEach(result -> result.getUris().forEach(iri -> {
      if (result.alreadyReplaced(iri)) return;

      final Optional<String> possibleNewIri = calculateNewIri(rdfStore, context, result, iri);
      if (possibleNewIri.isPresent()) {
        count.addAndGet(1);
        result.addReplacement(iri, possibleNewIri.get());
        ResourceUtils.renameResource(model.getResource(iri), possibleNewIri.get());
      }
    }));

    return count.get();
  }

  private Optional<String> calculateNewIri(final RdfStoreServiceAPI rdfStore,
                                           final Map<String, String> context,
                                           final IriGeneratorResult result,
                                           final String oldUri) {
    if (log.isTraceEnabled()) log.trace("calculate new uri for {}", oldUri);
    traceModel(rdfStore);

    final Map<String, String> variables = new HashMap<>(context);
    variables.put("uri", oldUri);

    // variable template can also NOT exist: then this step is skipped!
    final String variableSelector = result.getGenerator().getVariableSelector();
    if (StringUtils.isNotBlank(variableSelector)) {
      final String variableTemplateQuery = Utils.getPrefixQuery(iriGeneratorSpecification.getPrefixes()) + variableSelector;
      final String variableQuery = templateService.processTemplate(variableTemplateQuery, variables);
      if (log.isTraceEnabled()) log.trace("query: {}", variableQuery);

      final Supplier<String> contextSupplier = () -> result.getGenerator().getId();
      final Optional<Map<String, String>> variableMap = getQueryMap(contextSupplier, rdfStore, variableQuery);

      // if one of template variables is still a new IRI we should skip calculation for now
      if (variableMap.isEmpty()) return Optional.empty();

      final Map<String, String> map = variableMap.get();
      if (log.isTraceEnabled()) log.trace("query result: {}", map);
      variables.putAll(map);
    }
    if (log.isTraceEnabled()) log.debug("variables: {}", variables);

    final String iriTemplate = result.getGenerator().getIriTemplate();
    final String newIri = templateService.processTemplate(iriTemplate, variables);

    if (existsInModel(rdfStore, newIri))
      throw new RuntimeException("IRI overlap found for " + newIri);

    return Optional.of(newIri);
  }

  private void traceModel(RdfStoreServiceAPI rdfStore) {
    if (!log.isTraceEnabled()) return;

    final Model trace = rdfStore.executeConstructQuery("construct {?s ?p ?o} where {?s ?p ?o}");
    final StringWriter out = new StringWriter();
    trace.write(out, "ttl");
    log.trace("model: {}", out);
  }

  private boolean existsInModel(RdfStoreServiceAPI rdfStore, String newIri) {
    final QuerySolutionMap querySolution = new QuerySolutionMap();
    querySolution.add("x", ResourceFactory.createResource(newIri));

    return rdfStore.executeAskQuery(preparedStatements.get("exists-uri"), querySolution);
  }

  /**
   * @return empty optional if one of resources start with a <code>newIriPrefix</code> !
   * else a map of variables to be used in IRI template!
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
                    && node.asResource().getURI().startsWith(newIriPrefix));
    if (isBadMatch) return Optional.empty();

    final Map<String, String> result = new HashMap<>();
    nodeMap.forEach((k, v) -> result.put(k, (v.isResource() ? v.asResource().getURI() : v.asLiteral().getString())));

    return Optional.of(result);
  }

  private void nonNullCheck(Map<String, RDFNode> nodeMap, RDFNode node) {
    if (node == null) throw new RuntimeException("variableSelector result has some null values: " + nodeMap);
  }

  private Set<String> getNewSelectorIris(RdfStoreServiceAPI rdfStore, IriGenerator generator) {
    final String query = Utils.getPrefixQuery(iriGeneratorSpecification.getPrefixes()) + generator.getIriSelector();
    return rdfStore
            .executeSelectQuery(query, IriGeneratorCalculator::convertToList).stream()
            .filter(iri -> iri.startsWith(newIriPrefix))
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
    Set<String> problemIris = getProblemIris(model);

    if (!problemIris.isEmpty()) throw new RuntimeException("some IRIs could not be replaced: " + problemIris);
  }

  @Nonnull
  private Set<String> getProblemIris(Model model) {
    Set<String> problemIris = new HashSet<>();
    model.listStatements()
            .forEachRemaining(statement -> {
              if (statement.getSubject().getURI().startsWith(newIriPrefix)) {
                problemIris.add(statement.getSubject().getURI());
              }

              if (statement.getObject().isURIResource()
                      && statement.getObject().asResource().getURI().startsWith(newIriPrefix)) {
                problemIris.add(statement.getObject().asResource().getURI());
              }
            });
    return problemIris;
  }

}
