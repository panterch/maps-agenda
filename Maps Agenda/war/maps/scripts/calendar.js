/**
 * @fileoverview Displays a simple calendar that can be navigated only on per month basis.
 */

var DAYS_IN_MONTH = {
  0: 31,
  1: 28,
  2: 31,
  3: 30,
  4: 31,
  5: 30,
  6: 31,
  7: 31,
  8: 30,
  9: 31,
  10: 30,
  11: 31
};

var MONTHS = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December'
];

function CalendarCtrl($scope) {
  $scope.pivot = new Date();
  $scope.getDaysInMonth = function() {
    var days = DAYS_IN_MONTH[$scope.pivot.getMonth()];
    if (days == 28 && ($scope.pivot.getYear() - 100) % 4 == 0) {
      days++;
    }

    return days;
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
    $scope.titles = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    var currentWeek = [];
    $scope.weeks = [currentWeek];
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
        $scope.weeks.push(currentWeek);
      }

      currentWeek.push(i + 1);
    }
  };

  $scope.getMonth = function() {
    return MONTHS[$scope.pivot.getMonth()].toUpperCase();
  }

  $scope.isToday = function(day) {
    var today = new Date();
    return today.getDate() == day ? 'today' : '';
  }
};