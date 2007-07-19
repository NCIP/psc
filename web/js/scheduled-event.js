function updateFormForState() {
    var v = $F("new-mode-selector")
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

Event.observe(window, "load", registerStateBasedFormMutator)
Event.observe(window, "load", updateFormForState)
Event.observe(window, "load", registerBatchRescheduleHandlers);