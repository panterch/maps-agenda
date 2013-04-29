package ch.aoz.maps;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.aoz.maps.Language;

/**
 * A MAPS event.
 */
public class Event {
  public static final String entityKind = "Event";
  private boolean hasKey;
  private long key;
  private Date date;
  private Translation germanTranslation;
  private boolean ok;
  
  /**
   * Create a new Event with the specified parameters and key.
   *
   * @param date day at which the event takes place
   */
  public Event(Date date, Translation germanTranslation, long key) {
    this.date = date;
    this.germanTranslation = germanTranslation;
    this.key = key;
    this.ok = (date != null);
    hasKey = true;
  }

  /**
   * Create a new Event with the specified parameters.
   *
   * @param date day at which the event takes place
   * @param title the German title of the event
   */
  public Event(Date date, Translation germanTranslation) {
    this.date = date;
    this.germanTranslation = germanTranslation;
    this.ok = (date != null);
    this.key = 0;
    hasKey = false;
  }

  /**
   * Parse an Entity into a new Event. Check isOk() if all fields could be populated.
   *
   * @param entity the entity to parse
   */
  public Event(Entity entity) {
    key = entity.getKey().getId();
    hasKey = true; 
    
    ok = true;
    if (entity.hasProperty("date")) {
      date = (Date) entity.getProperty("date");
    } else {
      Calendar.getInstance();
      try {
        date = new SimpleDateFormat("yyyy-MM-dd").parse("1900-01-01");
      } catch (Exception e) {}
      ok = false;
    }
    
    try {
      germanTranslation = Translation.getGermanTranslationForEvent(this);
    } catch (EntityNotFoundException e) {
      germanTranslation = new Translation(entity.getKey(), "de", "", "", "", "");
      ok = false;
    }
  }

  public boolean addToStore() {
    if (!this.isOk() || !this.germanTranslation.isOk()) {
      return false;
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key key;
    try {
      key = datastore.put(this.toEntity());
      if (!hasKey()) {
        this.key = key.getId();
        hasKey = true;
      }
    } catch (Exception ex) {
      return false;
    }

    if (germanTranslation.getEventID() == null)
      germanTranslation.setEventID(key);
    return germanTranslation.addToStore();
  }

  /**
   * Export this Event into an Entity.
   *
   * @return the generated Entity.
   */
  public Entity toEntity() {
    Entity result = null;
    if (hasKey())
      result = new Entity(entityKind, getKey());
    else
      result = new Entity(entityKind);

    result.setProperty("date", date);
    return result;
  }

  public static List<Event> GetAllEvents() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query = new Query(entityKind).addSort("date", SortDirection.ASCENDING);
    List<Entity> items = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    
    ArrayList<Event> events = new ArrayList<Event>();
    for (Entity item : items) {
      events.add(new Event(item));
    }
    return events;
  }

  public static List<Event> GetEventListForTimespan(
      Calendar from, Calendar to) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Set up the range filter.
    Filter minimumFilter = new FilterPredicate(
        "date", Query.FilterOperator.GREATER_THAN_OR_EQUAL, from.getTime());
    Filter maximumFilter = new FilterPredicate(
        "date", Query.FilterOperator.LESS_THAN, to.getTime());
    Filter rangeFilter = CompositeFilterOperator.and(minimumFilter, maximumFilter);

