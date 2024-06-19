package zone.cogni.asquare.cube.spel;

public class NamedTemplate {

  private final String template;
  private final String name;

  protected NamedTemplate(String template, String name) {
    this.template = template;
    this.name = name;
  }

  /**
   * @return copy of template part, not result part
   */
  public NamedTemplate copy() {
    return new NamedTemplate(template, name);
  }

  public String getTemplate() {
    return template;
  }
}
