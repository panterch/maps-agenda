var MONTHS = [
  'mojanuar', 'mofebruar', 'momaerz', 'moapril',
  'momai', 'mojuni', 'mojuli', 'moaugust',
  'moseptember', 'mooktober', 'monovember', 'modezember'
];

var MONTHS_SHORT = [
  'moabjanuar', 'moabfebruar', 'moabmaerz', 'moabapril',
  'moabmai', 'moabjuni', 'moabjuli', 'moabaugust',
  'moabseptember', 'moaboktober', 'moabnovember', 'moabdezember'
];

var DAYS_OF_WEEK_SHORT = [
  'wtabmontag', 'woabdienstag', 'wtabmittwoch', 'wtabdonnerstag',
  'wtabfreitag', 'wtabsamstag', 'wtabsonntag'
];

var DAYS_OF_WEEK_LONG = [
  'wtsonntag', 'Wochentage', 'wtdienstag', 'wtmittwoch',
  'wtdonnerstag', 'wtfreitag', 'wtsamstag' 
];

var dateToString = function(date) {
  var dd = date.getDate();
  var mm = date.getMonth() + 1;  // January is 0.
  var yyyy = date.getFullYear();
  if(dd < 10) { dd = '0' + dd; }
  if(mm < 10) { mm = '0' + mm; } 
  return yyyy + '-' + mm + '-' + dd;    
}


var mapsApp = angular.module('mapsApp', ['ui.router']);

// Small service to keep the date between state transitions. For example,
// when coming back to the events after looking at the contacts, the selected
// date remains the same and does not jump back to the current day.
mapsApp.service('dateKeeper', function() {
  this.date = new Date();
  this.getDate = function() { return this.date; }
  this.setDate = function(new_date) { this.date = new Date(new_date); }
});

mapsApp.controller('MainCtrl', function ($scope, $location, $http, $sce, lang, 
                                         languages, phrases, tags,
                                         background_image, background_color) {
  $scope.lang = lang;
  $scope.newsletter_lang = lang;
	$scope.languages = languages;	
	$scope.phrases = phrases;
  $scope.tags = tags;  
  $scope.background_image = background_image;
  $scope.background_color = background_color;

  var html = document.body.parentNode;
  html.setAttribute('lang', lang);
  
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

  // Hack for long names. Tell the renderer that it can break after a '/'.
  // Also check that the language is supported. If not, redirect to 'de'.
  var found = false;
  for (var i = 0; i < $scope.languages.length; ++i) {
    var l = $scope.languages[i];
    l.name_br = $sce.trustAsHtml(l.name.replace(/\//g, '/<wbr>'));
    if (l.code == lang) {
      found = true;
      html.setAttribute('dir', l.isRtl? 'rtl' : 'ltr');
    }
  }
  if (!found) $scope.updateLang('de');
});

mapsApp.controller('EventsCtrl', function ($scope, $location, date, events, dateKeeper) {
  if (!date) {
    $location.search('date', dateToString(dateKeeper.getDate()));
    return;
  }
  $scope.events = events;
  $scope.date_str = date;
  $scope.date = new Date($scope.date_str);
  $scope.pivot = new Date($scope.date);
  $scope.pivot.setDate(1);
  dateKeeper.setDate($scope.date);
  
  $scope.printDate = function(dateStr) {
    var date = new Date(dateStr);
    return date.getDate() + '.' + (date.getMonth() + 1) + '.';
  };

  $scope.printDate2 = function(dateStr) {
    if (!dateStr) return '';
    var date = new Date(dateStr);
    var month = $scope.phrases[MONTHS_SHORT[date.getMonth()]];
    if (!month) return $scope.printDate(dateStr);
    return date.getDate() + ' ' + month;
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
      return '#706f6f';
    } else {
      return $scope.background_color;
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
  			  background_image: function($http, $window) {
  			    return $http.get('/maps/data?type=background-image').then(function(data, status) {
  			      if (status < 200 || status >= 300 || data == null || data.data == null || data.data.url == null || data.data.url == '') {
  			    	// Fallback to the hardcoded default.
  				    return '/maps2/images/temp-bg.png';
  			      } else {
  			    	// This code assumes that the browser window size would never be resized, which
  			    	// is not true of course. However, this assumption helps to minimize the data traffic
  			    	// from the site. Clients with smaller screens will only ever fetch a small version
  			    	// (think, mobile devices), while clients with huge screens will always get the 1280 px
  			    	// version. At the worst, a viewer decides to maximize a window that was initially loaded
  			    	// at a lower resolution, in which case the background would not be crispy until reload.
  			    	return data.data.url + "=s" + (($window.innerWidth < 1280) ? $window.innerWidth : 1280);
  			      }
  			    });
  			  },
  			  background_color: function($http) {
  			    return $http.get('/maps/data?type=background-color').then(function(data, status) {
  			      if (status < 200 || status >= 300 || data == null || data.data == null || data.data.color == null || data.data.color == '') {
  			    	// Fallback to the hardcoded default.
  				    return '#08a';
  			      } else {
  			    	return '#' + data.data.color;
		          }
  			    });
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
              'month=' + date
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
      return $match + '/events';
    }])
    $urlRouterProvider.otherwise('/de/events');
  }]
);
