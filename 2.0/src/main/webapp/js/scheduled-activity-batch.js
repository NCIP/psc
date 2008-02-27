function registerBatchRescheduleHandlers() {
    Event.observe('batch-form', "submit", function(e) {
        $('batch-indicator').reveal()
        Event.stop(e)
        SC.asyncSubmit('batch-form', {
            onComplete: function() {
                $('batch-indicator').conceal()
            }
        })
    })
}

function registerSubheaderCollapse() {
    $$(".subcollapsible").each(function(section) {
        var header = section.getElementsByByTagName("H3")[0]
        header.innerHTML += " <span class='collapse-icon'>&#43;</span>"
        header.title = "Click to reveal"
        Event.observe(header, 'click', function() {
            var content = section.getElementsByClassName("content")[0]
            var icon = section.getElementsByClassName("collapse-icon")[0]
            if (content.visible()) {
                SC.slideAndHide(content, {
                    afterFinish: function() {
                        header.title = "Click to reveal form"
                        Element.update(icon, '&#43;')
                    }
                });
            } else {
                SC.slideAndShow(content, {
                    afterFinish: function() {
                        header.title = "Click to conceal form"
                        Element.update(icon, '&#45;')
                    }
                });
            }
        })
    })
}

function registerCheckAllEvents() {
    Event.observe('check-all-events', "click", function(event){$('batch-form').checkCheckboxes('event');Event.stop(event);})
}

function registerUncheckAllEvents() {
    Event.observe('uncheck-all-events', "click", function(event){$('batch-form').uncheckCheckboxes('event');Event.stop(event);})
}

function registerCheckAllConditionalEvents() {
    Event.observe('check-all-conditional-events', "click", function(event){
        $('batch-form').uncheckCheckboxes('event');Event.stop(event);
        $('batch-form').checkCheckboxes('conditional-event');Event.stop(event);
    })
}

function registerCheckAllPastDueEvents() {
    Event.observe('check-all-past-due-events', "click", function(event){
        $('batch-form').uncheckCheckboxes('event');Event.stop(event);
        $('batch-form').checkCheckboxes('past-due-event');Event.stop(event);
    })
}


function registerShowDaysButton() {
    Event.observe('show_days_button', "click", function(event){
        var allDaysPerPeriod = document.getElementsByClassName("days_from_period")
        for (var i=0; i<allDaysPerPeriod.length; i++) {
            allDaysPerPeriod[i].show()
        }
        $('show_days_button').hide()
        $('hide_days_button').show()
    })
}


function registerHideDaysButton() {
    Event.observe('hide_days_button', "click", function(event){
        var allDaysPerPeriod = document.getElementsByClassName("days_from_period")
        for (var i=0; i<allDaysPerPeriod.length; i++) {
            allDaysPerPeriod[i].hide()
        }
        $('show_days_button').show()
        $('hide_days_button').hide()
    })
}

//for IE7 we need to implement loading the function in this way. IE7 otherwise doesn't preserve the order
function registerAllFunctions() {
    registerBatchRescheduleHandlers();
    registerSubheaderCollapse();
    registerCheckAllEvents();
    registerUncheckAllEvents();
    registerCheckAllConditionalEvents();
    registerCheckAllPastDueEvents();
    registerShowDaysButton();
    registerHideDaysButton();
}
Event.observe(window, "load", registerAllFunctions);