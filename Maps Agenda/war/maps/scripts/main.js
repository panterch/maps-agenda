common = {};

common.getHashParams = function() {
  var hash = window.location.hash.substring(1);
  var params = {};
  hash.split('&').forEach(function(pair) {
    var tokens = pair.split('=');
    var key = tokens.splice(0, 1)[0];
    var value = tokens.join('=');
    if (key) {
      params[key] = value;
    }
  });

  return params;
};

common.getSelectedLanguage = function() {
  return common.getHashParams()['lang'] || 'de';
};

common.getStartDate = function() {
  var pivot = window.calPivot || new Date();
  var offset = (pivot.getDay() - 1) % 7;
  if (offset < 0) {
    offset += 7;
  }

  var startDate = new Date(
      pivot - offset * 24 * 3600 * 1000);
  console.debug(startDate);

  function pad(number, spaces) {
    var output = '';
    for (var i = 0; i < spaces; i++) {
      output += '0';
    }
    output += number;

    return ([].splice.call(
        output.split(''),
        output.length - spaces,
        spaces)).join('');
  }

  return [
    startDate.getFullYear(),
    pad(startDate.getMonth() + 1, 2),
    pad(startDate.getDate(), 2)
  ].join('-');
};



//2013-10-01
