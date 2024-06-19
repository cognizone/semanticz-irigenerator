package zone.cogni.asquare.cube.urigenerator;


import zone.cogni.asquare.cube.urigenerator.json.UriGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UriGeneratorResult {

  private UriGenerator generator;
  private Set<String> uris;
  private final Map<String, String> replacements = new HashMap<>();

  public UriGeneratorResult() {
  }

  public UriGenerator getGenerator() {
    return generator;
  }

  public void setGenerator(UriGenerator generator) {
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
