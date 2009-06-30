/* TODO: these functions must all be namespaced.  Also, most of these event registrations will need
 * to be made dynamic (in response to schedule updates) instead of one time at load.
 */

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

function registerCheckAllEvents() {
    Event.observe('check-all-events', "click", function(event){$('batch-form').checkCheckboxes('event');Event.stop(event);})
}

function registerUncheckAllEvents() {
    Event.observe('uncheck-all-events', "click", function(event){$('batch-form').uncheckCheckboxes('event');Event.stop(event);})
}

function registerCheckAllConditionalEvents() {
    Event.observe('check-all-conditional-events', "click", function(event){
        $('batch-form').uncheckCheckboxes('event');Event.stop(event);
        $('batch-form').checkCheckboxes('conditional');Event.stop(event);
    })
}

function registerCheckAllPastDueEvents() {
    Event.observe('check-all-past-due-events', "click", function(event){
        $('batch-form').uncheckCheckboxes('event');Event.stop(event);
        $('batch-form').checkCheckboxes('past-due');Event.stop(event);
    })
}


//for IE7 we need to implement loading the function in this way. IE7 otherwise doesn't preserve the order
function registerAllFunctions() {
    registerBatchRescheduleHandlers();
    registerCheckAllEvents();
    registerUncheckAllEvents();
    registerCheckAllConditionalEvents();
    registerCheckAllPastDueEvents();
}
Event.observe(window, "load", registerAllFunctions);