var mapsApp = angular.module('mapsApp', ['ui.router']);
var dateToString = function(date) {
  var dd = date.getDate();
  var mm = date.getMonth() + 1;  // January is 0.
  var yyyy = date.getFullYear();
  if(dd < 10) { dd = '0' + dd; }
  if(mm < 10) { mm = '0' + mm; } 
  return yyyy + '-' + mm + '-' + dd;    
}
var today = function() {
  return dateToString(new Date());    
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

var MONTHS = [
  'mojanuar', 'mofebruar', 'momaerz', 'moapril',
  'momai', 'mojuni', 'mojuli', 'moaugust',
  'moseptember', 'mooktober', 'monovember', 'modezember'
];

var DAYS_OF_WEEK_SHORT = [
  'wtabmontag', 'woabdienstag', 'wtabmittwoch', 'wtabdonnerstag',
  'wtabfreitag', 'wtabsamstag', 'wtabsonntag'
];

var DAYS_OF_WEEK_LONG = [
  'Wochentage', 'wtdienstag', 'wtmittwoch', 'wtdonnerstag',
  'wtfreitag', 'wtsamstag', 'wtsonntag'
];

mapsApp.controller('MainCtrl', function ($scope, $location, $http, $sce, lang, 
                                         languages, phrases, tags) {
  $scope.lang = lang;
  $scope.newsletter_lang = lang;
	$scope.languages = languages;

	// Hack for long names. Tell the renderer that it can break after a '/'.
	for (var code in $scope.languages) {
	  var l = $scope.languages[code];
	  l.name_br = $sce.trustAsHtml(l.name.replace(/\//g, '/<wbr>'));
	}
	
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

mapsApp.controller('EventsCtrl', function ($scope, $location, date, events) {
  if (!date) {
    $location.search('date', today());
    return;
  }
  $scope.events = events;
  $scope.date_str = date;
  $scope.date = new Date($scope.date_str);
  $scope.pivot = new Date($scope.date);
  $scope.pivot.setDate(1);
  
  $scope.printDate = function(dateStr) {
    var date = new Date(dateStr);
    return date.getDate() + '. ' + (date.getMonth() + 1) + '.';
  };

  $scope.printDay = function(dateStr) {
    var date = new Date(dateStr);
    return DAYS_OF_WEEK_LONG[date.getDay()];
  };

  $scope.showPreviousEvents = function() {
    console.log('Pressed previous');
  }
  $scope.showNextEvents = function() {
    console.log('Pressed next');
  }
  
  $scope.getDaysInMonth = function() {
    return new Date($scope.pivot.getYear(), $scope.pivot.getMonth() + 1, 0).getDate();
  };

  $scope.movePivotForward = function() {
    $scope.pivot.setMonth($scope.pivot.getMonth() + 1);
    $scope.renderCalendar();
  };

  $scope.movePivotBack = function() {
    $scope.pivot.setMonth($scope.pivot.getMonth() - 1);
    $scope.renderCalendar();
  };

  $scope.renderCalendar = function() {
    $scope.strings = DAYS_OF_WEEK_SHORT;
    var currentWeek = [];
    var new_weeks = [currentWeek];
    for (var i = 0; i < $scope.getDaysInMonth(); i++) {
      var date = new Date($scope.pivot);
      date.setDate(i + 1);

      var day = date.getDay();

      // Add padding if the month doesn't begin on a Monday.
      for (var k = 1; i == 0 && k < (day > 0 ? day : 7); k++) {
        currentWeek.push('');
      }

      // Wrap to the next week.
      if ((day - 1) % 7 == 0) {
        currentWeek = [];
        new_weeks.push(currentWeek);
      }

      currentWeek.push(i + 1);
    }
    $scope.weeks = new_weeks;
  };

  $scope.getMonth = function() {
    return MONTHS[$scope.pivot.getMonth()];
  }

  $scope.isSelected = function(day) {
    var d = new Date($scope.pivot);
    d.setDate(day);
    if ($scope.date.toDateString() == d.toDateString()) {
      return 'selected';
    } else {
      return '';
    }
  }

  $scope.selectDate = function(day) {
    var date = new Date($scope.pivot);
    date.setDate(day);
    $location.search('date', dateToString(date));
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
        resolve: {
          date: ['$stateParams', function($stateParams) {
            return $stateParams.date;
          }],
          events: function($http, lang, date) {
            var params = [
              'type=events',
              'lang=' + lang,
              'startDate=' + date
            ];
            return $http({method: 'GET', url: '/maps/data?' + params.join('&')})
              .then (function (data) {
                return data.data.events;
              }
            );          
          },
        },
        controller: 'EventsCtrl',
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
      return $match + '/events?date=' + today();
    }])
    $urlRouterProvider.when(/^\/[a-z][a-z]\/events.*/, ['$match', function ($match) {
      return $match + '?date=' + today();
    }])
    $urlRouterProvider.otherwise('/de/events?date=' + today());
  }]
);
