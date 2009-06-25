/* TODO: these functions must all be namespaced.  Also, most of these event registrations will need
 * to be made dynamic (in response to schedule updates) instead of one time at load.
 */

function updateFormForState() {
    var selection = $("new-mode-selector").value
    //TODO - I couldn't find what's option 5 for
    if (5 == selection || "markAsScheduled" == selection ) {
        $("new-date-input-group").show()
    } else {
        $("new-date-input-group").hide()
    }
    if ("moveDate" == selection) {
        $("move_date_by_new-date-input-group").show()
    } else {
        $("move_date_by_new-date-input-group").hide()
    }
    if ("markAsCancelled" == selection || "markAsScheduled" == selection || "moveDate" == selection || "markAsMissed" == selection || 5 == selection) {
        $("new-reason-input-group").show()
    } else {
        $("new-reason-input-group").hide()
    }
    if ($("new-mode-submit") != null) {
        if ("selectAnAction" == selection) {
            $("new-mode-submit").hide()
        } else  {
            $("new-mode-submit").show()
        }
    }
}

function registerStateBasedFormMutator() {
    Event.observe("new-mode-selector", "change", updateFormForState)
}

Event.observe(window, "load", registerStateBasedFormMutator)
Event.observe(window, "load", updateFormForState)