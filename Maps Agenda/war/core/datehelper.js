(function() {
	'use strict';
	// Small service to keep the date between state transitions. For example,
	// when coming back to the events after looking at the contacts, the
	// selected
	// date remains the same and does not jump back to the current day.
	angular.module('app.core').factory('datehelper', datehelper);

	function datehelper() {
		var date = new Date();
		var service = {
			getDate : getDate,
			setDate : setDate,
			dateToString : dateToString,
			monthToString: monthToString
		}

		return service;

		function getDate() {
			return date;
		}

		function setDate(new_date) {
			this.date = new Date(new_date);
		}

		function dateToString(realDate) {
			var dd = realDate.getDate();
			var mm = realDate.getMonth() + 1; // January is 0.
			var yyyy = realDate.getFullYear();
			if (dd < 10) {
				dd = '0' + dd;
			}
			if (mm < 10) {
				mm = '0' + mm;
			}
			return yyyy + '-' + mm + '-' + dd;
		}
		
		function monthToString(month) {
			  var mm = month.getMonth() + 1;  // January is 0.
			  var yyyy = month.getFullYear();
			  if(mm < 10) { mm = '0' + mm; } 
			  return yyyy + '-' + mm;    
		}

	}
})();