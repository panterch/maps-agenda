(function() {
    'use strict';

    angular
        .module('app.core')
        .factory('dataservice', dataservice);

    /* @ngInject */
    function dataservice($http) {

        var service = {
            getDataFromUrl: getDataFromUrl
        };

        return service;

        function getDataFromUrl(url){
            return $http({'method': 'GET', 'url': url})
                .then (function (data) {
                  return data.data;
                });
        }
    }
})();