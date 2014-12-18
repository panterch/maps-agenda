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

adminApp.controller('TranslatorCtrl', function ($scope, languages, 
                                                translators) {
  $scope.languages = languages;
	$scope.translators = translators;
	$scope.edit = function(email) {
	  alert("Editing capabilities coming soon.");
	  console.log("Email: " + email);
	}
});
adminApp.controller('SubscriberCtrl', function ($scope, subscribers) {
	$scope.subscribers = subscribers;
	$scope.edit = function(email) {
	  alert("Editing capabilities coming soon.");
	  console.log("Email: " + email);
	}
});
adminApp.controller('LanguageCtrl', function ($scope, languages) {
	$scope.languages = languages;
	$scope.edit = function(lang) {
	  alert("Editing capabilities coming soon.");
	  console.log("Language: " + lang);
	}
});
adminApp.controller('PhraseCtrl', function ($scope, languages, de_phrases) {
  $scope.languages = languages;
  $scope.phrases = de_phrases;
  $scope.edit = function(key) {
    alert("Editing capabilities coming soon.");
    console.log("Key: " + key);
  }
});
adminApp.controller('EventCtrl', function ($scope) {});
adminApp.controller('GenerateCtrl', function ($scope) {});
adminApp.controller('NewsletterCtrl', function ($scope) {});

adminApp.config(['$stateProvider', '$urlRouterProvider',
  function ($stateProvider,   $urlRouterProvider) {
	$stateProvider
	  .state('parent', {
	    abstract: true,
	    template: "<ui-view/>",
      resolve: {
        languages: function($http) {
          return $http({method: 'GET', url: '/maps/data?type=languages'})
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
          return $http({method: 'GET', url: '/maps/data?type=translators'})
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
          return $http({method: 'GET', url: '/maps/data?type=subscribers'})
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
      url: '/translations',
      onEnter: function() { itemClick("phrases") },
      templateUrl: 'phrases.html',
      resolve: {
        de_phrases: function($http) {
          return $http({method: 'GET', url: '/maps/data?type=phrases&lang=de'})
            .then (function (data) {
              return data.data.phrases;
            });         
        },
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
  }]
);
