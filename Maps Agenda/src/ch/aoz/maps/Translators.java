package ch.aoz.maps;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class Translators implements java.io.Serializable {
  public static final String entityKind = "Translators";
  public static final char RS = 0x1e; // Record separator.
  private static final long serialVersionUID = 161723L;

  private SortedMap<String, Translator> translators;
  private boolean isOk;
  private String debug;

  public Translators() {
    translators = new TreeMap<String, Translator>();
    debug = "ok";
    isOk = true;
  }

  public Translators(Collection<Translator> translators) {
    this();
    for (Translator t : translators) {
      this.translators.put(t.getEmail(), t);
    }
  }

  private Translators(Entity entity) {
    this();
    for (String email : entity.getProperties().keySet()) {
      String s = (String) entity.getProperty(email);
      Translator t = extractTranslator(email, s);
      if (t != null) {
        translators.put(email, t);
      }
    }
    debug = "ok";
    isOk = true;
    addToCache();
  }

  /**
   * Returns the Translators object. It contains all the translators defined in
   * the application.
   */
  public static Translators GetTranslators() {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    if (syncCache.contains(entityKind)) {
      return (Translators) syncCache.get(entityKind);
    }
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      Entity e = datastore.get(KeyFactory.createKey(entityKind, entityKind));
      return new Translators(e);
    } catch (EntityNotFoundException e) {
      return new Translators();
    }
  }

  public static boolean addTranslator(Translator t) {
    if (t == null || !t.isOk())
      return false;
    Translators ts = GetTranslators();
    if (ts == null)
      return false;
    ts.translators.put(t.getEmail(), t);
    return ts.addToStore();
  }

  public static boolean addTranslators(Collection<Translator> translators) {
    HashSet<String> emails = new HashSet<String>();
    for (Translator t : translators) {
      if (t == null || !t.isOk())
        return false;
      if (emails.contains(t.getEmail()))
        return false;
      emails.add(t.getEmail());
    }
    Translators ts = GetTranslators();
    if (ts == null)
      return false;

    for (Translator t : translators) {
      ts.translators.put(t.getEmail(), t);
    }
    return ts.addToStore();
  }

  public static boolean removeTranslator(String email) {
    if (email == null)
      return false;
    System.out.println("Trying to delete: " + email);
    Translators ts = GetTranslators();
    if (ts.translators.containsKey(email)) {
      ts.translators.remove(email);
      return ts.addToStore();
    }
    System.out.println("Not found");
    return true;
  }

  /**
   * Add this Translators to the datastore.
   * 
   * @return true if this operation succeeded.
   */
  public boolean addToStore() {
    if (!this.isOk()) {
      return false;
    }
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      datastore.put(this.toEntity());
    } catch (Exception ex) {
      return false;
    }
    addToCache();
    return true;
  }

  /** Utilities for storing and caching */

  /**
   * Returns the Entity representation of this Translators.
   * 
   * @return an Entity with the properties of this Translators.
   */
  private Entity toEntity() {
    Entity translators = new Entity(entityKind, entityKind);
    for (Translator t : this.translators.values()) {
      translators.setUnindexedProperty(t.getEmail(), packTranslator(t));
    }
    return translators;
  }

  /**
   * Extracts a Translator from the packed representation in the database.
   * 
   * @param packed
   *          string representation of a Translator in the database.
   * @return a fully constructed Translator
   */
  private static Translator extractTranslator(String email, String packed) {
    String[] fields = packed.split("" + RS);
    if (fields.length < 2)
      return null;
    return new Translator(email, fields[0], Arrays.asList(Arrays.copyOfRange(
        fields, 1, fields.length)));
  }

  /**
   * Packs the provided Translator into its database representation.
   * 
   * @param t
   *          the translators to pack in its database representation.
   * @return the packed representation.
   */
  private static String packTranslator(Translator t) {
    StringBuilder s = new StringBuilder();
    s.append(t.getName() + RS);
    for (String l : t.getLanguages()) {
      s.append(l + RS);
    }
    return s.toString();
  }

  private void addToCache() {
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.put(entityKind, this);
  }

  /** Only setters and getters below. */
  public Set<String> getSortedEmails() {
    return translators.keySet();
  }

  public SortedMap<String, Translator> getSortedTranslators() {
    return translators;
  }

  public Translator getTranslator(String email) {
    if (translators.containsKey(email))
      return translators.get(email);
    else
      return null;
  }

  public boolean isOk() {
    return isOk;
  }

  public String debug() {
    return debug;
  }
}
