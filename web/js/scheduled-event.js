function updateFormForState() {
    var v = $F("new-mode-selector")
    if ("" == v) {
        $("new-date-input-group").hide()
        $("new-reason-input-group").hide()
    } else if (3 == v) {
        $("new-date-input-group").hide()
        $("new-reason-input-group").show()
    } else {
        $("new-date-input-group").show()
        $("new-reason-input-group").show()
    }
}

function registerStateBasedFormMutator() {
    Event.observe("new-mode-selector", "change", updateFormForState)
}

Event.observe(window, "load", registerStateBasedFormMutator)
Event.observe(window, "load", updateFormForState)
