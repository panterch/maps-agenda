/**
 * @fileoverview Displays upcoming events.
 */

var DAYS_OF_WEEK_LONG = [
  'Wochentage', 'wtdienstag', 'wtmittwoch', 'wtdonnerstag',
  'wtfreitag', 'wtsamstag', 'wtsonntag'
];

function EventsCtrl($scope, $http) {
  $scope.cursors = [];

  window.addEventListener('message', function(evt) {
    if (evt.data == 'datechanged') {
      $scope.queryEvents();
    }
  });

  window.addEventListener('hashchange', function() {
    var targetDate = common.getHashParams()['date'];
    if (targetDate) {
      window.calPivot = new Date(targetDate);
    }

    $scope.queryEvents();
  });

  $scope.queryEvents = function(opt_cursor) {
    var params = [
      'lang=' + common.getSelectedLanguage(),
      'startDate=' + common.getStartDate()
    ];

    if (Boolean(opt_cursor)) {
      params.push('cursor=' + opt_cursor);
    }

    var url = '/maps/scripts/data.json?' + params.join('&');
    $http.get(url).success(function(data) {
      $scope.events = data.events;
      $scope.cursors.push(data.cursor);
      common.updateLanguage(data.strings);
      common.applyLanguage();
    });
  };

  $scope.showNextEvents = function() {
    var cursor = $scope.cursors.pop();
    if (cursor) {
      $scope.cursors.push(cursor);
    }

    $scope.queryEvents(cursor);
  };

  $scope.showPreviousEvents = function() {
    var cursor = $scope.cursors.pop();
    $scope.queryEvents(cursor);
  };

  $scope.printDate = function(dateStr) {
    var date = new Date(dateStr);
    return date.getDate() + '. ' + (date.getMonth() + 1) + '.';
  };

  $scope.printDay = function(dateStr) {
    var date = new Date(dateStr);
    return common.getLanguageString(DAYS_OF_WEEK_LONG[date.getDay()]);
  };

};