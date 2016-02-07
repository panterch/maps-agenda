//Controller for the phrases page.
adminApp.controller('PhraseCtrl', function ($scope, $http, languages, de_phrases,
                                            lang_phrases, lang, $location) {
  $scope.getLang = function(code) {
    for (var i = 0; i < $scope.languages.length; ++i) {
      if ($scope.languages[i].code == code)
        return $scope.languages[i];
    }
    return {};
  }
  $scope.preparePhrases = function(phrases, lang) {
    if (phrases == null) return null;
    local_phrases = [];
    for (var i = 0; i < phrases.length; ++i) {
      phrases[i].lang = lang.code;
      var p = {
          is_modified: false, // is different than the original entry?
          is_new: false,      // has been created using the "add a new phrase button"?
          is_deleted: false,  // asked to be deleted?
          is_added: true,     // is present in $scope.phrases? This can be false if an is_new item has not yet been saved.
          value: cloneObject(phrases[i]),
          backup: cloneObject(phrases[i]) // Used for the unedit functionality.
      }
      local_phrases.push(p);
    }
    return local_phrases;
  } 
  $scope.getPhraseForKey = function(key, phrases) {
    if (phrases == null) return null;
    for (var i = 0; i < phrases.length; ++i) {
      if (phrases[i].value.key == key) {
        return phrases[i];
      }
    }
    return null;
  }
  
  $scope.languages = languages;
  $scope.lang = $scope.getLang(lang);
  $scope.de_phrases = $scope.preparePhrases(de_phrases, $scope.getLang("de"));
  $scope.lang_phrases = $scope.preparePhrases(lang_phrases, $scope.lang);
  if ($scope.lang_phrases) {
    for (var i = 0; i < $scope.de_phrases.length; ++i) {
      var p_de = $scope.de_phrases[i];
      var p_lang = $scope.getPhraseForKey(p_de.value.key, $scope.lang_phrases);
      if (!p_lang) {
        p_lang = cloneObject(p_de);
        p_lang.value.phrase = "";
        p_lang.backup.phrase = "";
        p_lang.value.lang = $scope.lang.code;
        p_lang.backup.lang = $scope.lang.code;
        $scope.lang_phrases.push(p_lang);
      }
    }
  }
  
  $scope.getLangPhraseForKey = function(key) {
    if ($scope.lang_phrases == null) return "";
    for (var i = 0; i < $scope.lang_phrases.length; ++i) {
      if ($scope.lang_phrases[i].value.key == key) {
        return $scope.lang_phrases[i].value.phrase;
      }
    }
    return "";
  }
  $scope.p_filter = function(expr) {
    return function(p) {
      return !expr
          || p.value.key.indexOf(expr) != -1 
          || p.value.group.indexOf(expr) != -1
          || p.value.phrase.indexOf(expr) != -1
          || $scope.getLangPhraseForKey(p.value.phrase).indexOf(expr) != -1
          || (expr == 'true' && p.value.isTag)
          || (expr == 'false' && !p.value.isTag);
    }
  }
  $scope.updateLang = function() {
    $location.path("/translations/" + $scope.lang.code);
  }
  $scope.getRowClass = function(p) {
    if (p.is_deleted)
      return "red-hover deleted";
    else if (p.is_modified || p.is_new)
      return "modified-hover modified";
    else return "hover";
  }
  $scope.edit = function(key) {
    if (!key) { 
      $scope.de_phrase = {is_modified: false, is_new: true, is_added: false, is_deleted: false, 
                          value: {key: '', group: '', phrase: '', isTag: false, lang: "de"}};
      $scope.lang_phrase = {is_modified: false, is_new: true, is_added: false, is_deleted: false, 
                            value: {key: '', group: '', phrase: '', isTag: false, lang: $scope.lang.code}};
    } else {
      $scope.de_phrase = $scope.getPhraseForKey(key, $scope.de_phrases);
      $scope.lang_phrase = $scope.getPhraseForKey(key, $scope.lang_phrases);
      // Make copies of the object for the Cancel button.
      $scope.old_de = cloneObject($scope.de_phrase);
      $scope.old_lang = cloneObject($scope.lang_phrase);
    }    
    $scope.showPopup('edit-popup');
  }
  $scope.save = function() {
    // Find out why this is happening...
    if ($scope.cancel_pressed) {
      $scope.cancel_pressed = false;
      return;
    }
    if (!$scope.de_phrase.is_added) {
      // We can only add phrase if lang == de.
      if ($scope.countKeys($scope.de_phrase.value.key) > 0) {
        $scope.de_phrase.value.key = '';
        return;
      }
      $scope.de_phrases.push($scope.de_phrase);
      $scope.de_phrase.is_added = true;
      $scope.de_phrase.is_modified = true;
      $scope.de_phrase.is_deleted = false;
    } else if ($scope.countKeys($scope.de_phrase.value.key) > 1) {
      // The key can only be changed when lang == de.
      $scope.de_phrase.value.key = $scope.old_de.value.key;
      return;
    } else {
      // Note that it is important to check is_modified first because new items don't have backups.
      // TODO: this does not work. Try modifying back to original state. sameAsBackup is not called.
      $scope.de_phrase.is_modified = $scope.old_de.is_modified || !$scope.sameAsBackup();
      $scope.de_phrase.is_deleted = false;
      if ($scope.lang_phrase) {
        $scope.lang_phrase.is_modified = $scope.old_lang.is_modified || !$scope.sameAsBackup();
        $scope.lang_phrase.is_deleted = false;
      }
    }
    $scope.hidePopup('edit-popup');
  }
  $scope.cancel = function() {
    $scope.hidePopup('edit-popup');
    $scope.de_phrase.value = cloneObject($scope.old_de.value);
    if ($scope.lang_phrase) {
      $scope.lang_phrase.value = cloneObject($scope.old_lang.value);
    }
    $scope.cancel_pressed = true;
  }
  $scope.noneModified = function() {
    for (var i = 0; i < $scope.de_phrases.length; ++i) {
      // TODO: Should not count the new events that are marked deleted. 
      if ($scope.de_phrases[i].is_modified || $scope.de_phrases[i].is_deleted) {
        return false;
      }
    }
    if ($scope.lang_phrases) {
      for (var i = 0; i < $scope.lang_phrases.length; ++i) {
        // TODO: Should not count the new events that are marked deleted. 
        if ($scope.lang_phrases[i].is_modified || $scope.lang_phrases[i].is_deleted) {
          return false;
        }
      }
    }
    return true;
  }
  
  $scope.saveAll = function() {
    json = {
        save : [],
        remove : [],
        lang: $scope.lang.code
      }
    var phrases = ($scope.lang_phrases == null ? $scope.de_phrases : $scope.lang_phrases);
    for ( var i = 0; i < phrases.length; ++i) {
      if (phrases[i].is_new) {
        if (!phrases[i].is_deleted) {
          json.save.push(phrases[i].value);
        }
      } else if (phrases[i].is_modified) {
        json.save.push(phrases[i].value);
      } else if (phrases[i].is_deleted) {
        json.remove.push(phrases[i].value);
      }
    }
    $http({
      method : 'POST',
      url : '/admin/data?type=mphrases&modifications=' + JSON.stringify(json)
    }).success(function(data){
      if (data.success) {
        alert("Please reload to see the changes... Automatic update still under construction.");
      } else {
        alert("Saving failed: " + data.error)
      }
  });
  }
  
  $scope.remove = function(key) {
    p = $scope.getPhraseForKey(key, $scope.de_phrases);
    p.is_deleted = !p.is_deleted;
    p = $scope.getPhraseForKey(key, $scope.lang_phrases);
    if (p) {
      p.is_deleted = !p.is_deleted;
    }
  }
  $scope.unedit = function(key) {
    p = $scope.getPhraseForKey(key, $scope.de_phrases);
    if (!p.is_new) {
      p.value = cloneObject(p.backup);
      p.is_modified = false;
      p.is_deleted = false;
      p.is_added = true;
      p = $scope.getPhraseForKey(key, $scope.lang_phrases);
      if (p) {
        p.value = cloneObject(p.backup);
        p.is_modified = false;
        p.is_deleted = false;
        p.is_added = true;
      }
    }
  }
  $scope.showPopup = function(elemId) {
    document.getElementById(elemId).style.display = "block";
  }
  $scope.hidePopup = function(elemId) {
    document.getElementById(elemId).style.display = "none";
  }
  $scope.sameAsBackup = function() {
    if ($scope.lang.code == "de") {
      return $scope.de_phrase.value.key == $scope.de_phrase.backup.key &&
             $scope.de_phrase.value.group == $scope.de_phrase.backup.group &&
             $scope.de_phrase.value.phrase == $scope.de_phrase.backup.phrase &&
             $scope.de_phrase.value.isTag == $scope.de_phrase.backup.isTag;
    } else {
      return $scope.lang_phrase.value.phrase == $scope.lang_phrase.backup.phrase;
    }
  }
  $scope.countKeys = function(key) {
    var count = 0;
    for (var i = 0; i < $scope.de_phrases.length; ++i) {
      if ($scope.de_phrases[i].value.key == key)
        ++count;
    }
    return count;
  }
});
