/*globals psc jQuery */
psc.namespace("schedule.preview");

psc.schedule.preview.Parameters = (function ($) {
  var query = null;
  var pending = false;

  function findPairKey(pair) {
    var found = null;
    $.each(query.get('segment'), function (prop, val) {
      if (val == pair.segment && query.get('start_date[' + prop + ']') === pair.start_date) {
        found = prop;
        return false;
      }
    });
    return found;
  }

  function nextKey() {
    var intKeys = [];
    $.each(query.get('segment'), function (key, _) {
      if (/^\d+$/.match(key)) {
        intKeys.push(parseInt(key));
      }
    });
    intKeys.sort();
    return (intKeys.pop() || 0) + 1;
  }

  return {
    init: function (hashParams) {
      query = $.query.load("#" + hashParams);
      pending = false;
      $('#schedule').bind('schedule-ready', function () {
        pending = false;
      });
    },

    size: function () {
      var ct = 0;
      $.each(query.get('segment'), function (prop, value) { ct += 1; });
      return ct;
    },

    clear: function () {
      query.EMPTY();
      pending = true;
    },

    add: function (newPair) {
      var newKey = nextKey();
      query.SET('segment[' + newKey + ']', newPair.segment);
      query.SET('start_date[' + newKey + ']', newPair.start_date);
      pending = true;
    },

    remove: function(pair) {
      var key = findPairKey(pair);
      if (key != null) {
        query.REMOVE('segment[' + key + ']');
        query.REMOVE('start_date[' + key + ']');
      }
      pending = true;
    },

    pending: function() {
      return pending;
    },

    requestedSegments: function () {
      var segments = [];
      $.each(query.get('segment'), function (prop, val) {
        segments.push({ segment: val, start_date: query.get('start_date')[prop] })
      });
      segments.sort(function (a, b) {
        var a_date = a.start_date;
        var b_date = b.start_date;
        if (a_date == b_date) {
          return 0;
        } else if (a_date < b_date) {
          return -1;
        } else {
          return 1;
        }
      });
      return segments;
    },

    toQueryString: function () {
      return query.toString();
    }
  };
}(jQuery));
