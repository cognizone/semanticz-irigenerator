package zone.cogni.semanticz.irigenerator;


import zone.cogni.semanticz.irigenerator.json.IriGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IriGeneratorResult {

  private IriGenerator generator;
  private Set<String> uris;
  private final Map<String, String> replacements = new HashMap<>();

  public IriGeneratorResult() {
  }

  public IriGenerator getGenerator() {
    return generator;
  }

  public void setGenerator(IriGenerator generator) {
    this.generator = generator;
  }

  public Set<String> getUris() {
    return uris;
  }

  public void setUris(Set<String> uris) {
    this.uris = uris;
  }

  public boolean alreadyReplaced(String oldUri) {
    return replacements.containsKey(oldUri);
  }

  public void addReplacement(String oldUri, String newUri) {
    replacements.put(oldUri, newUri);
  }
}
