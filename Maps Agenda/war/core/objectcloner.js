(function() {
    'use strict';

    angular
        .module('app.core')
        .factory('objectcloner', objectcloner);

    /* @ngInject */
    function objectcloner() {

        var service = {
        		cloneObject: cloneObject
        };

        return service;

        function cloneObject(object){
        	return JSON.parse(JSON.stringify(object));
        }
    }
})();
