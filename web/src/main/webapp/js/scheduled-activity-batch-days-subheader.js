function registerSubheaderCollapse() {
    $$(".subcollapsible").each(function(section) {
        var header = section.getElementsByByTagName("H3")[0]
        header.innerHTML += " <span class='collapse-icon'>&#43;</span>"
        header.title = "Click to reveal"
        Event.observe(header, 'click', function() {
            var content = section.getElementsByClassName("content")[0]
            var icon = section.getElementsByClassName("collapse-icon")[0]
            if (content.visible()) {
                SC.slideAndHide(content, {
                    afterFinish: function() {
                        header.title = "Click to reveal form"
                        Element.update(icon, '&#43;')
                    }
                });
            } else {
                SC.slideAndShow(content, {
                    afterFinish: function() {
                        header.title = "Click to conceal form"
                        Element.update(icon, '&#45;')
                    }
                });
            }
        })
    })
}

function registerShowDaysButton() {
    Event.observe('show_days_button', "click", function(event){
        var allDaysPerPeriod = document.getElementsByClassName("days_from_period")
        for (var i=0; i<allDaysPerPeriod.length; i++) {
            allDaysPerPeriod[i].show()
        }
        $('show_days_button').hide()
        $('hide_days_button').show()
    })
}


function registerHideDaysButton() {
    Event.observe('hide_days_button', "click", function(event){
        var allDaysPerPeriod = document.getElementsByClassName("days_from_period")
        for (var i=0; i<allDaysPerPeriod.length; i++) {
            allDaysPerPeriod[i].hide()
        }
        $('show_days_button').show()
        $('hide_days_button').hide()
    })
}

//for IE7 we need to implement loading the function in this way. IE7 otherwise doesn't preserve the order
function registerAllFunctions() {
    registerSubheaderCollapse();
    registerShowDaysButton();
    registerHideDaysButton();
}
Event.observe(window, "load", registerAllFunctions);