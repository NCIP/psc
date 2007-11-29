function updateFormForState() {
    var v = $("new-mode-selector").value
    if ("-1" == v) {
        $("new-date-input-group").hide()
        $("new-reason-input-group").hide()
        if ($("new-mode-submit") != null) $("new-mode-submit").hide()
    } else if (3 == v) {
        $("new-date-input-group").hide()
        $("new-reason-input-group").show()
        if ($("new-mode-submit") != null) $("new-mode-submit").show()
    } else if (1 == v){
        $("new-date-input-group").show()
        $("new-reason-input-group").show()
        if ($("new-mode-submit") != null) $("new-mode-submit").show()
    } else if (2 == v) {
        $("new-date-input-group").hide()
        $("new-reason-input-group").hide()
        if ($("new-mode-submit") != null) $("new-mode-submit").show()
    } else if (4 == v) {
        $("new-date-input-group").show()
        $("new-reason-input-group").show()
        if ($("new-mode-submit") != null) $("new-mode-submit").show()
    } else if (5 == v) {
        $("new-date-input-group").hide()
        $("new-reason-input-group").hide()
        if ($("new-mode-submit") != null) $("new-mode-submit").show()
    }
}

function registerStateBasedFormMutator() {
    Event.observe("new-mode-selector", "change", updateFormForState)
}


//    var v = $("new-mode-selector").value
//    if ("" == v) {
//        $("new-date-input-group").hide()
//        $("new-reason-input-group").hide()
//        if ($("new-mode-submit") != null) $("new-mode-submit").hide()
//    } else if (4 == v) {
//        $("new-date-input-group").hide()
//        $("new-reason-input-group").show()
//        if ($("new-mode-submit") != null) $("new-mode-submit").show()
//    } else if (1 == v || 2 == v){
//        $("new-date-input-group").show()
//        $("new-reason-input-group").show()
//        if ($("new-mode-submit") != null) $("new-mode-submit").show()
//    } else if (3 == v) {
//        $("new-date-input-group").hide()
//        $("new-reason-input-group").hide()
//        if ($("new-mode-submit") != null) $("new-mode-submit").show()
//    } else if (5 == v) {
//        $("new-date-input-group").show()
//        $("new-reason-input-group").show()
//        if ($("new-mode-submit") != null) $("new-mode-submit").show()
//    } else if (6 == v) {
//        $("new-date-input-group").hide()
//        $("new-reason-input-group").hide()
//        if ($("new-mode-submit") != null) $("new-mode-submit").show()
//    }
//}



Event.observe(window, "load", registerStateBasedFormMutator)
Event.observe(window, "load", updateFormForState)