var mapsApp = angular.module('mapsApp', ['ui.router']);

mapsApp.run(['$rootScope', '$state', '$stateParams',
  function ($rootScope,   $state,   $stateParams) {
    // It's very handy to add references to $state and $stateParams to the $rootScope
    // so that you can access them from any scope within your applications. For example,
    // <li ng-class="{ active: $state.includes('contacts.list') }"> will set the <li>
    // to active whenever 'contacts.list' or one of its decendents is active.
    $rootScope.$state = $state;
    $rootScope.$stateParams = $stateParams;
  }]
)

mapsApp.controller('MainCtrl', function ($scope, $location, $http,lang, 
                                         languages, phrases, tags) {
  $scope.lang = lang;
  $scope.newsletter_lang = lang;
	$scope.languages = languages;
	$scope.phrases = phrases;
  $scope.tags = tags;
  
  $scope.updateLang = function(new_lang) {
    if (new_lang == null)
      $location.path('/' + $scope.lang);
    else
      $location.path('/' + new_lang);
  }
  $scope.register = function() {
    var params = [
      'lang=' + $scope.newsletter_lang,
      'name=' + $scope.name,
      'email=' + $scope.email
    ];
    var url = '/maps/subscribe?' + params.join('&');
    $http.get(url).success(function(data) {
      if (data.status == "ok") {
        $scope.name = "";
        $scope.email = "";
        alert(data.message);
      } else {
        alert("Error: " + data.message);
      }
    });   
  }
});

mapsApp.config(['$stateProvider', '$urlRouterProvider',
  function ($stateProvider,   $urlRouterProvider) {
  	$stateProvider
  	  // Parent state that loads the languages and phrases for the entire site. 
  	  .state('main', {
  		  url: '/{lang:[a-z][a-z]}',
  		  templateUrl: 'main.html',
  		  resolve: {
  		    lang: ['$stateParams', function($stateParams) {
            return $stateParams.lang;
  	      }],
  			  languages: function($http) {
  				  return $http({method: 'GET', url: '/maps/data?type=languages'})
  	          .then (function (data) {
  	            return data.data.languages;
  	          }
  	        );				  
  			  },
  			  phrases: function($http, lang) {
            return $http({method: 'GET', 
                          url: '/maps/data?type=phrases&lang=' + lang})
              .then (function (data) {
                return data.data.phrases;
              }
            );         
  			  },
          tags: function($http) {
            return $http({method: 'GET', url: '/maps/data?type=tags'})
              .then (function (data) {
                return data.data.tags;
              }
            );          
          },
  		  },
        controller: 'MainCtrl'
      });
  	$urlRouterProvider.when('', '/de');
  }]
);
