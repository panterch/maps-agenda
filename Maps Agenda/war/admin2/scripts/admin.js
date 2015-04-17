monthToString = function(month) {
  var mm = month.getMonth() + 1;  // January is 0.
  var yyyy = month.getFullYear();
  if(mm < 10) { mm = '0' + mm; } 
  return yyyy + '-' + mm;    
}

// An item of the menu has been clicked.
function itemClick(item_id) {
  // Move the selector.
  var item = document.getElementById(item_id);
  if (item == null)
    return false;
  var selector = document.getElementById("menu_selector");
  if (selector == null)
    return false;
  selector.style.width = item.offsetWidth + 'px';
  selector.style.height = item.offsetHeight + 'px';
  selector.style.top = item.offsetTop + 'px';
  selector.style.left = item.offsetLeft + 'px';
  return true;
}

var adminApp = angular.module('adminApp', ['ui.router', 'angularFileUpload']);

adminApp.run(['$rootScope', '$state', '$stateParams',
  function ($rootScope,   $state,   $stateParams) {
    // It's very handy to add references to $state and $stateParams to the $rootScope
    // so that you can access them from any scope within your applications. For example,
    // <li ng-class="{ active: $state.includes('contacts.list') }"> will set the <li>
    // to active whenever 'contacts.list' or one of its decendents is active.
    $rootScope.$state = $state;
    $rootScope.$stateParams = $stateParams;
  }]
)

