psc.namespace("schedule");

psc.schedule.IcsInstructions = (function ($) {
  function displayInstructions() {
    var base = $(this).attr('href');
    var https = base.match(/^https/);
  }

  return {
    init: function() {
      $("a.control.ics-subscribe").click(displayInstructions);
    }
  }
}(jQuery));