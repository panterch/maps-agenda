var mapsApp = angular.module('mapsApp', ['ui.router']);
var today = function() {
  var today = new Date();
  var dd = today.getDate();
  var mm = today.getMonth() + 1;  // January is 0.
  var yyyy = today.getFullYear();
  if(dd < 10) { dd = '0' + dd; }
  if(mm < 10) { mm = '0' + mm; } 
  return yyyy + '-' + mm + '-' + dd;    
}

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

mapsApp.controller('MainCtrl', function ($scope, $location, $http, lang, 
                                         languages, phrases, tags) {
  $scope.lang = lang;
  $scope.newsletter_lang = lang;
	$scope.languages = languages;
	$scope.phrases = phrases;
  $scope.tags = tags;

  $scope.updateLang = function(new_lang) {
    var path = $location.path();
    if (new_lang == null)
      $location.path(path.replace(/^.../, '/' + $scope.lang));
    else
      $location.path(path.replace(/^.../, '/' + new_lang));
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
  // Check if the language is supported. If not, redirect to 'de'.
  if (!(lang in languages)) {
    $scope.updateLang('de');
  }
});

mapsApp.config(['$stateProvider', '$urlRouterProvider',
  function ($stateProvider, $urlRouterProvider) {
  	$stateProvider
  	  // Parent state that loads the languages and phrases for the entire site. 
  	  .state('main', {
  	    abstract: true,
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
      })
      .state('main.events', {
        url: '/events?{date:[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]}',
        templateUrl: 'events.html',
      })
      .state('main.about', {
        url: '/about',
        templateProvider: function($templateFactory, $stateParams) {
          // First try to load the about page for the requested language.
          var templateUrl = 'about/' + $stateParams.lang + '.html';
          return $templateFactory.fromUrl(templateUrl).then(
            function (data) { return data; },
            function (reason) { 
              // If not available, return the German about page. 
              return $templateFactory.fromUrl('about/de.html'); 
          });
        },
      })
      .state('main.contact', {
        url: '/contact',
        templateUrl: 'contact.html',        
      })
      .state('main.impressum', {
        url: '/impressum',
        templateUrl: 'impressum.html',        
      });
    $urlRouterProvider.when(/^\/[a-z][a-z]/, ['$match', function ($match) {
      return $match + '/events?' + today();
    }])
    $urlRouterProvider.when(/^\/[a-z][a-z]\/events.*/, ['$match', function ($match) {
      return $match + '?' + today();
    }])
    $urlRouterProvider.otherwise('/de/events?' + today());
  }]
);
