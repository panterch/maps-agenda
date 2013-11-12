/**
 * @fileoverview Displays upcoming events.
 */

var DAYS_OF_WEEK = [
  'Monday', 'Tuesday', 'Wednesday', 'Thursday',
  'Friday', 'Saturday', 'Sunday'
];

function EventsCtrl($scope, $http) {
  window.addEventListener('message', function(evt) {
    if (evt.data == 'datechanged') {
      $scope.queryEvents();
    }
  });

  window.addEventListener('hashchange', function() {
    $scope.queryEvents();
  });

  $scope.queryEvents = function() {
    var params = [
      'lang=' + common.getSelectedLanguage(),
      'startDate=' + common.getStartDate()
    ];
    var url = '/maps/scripts/data.json?' + params.join('&');
    $http.get(url).success(function(data) {
      $scope.events = data;
    });
  };

  $scope.printDate = function(dateStr) {
    var date = new Date(dateStr);
    return date.getDate() + '. ' + (date.getMonth() + 1) + '.';
  };

  $scope.printDay = function(dateStr) {
    var date = new Date(dateStr);
    return DAYS_OF_WEEK[date.getDay()];
  };

};