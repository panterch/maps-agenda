// Controller for the events page.
adminApp.controller('EventCtrl', function ($scope, $location, month_str, events) {
  if (month_str == null || month_str == '') {
    $scope.month_str = monthToString(new Date())
    $scope.date = $scope.month_str + "-01";
    $scope.month = new Date($scope.date);
  } else {
    $scope.date = month_str + "-01";
    $scope.month = new Date($scope.date);
    $scope.month_str = monthToString($scope.month)
  }
  $scope.events = events;
  $scope.month_regex = /^[0-9]{4}-(0[0-9]|1[012])$/;
  $scope.previousMonth = function() {
    $scope.month.setMonth($scope.month.getMonth() - 1);
    $scope.month_str = monthToString($scope.month)
    $scope.date = $scope.month_str + "-01";
    $location.search('month', $scope.month_str);    
  }
  $scope.nextMonth = function() {
    $scope.month.setMonth($scope.month.getMonth() + 1);
    $scope.month_str = monthToString($scope.month)
    $scope.date = $scope.month_str + "-01";
    $location.search('month', $scope.month_str);    
  }
  $scope.updateMonth = function() {
    if ($scope.month_regex.test($scope.month_str) &&
        $scope.month_str != monthToString($scope.month)) {
      $scope.date = $scope.month_str + "-01";
      $scope.month = new Date($scope.date);
      $location.search('month', $scope.month_str);    
    }
  }
  $scope.edit = function(index) {
    console.log("Editing " + index)
  }
  $scope.remove = function(index) {
    console.log("Removing " + index)
  }
});
