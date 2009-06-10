/* TODO: these functions must all be namespaced.  Also, most of these event registrations will need
 * to be made dynamic (in response to schedule updates) instead of one time at load.
 */

function updateFormForState() {
    var v = $("new-mode-selector").value
    // TODO: what does this do?  Are you going to be able to tell in 6mos?  In one mo?
    // Use a better abstraction.
    if ("-1" == v) {
        $("new-date-input-group").hide()
        $("move_date_by_new-date-input-group").hide()
        $("new-reason-input-group").hide()
        if ($("new-mode-submit") != null) $("new-mode-submit").hide()
    } else if (3 == v) {
        $("new-date-input-group").hide()
        $("move_date_by_new-date-input-group").hide()
        $("new-reason-input-group").show()
        if ($("new-mode-submit") != null) $("new-mode-submit").show()
    } else if (1 == v ){
        $("new-date-input-group").show()
        $("move_date_by_new-date-input-group").hide()
        $("new-reason-input-group").show()
        if ($("new-mode-submit") != null) $("new-mode-submit").show()
    } else if ("" == v ){
        $("new-date-input-group").hide()
        $("move_date_by_new-date-input-group").show()
        $("new-reason-input-group").show()
        if ($("new-mode-submit") != null) $("new-mode-submit").show()
    } else if (2 == v) {
        $("new-date-input-group").hide()
        $("move_date_by_new-date-input-group").hide()
        $("new-reason-input-group").hide()
        if ($("new-mode-submit") != null) $("new-mode-submit").show()
    } else if (5 == v) {
        $("new-date-input-group").show()
        $("move_date_by_new-date-input-group").hide()
        $("new-reason-input-group").show()
        if ($("new-mode-submit") != null) $("new-mode-submit").show()
    } else if (7 == v) {
        $("new-date-input-group").hide()
        $("move_date_by_new-date-input-group").hide()
        $("new-reason-input-group").hide()
        if ($("new-mode-submit") != null) $("new-mode-submit").show()
    } else if (6 == v) {
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