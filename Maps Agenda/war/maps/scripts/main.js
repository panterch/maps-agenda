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

common.setHashParams = function(params) {
  var pairs = [];
  for(var key in params) {
    pairs.push(key + '=' + params[key]);
  }

  window.location.hash = '#' + pairs.join('&');
};

common.getSelectedLanguage = function() {
  return common.getHashParams()['lang'] || 'de';
};

common.getStartDate = function() {
  var pivot = window.calPivot || new Date();

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
    pivot.getFullYear(),
    pad(pivot.getMonth() + 1, 2),
    pad(pivot.getDate(), 2)
  ].join('-');
};


//2013-10-01
