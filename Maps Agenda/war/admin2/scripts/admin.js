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


var adminApp = angular.module('adminApp', ['ui.router']);

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
  $scope.translator = {}
  $scope.getRowClass = function(t) {
    if (t.modified)
      return "hover modified";
    else return "hover";
  }
  $scope.edit = function(email) {
    if (!email) 
      $scope.translator = {};
    else {
      for (var i = 0; i < $scope.translators.length; ++i) {
        if ($scope.translators[i].email == email) {
          $scope.translator = $scope.translators[i];
          break;
        }
      }
    }
    $scope.showPopup('edit-popup');
  }
  $scope.updateLang = function() {
    if (!$scope.translator.langs)
      $scope.translator.langs = [$scope.lang];
    else if ($scope.translator.langs.indexOf($scope.lang) == -1) {
      $scope.translator.langs.push($scope.lang);
    } else {
      $scope.translator.langs.splice($scope.translator.langs.indexOf($scope.lang), 1);
    }
    $scope.lang = '';
  }
  $scope.save = function() {
    $scope.hidePopup('edit-popup');
    $scope.translator.modified = true;
    for (var i = 0; i < $scope.translators.length; ++i) {
      if ($scope.translators[i].email == $scope.translator.email) {
        $scope.translators[i] = $scope.translator;
        return;
      }
    }
    $scope.translators.push($scope.translator);
  }
  $scope.isAnyModified = function() {
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
  $scope.toGermanNames = function(langs) {
    var german_names = [];
    if (!langs) return german_names;
    for (var i = 0; i < langs.length; ++i) {
      if (languages[langs[i]] == null) {
        german_names.push(langs[i] + " (unknown)")
      } else {
        german_names.push(languages[langs[i]].germanName);
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
});

// Controller for the subscribers page.
adminApp.controller('SubscriberCtrl', function ($scope, subscribers) {
  $scope.subscribers = subscribers;
  $scope.getLangName = function(lang) {
    if (languages[lang] == null) {
      return lang + " (unknown)";
    } else {
      return languages[lang].germanName;
    }
  }
  $scope.remove = function(email) {
    alert("Deletion capabilities coming soon.");
    console.log("Delete email: " + email);
  }
  $scope.subs_filter = function(expr) {
    return function(s) {
      return !expr
          || s.email.indexOf(expr) != -1 
          || s.name.indexOf(expr) != -1
          || $scope.getLangName(s.lang).indexOf(expr) != -1;
    }
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
adminApp.controller('EventCtrl', function ($scope) {});

//Controller for the xml generation page.
adminApp.controller('GenerateCtrl', function ($scope) {});

//Controller for the send newsletter page.
adminApp.controller('NewsletterCtrl', function ($scope) {});

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
		  url: '/events',
      onEnter: function() { itemClick("events") },
		  templateUrl: 'events.html',
      controller: 'EventCtrl'
    })
	  .state('generate', {
		  url: '/generate_xml',
      onEnter: function() { itemClick("generate") },
		  templateUrl: 'generate.html',
      controller: 'GenerateCtrl'
    })
	  .state('newsletter', {
		  url: '/send_newsletter',
      onEnter: function() { itemClick("send_newsletter") },
		  templateUrl: 'newsletter.html',
      controller: 'NewsletterCtrl'
    })
    $urlRouterProvider.when(/\/translations\/?/, '/translations/de');
    $urlRouterProvider.otherwise('/');
  }]
);
