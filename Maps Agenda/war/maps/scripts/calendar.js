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

function CalendarCtrl($scope, $http) {
  var targetDate = common.getHashParams()['date'];
  window.calPivot = targetDate ? new Date(targetDate) : new Date();
  $scope.getDaysInMonth = function() {
    var days = DAYS_IN_MONTH[window.calPivot.getMonth()];
    if (days == 28 && (window.calPivot.getYear() - 100) % 4 == 0) {
      days++;
    }

    return days;
  };

  $scope.movePivotForward = function() {
    window.calPivot.setMonth(window.calPivot.getMonth() + 1);
    $scope.renderCalendar();
    notifyDateChange();
  };

  $scope.movePivotBack = function() {
    window.calPivot.setMonth(window.calPivot.getMonth() - 1);
    $scope.renderCalendar();
    notifyDateChange();
  };

  var notifyDateChange = function() {
    window.postMessage('datechanged', window.location.href);
  };

  $scope.renderCalendar = function() {
    $scope.titles = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    var currentWeek = [];
    $scope.weeks = [currentWeek];
    for (var i = 0; i < $scope.getDaysInMonth(); i++) {
      var date = new Date(window.calPivot);
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
    return MONTHS[window.calPivot.getMonth()].toUpperCase();
  }

  $scope.isSelected = function(day) {
    var targetDate = common.getHashParams()['date'];
    var selected = targetDate ? new Date(targetDate) : new Date();

    if (selected.getDate() == day &&
        window.calPivot.getMonth() == selected.getMonth()) {
      return 'selected';
    } else {
      return '';
    }
  }

  $scope.selectDate = function(date) {
    window.calPivot.setDate(date);
    var params = common.getHashParams();
    params['date'] = common.getStartDate();
    common.setHashParams(params);
    notifyDateChange();
  }
};