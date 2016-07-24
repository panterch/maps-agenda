(function() {
    'use strict';
  //Controller for the look & feel page.
  angular.module('app.admin')
  		.controller('LookNFeelCtrl', LookNFeelCtrl);

  function LookNFeelCtrl($scope, $http, background_thumbnails, background_color){
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
  };
})();
