// Event handler for updating the current activities on the subject coordinator dashboards
SC.updateCurrentActivities = function(href) {
    new Ajax.Request(href, {
        postBody: $('current-activities-form').serialize()
    });
}

var timeout = 3000;
var delay;
SC.registerCurrentActivitiesUpdaters = function(href) {
    Event.observe(window, 'load', function() {
        var toDateValueOld = $('toDate').value;
        delay = function() { timeoutFunction(toDateValueOld, href) };
        setTimeout(delay, timeout);

        var handler = function() { SC.updateCurrentActivities(href) }

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

        $('activityTypesList').observe('change', handler)

        var handler1 = function(evt) {
            Event.stop(evt)
        }
        $('current-activities-form').observe('submit', handler1)
    })
}

function timeoutFunction(toDateValueOld, href) {
  var toDateValueNew = $('toDate').value;
  if (toDateValueOld != toDateValueNew) {
    SC.updateCurrentActivities(href)
  }
  delay = function() {timeoutFunction(toDateValueNew, href)};
  setTimeout(delay, timeout)
}

function selectAll(selectBox, selectAll) {
    var box = $(selectBox);
    for (var i = 0; i < box.options.length; i++) {
        box.options[i].selected = selectAll;
    }
}
