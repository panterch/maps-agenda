(function() {
    'use strict';

  angular.module('app.maps')
  	   .controller('MainCtrl', MainCtrl);

  MainCtrl.$inject = ['$scope', '$location', 'lang', 'languages', 'phrases', 'tags'];

  function MainCtrl ($scope, $location, lang, languages, phrases, tags){
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
  };
})();