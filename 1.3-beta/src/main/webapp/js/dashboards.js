// Event handler for updating the current activities on the subject coordinator dashboards
SC.updateCurrentActivities = function(href) {
    new Ajax.Request(href, {
        postBody: $('current-activities-form').serialize()
    });
}

SC.registerCurrentActivitiesUpdaters = function(href) {
    Event.observe(window, 'load', function() {
        console.log("Registering")
        var handler = function() { SC.updateCurrentActivities(href) }
        $('toDate').observe('change', handler)
        $$('input.activity-type').each(function(input) {
            input.observe('change', handler)
        })
    })
}