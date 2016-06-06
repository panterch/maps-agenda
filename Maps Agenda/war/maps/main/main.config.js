(function() {
    'use strict';
  angular.module('mapsApp')
  	   .config(configMain);
  	    
  function configMain($stateProvider, $urlRouterProvider) {
  	$stateProvider
    	  // Parent state that loads the languages and phrases for the entire site. 
    	  .state('main', {
          'abstract': true,
          'url': '/{lang:[a-z][a-z]}',
          'templateUrl': 'main/main.html',
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
          'templateUrl': 'events/events.html',
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
          templateUrl: 'contact/contact.html'        
        })
        .state('main.impressum', {
          url: '/impressum',
          templateUrl: 'impressum/impressum.html'        
        });
      $urlRouterProvider.when(/^\/[a-z][a-z]/, ['$match', function ($match) {
        return $match + '/events';
      }])
      $urlRouterProvider.otherwise('/de/events');
    };
})();