// Controller for the translators page.
adminApp.controller('TranslatorCtrl', function ($scope, languages, 
                                                translators) {
  $scope.languages = languages;
  $scope.translators = translators;
  for (var i = 0; i < $scope.translators.length; ++i) {
    $scope.translators[i].modified = false;
    $scope.translators[i].is_new = false;
  }  
  $scope.translator = { modified: false, is_new: true }
  
  $scope.getRowClass = function(t) {
    if (t.modified)
      return "hover modified";
    else return "hover";
  }
  $scope.edit = function(email) {
    $scope.lang_options = [];
    $scope.select_length = 30;
    for (var i = 0; i < $scope.languages.length; ++i) {
      $scope.lang_options.push($scope.languages[i]);
    }
    if (!email) { 
      $scope.translator = {modified: false, is_new: true, langs: [] };
    } else {
      for (var i = 0; i < $scope.translators.length; ++i) {
        if ($scope.translators[i].email == email) {
          $scope.translator = $scope.translators[i];
          // Make a copy of the object.
          $scope.old_translator = JSON.parse(JSON.stringify($scope.translator));
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
    console.log("Email: " + $scope.countEmails($scope.translator.email));
    if ($scope.translator.is_new) {
      if ($scope.countEmails($scope.translator.email) > 0) {
        $scope.translator.email = '';
        return;
      }
      $scope.translators.push($scope.translator);      
      $scope.translator.modified = true;
    } else if ($scope.countEmails($scope.translator.email) > 1) {
      $scope.translator.email = $scope.old_translator.email;
      return;
    } else {
      $scope.translator.modified = !$scope.sameAsBackup() || $scope.old_translator.modified;
      $scope.hidePopup('edit-popup');
    }
    $scope.hidePopup('edit-popup');
  }
  $scope.cancel = function() {
    $scope.hidePopup('edit-popup');
    $scope.translator.email = $scope.old_translator.email;
    $scope.translator.name = $scope.old_translator.name;
    $scope.translator.langs = $scope.old_translator.langs;
  }
  $scope.noneModified = function() {
    for (var i = 0; i < $scope.translators.length; ++i) {
      if ($scope.translators[i].modified) {
        return false;
      }
    }
    return true;
  }
  $scope.saveAll = function() {
    alert("Save capabilities coming soon.");
  }
  $scope.remove = function(email) {
    alert("Deletion capabilities coming soon.");
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
adminApp.controller('NewsletterCtrl', function ($scope, $location, month_str, newsletters) {
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
    document.getElementById('ta').value = $scope.text;
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
  $scope.updateLang();  
});

adminApp.config(['$stateProvider', '$urlRouterProvider',
  function ($stateProvider,   $urlRouterProvider) {
	$stateProvider
	  .state('parent', {
	    abstract: true,
	    template: "<ui-view/>",
      resolve: {
        languages: function($http) {
          return $http({method: 'GET', url: '/admin/data?type=languages'})
            .then (function (data) {
              return data.data.languages;
            });         
        },
      },	    
	  })
	  .state('translators', {
      parent: 'parent',
		  url: '/translators',
      onEnter: function() { itemClick("translators") },
      templateUrl: 'translators.html',
      resolve: {
        translators: function($http) {
          return $http({method: 'GET', url: '/admin/data?type=translators'})
  	        .then(function(data) {
  	          return data.data.translators;
            });
        },
  		},
      controller: 'TranslatorCtrl'
    })
	  .state('subscribers', {
		  url: '/subscribers',
          onEnter: function() { itemClick("subscribers") },
		  templateUrl: 'subscribers.html',
      resolve: {
        subscribers: function($http) {
          return $http({method: 'GET', url: '/admin/data?type=subscribers'})
  	        .then (function (data) {
  	          return data.data.subscribers;
            });				  
        },
  		},
      controller: 'SubscriberCtrl'
    })
	  .state('languages', {
	    parent: 'parent',
		  url: '/languages',
          onEnter: function() { itemClick("languages") },
		  templateUrl: 'languages.html',
      controller: 'LanguageCtrl'
    })
	  .state('phrases', {
      parent: 'parent',
      url: '/translations/{lang:[a-z][a-z]}',
      onEnter: function() { itemClick("phrases") },
      templateUrl: 'phrases.html',
      resolve: {
        lang: ['$stateParams', function($stateParams) {
          return $stateParams.lang;
        }],
        de_phrases: function($http) {
          return $http({method: 'GET', url: '/admin/data?type=phrases&lang=de'})
            .then (function (data) {
              return data.data.phrases;
            });         
        },
        lang_phrases: function(lang, $http) {
          if (lang == 'de') return null;
          return $http({method: 'GET', url: '/admin/data?type=phrases&lang=' + lang})
            .then (function (data) {
              return data.data.phrases;
          });         
        }
      },
      controller: 'PhraseCtrl'
    })
	  .state('events', {
		  url: '/events?{month:[0-9][0-9][0-9][0-9]-[0-9][0-9]}',
      onEnter: function() { itemClick("events") },
		  templateUrl: 'events.html',
      resolve: {
        month_str: ['$stateParams', function($stateParams) {
          return $stateParams.month;
        }],
        events: function(month_str, $http) {
          month = "";
          if (month_str) {
            month = "&month=" + month_str;
          }
          return $http({method: 'GET', url: '/admin/data?type=events' + month})
            .then (function (data) {
              return data.data.events;
            });         
        }
      },
      controller: 'EventCtrl'
    })
	  .state('generate', {
		  url: '/generate_xml',
      onEnter: function() { itemClick("generate") },
		  templateUrl: 'generate.html',
      controller: 'GenerateCtrl'
    })
	  .state('looknfeel', {
		  url: '/looknfeel',
      onEnter: function() { itemClick("looknfeel") },
		  templateUrl: 'looknfeel.html',
	  resolve : {
		background_color : function($http) {
		  return $http({
		    method : 'GET',
		    url : '/admin/background_images?type=color'
		  }).then(function(data) {
			if (data.data.color == null || data.data.color == '') {
			  return "000000";
			} else {
			  return data.data.color;	
			}
	      });
		},
	    background_thumbnails : function($http) {
		  return $http({
			method : 'GET',
	        url : '/admin/background_images?type=thumbnails'
		  }).then(function(data) {
			return data.data.background_thumbnails;
     	  });
        },
	  },
      controller: 'LookNFeelCtrl'
    })
	  .state('newsletter', {
      url: '/newsletter_hack?{month:[0-9][0-9][0-9][0-9]-[0-9][0-9]}',
      onEnter: function() { itemClick("newsletter_hack") },
      templateUrl: 'newsletter.html',
      resolve: {
        month_str: ['$stateParams', function($stateParams) {
          return $stateParams.month;
        }],
        newsletters: function(month_str, $http) {
          month = "";
          if (month_str) {
            month = "&month=" + month_str;
          }
          return $http({method: 'GET', url: '/admin/data?type=newsletter' + month})
            .then (function (data) {
              return data.data.newsletters;
            });         
        }
      },
      controller: 'NewsletterCtrl'
    })
    $urlRouterProvider.when(/\/translations\/?/, '/translations/de');
    // $urlRouterProvider.otherwise('/');
  }]
);

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

