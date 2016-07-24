(function() {
    'use strict';
    
  angular.module('app.admin')
  .controller('UploadBackgroundCtrl', UploadBackgroundCtrl); 

  function UploadBackgroundCtrl($scope, $upload, $http, $state) {
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
  };
})();
