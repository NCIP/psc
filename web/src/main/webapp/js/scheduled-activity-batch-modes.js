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

function getCheckboxClassName(studyNameForCheckbox){
    var selectedStudyValue;
    var selectedStudyName;

    if (jQuery('#studySelector.studySelector option:selected').size() > 0) {
      selectedStudyValue = jQuery('#studySelector.studySelector option:selected')[0].value
      selectedStudyName = jQuery('#studySelector.studySelector option:selected')[0].innerHTML
    } else {
      selectedStudyName = $('studyIdentifier').value
    }
    if (selectedStudyValue != "all") {
        studyNameForCheckbox = studyNameForCheckbox+'.assignment-'+selectedStudyName.replace(/\W/g, '_')
    }
    return studyNameForCheckbox;
}

function registerCheckAllEvents() {
    Event.observe('check-all-events', "click", function(event){
        var studyNameForCheckbox = 'event'
        $('batch-form').checkCheckboxes(getCheckboxClassName(studyNameForCheckbox));Event.stop(event);
    })
}

function registerUncheckAllEvents() {
    Event.observe('uncheck-all-events', "click", function(event){$('batch-form').uncheckCheckboxes('event');Event.stop(event);})
}

function registerCheckAllConditionalEvents() {
    Event.observe('check-all-conditional-events', "click", function(event){
        var unchecking = 'event'
        var checking ='conditional'
        $('batch-form').uncheckCheckboxes(getCheckboxClassName(unchecking));Event.stop(event);
        $('batch-form').checkCheckboxes(getCheckboxClassName(checking));Event.stop(event);
    })
}

function registerCheckAllPastDueEvents() {
    Event.observe('check-all-past-due-events', "click", function(event){
        var unchecking ='event'
        var checking = 'past-due'
        $('batch-form').uncheckCheckboxes(getCheckboxClassName(unchecking));Event.stop(event);
        $('batch-form').checkCheckboxes(getCheckboxClassName(checking));Event.stop(event);
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