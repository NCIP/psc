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
        var header = section.getElementsByTagName("H3")[0]
        header.innerHTML += " <span class='collapse-icon'>&#65291;</span>"
        header.title = "Click to reveal"
        Event.observe(header, 'click', function() {
            var content = section.getElementsByClassName("content")[0]
            var icon = section.getElementsByClassName("collapse-icon")[0]
            if (content.visible()) {
                SC.slideAndHide(content, {
                    afterFinish: function() {
                        header.title = "Click to reveal form"
                        Element.update(icon, '&#65291;')
                    }
                });
            } else {
                SC.slideAndShow(content, {
                    afterFinish: function() {
                        header.title = "Click to conceal form"
                        Element.update(icon, '&#8212;')
                    }
                });
            }
        })
    })
}

function registerCheckAllEvents() {
    Event.observe('check-all-events', "click", function(event){$('batch-form').checkCheckboxes('events');Event.stop(event);})    
}

function registerUncheckAllEvents() {
    Event.observe('uncheck-all-events', "click", function(event){$('batch-form').uncheckCheckboxes('events');Event.stop(event);})
}

Event.observe(window, "load", registerBatchRescheduleHandlers)
Event.observe(window, "load", registerSubheaderCollapse)
Event.observe(window, "load", registerCheckAllEvents)
Event.observe(window, "load", registerUncheckAllEvents)