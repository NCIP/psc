/*
 * Event handler registration for the preview mode of the
 * single-subject schedule page.
 */

jQuery(window).load(function () {
  psc.schedule.preview.Controls.init(window.location.hash);
});
