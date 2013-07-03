package ch.aoz.maps;

/**
 * Special translation which is primarily from a given language, but includes a
 * fallback language (assumed to be German) used when the first is missing data.
 */
public class TranslationWithFallback extends Translation {
  private final boolean isTitleRtl;
  private final boolean isDescRtl;
  private final boolean isLocationRtl;
  private final boolean isUrlRtl;
  
  public TranslationWithFallback(Translation primary, Translation fallback) {
    super(
        primary.getLang(),
        getOrDefault(primary.getTitle(), fallback.getTitle()),
        getOrDefault(primary.getDesc(), fallback.getDesc()),
        getOrDefault(primary.getLocation(), fallback.getLocation()),
        getOrDefault(primary.getUrl(), fallback.getUrl()));
    
    boolean primaryLangRtl =
        Language.GetByCode(primary.getLang()).isRightToLeft();
    
    this.isTitleRtl = primary.getTitle() != null ? primaryLangRtl : false;
    this.isDescRtl = primary.getDesc() != null ? primaryLangRtl : false;
    this.isLocationRtl = primary.getLocation() != null ? primaryLangRtl : false;
    this.isUrlRtl = primary.getUrl() != null ? primaryLangRtl : false;
  }
  
  /** Whether the title is from an RTL language. */
  public boolean isTitleRtl() {
    return this.isTitleRtl;
  }
  
  /** Whether the description is from an RTL language. */
  public boolean isDescRtl() {
    return this.isDescRtl;
  }
  
  /** Whether the location is from an RTL language. */
  public boolean isLocationRtl() {
    return this.isLocationRtl;
  }
  
  /** Whether the URL is from an RTL language. */
  public boolean isUrlRtl() {
    return this.isUrlRtl;
  }
  
  // Utility - returns the first if provided, otherwise the second
  private static <T> T getOrDefault(T value, T defaultValue) {
    return value == null || value.toString().isEmpty() ? defaultValue : value;
  }
}
