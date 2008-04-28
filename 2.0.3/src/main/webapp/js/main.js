SC.Main = new Object();

// add handlers to have the entire LI for each studySegment act like the A was clicked
// this is so that the LI can be used as a container for other controls, but still be
// generally useful
SC.Main.registerClickForwarders = function() {
    $$('.epochs-and-studySegments li').each(SC.Main.registerClickForwarder)
}

SC.Main.registerClickForwarder = function(studySegmentItem) {
    Event.observe(studySegmentItem, 'click', function() {
        var studySegmentA = studySegmentItem.select("a.studySegment")[0];
        studySegmentA.click();
    });
}

Event.observe(window, "load", SC.Main.registerClickForwarders)


SC.Main.registerGoToScheduleControl = function() {
    if ($('go-to-schedule-control')) {
        Event.observe('go-to-schedule-control', "click", function(e) {
            Event.stop(e)
            var a = $('go-to-schedule-control')
            var scheduleId = $F('assigned-subject-selector')
            window.location.href = a.href + "?calendar=" + scheduleId;
        })
    }
}
Event.observe(window, "load", SC.Main.registerGoToScheduleControl)


SC.Main.registerGoToScheduleControl = function() {
    if ($('go-to-schedule-control')) {
        Event.observe('go-to-schedule-control', "click", function(e) {
            Event.stop(e)
            var a = $('go-to-schedule-control')
            var scheduleId = $F('assigned-subject-selector')
            window.location.href = a.href + "?calendar=" + scheduleId;
        })
    }
}
Event.observe(window, "load", SC.Main.registerGoToScheduleControl)

SC.Main.registerOffStudyGoToScheduleControl = function() {
    if ($('offstudy-go-to-schedule-control')) {
        Event.observe('offstudy-go-to-schedule-control', "click", function(e) {
            Event.stop(e)
            var a = $('offstudy-go-to-schedule-control')
            var scheduleId = $F('offstudy-assigned-subject-selector')
            window.location.href = a.href + "?calendar=" + scheduleId;
        })
    }
}
Event.observe(window, "load", SC.Main.registerOffStudyGoToScheduleControl)
