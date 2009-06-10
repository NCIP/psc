/*
 * Event handler registration for the single-subject schedule page
 */

(function ($) {
  // for schedule-data
  
  $(window).load(function () {
    setTimeout(psc.subject.ScheduleData.refresh, 500);
  })
  
  // for schedule-timeline
  
  $(window).load(function () {
    psc.subject.ScheduleTimeline.create();
    psc.subject.ScheduleTimeline.FocusHandler.init();
  }).resize(psc.subject.ScheduleTimeline.resize);

  // for schedule-list

  $(window).load(function () {
    psc.subject.ScheduleList.init();
    psc.subject.ScheduleList.FocusHandler.init();
  });

}(jQuery));