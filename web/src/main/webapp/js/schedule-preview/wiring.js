/*
 * Event handler registration for the preview mode of the
 * single-subject schedule page.
 */

(function ($) {
  // for parameters
  
  $(window).load(function () {
    psc.schedule.preview.Parameters.init(window.location.hash);
  });

}(jQuery));