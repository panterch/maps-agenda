common = {};

common.languageDict_ = {
  "woabdienstag":"Di",
  "moapril":"April",
  "wtabsonntag":"So",
  "moabseptember":"Sept.",
  "wtdienstag":"Dienstag",
  "UeberMAPS":"\u00dcber MAPS",
  "wtmittwoch":"Mittwoch",
  "impprojektleitung":"Projektleitung",
  "nlabbestellen":"Newsletter abbestellen",
  "modezember":"Dezember",
  "senden":"Senden",
  "moabjuni":"Juni",
  "headNL":"Was l\u00e4uft in Z\u00fcrich?",
  "Impressum":"Impressum",
  "Frueher":"Fr\u00fcher",
  "keinepassVeranst":"Es konnten leider keine passenden Veranstaltungen gefunden werden.",
  "nlstadtverwaltung":"Ihre Mitteilung wird mit diesem Formular verschl\u00fcsselt an die Stadtverwaltung Z\u00fcrich \u00fcbertragen.",
  "ktstrasse":"Strasse / Hausnummer",
  "mojuli":"Juli",
  "moabnovember":"Nov.",
  "kttelefon":"Telefon",
  "nlkorrekt":"Wurde diese E-Mail korrekt angezeigt?",
  "Spaeter":"Sp\u00e4ter",
  "ktanhang":"Die maximale Gr\u00f6sse f\u00fcr Anh\u00e4nge betr\u00e4gt 5 MB (5120 KB).",
  "moabjuli":"Juli",
  "ktemail":"E-Mail",
  "ktbetreff":"Betreff",
  "mooktober":"Oktober",
  "monovember":"November",
  "ktanrede":"Anrede",
  "tagsport":"Sport",
  "moabaugust":"Aug.",
  "wtabmontag":"Mo",
  "wtabsamstag":"Sa",
  "wtsonntag":"Sonntag",
  "moabapril":"Apr.",
  "nlkopie":"Ich m\u00f6chte eine unverschl\u00fcsselte Kopie dieser Nachricht an meine E-Mail-Adresse erhalten.",
  "imherausgeberin":"Herausgeberin",
  "Newsletter":"Newsletter",
  "moaugust":"August",
  "ktfirma":"Firma / Organisation",
  "ktherr":"Herr",
  "wtfreitag":"Freitag",
  "imponline":"Online",
  "Wochentage":"Montag",
  "ktnachname":"Nachname",
  "ktFeld":"Dieses Feld muss ausgef\u00fcllt werden.",
  "kzadresse":"Adresse",
  "impfotos":"Fotos",
  "tagnatur":"Natur",
  "GKFZA":"G\u00fcnstige Kultur- und Freizeitangebote",
  "ktdurchsuchen":"Durchsuchen",
  "mojanuar":"Januar",
  "moabjanuar":"Jan.",
  "impprojektleitungsassistenz":"Projektleitungsassistenz",
  "moabmaerz":"M\u00e4rz",
  "moaboktober":"Okt.",
  "moseptember":"September",
  "ktvorname":"Vorname",
  "taggratis":"Gratis",
  "wtabfreitag":"Fr",
  "impuebersetzer":"\u00dcbersetzer/innen",
  "momaerz":"M\u00e4rz",
  "tagaktiv":"Aktiv",
  "moabdezember":"Dez.",
  "momai":"Mai",
  "mojuni":"Juni",
  "ktmitteilung":"Mitteilung",
  "tagfamilie":"Familie",
  "wtdonnerstag":"Donnerstag",
  "Anmelden":"Anmelden",
  "moabmai":"Mai",
  "mofebruar":"Februar",
  "taglernen":"Lernen",
  "ktdatei":"Datei",
  "ktplz":"PLZ / Ort",
  "wtabmittwoch":"Mi",
  "ktfrau":"Frau",
  "tagdeutsch":"Deutsch",
  "impversand":"Versand- und Kurierdienst",
  "wtabdonnerstag":"Do",
  "nlweiterleiten":"Newsletter weiterleiten",
  "wtsamstag":"Samstag",
  "AnmeldungNewsletter":"Erhalten Sie die MAPS Z\u00fcri Agenda einmal pro Monat in Ihrer Sprache per E-Mail.",
  "Kontakt":"Kontakt",
  "impdruck":"Druck",
  "moabfebruar":"Feb.",
  "impkonzept":"Konzept"
};

common.getLanguageString = function(key) {
  return common.languageDict_[key] || '(empty)';
};

common.updateLanguage = function(newLang) {
  for (var key in newLang) {
    common.languageDict_[key] = newLang[key];
  }
};

common.applyLanguage = function() {
  ([]).forEach.call(document.querySelectorAll('[i18n]'), function(elem) {
    var key = elem.getAttribute('i18n');
    elem.innerText = common.getLanguageString(key);
  });
};

common.getSearchParams = function() {
  var hash = window.location.search.substring(1);
  var params = {};
  hash.split('&').forEach(function(pair) {
    var tokens = pair.split('=');
    var key = tokens.splice(0, 1)[0];
    var value = tokens.join('=');
    if (key) {
      params[key] = value;
    }
  });

  return params;
};

common.setSearchParams = function(params) {
  var pairs = [];
  for(var key in params) {
    pairs.push(key + '=' + params[key]);
  }

  window.location.search = '?' + pairs.join('&');
};

common.getSelectedLanguage = function() {
  return common.getSearchParams()['lang'] || 'de';
};

common.setSelectedLanguage = function(language) {
  var params = common.getSearchParams();
  params['lang'] = language;
  common.setSearchParams(params);
};

common.getStartDate = function() {
  var pivot = window.calPivot || new Date();

  function pad(number, spaces) {
    var output = '';
    for (var i = 0; i < spaces; i++) {
      output += '0';
    }
    output += number;

    return ([].splice.call(
        output.split(''),
        output.length - spaces,
        spaces)).join('');
  }

  return [
    pivot.getFullYear(),
    pad(pivot.getMonth() + 1, 2),
    pad(pivot.getDate(), 2)
  ].join('-');
};

function PopupCtrl($scope, $http) {
  $scope.showPopup = function(elemId) {
    var elem = document.getElementById(elemId);
    if (elem != null) {
      elem.style.display = "block";
    } else {
      console.log("Cannot show popup: " + elemId);
    }
  }

  $scope.hidePopup = function(elemId) {
    var elem = document.getElementById(elemId);
    if (elem != null) {
      elem.style.display = "none";
    } else {
      console.log("Cannot hide popup: " + elemId);
    }
  }
}

//2013-10-01
