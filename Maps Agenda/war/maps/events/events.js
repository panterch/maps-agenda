(function() {
	'use strict';

	angular.module('app.maps').controller('EventsCtrl', EventsCtrl);

	EventsCtrl.$inject = [ '$scope', '$location', 'date', 'events',
			'datehelper' ];

	function EventsCtrl($scope, $location, date, events, datehelper) {

		var MONTHS = [ 'mojanuar', 'mofebruar', 'momaerz', 'moapril', 'momai',
				'mojuni', 'mojuli', 'moaugust', 'moseptember', 'mooktober',
				'monovember', 'modezember' ];

		var MONTHS_SHORT = [ 'moabjanuar', 'moabfebruar', 'moabmaerz',
				'moabapril', 'moabmai', 'moabjuni', 'moabjuli', 'moabaugust',
				'moabseptember', 'moaboktober', 'moabnovember', 'moabdezember' ];

		var DAYS_OF_WEEK_SHORT = [ 'wtabmontag', 'woabdienstag',
				'wtabmittwoch', 'wtabdonnerstag', 'wtabfreitag', 'wtabsamstag',
				'wtabsonntag' ];

		var DAYS_OF_WEEK_LONG = [ 'wtsonntag', 'Wochentage', 'wtdienstag',
				'wtmittwoch', 'wtdonnerstag', 'wtfreitag', 'wtsamstag' ];
		// If there is no date in the URL, set the date from the keeper.
		if (!date) {
			$location.search('date', datehelper.dateToString(datehelper.getDate()));
			return;
		}

		$scope.events = events;
		// Fix the date format for IE 8.
		for (var i = 0; i < $scope.events.length; i++) {
			$scope.events[i].date = $scope.events[i].date.replace(/[-]/g, '/');
		}

		$scope.date_str = date.replace(/[-]/g, '/');
		$scope.date = new Date($scope.date_str);
		$scope.pivot = new Date($scope.date);
		// $scope.pivot.setDate(1);
		datehelper.setDate($scope.date);

		$scope.printDate = function(dateStr) {
			var date = new Date(dateStr);
			if ($scope.lang == "ma")
				return (date.getMonth() + 1) + '.' + date.getDate() + '.';
			else
				return date.getDate() + '.' + (date.getMonth() + 1) + '.';
		};

		$scope.printDate2 = function(dateStr) {
			if (!dateStr)
				return '';
			var date = new Date(dateStr);
			var month = $scope.phrases[MONTHS_SHORT[date.getMonth()]];
			if (!month)
				return $scope.printDate(dateStr);
			else if ($scope.lang == "ma")
				return month + ' ' + date.getDate();
			else
				return date.getDate() + ' ' + month;
		};

		$scope.printDay = function(dateStr) {
			var date = new Date(dateStr);
			return DAYS_OF_WEEK_LONG[date.getDay()];
		};
		$scope.printFirstDate = function() {
			if ($scope.events.length == 0)
				return $scope.printDate2($scope.date_str);
			return $scope.printDate2($scope.events[0].date);
		}
		$scope.canGoBack = function() {
			if ($scope.events.length > 0)
				return true;
			return new Date($scope.date_str) > new Date();
		}
		$scope.canGoForward = function() {
			if ($scope.events.length > 0)
				return true;
			return new Date($scope.date_str) < new Date();
		}
		$scope.printLastDate = function() {
			if ($scope.events.length == 0)
				return $scope.printDate2($scope.date_str);
			return $scope
					.printDate2($scope.events[$scope.events.length - 1].date);
		}
		$scope.showPreviousEvents = function() {
			var new_date;
			if ($scope.events.length > 0) {
				new_date = new Date($scope.events[0].date);
				new_date.setDate(new_date.getDate() - 1);
			} else if ($location.search().back == null) {
				new_date = new Date($scope.date);
				new_date.setDate($scope.date.getDate() - 1);
			} else {
				new_date = new Date($scope.date);
				new_date.setDate($scope.date.getDate() - 15);
			}
			$location.search('date', datehelper.dateToString(new_date));
			$location.search('back', true);
		}
		$scope.showNextEvents = function() {
			var new_date;
			if ($scope.events.length > 0) {
				new_date = new Date(
						$scope.events[$scope.events.length - 1].date);
				new_date.setDate(new_date.getDate() + 1);
			} else if ($location.search().back == null) {
				new_date = new Date($scope.date);
				new_date.setDate(new_date.getDate() + 15);
			} else {
				new_date = new Date($scope.date);
				new_date.setDate(new_date.getDate() + 1);
			}
			$location.search('date', datehelper.dateToString(new_date));
			$location.search('back', null);
		}

		$scope.getDaysInMonth = function() {
			return new Date($scope.pivot.getYear(),
					$scope.pivot.getMonth() + 1, 0).getDate();
		};

		$scope.movePivotForward = function() {
			$scope.pivot.setMonth($scope.pivot.getMonth() + 1);
			$scope.renderCalendar();
		};

		$scope.movePivotBack = function() {
			$scope.pivot.setMonth($scope.pivot.getMonth() - 1);
			$scope.renderCalendar();
		};

		$scope.renderCalendar = function() {
			$scope.strings = DAYS_OF_WEEK_SHORT;
			var currentWeek = [];
			var new_weeks = [ currentWeek ];
			for (var i = 0; i < $scope.getDaysInMonth(); i++) {
				var date = new Date($scope.pivot);
				date.setDate(i + 1);

				var day = date.getDay();

				// Add padding if the month doesn't begin on a Monday.
				for (var k = 1; i == 0 && k < (day > 0 ? day : 7); k++) {
					currentWeek.push('');
				}

				// Wrap to the next week.
				if ((day - 1) % 7 == 0) {
					currentWeek = [];
					new_weeks.push(currentWeek);
				}

				currentWeek.push(i + 1);
			}
			$scope.weeks = new_weeks;
		};

		$scope.getMonth = function() {
			return MONTHS[$scope.pivot.getMonth()];
		}

		$scope.isSelected = function(day) {
			var d = new Date($scope.pivot);
			d.setDate(day);
			if ($scope.date.toDateString() == d.toDateString()) {
				return 'selected';
			} else {
				return 'background-color';
			}
		}

		$scope.selectDate = function(day) {
			var date = new Date($scope.pivot);
			date.setDate(day);
			$location.search('date', datehelper.dateToString(date));
			$location.search('back', null);
		}
	}
	;
})();
