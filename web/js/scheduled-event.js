function updateFormForState() {
    var v = $("new-mode-selector").value
    if ("" == v) {
        $("new-date-input-group").hide()
        $("new-reason-input-group").hide()
        $("new-mode-submit").hide()
    } else if (3 == v) {
        $("new-date-input-group").hide()
        $("new-reason-input-group").show()
        $("new-mode-submit").show()
    } else if (1 == v){
        $("new-date-input-group").show()
        $("new-reason-input-group").show()
        $("new-mode-submit").show()
    } else if (2 == v) {
        $("new-date-input-group").hide()
        $("new-reason-input-group").hide()
        $("new-mode-submit").show()
    }
}

function registerStateBasedFormMutator() {
    Event.observe("new-mode-selector", "change", updateFormForState)
}

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
        header.innerHTML += " <span class='collapse-icon'>&#9660;</span>"
        header.title = "Click to reveal"
        Event.observe(header, 'click', function() {
            var content = section.getElementsByClassName("content")[0]
            var icon = section.getElementsByClassName("collapse-icon")[0]
            if (content.visible()) {
                SC.slideAndHide(content, {
                    afterFinish: function() {
                        header.title = "Click to reveal form"
                        Element.update(icon, '&#9660;')
                    }
                });
            } else {
                SC.slideAndShow(content, {
                    afterFinish: function() {
                        header.title = "Click to conceal form"
                        Element.update(icon, '&#9650;')
                    }
                });
            }
        })
    })
}

function registerCheckAllEvents() {
    Event.observe('check-all-events', "click", function(event){$('batch-form').checkCheckboxes('events')})
}

function registerUncheckAllEvents() {
    Event.observe('uncheck-all-events', "click", function(event){$('batch-form').uncheckCheckboxes('events')})
}


Event.observe(window, "load", registerStateBasedFormMutator)
Event.observe(window, "load", updateFormForState)
Event.observe(window, "load", registerBatchRescheduleHandlers)
Event.observe(window, "load", registerSubheaderCollapse)
Event.observe(window, "load", registerCheckAllEvents)
Event.observe(window, "load", registerUncheckAllEvents)