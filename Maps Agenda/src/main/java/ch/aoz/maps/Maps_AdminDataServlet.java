package ch.aoz.maps;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("serial")
public class Maps_AdminDataServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String response = null;
    switch (req.getParameter("type")) {
    case "languages":
      response = getLanguages();
      break;
    case "events":
      response = getEvents(req.getParameter("lang"),
          stringToMonth(req.getParameter("month")));
      break;
    case "phrases":
      response = getPhrases(req.getParameter("lang"));
      break;
    case "translators":
      response = getTranslators();
      break;
    case "newsletter":
      response = getNewsletters(req);
      break;
    case "campaign":
      response = createCampaign(req);
      break;
    case "mailchimp_credentials":
      response = getMailChimpCredentials();
      break;
    case "save_mailchimp_credentials":
      response = setMailChimpCredentials(req);
      break;
    }
    if (response == null) {
      JSONObject json = new JSONObject();
      json.put("success", false);
      json.put("error", "Unknown request type: " + req.getParameter("type"));
      response = json.toString();
    }
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.getWriter().println(response);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String response = null;
    switch (req.getParameter("type")) {
    case "mtranslators":
      response = modifyTranslators(req);
      break;
    case "mlanguages":
      response = modifyLanguages(req);
      break;
    case "mphrases":
      response = modifyPhrases(req);
      break;
    case "mevents":
      response = modifyEvents(req);
      break;
    }
    if (response == null) {
      JSONObject json = new JSONObject();
      json.put("success", false);
      json.put("error", "Unknown request type: " + req.getParameter("type"));
      response = json.toString();
    }
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.getWriter().println(response);
  }

  private String getPhrases(String lang) {
    JSONObject json = new JSONObject();
    for (Phrase p : Phrases.GetPhrasesForLanguage(lang).getPhrases()) {
      json.append("phrases", p.toJSON());
    }
    return json.toString();
  }

  private String getEvents(String language, Calendar month) {
    Language lang = Language.GetByCode(language);
    if (lang == null) {
      lang = Language.GetByCode("de");
    }

    // Set the time at midnight, so that the below query stays the same.
    month.set(Calendar.MILLISECOND, 0);
    month.set(Calendar.SECOND, 0);
    month.set(Calendar.MINUTE, 0);
    month.set(Calendar.HOUR_OF_DAY, 0);
    month.set(Calendar.DATE, 1);

    Events events = Events.getEvents(month, lang.getCode());

    JSONObject json = new JSONObject();
    for (Event e : events.getSortedEvents()) {
      if (e.getDescription() != null) {
        json.append("events", e.toJSON());
      }
    }
    return json.toString();
  }

  public String getLanguages() {
    JSONObject json = new JSONObject();
    for (Language l : Language.getAllLanguages()) {
      json.append("languages", l.toJSON());
    }
    return json.toString();
  }

  public String getTranslators() {
    JSONObject json = new JSONObject();
    for (Translator t : Translator.getAllTranslators().values()) {
      json.append("translators", t.toJSON());
    }
    return json.toString();
  }

  // TODO (pascalgwosdek) remove this function and the whole getter hierarchy.
  public String getNewsletters(HttpServletRequest req) {
    Calendar date = Calendar.getInstance();
    String requested_date = req.getParameter("month");
    if (requested_date != null) {
      try {
        date.setTime(new SimpleDateFormat("yyyy-MM").parse(requested_date));
      } catch (Exception e) {
      }
    }
    // Set the time at midnight, so that the below query stays the same.
    date.set(Calendar.MILLISECOND, 0);
    date.set(Calendar.SECOND, 0);
    date.set(Calendar.MINUTE, 0);
    date.set(Calendar.HOUR_OF_DAY, 0);
    date.set(Calendar.DATE, 1);

    StringBuilder response = new StringBuilder();
    response.append("{ \"newsletters\": {");

    Set<Language> langs = Language.getAllLanguages();
    Events eventsDe = Events.getEvents(date, "de");
    String baseUrl = "localhost".equals(req.getServerName()) ? "http://localhost:8888"
        : "http://www.maps-agenda.ch";

    for (Language l : langs) {
      Events eventsLang = null;
      if (!l.getCode().equals("de")) {
        eventsLang = eventsDe.clone();
        eventsLang.loadDescriptions(l.getCode());
      }
      NewsletterExport exporter = new NewsletterExport(eventsDe, eventsLang,
          l.getCode(), baseUrl, date.get(Calendar.YEAR),
          date.get(Calendar.MONTH), null /* subscriber, none for public render. */);

      response.append("\"" + l.getCode() + "\":\""
          + Utils.toUnicode(exporter.render()) + "\",");
    }
    if (response.charAt(response.length() - 1) == ',') {
      response.deleteCharAt(response.length() - 1); // remove the last ,
    }
    response.append("}}");
    return response.toString();
  }

  public Map<String, String> generateNewslettersList(Calendar date,
      String color, String serverName) {
    Map<String, String> response = new HashMap<String, String>();
    Set<Language> langs = Language.getAllLanguages();

    Events eventsDe = Events.getEvents(date, "de");
    String baseUrl = "localhost".equals(serverName) ? "http://localhost:8888"
        : "http://www.maps-agenda.ch";

    for (Language l : langs) {
      Events eventsLang = null;
      if (!l.getCode().equals("de")) {
        eventsLang = eventsDe.clone();
        eventsLang.loadDescriptions(l.getCode());
      }
      NewsletterExport exporter = new NewsletterExport(eventsDe, eventsLang,
          l.getCode(), baseUrl, date.get(Calendar.YEAR),
          date.get(Calendar.MONTH), null /* subscriber, none for public render. */);

      response.put(
          l.getCode(),
          exporter.render().replaceAll(Pattern.quote("{{background_color}}"),
              color));
    }
    return response;
  }

  public String generateNewsletters(Calendar date, String color,
      String serverName) {
    Map<String, String> newsletters = generateNewslettersList(date, color,
        serverName);
    StringBuilder builder = new StringBuilder();
    builder.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional"
        + "//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transition"
        + "al.dtd\">\n");
    builder.append("*|");
    for (String code : newsletters.keySet()) {
      if (!code.equals("en")) {
        builder.append("IF:LANGUAGE=" + code + "|*" + newsletters.get(code)
            + "*|ELSE");
      }
    }
    builder.append(":|*" + newsletters.get("en") + "*|END:IF|*");
    return builder.toString();
    // return Utils.toUnicode(builder.toString());
  }

  public JSONObject mailchimpPost(String url, String apiKey, JSONObject request) {
    byte[] mailchimpRequest = request.toString().getBytes(StandardCharsets.UTF_8);
    StringBuilder response = new StringBuilder();
    HttpURLConnection connection = null;
    try {
      String auth = "anystring:" + apiKey;
      String basicAuth = "Basic " + new String(Base64.getEncoder().encode(auth.getBytes()));
      URL urlObj = new URL(url);
      connection = (HttpURLConnection) urlObj.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("charset", "utf-8");
      connection.setRequestProperty("Content-Length",
          Integer.toString(mailchimpRequest.length));
      connection.setRequestProperty ("Authorization", basicAuth);
      connection.setDoOutput(true);
      connection.setUseCaches(false);

      DataOutputStream outStream = new DataOutputStream(
          connection.getOutputStream());
      outStream.write(mailchimpRequest);
      outStream.flush();
      outStream.close();

      connection.getResponseCode();
      InputStream stream = connection.getErrorStream();
      if (stream == null) {
        stream = connection.getInputStream();
      }

      InputStreamReader inStream = new InputStreamReader(stream);
      BufferedReader reader = new BufferedReader(inStream);
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line);
        response.append('\r');
      }
      reader.close();
    } catch (Exception e) {
      // Emulate a Mailchimp error, for consistent error handling below.
      JSONObject error = new JSONObject();
      error.put("type", "https://mailchimp.com/developer/marketing/docs/errors/");
      error.put("status", 500);
      error.put("detail", "Error while sending request to MailChimp: " + e.toString());
      return error;
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
    return new JSONObject(response.toString());
  }

  public boolean isError(JSONObject json) {
    try {
      if (json.getString("type") != "https://mailchimp.com/developer/marketing/docs/errors/") {
        return false;
      }
      if (json.getInt("status") != 200) {
        return true;
      }
    } catch (Exception e) {}
    return false;
  }

  public JSONObject initCampaign(MailChimpCredentials credentials, String title, int templateID) {
    // See https://mailchimp.com/developer/marketing/api/campaigns/add-campaign/.
    String url = "https://" + credentials.getApiKey().split("-")[1]
        + ".api.mailchimp.com/3.0/campaigns";

    // Outer request object.
    JSONObject request = new JSONObject();
    request.put("type", "regular");
    request.put("content_type", "template");

    // Request's recipients sub-object.
    JSONObject recipients = new JSONObject();
    recipients.put("list_id", credentials.getListId());
    request.put("recipients", recipients);

    // Request's settings sub-object.
    JSONObject settings = new JSONObject();
    settings.put("title", title);
    settings.put("subject_line", title);
    settings.put("from_name", JSONObject.stringToValue(Phrases.getMergedPhrases("de").get("zuriAgenda").getPhrase()));
    settings.put("reply_to", "maps@aoz.ch");
    settings.put("to_name", "*|NAME|*");
    settings.put("template_id", templateID);
    request.put("settings", settings);

    // POST to /campaigns.
    return mailchimpPost(url, credentials.getApiKey(), request);
  }

  public JSONObject createTemplate(MailChimpCredentials credentials, String title, Calendar date, String color, String serverName) {
    // See https://mailchimp.com/developer/marketing/api/campaign-content/set-campaign-content/.
    String url = "https://" + credentials.getApiKey().split("-")[1]
        + ".api.mailchimp.com/3.0/templates";

    JSONObject template = new JSONObject();
    template.put("name", title);
    template.put("html", generateNewsletters(date, color, serverName));
    return mailchimpPost(url, credentials.getApiKey(), template);
  }

  public String createCampaign(HttpServletRequest req) {
    // Parse the requested date for the newsletter.
    Calendar date = Calendar.getInstance();
    String requested_date = req.getParameter("month");
    if (requested_date != null) {
      try {
        date.setTime(new SimpleDateFormat("yyyy-MM").parse(requested_date));
      } catch (Exception e) {
      }
    }
    // Morph the time to midnight, so that the query stays the same.
    date.set(Calendar.MILLISECOND, 0);
    date.set(Calendar.SECOND, 0);
    date.set(Calendar.MINUTE, 0);
    date.set(Calendar.HOUR_OF_DAY, 0);
    date.set(Calendar.DATE, 1);

    // Title for the template and for the campaign.
    String title = "MAPS Agenda Newsletter "
        + date.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.GERMAN)
        + " " + date.get(Calendar.YEAR);

    // Determine the color for this newsletter.
    String bgColor = req.getParameter("bgcolor");
    if (bgColor == null) {
      bgColor = BackgroundColor.fetchFromStore().getColor();
    }

    // Create a new template with the contents for the campaign.
    MailChimpCredentials credentials = MailChimpCredentials.fetchFromStore();
    JSONObject templateResponse = createTemplate(credentials, title, date, bgColor, req.getServerName());
    if (isError(templateResponse)) {
      return templateResponse.toString();
    }

    // Create a new capmaign.
    return initCampaign(credentials, title, templateResponse.getInt("id")).toString();
  }

  public String modifyTranslators(HttpServletRequest req) {
    JSONObject response = new JSONObject();
    if (req.getParameter("modifications") == null
        || req.getParameter("modifications").equals("")) {
      response.put("success", false);
      response.put("error", "No modifications");
      return response.toString();
    }

    try {
      JSONObject json = new JSONObject(req.getParameter("modifications"));
      response.put("request", json);
      for (int i = 0; i < json.getJSONArray("save").length(); ++i) {
        JSONObject o = json.getJSONArray("save").getJSONObject(i);
        JSONArray ls = o.getJSONArray("langs");
        ArrayList<String> langs = new ArrayList<String>();
        for (int j = 0; j < ls.length(); ++j) {
          langs.add(ls.getString(j));
        }
        Translator t = new Translator(o.getString("email"),
            o.getString("name"), langs);
        if (!t.isOk()) {
          response.put("success", false);
          response.put("error",
              "Failed to save translator: " + o.getString("email"));
          return response.toString();
        } else if (!Translator.AddTranslator(t)) {
          response.put("success", false);
          response.put("error",
              "Failed to save translator: " + o.getString("email"));
          return response.toString();
        }
      }
      for (int i = 0; i < json.getJSONArray("remove").length(); ++i) {
        JSONObject o = json.getJSONArray("remove").getJSONObject(i);
        if (!Translators.removeTranslator(o.getString("email"))) {
          response.put("success", false);
          response.put("error",
              "Failed to remove translator: " + o.getString("email"));
          return response.toString();
        }
      }
      JSONObject translators = new JSONObject(getTranslators());
      response.put("success", true);
      response.put("translators", translators.getJSONArray("translators"));
    } catch (JSONException e) {
      response.put("success", false);
      response.put("error", e.getMessage());
      response.put("request", req.getParameter("modifications"));
    }
    return response.toString();
  }

  public String modifyLanguages(HttpServletRequest req) {
    JSONObject response = new JSONObject();
    if (req.getParameter("modifications") == null
        || req.getParameter("modifications").equals("")) {
      response.put("success", false);
      response.put("error", "No modifications");
      return response.toString();
    }

    try {
      JSONObject json = new JSONObject(req.getParameter("modifications"));
      response.put("request", json);
      for (int i = 0; i < json.getJSONArray("save").length(); ++i) {
        JSONObject o = json.getJSONArray("save").getJSONObject(i);
        Language l = new Language(o);
        if (!l.isOk()) {
          response.put("success", false);
          response.put("error",
              "Failed to save language with code=" + o.getString("code"));
          return response.toString();
        } else if (!Languages.addLanguage(l)) {
          response.put("success", false);
          response.put("error",
              "Failed to save language with code=" + o.getString("code"));
          return response.toString();
        }
      }
      for (int i = 0; i < json.getJSONArray("remove").length(); ++i) {
        JSONObject o = json.getJSONArray("remove").getJSONObject(i);
        if (!Languages.removeLanguage(o.getString("code"))) {
          response.put("success", false);
          response.put("error",
              "Failed to remove language with code=" + o.getString("code"));
          return response.toString();
        }
      }
      JSONObject languages = new JSONObject(getLanguages());
      response.put("success", true);
      response.put("languages", languages.getJSONArray("languages"));
    } catch (JSONException e) {
      response.put("success", false);
      response.put("error", e.getMessage());
      response.put("request", req.getParameter("modifications"));
    }
    return response.toString();
  }

  public String modifyPhrases(HttpServletRequest req) {
    JSONObject response = new JSONObject();
    if (req.getParameter("modifications") == null
        || req.getParameter("modifications").equals("")) {
      response.put("success", false);
      response.put("error", "No modifications");
      return response.toString();
    }

    try {
      JSONObject json = new JSONObject(req.getParameter("modifications"));
      response.put("request", json);
      for (int i = 0; i < json.getJSONArray("save").length(); ++i) {
        JSONObject o = json.getJSONArray("save").getJSONObject(i);
        Phrase p = new Phrase(o);
        if (!p.isOk()) {
          response.put("success", false);
          response.put("error",
              "Failed to save phrase with key=" + o.getString("key"));
          return response.toString();
        } else if (!p.addToStore()) {
          response.put("success", false);
          response.put("error",
              "Failed to save phrase with key=" + o.getString("key"));
          return response.toString();
        }
      }
      for (int i = 0; i < json.getJSONArray("remove").length(); ++i) {
        JSONObject o = json.getJSONArray("remove").getJSONObject(i);
        if (!Phrases.deleteKey(o.getString("key"))) {
          response.put("success", false);
          response.put("error",
              "Failed to remove phrase with key=" + o.getString("key"));
          return response.toString();
        }
      }
      JSONObject phrases = new JSONObject(getPhrases(json.getString("lang")));
      response.put("success", true);
      response.put("phrases", phrases.getJSONArray("phrases"));
    } catch (JSONException e) {
      response.put("success", false);
      response.put("error", e.getMessage());
      response.put("request", req.getParameter("modifications"));
    }
    return response.toString();
  }

  public String modifyEvents(HttpServletRequest req) {
    JSONObject response = new JSONObject();
    if (req.getParameter("modifications") == null
        || req.getParameter("modifications").equals("")) {
      response.put("success", false);
      response.put("error", "No modifications");
      return response.toString();
    }

    try {
      JSONObject json = new JSONObject(req.getParameter("modifications"));
      response.put("request", json);
      for (int i = 0; i < json.getJSONArray("save").length(); ++i) {
        JSONObject o = json.getJSONArray("save").getJSONObject(i);
        Event e = new Event(o);
        if (!e.isOk()) {
          response.put("success", false);
          response.put("error", "Failed to save event: " + e);
          return response.toString();
        } else if (!Events.addEvent(e)) {
          response.put("success", false);
          response.put("error", "Failed to save event: " + e);
          return response.toString();
        }
      }
      for (int i = 0; i < json.getJSONArray("remove").length(); ++i) {
        JSONObject o = json.getJSONArray("remove").getJSONObject(i);
        Event e = new Event(o);
        if (!Events.removeEvent(e.getKey(), e.getCalendar())) {
          response.put("success", false);
          response.put("error", "Failed to remove event: " + e);
          return response.toString();
        }
      }
      JSONObject events = new JSONObject(getEvents(json.getString("lang"),
          stringToMonth(json.getString("month"))));
      response.put("success", true);
      response.put("events", events.getJSONArray("events"));
    } catch (JSONException e) {
      response.put("success", false);
      response.put("error", e.getMessage());
      response.put("request", req.getParameter("modifications"));
    }
    return response.toString();
  }

  private String getMailChimpCredentials() {
    MailChimpCredentials credentials = MailChimpCredentials.fetchFromStore();
    JSONObject response = new JSONObject();
    response.put("list_id", credentials.getListId());
    response.put("api_key", credentials.getApiKey());
    return response.toString();
  }

  private String setMailChimpCredentials(HttpServletRequest req) {
    String listId = req.getParameter("list_id");
    String apiKey = req.getParameter("api_key");
    if (listId == null || listId == "" || apiKey == null || apiKey == "") {
      JSONObject response = new JSONObject();
      response.put("error", "Empty arguments.");
      return response.toString();
    }

    MailChimpCredentials credentials = new MailChimpCredentials(listId, apiKey);
    if (!credentials.addToStore()) {
      JSONObject response = new JSONObject();
      response.put("error", "Failed to add data to the store.");
      return response.toString();
    }

    return new JSONObject().toString();
  }

  private Calendar stringToMonth(String s) {
    Calendar month = Calendar.getInstance();
    if (s == null)
      return month;

    try {
      month.setTime(new SimpleDateFormat("yyyy-MM").parse(s));
    } catch (Exception e) {
    }
    return month;
  }
}
