function updateFormForState() {
    var v = $("new-mode-selector").value
    if ("" == v) {
        $("new-date-input-group").hide()
        $("new-reason-input-group").hide()
        if (typeof($("new-mode-submit")) != 'undefined') $("new-mode-submit").hide()
    } else if (3 == v) {
        $("new-date-input-group").hide()
        $("new-reason-input-group").show()
        if (typeof($("new-mode-submit")) != 'undefined') $("new-mode-submit").show()
    } else if (1 == v){
        $("new-date-input-group").show()
        $("new-reason-input-group").show()
        if (typeof($("new-mode-submit")) != 'undefined') $("new-mode-submit").show()
    } else if (2 == v) {
        $("new-date-input-group").hide()
        $("new-reason-input-group").hide()
        if (typeof($("new-mode-submit")) != 'undefined') $("new-mode-submit").show()
    }
}

function registerStateBasedFormMutator() {
    Event.observe("new-mode-selector", "change", updateFormForState)
}




Event.observe(window, "load", registerStateBasedFormMutator)
Event.observe(window, "load", updateFormForState)