function updateFormForState() {
    var v = $("new-mode-selector").value
    if ("-1" == v) {
        console.log("-1")
        $("new-date-input-group").hide()
        $("move_date_by_new-date-input-group").hide()
        $("new-reason-input-group").hide()
        if ($("new-mode-submit") != null) $("new-mode-submit").hide()
    } else if (3 == v) {
        console.log("3")
        $("new-date-input-group").hide()
        $("move_date_by_new-date-input-group").hide()
        $("new-reason-input-group").show()
        if ($("new-mode-submit") != null) $("new-mode-submit").show()
    } else if (1 == v ){
        console.log("1")
        $("new-date-input-group").show()
        $("move_date_by_new-date-input-group").hide()
        $("new-reason-input-group").show()
        if ($("new-mode-submit") != null) $("new-mode-submit").show()
    } else if ("" == v ){
        console.log(" empty ")
        $("new-date-input-group").hide()
        $("move_date_by_new-date-input-group").show()
        $("new-reason-input-group").show()
        if ($("new-mode-submit") != null) $("new-mode-submit").show()
    } else if (2 == v) {
        console.log("2")
        $("new-date-input-group").hide()
        $("move_date_by_new-date-input-group").hide()
        $("new-reason-input-group").hide()
        if ($("new-mode-submit") != null) $("new-mode-submit").show()
    } else if (5 == v) {
        console.log("5")
        $("new-date-input-group").show()
        $("move_date_by_new-date-input-group").hide()
        $("new-reason-input-group").show()
        if ($("new-mode-submit") != null) $("new-mode-submit").show()
    } else if (7 == v) {
        console.log("7")
        $("new-date-input-group").hide()
        $("move_date_by_new-date-input-group").hide()
        $("new-reason-input-group").hide()
        if ($("new-mode-submit") != null) $("new-mode-submit").show()
    } else if (6 == v) {
        console.log("6")
        $("new-date-input-group").hide()
        $("move_date_by_new-date-input-group").hide()
        $("new-reason-input-group").show()
        if ($("new-mode-submit") != null) $("new-mode-submit").show()
    }
}

function registerStateBasedFormMutator() {
    Event.observe("new-mode-selector", "change", updateFormForState)
}



Event.observe(window, "load", registerStateBasedFormMutator)
Event.observe(window, "load", updateFormForState)