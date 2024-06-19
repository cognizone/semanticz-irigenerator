package zone.cogni.sem.jena;

import com.google.common.base.Preconditions;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFErrorHandler;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFReaderI;
import org.apache.jena.rdf.model.RDFVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import zone.cogni.core.spring.ResourceHelper;
import zone.cogni.core.util.FileHelper;
import zone.cogni.core.util.IOHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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
    return read(Arrays.asList(resources));
  }

  public static Model read(Iterable<Resource> resources) {
    return read(resources, null);
  }

  public static Model read(Iterable<Resource> resources, Map<String, Object> readerProperties) {
    Model model = ModelFactory.createDefaultModel();

    for (Resource resource : resources) {
      InputStream inputstream = null;
      try {
        inputstream = ResourceHelper.getInputStream(resource);
        InternalRdfErrorHandler errorHandler = new InternalRdfErrorHandler(resource.getDescription());

        RDFReaderI rdfReader = getReader(model, resource, errorHandler, readerProperties);
        rdfReader.read(model, inputstream, null);

        Preconditions.checkState(!errorHandler.isFailure(), errorHandler.getInfo());
      }
      catch (RuntimeException e) {
        closeQuietly(model);

        throw e;
      }
      finally {
        IOUtils.closeQuietly(inputstream);
      }
    }

    return model;
  }

  private static RDFReaderI getReader(Model model, Resource resource, RDFErrorHandler rdfErrorHandler, Map<String, Object> readerProperties) {
    return getReader(model, rdfErrorHandler, readerProperties, getRdfSyntax(resource));
  }

  private static RDFReaderI getReader(Model model, RDFErrorHandler rdfErrorHandler, Map<String, Object> readerProperties, String language) {
    RDFReaderI rdfReader = getReaderByRdfSyntax(model, language);
    rdfReader.setErrorHandler(rdfErrorHandler);
    if (readerProperties == null) return rdfReader;

    for (String propertyName : readerProperties.keySet()) {
      rdfReader.setProperty(propertyName, readerProperties.get(propertyName));
    }
    return rdfReader;
  }

  private static RDFReaderI getReaderByRdfSyntax(Model model, String language) {
    try {
      return model.getReader(language);
    }
    catch (IllegalStateException ignored) {
      return model.getReader();
    }
  }

  private static String getRdfSyntax(org.springframework.core.io.Resource resource) {
    String extension = StringUtils.lowerCase(StringUtils.substringAfterLast(resource.getFilename(), "."));

    // when return value is null, fall back to RDF/XML
    return extensionToLanguageMap.getOrDefault(extension, null);
  }

  public static void write(Model model, File file) {
    write(model, FileHelper.openOutputStream(file));
  }

  public static void write(Model model, OutputStream out) {
    try {
      model.write(out);
    }
    finally {
      IOHelper.flushAndClose(out);
    }
  }

  public static String toString(Model model) {
    return toString(model, "RDF/XML");
  }

  public static String toString(Model model, String language) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      model.write(out, language);

      return out.toString("UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }


  public static void closeQuietly(Model... models) {
    Arrays.stream(models).filter(Objects::nonNull).filter(model -> !model.isClosed()).forEach(model -> {
      try {
        model.close();
      }
      catch (Exception e) {
        log.warn("Closing model failed.", e);
      }
    });
  }

  public static Model readInto(File file, Model model) {
    return readInto(file, model, getLangByResourceName(file.getName()));
  }

  public static String getLangByResourceName(String resourceName) {
    String ext = FilenameUtils.getExtension(resourceName);
    if (ext.equalsIgnoreCase("ttl")) return "TTL";
    //TODO: add other types
    return null;
  }

  public static Model readInto(File file, Model model, String lang) {
    try (InputStream inputStream = FileHelper.openInputStream(file)) {
      return readInto(inputStream, file.getAbsolutePath(), model, lang);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Model readInto(InputStream inputStream, String streamName, Model model, String lang) {
    try {
      RDFReaderI reader = model.getReader(lang);
      InternalRdfErrorHandler errorHandler = new InternalRdfErrorHandler(streamName);
      reader.setErrorHandler(errorHandler);
      reader.read(model, inputStream, null);

      Preconditions.checkState(errorHandler.isFailure(), errorHandler.getInfo());
      return model;
    }
    finally {
      IOUtils.closeQuietly(inputStream);
    }
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

