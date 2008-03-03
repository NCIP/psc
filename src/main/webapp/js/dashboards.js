// Event handler for updating the current activities on the subject coordinator dashboards
SC.updateCurrentActivities = function(href) {
    new Ajax.Request(href, {
        postBody: $('current-activities-form').serialize()
    });
}

SC.registerCurrentActivitiesUpdaters = function(href) {
    Event.observe(window, 'load', function() {
        var handler = function() { SC.updateCurrentActivities(href) }

        $('toDate').observe('change', handler)
        var browser=navigator.appName;
        if (browser=="Microsoft Internet Explorer") {
            $('toDate').observe('keypress', function(e) {
                if(e.keyCode == 13) {
                    SC.updateCurrentActivities(href)
                }
            })
        }

        $$('input.activity-type').each(function(input) {
            input.observe('click', handler)
        })

        var handler1 = function(evt) {
            Event.stop(evt)
        }
        $('current-activities-form').observe('submit', handler1)
    })
}

