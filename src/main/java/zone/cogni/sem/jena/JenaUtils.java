package zone.cogni.sem.jena;

import com.google.common.base.Preconditions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFErrorHandler;
import org.apache.jena.rdf.model.RDFReaderI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Deprecated
public class JenaUtils {
  private static final Logger log = LoggerFactory.getLogger(JenaUtils.class);
  private static final Map<String, String> extensionToLanguageMap = Collections.synchronizedMap(new HashMap<>());

  static {
    extensionToLanguageMap.put("nt", "N-TRIPLE");
    extensionToLanguageMap.put("n3", "N3");
    extensionToLanguageMap.put("ttl", "TURTLE");
    extensionToLanguageMap.put("jsonld", "JSONLD");
  }

  private JenaUtils() {
  }

  public static Model read(Resource... resources) {
    Model model = ModelFactory.createDefaultModel();

    for (Resource resource : resources) {
      InputStream inputstream = null;
      try {
        InputStream result;
        try {
          result = resource.getInputStream();
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
        inputstream = result;
        InternalRdfErrorHandler errorHandler = new InternalRdfErrorHandler(resource.getDescription());

        String extension = StringUtils.lowerCase(StringUtils.substringAfterLast(resource.getFilename(), "."));

        // when return value is null, fall back to RDF/XML
        String language = extensionToLanguageMap.getOrDefault(extension, null);
        RDFReaderI rdfReader1;
        try {
          rdfReader1 = model.getReader(language);
        }
        catch (IllegalStateException ignored) {
          rdfReader1 = model.getReader();
        }
        rdfReader1.setErrorHandler(errorHandler);
        RDFReaderI rdfReader = rdfReader1;
        rdfReader.read(model, inputstream, null);

        Preconditions.checkState(!errorHandler.isFailure(), errorHandler.getInfo());
      }
      catch (RuntimeException e) {
        Arrays.stream(new Model[]{model}).filter(Objects::nonNull).filter(model1 -> !model1.isClosed()).forEach(model1 -> {
          try {
            model1.close();
          }
          catch (Exception e1) {
            log.warn("Closing model failed.", e1);
          }
        });

        throw e;
      }
      finally {
        IOUtils.closeQuietly(inputstream);
      }
    }

    return model;
  }

  private static class InternalRdfErrorHandler implements RDFErrorHandler {

    private final String info;
    private boolean failure;

    private InternalRdfErrorHandler(String loadedFile) {
      info = "Load rdf file (" + loadedFile + ") problem.";
    }

    public boolean isFailure() {
      return failure;
    }

    public String getInfo() {
      return info;
    }

    @Override
    public void warning(Exception e) {
      String message = e.getMessage();
      if (null != message && message.contains("ISO-639 does not define language:")) {
        log.warn("{}: {}", info, message);
        return;
      }
      log.warn(info, e);
    }

    @Override
    public void error(Exception e) {
      failure = true;
      log.error(info, e);
    }

    @Override
    public void fatalError(Exception e) {
      failure = true;
      log.error(info, e);
    }
  }
}

