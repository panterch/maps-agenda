// Controller for the translators page.
adminApp.controller('TranslatorCtrl', function ($scope, languages, 
                                                translators) {
  $scope.languages = languages;
  $scope.translators = translators;
  for (var i = 0; i < $scope.translators.length; ++i) {
    $scope.translators[i].is_modified = false; // different than the original entry
    $scope.translators[i].is_new = false; // created using the "add a new translator button"
    $scope.translators[i].is_deleted = false; // asked to be deleted
    $scope.translators[i].is_added = true; // is present in $scope.translators
    $scope.translators[i].backup = cloneObject($scope.translators[i]);
  }  
  
  $scope.getRowClass = function(t) {
    if (t.is_deleted)
      return "red-hover deleted";
    else if (t.is_modified || t.is_new)
      return "modified-hover modified";
    else return "hover";
  }
  $scope.edit = function(email) {
    $scope.lang_options = [];
    $scope.select_length = 30;
    for (var i = 0; i < $scope.languages.length; ++i) {
      $scope.lang_options.push($scope.languages[i]);
    }
    if (!email) { 
      $scope.translator = {is_modified: false, is_new: true, is_added: false, is_deleted: false, langs: [] };
    } else {
      for (var i = 0; i < $scope.translators.length; ++i) {
        if ($scope.translators[i].email == email) {
          $scope.translator = $scope.translators[i];
          // Make a copy of the object for the Cancel button.
          $scope.old_translator = cloneObject($scope.translator);
          if ($scope.translator.langs) {
            for (var j = 0; j < $scope.translator.langs.length; ++j) {
              if (!$scope.getLang($scope.translator.langs[j])) {
                $scope.lang_options.push({code: $scope.translator.langs[j], germanName:$scope.translator.langs[j] + " (unknown)"});                
              }
            }
          }
          break;
        }
      }
    }    
    document.getElementById('select').setAttribute('size', $scope.lang_options.length)
    $scope.showPopup('edit-popup');
  }
  $scope.save = function() {
    // Find out why this is happening...
    if ($scope.cancel_pressed) {
      $scope.cancel_pressed = false;
      return;
    }
    if (!$scope.translator.is_added) {
      if ($scope.countEmails($scope.translator.email) > 0) {
        $scope.translator.email = '';
        return;
      }
      $scope.translators.push($scope.translator);
      $scope.translator.is_added = true;
      $scope.translator.is_modified = true;
      $scope.translator.is_deleted = false;
    } else if ($scope.countEmails($scope.translator.email) > 1) {
      $scope.translator.email = $scope.old_translator.email;
      return;
    } else {
      $scope.translator.is_modified = !$scope.sameAsBackup() || $scope.old_translator.is_modified;
      $scope.translator.is_deleted = false;
    }
    $scope.hidePopup('edit-popup');
  }
  $scope.cancel = function() {
    $scope.hidePopup('edit-popup');
    $scope.translator.email = $scope.old_translator.email;
    $scope.translator.name = $scope.old_translator.name;
    $scope.translator.langs = $scope.old_translator.langs;
    $scope.cancel_pressed = true;
  }
  $scope.noneModified = function() {
    for (var i = 0; i < $scope.translators.length; ++i) {
      if ($scope.translators[i].is_modified || $scope.translators[i].is_deleted) {
        return false;
      }
    }
    return true;
  }
  $scope.saveAll = function() {
    alert("Save capabilities coming soon.");
  }
  $scope.remove = function(email) {
    for (var i = 0; i < $scope.translators.length; ++i) {
      if ($scope.translators[i].email == email) {
        $scope.translators[i].is_deleted = !$scope.translators[i].is_deleted;
        break;
      }
    }
  }
  $scope.unedit = function(email) {
    for (var i = 0; i < $scope.translators.length; ++i) {
      if ($scope.translators[i].email == email) {
        if (!$scope.translators[i].is_new) {
          var b = $scope.translators[i].backup;
          $scope.translators[i] = cloneObject(b);
          $scope.translators[i].backup = b;
        }
        break;
      }
    }
  }
  $scope.getLang = function(code) {
    for (var i = 0; i < $scope.languages.length; ++i) {
      if ($scope.languages[i].code == code) {
        return $scope.languages[i];
      }
    }
    return null;
  }
  $scope.toGermanNames = function(langs) {
    var german_names = [];
    if (!langs) return german_names;
    for (var i = 0; i < langs.length; ++i) {
      var l = $scope.getLang(langs[i]);
      if (l) {
        german_names.push(l.germanName);
      } else {
        german_names.push(langs[i] + " (unknown)")
      }
    }
    return german_names;
  }
  $scope.getLangStrings = function(langs) {
    return $scope.toGermanNames(langs).join(", ");
  }
  $scope.t_filter = function(expr) {
    return function(t) {
      return !expr
          || t.email.indexOf(expr) != -1 
          || t.name.indexOf(expr) != -1
          || $scope.toGermanNames(t.langs).join("\n").indexOf(expr) != -1;
    }
  }
  $scope.showPopup = function(elemId) {
    document.getElementById(elemId).style.display = "block";
  }
  $scope.hidePopup = function(elemId) {
    document.getElementById(elemId).style.display = "none";
  }
  $scope.sameAsBackup = function() {
    var same = $scope.translator.email == $scope.old_translator.email &&
                $scope.translator.name == $scope.old_translator.name &&
                $scope.translator.langs.length == $scope.old_translator.langs.length;
    if (!same) return false;
    for (var i = 0; i < $scope.translator.langs.length; ++i) {
      if ($scope.translator.langs[i] != $scope.old_translator.langs[i])
        return false;
    }
    return true;
  }
  $scope.countEmails = function(email) {
    var count = 0;
    for (var i = 0; i < $scope.translators.length; ++i) {
      if ($scope.translators[i].email == email)
        ++count;
    }
    return count;
  }
});
