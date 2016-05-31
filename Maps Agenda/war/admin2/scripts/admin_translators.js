// Controller for the translators page.
adminApp.controller('TranslatorCtrl', function ($scope, $http, languages, translators) {
  $scope.languages = languages;
  
  $scope.setTranslators = function(translators) {
	  $scope.translators = [];
	  if (translators == null)
		  return
	  for (var i = 0; i < translators.length; ++i) {
	    var t = {
	        is_modified: false, // is different than the original entry?
	        is_new: false,      // has been created using the "add a new translator button"?
	        is_deleted: false,  // asked to be deleted?
	        is_added: true,     // is present in $scope.translators? This can be false if an is_new item has not yet been saved.
	        value: cloneObject(translators[i]),
	        backup: cloneObject(translators[i]) // Used for the unedit functionality.
	    }
	    $scope.translators.push(t);
	  }  	  
  } 
  $scope.setTranslators(translators);
  
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
      $scope.translator = {is_modified: false, is_new: true, is_added: false, is_deleted: false, value: {langs: []} };
    } else {
      for (var i = 0; i < $scope.translators.length; ++i) {
        if ($scope.translators[i].value.email == email) {
          $scope.translator = $scope.translators[i];
          // Make a copy of the object for the Cancel button.
          $scope.old_translator = cloneObject($scope.translator);
          if ($scope.translator.value.langs) {
            for (var j = 0; j < $scope.translator.value.langs.length; ++j) {
              if (!$scope.getLang($scope.translator.value.langs[j])) {
                $scope.lang_options.push({code: $scope.translator.value.langs[j], germanName:$scope.translator.value.langs[j] + " (unknown)"});                
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
      if ($scope.countEmails($scope.translator.value.email) > 0) {
        $scope.translator.value.email = '';
        return;
      }
      $scope.translators.push($scope.translator);
      $scope.translator.is_added = true;
      $scope.translator.is_modified = true;
      $scope.translator.is_deleted = false;
    } else if ($scope.countEmails($scope.translator.value.email) > 1) {
      $scope.translator.value.email = $scope.old_translator.value.email;
      return;
    } else {
      // Note that it is important to check is_modified first because new items don't have backups.
      $scope.translator.is_modified = $scope.old_translator.is_modified || !$scope.sameAsBackup();
      $scope.translator.is_deleted = false;
    }
    $scope.hidePopup('edit-popup');
  }
  $scope.cancel = function() {
    $scope.hidePopup('edit-popup');
    $scope.translator.value = cloneObject($scope.old_translator.value);
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
    json = {
        save: [],
        remove: []
    }    
    for (var i = 0; i < $scope.translators.length; ++i) {
      if ($scope.translators[i].is_new) {
        if (!$scope.translators[i].is_deleted) {
          json.save.push($scope.translators[i].value);
        }
      } else if ($scope.translators[i].is_modified) {
        json.save.push($scope.translators[i].value);
      } else if ($scope.translators[i].is_deleted) {
        json.remove.push($scope.translators[i].value);
      }
    }
    $http({
      method : 'POST',
          url : '/admin/data?type=mtranslators&modifications=' + JSON.stringify(json)
        }).success(function(data){
          if (data.success) {
        	  $scope.setTranslators(data.translators);
          } else {
        	  alert("Saving failed: " + data.error)
          }
        });
  }
  $scope.remove = function(email) {
    for (var i = 0; i < $scope.translators.length; ++i) {
      if ($scope.translators[i].value.email == email) {
        $scope.translators[i].is_deleted = !$scope.translators[i].is_deleted;
        break;
      }
    }
  }
  $scope.unedit = function(email) {
    for (var i = 0; i < $scope.translators.length; ++i) {
      if ($scope.translators[i].value.email == email) {
        if (!$scope.translators[i].is_new) {
          $scope.translators[i].value = cloneObject($scope.translators[i].backup);
          $scope.translators[i].is_modified = false;
          $scope.translators[i].is_deleted = false;
          $scope.translators[i].is_added = true;
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
          || t.value.email.indexOf(expr) != -1 
          || t.value.name.indexOf(expr) != -1
          || $scope.toGermanNames(t.value.langs).join("\n").indexOf(expr) != -1;
    }
  }
  $scope.showPopup = function(elemId) {
    document.getElementById(elemId).style.display = "block";
  }
  $scope.hidePopup = function(elemId) {
    document.getElementById(elemId).style.display = "none";
  }
  $scope.sameAsBackup = function() {
    var same = $scope.translator.value.email == $scope.translator.backup.email &&
                $scope.translator.value.name == $scope.translator.backup.name &&
                $scope.translator.value.langs.length == $scope.translator.backup.langs.length;
    if (!same) return false;
    for (var i = 0; i < $scope.translator.value.langs.length; ++i) {
      if ($scope.translator.value.langs[i] != $scope.translator.backup.langs[i])
        return false;
    }
    return true;
  }
  $scope.countEmails = function(email) {
    var count = 0;
    for (var i = 0; i < $scope.translators.length; ++i) {
      if ($scope.translators[i].value.email == email)
        ++count;
    }
    return count;
  }
});
