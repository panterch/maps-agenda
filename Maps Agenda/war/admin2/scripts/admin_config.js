adminApp.config(['$stateProvider', '$urlRouterProvider',
  function ($stateProvider,   $urlRouterProvider) {
  $stateProvider
    .state('parent', {
      abstract: true,
      template: "<ui-view/>",
      resolve: {
        languages: function($http) {
          return $http({method: 'GET', url: '/admin/data?type=languages'})
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
          return $http({method: 'GET', url: '/admin/data?type=translators'})
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
          return $http({method: 'GET', url: '/admin/data?type=subscribers'})
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
      url: '/translations/{lang:[a-z][a-z]}',
      onEnter: function() { itemClick("phrases") },
      templateUrl: 'phrases.html',
      resolve: {
        lang: ['$stateParams', function($stateParams) {
          return $stateParams.lang;
        }],
        de_phrases: function($http) {
          return $http({method: 'GET', url: '/admin/data?type=phrases&lang=de'})
            .then (function (data) {
              return data.data.phrases;
            });         
        },
        lang_phrases: function(lang, $http) {
          if (lang == 'de') return null;
          return $http({method: 'GET', url: '/admin/data?type=phrases&lang=' + lang})
            .then (function (data) {
              return data.data.phrases;
          });         
        }
      },
      controller: 'PhraseCtrl'
    })
    .state('events', {
      url: '/events?{month:[0-9][0-9][0-9][0-9]-[0-9][0-9]}',
      onEnter: function() { itemClick("events") },
      templateUrl: 'events.html',
      resolve: {
        month_str: ['$stateParams', function($stateParams) {
          return $stateParams.month;
        }],
        events: function(month_str, $http) {
          month = "";
          if (month_str) {
            month = "&month=" + month_str;
          }
          return $http({method: 'GET', url: '/admin/data?type=events' + month})
            .then (function (data) {
              return data.data.events;
            });         
        }
      },
      controller: 'EventCtrl'
    })
    .state('generate', {
      url: '/generate_xml',
      onEnter: function() { itemClick("generate") },
      templateUrl: 'generate.html',
      controller: 'GenerateCtrl'
    })
    .state('looknfeel', {
      url: '/looknfeel',
      onEnter: function() { itemClick("looknfeel") },
      templateUrl: 'looknfeel.html',
    resolve : {
    background_color : function($http) {
      return $http({
        method : 'GET',
        url : '/admin/background_images?type=color'
      }).then(function(data) {
      if (data.data.color == null || data.data.color == '') {
        return "000000";
      } else {
        return data.data.color; 
      }
        });
    },
      background_thumbnails : function($http) {
      return $http({
      method : 'GET',
          url : '/admin/background_images?type=thumbnails'
      }).then(function(data) {
      return data.data.background_thumbnails;
        });
        },
    },
      controller: 'LookNFeelCtrl'
    })
    .state('newsletter', {
      url: '/newsletter?{month:[0-9][0-9][0-9][0-9]-[0-9][0-9]}',
      onEnter: function() { itemClick("newsletter") },
      templateUrl: 'newsletter.html',
      resolve: {
        background_color : function($http) {
          return $http({
            method : 'GET',
            url : '/admin/background_images?type=color'
          }).then(function(data) {
          if (data.data.color == null || data.data.color == '') {
            return "000000";
          } else {
            return data.data.color; 
          }
            });
        },
        month_str: ['$stateParams', function($stateParams) {
          return $stateParams.month;
        }]
      },
      controller: 'NewsletterCtrl'
    })
    .state('settings', {
      parent: 'parent',
      url: '/settings',
          onEnter: function() { itemClick("settings") },
      templateUrl: 'settings.html',
      resolve: {
        mailchimp_credentials : function($http) {
          return $http({
            method : 'GET',
            url : '/admin/data?type=mailchimp_credentials'
          }).then(function(data) {
            if (data == null || data.data == null) {
              return "{\"list_id\" : \"\", \"api_key\" : \"\"}";
            } else {
              return data.data; 
            }
          })
        }
      },
      controller: 'SettingsCtrl'
    })
    $urlRouterProvider.when(/\/translations\/?/, '/translations/de');
    // $urlRouterProvider.otherwise('/');
  }]
);
