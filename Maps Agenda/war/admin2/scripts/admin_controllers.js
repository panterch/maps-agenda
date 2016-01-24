//Controller for the xml generation page.
adminApp.controller('GenerateCtrl', function ($scope) {});

//Controller for the look & feel page.
adminApp.controller('LookNFeelCtrl', function ($scope, $http, background_thumbnails, background_color) {
  $scope.background_thumbnails = background_thumbnails;
  $scope.background_color = background_color;

  $scope.serve_background = function(blobKey) {
    $http({
    method : 'GET',
        url : '/admin/background_images?type=serve_image&blob_key=' + blobKey
      }).success(function(){
      for (var i = 0; i <  $scope.background_thumbnails.length; ++i) {
      $scope.background_thumbnails[i].served =
        (String)($scope.background_thumbnails[i].key == blobKey);  
      }
      });
  }
  
  $scope.delete_background = function(blobKey) {
    $http({
      method : 'GET',
      url : '/admin/background_images?type=delete&blob_key=' + blobKey
      }).then(function(data) {
      if (data.data.result == "true") {
          for (var i = 0; i <  $scope.background_thumbnails.length; ++i) {
            if ($scope.background_thumbnails[i].key == blobKey) {
              $scope.background_thumbnails.splice(i, 1);
              return;
            }
          }
      }
      })
  }
  
  $scope.get_upload_url = function(form) {
    $http({
      method : 'GET',
      url : '/admin/background_images?type=get_upload_url'
    }).success(function(url) {form.action = url});
  }
  
  $scope.apply_bg_color = function() {
    $http({
    method : 'GET',
    url : '/admin/background_images?type=serve_color&color=' + $scope.background_color
    });
  }
});

//Controller for the send newsletter page.
adminApp.controller('NewsletterCtrl', function ($scope, $location, $http, month_str, background_color) {
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
});

//Controller for the settings page.
adminApp.controller('SettingsCtrl', function ($scope, $http, mailchimp_credentials) {
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
});

adminApp.controller('UploadBackgroundCtrl', [ '$scope', '$upload', '$http', '$state', function($scope, $upload, $http, $state) {
  $scope.onFileSelect = function($files) {
    for (var i = 0; i < $files.length; i++) {
      var $file = $files[i];
      $http({
        method : 'GET',
        url : '/admin/background_images?type=get_upload_url&redirect=/admin2/#/looknfeel'
      }).success(function(data){
        $upload.upload({
          url: data.url,
          file: $file,
          progress: function(e){}
        }).then(function(data, status, headers, config) {
          console.log(data);
          $state.reload();
        });
      });
    }
  }
}]);
