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


var mapsApp = angular.module('mapsApp', ['ui.router', 'ngSanitize']);

// Small service to keep the date between state transitions. For example,
// when coming back to the events after looking at the contacts, the selected
// date remains the same and does not jump back to the current day.
mapsApp.service('dateKeeper', function() {
  this.date = new Date();
  this.getDate = function() { return this.date; }
  this.setDate = function(new_date) { this.date = new Date(new_date); }
});

mapsApp.controller('MainCtrl', function ($scope, $location, $http, lang, 
                                         languages, phrases, tags) {
  $scope.lang = lang;
  $scope.newsletter_lang = lang;
	$scope.languages = languages;	
	$scope.phrases = phrases;
  $scope.tags = tags;

  var html = document.body.parentNode;
  html.setAttribute('lang', lang);
  
  $scope.updateLang = function(new_lang) {
    var path = $location.path();
    if (new_lang == null)
      $location.path(path.replace(/^.../, '/' + $scope.lang));
    else
      $location.path(path.replace(/^.../, '/' + new_lang));
  }

  // Hack for long names. Tell the renderer that it can break after a '/'.
  // Also check that the language is supported. If not, redirect to 'de'.
  // Finally, set the rtl or ltr rendering.
  var found = false;
  for (var i = 0; i < $scope.languages.length; ++i) {
    var l = $scope.languages[i];
    l.name_br = l.name.replace(/\//g, '/&#x200b;');
    if (l.code == lang) {
      found = true;
      html.setAttribute('dir', l.isRtl? 'rtl' : 'ltr');
    }
  }
  if (!found) $scope.updateLang('de');
});

mapsApp.controller('EventsCtrl', function ($scope, $location, date, events, dateKeeper) {
  // If there is no date in the URL, set the date from the keeper.
  if (!date) {
    $location.search('date', dateToString(dateKeeper.getDate()));
    return;
  }

  $scope.events = events;
  // Fix the date format for IE 8.
  for (var i = 0; i < $scope.events.length; i++) {
    $scope.events[i].date = $scope.events[i].date.replace(/[-]/g, '/');
  }
  
  $scope.date_str = date.replace(/[-]/g, '/');
  $scope.date = new Date($scope.date_str);
  $scope.pivot = new Date($scope.date);
  // $scope.pivot.setDate(1);
  dateKeeper.setDate($scope.date);
      
  $scope.printDate = function(dateStr) {
    var date = new Date(dateStr);
    if ($scope.lang == "ma")
      return (date.getMonth() + 1) + '.' + date.getDate() + '.';
    else
      return date.getDate() + '.' + (date.getMonth() + 1) + '.';
  };

  $scope.printDate2 = function(dateStr) {
    if (!dateStr) return '';
    var date = new Date(dateStr);
    var month = $scope.phrases[MONTHS_SHORT[date.getMonth()]];
    if (!month) 
      return $scope.printDate(dateStr);
    else if ($scope.lang == "ma") 
      return month + ' ' + date.getDate();
    else 
      return date.getDate() + ' ' + month;
  };

  $scope.printDay = function(dateStr) {
    var date = new Date(dateStr);
    return DAYS_OF_WEEK_LONG[date.getDay()];
  };
  $scope.printFirstDate = function() {
    if ($scope.events.length == 0) 
      return $scope.printDate2($scope.date_str);
    return $scope.printDate2($scope.events[0].date);
  }
  $scope.canGoBack = function() {
    if ($scope.events.length > 0)
      return true;
    return new Date($scope.date_str) > new Date();
  }
  $scope.canGoForward = function() {
    if ($scope.events.length > 0)
      return true;
    return new Date($scope.date_str) < new Date();
  }
  $scope.printLastDate = function() {
    if ($scope.events.length == 0)
      return $scope.printDate2($scope.date_str);
    return $scope.printDate2($scope.events[$scope.events.length - 1].date);
  }
  $scope.showPreviousEvents = function() {
    var new_date;
    if ($scope.events.length > 0) {
      new_date = new Date($scope.events[0].date);
      new_date.setDate(new_date.getDate() - 1);
    } else if ($location.search().back == null) {
      new_date = new Date($scope.date);
      new_date.setDate($scope.date.getDate() - 1);      
    } else {
      new_date = new Date($scope.date);
      new_date.setDate($scope.date.getDate() - 15);      
    }
    $location.search('date', dateToString(new_date));
    $location.search('back', true);
  }
  $scope.showNextEvents = function() {
    var new_date;
    if ($scope.events.length > 0) {
      new_date = new Date($scope.events[$scope.events.length - 1].date);
      new_date.setDate(new_date.getDate() + 1);
    } else if ($location.search().back == null) {
      new_date = new Date($scope.date);
      new_date.setDate(new_date.getDate() + 15);
    } else {
      new_date = new Date($scope.date);
      new_date.setDate(new_date.getDate() + 1);
    }
    $location.search('date', dateToString(new_date));
    $location.search('back', null);
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
      return 'background-color';
    }
  }

  $scope.selectDate = function(day) {
    var date = new Date($scope.pivot);
    date.setDate(day);
    $location.search('date', dateToString(date));
    $location.search('back', null);
  }
});

mapsApp.config(['$stateProvider', '$urlRouterProvider',
  function ($stateProvider, $urlRouterProvider) {
  	$stateProvider
  	  // Parent state that loads the languages and phrases for the entire site. 
  	  .state('main', {
        'abstract': true,
        'url': '/{lang:[a-z][a-z]}',
        'templateUrl': 'main.html',
        'resolve': {
          'lang': ['$stateParams', function($stateParams) {
            return $stateParams.lang;
          }],
          'languages': function($http) {
            return $http({'method': 'GET', 'url': '/maps/data?type=languages'})
              .then (function (data) {
                return data.data.languages;
              }
            );
          },
          'phrases': function($http, lang) {
            return $http({'method': 'GET', 
                          'url': '/maps/data?type=phrases&lang=' + lang})
              .then (function (data) {
                return data.data.phrases;
              }
            );         
          },
          'tags': function($http) {
            return $http({'method': 'GET', 'url': '/maps/data?type=tags'})
              .then (function (data) {
                return data.data.tags;
              }
            );          
          }
        },
  		  'controller': "MainCtrl"
      })
      .state('main.events', {
        'url': '/events?{date:[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]}&back',
        'templateUrl': 'events.html',
        'resolve': {
          'date': ['$stateParams', function($stateParams) {
            return $stateParams.date;
          }],
          'back': ['$stateParams', function($stateParams) {
            return $stateParams.back != null;
          }],
          'events': function($http, lang, date, back) {
            var params = [
              'type=events',
              'lang=' + lang,
              'date=' + date,
              back? "back" : ""
            ];
            return $http({method: 'GET', url: '/maps/data?' + params.join('&')})
              .then (function (data) {
                return data.data.events;
              }
            );          
          }
        },
        controller: 'EventsCtrl'
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
        }
      })
      .state('main.contact', {
        url: '/contact',
        templateUrl: 'contact.html'        
      })
      .state('main.impressum', {
        url: '/impressum',
        templateUrl: 'impressum.html'        
      });
    $urlRouterProvider.when(/^\/[a-z][a-z]/, ['$match', function ($match) {
      return $match + '/events';
    }])
    $urlRouterProvider.otherwise('/de/events');
  }]
);
