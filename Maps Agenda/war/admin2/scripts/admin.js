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

adminApp.controller('TranslatorCtrl', function ($scope) {});
adminApp.controller('SubscriberCtrl', function ($scope) {});
adminApp.controller('LanguageCtrl', function ($scope, languages) {
	$scope.languages = languages;
});
adminApp.controller('PhraseCtrl', function ($scope) {});
adminApp.controller('EventCtrl', function ($scope) {});
adminApp.controller('GenerateCtrl', function ($scope) {});
adminApp.controller('NewsletterCtrl', function ($scope) {});

adminApp.config(['$stateProvider', '$urlRouterProvider',
  function ($stateProvider,   $urlRouterProvider) {
	$stateProvider
	  .state('translators', {
		  url: '/translators',
      onEnter: function() { itemClick("translators") },
      templateUrl: 'translators.html',
      controller: 'TranslatorCtrl'
    })
	  .state('subscribers', {
		  url: '/subscribers',
      onEnter: function() { itemClick("subscribers") },
		  templateUrl: 'subscribers.html',
      controller: 'SubscriberCtrl'
    })
	  .state('languages', {
		  url: '/languages',
      onEnter: function() { itemClick("languages") },
		  templateUrl: 'languages.html',
		  resolve: {
			  languages: function($http) {
				  return $http({method: 'GET', url: '/maps/data?type=languages'})
	          .then (function (data) {
	            return data.data.languages;
	          });				  
			  },
		  },
      controller: 'LanguageCtrl'
    })
	  .state('phrases', {
		  url: '/translations',
		  onEnter: function() { itemClick("phrases") },
		  templateUrl: 'phrases.html',
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
