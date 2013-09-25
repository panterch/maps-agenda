/**
 * @fileoverview Displays upcoming events.
 */

var DAYS_OF_WEEK = [
  'Monday', 'Tuesday', 'Wednesday', 'Thursday',
  'Friday', 'Saturday', 'Sunday'
];

function EventsCtrl($scope) {
  $scope.queryEvents = function() {
    $scope.events = [
      {
        date: '3/21/2013',
        title: 'My super cool event',
        description: 'Some description about my event',
        url: 'http://www.example.com'
      },
      {
        date: '3/25/2013',
        title: 'My super cool event',
        description: 'Some description about my event',
        url: 'http://www.example.com'
      }
    ];
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