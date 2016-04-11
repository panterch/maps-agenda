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

// Small service to keep the date between state transitions. For example,
// when coming back to the events after looking at the contacts, the selected
// date remains the same and does not jump back to the current day.
angular.module('mapsApp')
	   .service('dateKeeper', dateKeeper);
function dateKeeper(){
  this.date = new Date();
  this.getDate = function() { return this.date; }
  this.setDate = function(new_date) { this.date = new Date(new_date); }
};

angular.module('mapsApp')
	   .controller('MainCtrl', MainCtrl);

MainCtrl.$inject = ['$scope', '$location', '$http', 'lang', 'languages', 'phrases', 'tags'];

function MainCtrl ($scope, $location, $http, lang, languages, phrases, tags){
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