    Query query = new Query(entityKind).setFilter(rangeFilter)
        .addSort("date", SortDirection.ASCENDING);
    List<Entity> items = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    
    ArrayList<Event> events = new ArrayList<Event>();
    for (Entity item : items) {
      events.add(new Event(item));
    }
    return events;
  }
  
  public static List<Event> GetEventListForMonth(int year, int month) {
    Calendar from = Calendar.getInstance();
    from.set(year, month, 1);
    
    Calendar to = Calendar.getInstance();
    to.setTime(from.getTime());
    to.add(month, 1);
    
    return GetEventListForTimespan(from, to);
  }
  
  /**
   * Render this Event into an XML tag.
   *
   * @return the generated XML tag without headers.
   */
  public String GetXML(
      int yearFrom,
      int monthFrom,
      int dayFrom,
      int yearTo,
      int monthTo,
      int dayTo) {
    // Set up range to export.
    Calendar from = Calendar.getInstance();
    from.set(yearFrom, monthFrom, dayFrom);
    Calendar to = Calendar.getInstance();
    to.set(yearTo, monthTo, dayTo);
   
    // Header.
    String xml = new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    xml += "<Root>\n";
    xml += "  <Tag1>\n";
    
    Map<String, Language> languages = Language.getAllLanguages();
    for (Language language : languages.values()) {
      xml += "    <" + language.getCode() + ">\n";
      xml += "      <inh>\n";
      
      // Topic of the month.
      if (topic_of_month.size() > 0) {
        /*
         foreach($_POST[monatsthema] as $value){
    if(file_exists($datapfad.str_replace ( "de" , $spr,  $value))){
            eintrag(str_replace ( "de" , $spr,  $value),2,$spr);
        }
         */
      }
      
      // Each entry.
      for (Event event : GetEventListForTimespan(from, to)) {
        xml += GetXMLEntry(event, language);
      }
      xml += "      </inh>\n";
      xml += "    </" + language.getCode() + ">\n";
    }
    xml += "  </Tag1>\n";
    
    // TODO 'bildtexte'.
    xml += "</Root>\n";
  }
  
  public String GetXMLEntry(Event event, Language language) {
    String xml = new String();
    
    Translation translation; // TODO assign the translation of this event to the given language.
    // TODO implement the stuff below:
    /*
         // SOLL AUS DEM EINTRAG EINE BILDUNTERSCHRIFT GENERIERT WERDEN?
         if(count($_POST["bildtexte"])>0){
         foreach($_POST["bildtexte"] as $value_b){

             if($value==$value_b){

             if(!array_key_exists($value, $bildtext_array)){

             $bildtext_array[$value]=array();
         //  array_push($bildtext_array,array( $value => array()));
             } 
             
             if(file_exists($datapfad.str_replace ( "de" , $spr,  $value))){
             $getxml = simplexml_load_file($datapfad.str_replace ( "de" , $spr,  $value));
             $titel_spr=$getxml->title[0];

             array_push  ($bildtext_array[$value], array($spr,$titel_spr));
             }


             }
         }

         }
*/
    
    // TODO everywhere:
    /*
    function edittext($text,$sprache){
      $text=trim($text);
      $text=str_replace("\n", "", $text);
      if($sprache=="ta"){
      $text=str_replace("&#160;", "", $text);
      }else{
      $text=str_replace("&#160;", " ", $text);
      }
      $text=strip_tags($text);


      return $text;
      }
*/
    
    boolean enlarge = true;           // TODO Pass in, or retrieve.
    String dayOfWeek = new String();  // TODO Day of week in the particular language. 
    
    String formatSupplement = new String();
    if (language.hasSpecificFormat()) {
      formatSupplement = "_" + language.getCode();
    }
  
    if (!enlarge) {
      xml += "<Table_inside xmlns:aid5=\"http://ns.adobe.com/AdobeInDesign/5.0/\" aid5:tablestyle=\"ts_inside\" xmlns:aid=\"http://ns.adobe.com/AdobeInDesign/4.0/\" aid:table=\"table\" aid:trows=\"1\" aid:tcols=\"3\">\n";
      if (!language.isRightToLeft()) {
        xml += GetXMLDayOfWeek(translation, formatSupplement, enlarge, false);
        xml += GetXMLDate(translation, event, enlarge, date_changed);
        xml += GetXMLSmallContents(translation, 358, formatSupplement, false);
      } else {
        xml += GetXMLSmallContents(translation, 374, formatSupplement, true);
        xml += GetXMLDate(translation, event, enlarge, date_changed);
        xml += GetXMLDayOfWeek(translation, formatSupplement, enlarge, true);
      }
    } else {
      xml += "<Table_inside xmlns:aid5=\"http://ns.adobe.com/AdobeInDesign/5.0/\" aid5:tablestyle=\"ts_inside\" xmlns:aid=\"http://ns.adobe.com/AdobeInDesign/4.0/\" aid:table=\"table\" aid:trows=\"2\" aid:tcols=\"3\">\n";
      if (!language.isRightToLeft()) {
        xml += GetXMLDayOfWeek(translation, formatSupplement, enlarge, false);
        xml += GetXMLDate(translation, event, enlarge, false);
        xml += GetXMLTitle(translation, 303, formatSupplement);
      } else {
        xml += GetXMLTitle(translation, 374, formatSupplement);
        xml += GetXMLDate(translation, event, enlarge, false);
        xml += GetXMLDayOfWeek(translation, formatSupplement, enlarge, true);
      }
      xml += "  <Tag_inside aid:table=\"cell\" aid:crows=\"1\" aid:ccols=\"3\" aid5:cellstyle=\"cs_desc_gross\">\n";
      xml += "    <Inhalttag aid:pstyle=\"inhalt_gross" + formatSupplement + "\">\n";
      xml += "      " + translation.getDesc();
      xml += "    </Inhalttag>\n";
      xml += GetXMLLocation(translation, formatSupplement, enlarge, language.isRightToLeft());
      xml += "  </Tag_inside>\n";
    }
    xml += "</Table_inside>";
  }

  private static String GetXMLSmallContents(
      Translation translation, int width, String formatSupplement, Boolean rtl) {
    String xml = new String();
    xml += "  <Tag_inside aid:table=\"cell\" aid:crows=\"1\" aid:ccols=\"1\" aid:ccolwidth=\"374\" aid5:cellstyle=\"cs_desc\">\n";
    xml += "    <title aid:pstyle=\"titel" + formatSupplement + "\">\n";
    xml += "      " + translation.getTitle() + "\n";
    xml += "    </title>\n";
    xml += "    <Inhalttag aid:pstyle=\"inhalt" + formatSupplement + "\">\n";
    xml += "      " + translation.getDesc();
    xml += "    </Inhalttag>\n";
    xml += GetXMLLocation(translation, formatSupplement, false, rtl);
    xml += "  </Tag_inside>\n";
    return xml;
  }

  private static String GetXMLDayOfWeek(Translation translation, String formatSupplement, Boolean enlarge, Boolean rtl) {
    String xml = new String();
    xml += "  <Tag_inside aid:table=\"cell\" aid:crows=\"1\" aid:ccols=\"1\" aid:ccolwidth=\"" + (rtl ? "45" : "52") + "\" aid5:cellstyle=\"" + (enlarge ? "cs_gross" : "cs_datum") + "\" aid:pstyle=\"wochentag" + formatSupplement + "\">\n";
    xml += "    <Wochentag>\n";
    xml += "      " + dayOfWeek + "\n";
    xml += "    </Wochentag>\n";
    xml += "  </Tag_inside>\n";
    return xml;
  }

  private static String GetXMLDate(Translation translation, Event event, Boolean enlarge, Boolean date_changed) {
    String xml = new String();
    xml += "  <Tag_inside aid:table=\"cell\" aid:crows=\"1\" aid:ccols=\"1\" aid:ccolwidth=\"40\" aid5:cellstyle=\"" + (enlarge ? "cs_gross" : "cs_datum") + "\" aid:pstyle=\"datum\">\n";
    if (!enlarge || date_changed) {
      // TODO get rid of the deprecated functions.
      xml += "    " + Integer.toString(event.getDate().getDay()) + "." + Integer.toString(event.getDate().getMonth()) + ".\n";
    }
    xml += "  </Tag_inside>\n";
    return xml;
  }
  
  private static String GetXMLTitle(Translation translation, int width, String formatSupplement) {
    String xml = new String();
    xml += "  <Tag_inside aid:table=\"cell\" aid:crows=\"1\" aid:ccols=\"1\" aid:ccolwidth=\"" + Integer.toString(width) + "\" aid5:cellstyle=\"cs_titel_gross\">\n";
    xml += "    <title aid:pstyle=\"titel" + formatSupplement + "\">\n";
    xml += "      " + translation.getTitle() + "\n";
    xml += "    </title>\n";
    xml += "  </Tag_inside>\n";
    return xml;
  }
  
  private static String GetXMLLocation(Translation translation, String formatSupplement, Boolean enlarge, Boolean rtl) {
    String xml = new String();
    if (translation.getLocation() != "") {
      if (enlarge) {
        xml += "    <Orttag aid:pstyle=\"ort_gross";
      } else {
        if (rtl) {
          xml += "    <Orttag aid:pstyle=\"ort";
        } else {
          xml += "    <Orttag aid:pstyle=\"ort_rtl";        
        }
      }
      // TODO remove special hack for _ru.
      xml += (formatSupplement == "_ru" ? formatSupplement : "") + "\">\n";
      xml += "      " + translation.getLocation() + "\n";

      if (translation.getUrl() != "") {
        // TODO remove this replacement hack.
        xml += "      " + translation.getUrl().replace("www", "http://www") + "\n";
      }
      xml += "    </Orttag>\n";
    }
    return xml;
  }
  
  /*

<bildtexte>
<?
 foreach ($bildtext_array as $value => $titel_spr){



?>
<<? echo "bild_".substr($value,8,2)."_". substr($value,5,2) ?>>
<Table_main xmlns:aid5="http://ns.adobe.com/AdobeInDesign/5.0/" aid5:tablestyle="ts_main" xmlns:aid="http://ns.adobe.com/AdobeInDesign/4.0/" aid:table="table" aid:trows="1" aid:tcols="1"><?
?><Tag_main aid:table="cell" aid:crows="1" aid:ccols="1" aid:ccolwidth="450" aid5:cellstyle="cs_bildtitel"><bilddatum  aid:pstyle="bilddatum"> <? echo substr($value,8,2).".". substr($value,5,2)."." ?> </bilddatum>
<p_titel  aid:pstyle="bildtitel" ><?
if(is_array($titel_spr)){
     foreach ($titel_spr as $value2 => $titel)
        {


?><b_titel  aid:cstyle="bildtitel<? echo $formatzusatz ?>" ><?

//echo " ".str_replace(" ",chr(160),$titel[1])." "; 
echo " ".$titel[1]." ";

?></b_titel><space  aid:cstyle="space" > </space><?
    } 
}
?>

</p_titel></Tag_main>
</Table_main>
</<? echo "bild_".substr($value,8,2)."_". substr($value,5,2) ?>>



<?

}

?>


<individuell>
<Table_main xmlns:aid5="http://ns.adobe.com/AdobeInDesign/5.0/" aid5:tablestyle="ts_main" xmlns:aid="http://ns.adobe.com/AdobeInDesign/4.0/" aid:table="table" aid:trows="1" aid:tcols="1"><Tag_main aid:table="cell" aid:crows="1" aid:ccols="1" aid:ccolwidth="450" aid5:cellstyle="cs_bildtitel"><bilddatum  aid:pstyle="bilddatum"> 03.12. </bilddatum>
<p_titel  aid:pstyle="bildtitel" ><b_titel  aid:cstyle="bildtitel" > Text text text... </b_titel>
</p_titel></Tag_main>
</Table_main></individuell></bildtexte></Root>
  /// XXX
  
  
  public static String getXMLForMonth(int year, int month) {
    return getXML(year, month, 0, year, month + 1, 0);
  }
  */

  /**
   * @return the date
   */
  public Date getDate() {
    return date;
  }

  /**
   * @param date the date of this event
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * @return the key
   */
  public long getKey() {
    return key;
  }

  /**
   * @return If true, this entity already has a key. 
   */
  public boolean hasKey() {
    return hasKey;
  }
  
  /**
   * @return the validation status of this Event
   */
  public boolean isOk() {
    return ok;
  }

  /**
   * Validates or invalidates an Event.
   *
   * @param ok the validation status to set
   */
  public void setOk(boolean ok) {
    this.ok = ok;
  }

  public Translation getGermanTranslation() {
    return germanTranslation;
  }

  public void setGermanTranslation(Translation germanTranslation) {
    this.germanTranslation = germanTranslation;
  }
  
  /**
   * Queries the store for a comprehensive list of translations of this event.
   *
   * @returns a list with language identifiers.
   */
  /*
  public List<String> GetLanguages() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey(entityKind, CreateKey(year, month, day, title));
    Query translationQuery = new Query().setAncestor(key).setFilter(
        new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.GREATER_THAN, key))
        .setKeysOnly();
    List<Entity> translations =
        datastore.prepare(translationQuery).asList(FetchOptions.Builder.withDefaults());

    // Extract the languages.
    Iterator<Entity> iterator = translations.iterator();
    List<String> languages = new ArrayList<String>();
    while (iterator.hasNext()) {
      languages.add(iterator.next().getKey().getName());
    }
    return languages;
  }
  */
}
