(function() {
    'use strict';
  //Controller for the send newsletter page.
  angular.module('app.admin')
  		.controller('NewsletterCtrl', NewsletterCtrl);

  function NewsletterCtrl($scope, $location, $http, month_str, background_color, datekeeper){
    if (month_str == null || month_str == '') {
      $scope.month_str = monthToString(new Date())
      $scope.date = $scope.month_str + "-01";
      $scope.month = new Date($scope.date);
    } else {
      $scope.date = month_str + "-01";
      $scope.month = new Date($scope.date);
      $scope.month_str = monthToString($scope.month)
    }
    
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
    $scope.createCampaign = function() {
      $http({
        method : 'GET',
            url : '/admin/data?type=campaign&month=' + $scope.month_str + '&bgcolor=' + $scope.background_color
          }).success(function(data) {
            if (data.status == "error") {
              alert(
                "Campaign creation failed! Reason: " + data.name + ": " +
                data.error);
            } else {
              alert(
                "Campaign \"" + data.title + "\" successfully created! " +
                "To give you a chance to preview the email, it will not be " +
                "sent automatically. Please head over to mailchimp.com, " +
                "review the campaign, and click send if you are ok with it.");
            }
            console.log(data)
          });
    }
    $scope.background_color = background_color;
  };
})();