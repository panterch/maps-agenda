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

adminApp.controller('TranslatorCtrl', function ($scope, simpleObj) {
	$scope.simple = simpleObj;
});
adminApp.controller('SubscriberCtrl', function ($scope) {});
adminApp.controller('LanguageCtrl', function ($scope, languages) {
	console.log("Languages: " + languages);
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
          templateUrl: 'translators.html',
          resolve:{
        	  simpleObj:  function(){
                  return 'simpleeee';
               },  
          },
          controller: 'TranslatorCtrl'
      })
	  .state('subscribers', {
		  url: '/subscribers',
		  templateUrl: 'subscribers.html',
          controller: 'SubscriberCtrl'
      })
	  .state('languages', {
		  url: '/languages',
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
		  templateUrl: 'phrases.html',
          controller: 'PhraseCtrl'
      })
	  .state('events', {
		  url: '/events',
		  templateUrl: 'events.html',
          controller: 'EventCtrl'
      })
	  .state('generate', {
		  url: '/generate_xml',
		  templateUrl: 'generate.html',
          controller: 'GenerateCtrl'
      })
	  .state('newsletter', {
		  url: '/send_newsletter',
		  templateUrl: 'newsletter.html',
          controller: 'NewsletterCtrl'
      })
  }]
);
