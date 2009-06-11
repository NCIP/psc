jQuery.noConflict();
require_main("prototype.js");

(function ($) {
  Screw.Matchers["raise"] = {
    match: function (expected, actual) {
      try {
        actual();
        return false;
      } catch (e) {
        actual.actualException = e;
        return e == expected;
      }
    },

    failure_message: function (expected, actual, not) {
      var msg = 'expected to ' + (not ? 'not ' : '') + 'throw ' + jQuery.print(expected)
      if (actual.actualException) {
        msg += '; was ' + jQuery.print(actual.actualException);
      }
      return msg;
    }
  }

  Screw.Matchers["include"] = {
    match: function (expected, actual) {
      return $.inArray(expected, actual) >= 0;
    },

    failure_message: function (expected, actual, not) {
      return 'expected ' + $.print(actual) + ' to ' + (not ? 'not ' : '') + 
        'include ' + $.print(expected);
    }
  }

  Screw.Matchers["equal_date"] = {
    match: function(expected, actual) {
      return expected.getFullYear() == actual.getFullYear() &&
        expected.getMonth() == actual.getMonth() &&
        expected.getDate() == actual.getDate();
    },

    failure_message: function(expected, actual, not) {
      return 'expected ' + actual + (not ? ' to not equal ' : ' to equal ') + expected;
    }
  }

  Screw.Matchers["equal_utc_date"] = {
    match: function (expected, actual) {
      return expected.getUTCFullYear() == actual.getUTCFullYear() &&
        expected.getUTCMonth() == actual.getUTCMonth() &&
        expected.getUTCDate() == actual.getUTCDate();
    },

    failure_message: function (expected, actual, not) {
      return "expected " + actual + " to " + (not ? 'not ' : '') + 
        "be on same UTC day as " + expected;
    }
  }

  Screw.Matchers["equal_utc_time"] = {
    match: function (expected, actual) {
      return expected.getTime() == actual.getTime()
    },

    failure_message: function (expected, actual, not) {
      return "expected " + actual + " to " + (not ? 'not ' : '') + 
        "be exactly the same as " + expected;
    }
  }
}(jQuery));
