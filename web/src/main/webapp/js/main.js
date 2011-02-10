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
    $$('.go-to-schedule-control').each(function (elt) {
        Event.observe(elt, "click", function(evt) {
            Event.stop(evt);
            var a = Event.element(evt);
            var ssId = a.id.substring('go-to-schedule-control-'.length);
            var selected = jQuery('#assigned-subject-selector-' + ssId + ' option:selected');
            var assignment = selected.attr('assignment');
            window.location.href = a.href + "?assignment=" + assignment;
        });
    });
};
Event.observe(window, "load", SC.Main.registerGoToScheduleControl);

SC.Main.registerTakeSubjectOffStudy = function() {
    $$('.take-subject-off-study').each(function (elt) {
        Event.observe(elt, "click", function(evt) {
            Event.stop(evt);
            var a = Event.element(evt);
            var ssId = a.id.substring('take-subject-off-study-'.length);
            var selected = jQuery('#assigned-subject-selector-' + ssId + ' option:selected');
            if (selected.attr('off') == 'false') {
                var assignment = selected.attr('assignment');
                window.location.href = a.href + "?assignment=" + assignment;
            } else {
                alert("That subject is already off the study");
            }
        })
    });
};
Event.observe(window, "load", SC.Main.registerTakeSubjectOffStudy);
