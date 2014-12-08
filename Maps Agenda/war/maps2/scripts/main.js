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

mapsApp.controller('MainCtrl', function ($scope, languages) {
	$scope.languages = languages;
});

mapsApp.config(['$stateProvider', '$urlRouterProvider',
  function ($stateProvider,   $urlRouterProvider) {
	$stateProvider
	  .state('main', {
      // onEnter: function() { itemClick("languages") },
		  url: '/main',
		  //templateUrl: 'languages.html',
		  resolve: {
			  languages: function($http) {
				  return $http({method: 'GET', url: '/maps/data?type=languages'})
	          .then (function (data) {
	            return data.data.languages;
	          });				  
			  },
		  },
      controller: 'MainCtrl'
    })
  $urlRouterProvider.when('', '/main');
  }]
);
