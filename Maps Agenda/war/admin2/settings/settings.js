(function() {
    'use strict';
  //Controller for the settings page.
  angular.module('app.admin')
        .controller('SettingsCtrl', SettingsCtrl); 
        
  function SettingsCtrl($scope, $http, mailchimp_credentials) {
    $scope.list_id = mailchimp_credentials.list_id;
    $scope.api_key = mailchimp_credentials.api_key;
    
    $scope.update = function() {
      $http({
        method : 'GET',
        url : '/admin/data?type=save_mailchimp_credentials&list_id=' +
              $scope.list_id + "&api_key=" + $scope.api_key
        }).success(function(data) {
          if (data != null && data.data != null && data.data.error != null) {
            alert("Failed to save credentials: " + data.data.error);
          } else {
            alert("Successfully saved " + $scope.list_id + ", " + $scope.api_key +
                  ". Your next campaign will be created using these credentials.")
          }});
    }
  };
})();
