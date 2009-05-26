jQuery.noConflict();

require_main("prototype.js")

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
    var msg = 'expected to ' + (not ? 'not ' : '') + 'throw ' + jQuery.print(expected)
    if (actual.actualException) {
      msg += '; was ' + jQuery.print(actual.actualException)
    }
    return msg
  }
}