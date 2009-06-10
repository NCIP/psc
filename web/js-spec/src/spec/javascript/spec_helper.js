jQuery.noConflict();

require_main("prototype.js");

(function($) {
  Screw.Matchers["raise"] = {
    match: function(expected, actual) {
      try {
        actual()
        return false
      } catch (e) {
        actual.actualException = e
        return e == expected
      }
    },
  
    failure_message: function(expected, actual, not) {
      var msg = 'expected to ' + (not ? 'not ' : '') + 'throw ' + $.print(expected)
      if (actual.actualException) {
        msg += '; was ' + $.print(actual.actualException)
      }
      return msg
    }
  }

  Screw.Matchers["include"] = {
    match: function(expected, actual) {
      return $.inArray(expected, actual) >= 0
    },
  
    failure_message: function(expected, actual, not) {
      return 'expected ' + $.print(actual) + ' to ' + (not ? 'not ' : '') + 
        'include ' + $.print(expected)
    }
  }
})(jQuery)