// Controller for the languages page.
adminApp.controller('LanguageCtrl', function ($scope, languages) {
  $scope.languages = languages;
  $scope.edit = function(lang) {
    alert("Editing capabilities coming soon.");
    console.log("Language: " + lang);
  }
});

//Controller for the phrases page.
adminApp.controller('PhraseCtrl', function ($scope, languages, de_phrases,
                                            lang_phrases, lang, $location) {
  $scope.languages = languages;
  $scope.lang = lang;
  $scope.de_phrases = de_phrases;
  $scope.lang_phrases = lang_phrases;
  $scope.edit = function(key) {
    alert("Editing capabilities coming soon.");
    console.log("Key: " + key);
  }
  $scope.getPhraseForKey = function(key) {
    if (lang_phrases == null) return "";
    for (var i = 0; i < $scope.lang_phrases.length; ++i) {
      if ($scope.lang_phrases[i].key == key) {
        return $scope.lang_phrases[i].phrase;
      }
    }
    return "";
  }
  $scope.p_filter = function(expr) {
    return function(p) {
      return !expr
          || p.key.indexOf(expr) != -1 
          || p.group.indexOf(expr) != -1
          || p.phrase.indexOf(expr) != -1
          || $scope.getPhraseForKey(p.phrase).indexOf(expr) != -1
          || (expr == 'true' && p.isTag)
          || (expr == 'false' && !p.isTag);
    }
  }
  $scope.updateLang = function() {
    $location.path("/translations/" + $scope.lang);
  }
  $scope.edit = function(email) {
    alert("Editing capabilities coming soon.");
    console.log("Email: " + email);
  }
  $scope.remove = function(email) {
    alert("Deletion capabilities coming soon.");
    console.log("Delete email: " + email);
  }
});

// Controller for the events page.
adminApp.controller('EventCtrl', function ($scope, $location, month_str, events) {
  if (month_str == null || month_str == '') {
    $scope.month_str = monthToString(new Date())
    $scope.date = $scope.month_str + "-01";
    $scope.month = new Date($scope.date);
  } else {
    $scope.date = month_str + "-01";
    $scope.month = new Date($scope.date);
    $scope.month_str = monthToString($scope.month)
  }
  $scope.events = events;
  $scope.month_regex = /^[0-9]{4}-(0[0-9]|1[012])$/;
  $scope.previousMonth = function() {
    $scope.month.setMonth($scope.month.getMonth() - 1);
    $scope.month_str = monthToString($scope.month)
    $scope.date = $scope.month_str + "-01";
    $location.search('month', $scope.month_str);    
  }
  $scope.nextMonth = function() {
    $scope.month.setMonth($scope.month.getMonth() + 1);
    $scope.month_str = monthToString($scope.month)
    $scope.date = $scope.month_str + "-01";
    $location.search('month', $scope.month_str);    
  }
  $scope.updateMonth = function() {
    if ($scope.month_regex.test($scope.month_str) &&
        $scope.month_str != monthToString($scope.month)) {
      $scope.date = $scope.month_str + "-01";
      $scope.month = new Date($scope.date);
      $location.search('month', $scope.month_str);    
    }
  }
  $scope.edit = function(index) {
    console.log("Editing " + index)
  }
  $scope.remove = function(index) {
    console.log("Removing " + index)
  }
});

//Controller for the xml generation page.
adminApp.controller('GenerateCtrl', function ($scope) {});

//Controller for the look & feel page.
adminApp.controller('LookNFeelCtrl', function ($scope, $http, background_thumbnails, background_color) {
  $scope.background_thumbnails = background_thumbnails;
  $scope.background_color = background_color;

  $scope.serve_background = function(blobKey) {
    $http({
    method : 'GET',
        url : '/admin/background_images?type=serve_image&blob_key=' + blobKey
      }).success(function(){
      for (var i = 0; i <  $scope.background_thumbnails.length; ++i) {
      $scope.background_thumbnails[i].served =
        (String)($scope.background_thumbnails[i].key == blobKey);  
      }
      });
  }
  
  $scope.delete_background = function(blobKey) {
    $http({
      method : 'GET',
      url : '/admin/background_images?type=delete&blob_key=' + blobKey
      }).then(function(data) {
      if (data.data.result == "true") {
          for (var i = 0; i <  $scope.background_thumbnails.length; ++i) {
            if ($scope.background_thumbnails[i].key == blobKey) {
              $scope.background_thumbnails.splice(i, 1);
              return;
            }
          }
      }
      })
  }
  
  $scope.get_upload_url = function(form) {
    $http({
      method : 'GET',
      url : '/admin/background_images?type=get_upload_url'
    }).success(function(url) {form.action = url});
  }
  
  $scope.apply_bg_color = function() {
    $http({
    method : 'GET',
    url : '/admin/background_images?type=serve_color&color=' + $scope.background_color
    });
  }
});

//Controller for the send newsletter page.
adminApp.controller('NewsletterCtrl', function ($scope, $location, month_str, newsletters, background_color) {
  if (month_str == null || month_str == '') {
    $scope.month_str = monthToString(new Date())
    $scope.date = $scope.month_str + "-01";
    $scope.month = new Date($scope.date);
  } else {
    $scope.date = month_str + "-01";
    $scope.month = new Date($scope.date);
    $scope.month_str = monthToString($scope.month)
  }
  
  $scope.month_regex = /^[0-9]{4}-(0[0-9]|1[012])$/;
  $scope.previousMonth = function() {
    $scope.month.setMonth($scope.month.getMonth() - 1);
    $scope.month_str = monthToString($scope.month)
    $scope.date = $scope.month_str + "-01";
    $location.search('month', $scope.month_str);    
  }
  $scope.nextMonth = function() {
    $scope.month.setMonth($scope.month.getMonth() + 1);
    $scope.month_str = monthToString($scope.month)
    $scope.date = $scope.month_str + "-01";
    $location.search('month', $scope.month_str);    
  }
  $scope.updateMonth = function() {
    if ($scope.month_regex.test($scope.month_str) &&
        $scope.month_str != monthToString($scope.month)) {
      $scope.date = $scope.month_str + "-01";
      $scope.month = new Date($scope.date);
      $location.search('month', $scope.month_str);    
    }
  }
  $scope.updateLang = function() {
    document.getElementById('ta').value =
        $scope.text.replace(/{{background_color}}/g, $scope.background_color);
  }
  $scope.newsletters = newsletters;
  $scope.all_text = '';
  for (var lang in newsletters) {
    $scope.all_text += '*|IF:LANGUAGE=' + lang + '|*';
    $scope.all_text += newsletters[lang];
    $scope.all_text += '*|END:IF|*\n';
  }
  $scope.newsletters['All languages'] = $scope.all_text;
  $scope.text = $scope.newsletters['All languages'];
  $scope.background_color = background_color;
  $scope.updateLang();  
});

adminApp.controller('UploadBackgroundCtrl', [ '$scope', '$upload', '$http', '$state', function($scope, $upload, $http, $state) {
  $scope.onFileSelect = function($files) {
    for (var i = 0; i < $files.length; i++) {
      var $file = $files[i];
      $http({
        method : 'GET',
        url : '/admin/background_images?type=get_upload_url&redirect=/admin2/#/looknfeel'
      }).success(function(data){
        $upload.upload({
          url: data.url,
          file: $file,
          progress: function(e){}
        }).then(function(data, status, headers, config) {
          console.log(data);
          $state.reload();
        });
      });
    }
  }
}]);
