(function() {
    'use strict';
  // Controller for the languages page.
  angular.module('app.admin')
  		.controller('LanguageCtrl', LanguageCtrl);

  function LanguageCtrl($scope, $http, languages, objectcloner){
    $scope.setLanguages = function(languages) {
  	  $scope.languages = [];
  	  for (var i = 0; i < languages.length; ++i) {
  	    var l = {
  	        is_modified: false, // is different than the original entry?
  	        is_new: false,      // has been created using the "add a new language button"?
  	        is_deleted: false,  // asked to be deleted?
  	        is_added: true,     // is present in $scope.languages? This can be false if an is_new item has not yet been saved.
  	        value: objectcloner.cloneObject(languages[i]),
  	        backup: objectcloner.cloneObject(languages[i]) // Used for the unedit functionality.
  	    }
  	    $scope.languages.push(l);
  	  }  	  
    }
    if (angular.isUndefined(languages)){
  	  $scope.setLanguages([]);
    } else {
  	  $scope.setLanguages(languages);	  
    }
   
    $scope.getRowClass = function(l) {
      if (l.is_deleted)
        return "red-hover deleted";
      else if (l.is_modified || l.is_new)
        return "modified-hover modified";
      else return "hover";
    }
    
    $scope.edit = function(code) {
      if (!code) { 
        $scope.language = {is_modified: false, is_new: true, is_added: false, is_deleted: false, 
      		             value: {days: [], isRtl: false, inAgenda: false, specificFormat: false}};
      } else {
        for (var i = 0; i < $scope.languages.length; ++i) {
          if ($scope.languages[i].value.code == code) {
            $scope.language = $scope.languages[i];
            // Make a copy of the object for the Cancel button.
            $scope.old_language = objectcloner.cloneObject($scope.language);
            break;
          }
        }
      }    
      $scope.showPopup('edit-popup');
    }
    $scope.save = function() {
      // Find out why this is happening...
      if ($scope.cancel_pressed) {
        $scope.cancel_pressed = false;
        return;
      }
      if (!$scope.language.is_added) {
        if ($scope.countCodes($scope.language.value.code) > 0) {
          $scope.language.value.code = '';
          return;
        }
        $scope.languages.push($scope.language);
        $scope.language.is_added = true;
        $scope.language.is_modified = true;
        $scope.language.is_deleted = false;
      } else if ($scope.countCodes($scope.language.value.code) > 1) {
        $scope.language.value.code = $scope.old_language.value.code;
        return;
      } else {
        // Note that it is important to check is_modified first because new items don't have backups.
        // TODO: this does not work. Try modifying back to original state. sameAsBackup is not called.
        $scope.language.is_modified = $scope.old_language.is_modified || !$scope.sameAsBackup();
        $scope.language.is_deleted = false;
      }
      $scope.hidePopup('edit-popup');
    }
    $scope.cancel = function() {
      $scope.hidePopup('edit-popup');
      $scope.language.value = objectcloner.cloneObject($scope.old_language.value);
      $scope.cancel_pressed = true;
    }
    $scope.noneModified = function() {
      for (var i = 0; i < $scope.languages.length; ++i) {
        // TODO: Should not count the new events that are marked deleted. 
        if ($scope.languages[i].is_modified || $scope.languages[i].is_deleted) {
          return false;
        }
      }
      return true;
    }
    $scope.saveAll = function() {
      var json = {
        save : [],
        remove : []
      }
      for ( var i = 0; i < $scope.languages.length; ++i) {
        if ($scope.languages[i].is_new) {
          if (!$scope.languages[i].is_deleted) {
            json.save.push($scope.languages[i].value);
          }
        } else if ($scope.languages[i].is_modified) {
          json.save.push($scope.languages[i].value);
        } else if ($scope.languages[i].is_deleted) {
          json.remove.push($scope.languages[i].value);
        }
      }
      $http({
        method : 'POST',
        url : '/admin/data?type=mlanguages&modifications=' + JSON.stringify(json)
      }).success(function(data){
        if (data.success) {
          $scope.setLanguages(data.languages);
        } else {
          alert("Saving failed: " + data.error)
        }
  	});
    }  
    $scope.remove = function(code) {
      for (var i = 0; i < $scope.languages.length; ++i) {
        if ($scope.languages[i].value.code == code) {
          $scope.languages[i].is_deleted = !$scope.languages[i].is_deleted;
          break;
        }
      }
    }
    $scope.unedit = function(code) {
      for (var i = 0; i < $scope.languages.length; ++i) {
        if ($scope.languages[i].value.code == code) {
          if (!$scope.languages[i].is_new) {
            $scope.languages[i].value = objectcloner.cloneObject($scope.languages[i].backup);
            $scope.languages[i].is_modified = false;
            $scope.languages[i].is_deleted = false;
            $scope.languages[i].is_added = true;
          }
          break;
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
      return $scope.language.value.code == $scope.language.backup.code &&
             $scope.language.value.name == $scope.language.backup.name &&
             $scope.language.value.germanName == $scope.language.backup.germanName &&
             $scope.language.value.days[0] == $scope.language.backup.days[0] &&
             $scope.language.value.days[1] == $scope.language.backup.days[1] &&
             $scope.language.value.days[2] == $scope.language.backup.days[2] &&
             $scope.language.value.days[3] == $scope.language.backup.days[3] &&
             $scope.language.value.days[4] == $scope.language.backup.days[4] &&
             $scope.language.value.days[5] == $scope.language.backup.days[5] &&
             $scope.language.value.days[6] == $scope.language.backup.days[6] &&
             $scope.language.value.isRtl == $scope.language.backup.isRtl &&
             $scope.language.value.inAgenda == $scope.language.backup.inAgenda &&
             $scope.language.value.specificFormat == $scope.language.backup.specificFormat;
    }
    $scope.countCodes = function(code) {
      var count = 0;
      for (var i = 0; i < $scope.languages.length; ++i) {
        if ($scope.languages[i].value.code == code)
          ++count;
      }
      return count;
    }
  };
})();