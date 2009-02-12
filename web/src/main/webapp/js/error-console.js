var EC = { }

EC.AjaxResponders = {
    onException: function(request, exception) {
        // since IE doesn't have a reasonable toString
        var msg = exception.description ? exception.description : exception; 
        new Insertion.Top('error-console-errors', "<li>" + msg + "</li>")
        $('error-console').show()
    },
    onComplete: function(request, transport, json) {
        var retryMessage = "Please retry your last action now that you have logged back in"
        if (request.responseIsFailure()) {
            // handle 400s caused by GETting a POST-only resource (see ControllerTools#sendPostOnlyError)
            if (transport.status == 400 && transport.statusText == "POST is the only valid method for this URL") {
                alert(retryMessage)
            } else if (transport.status == 400 && transport.statusText == "GET is the only valid method for this URL") {
                alert(retryMessage)
            } else {
                new Insertion.Top('error-console-errors', "<li>" + transport.status + " " + transport.statusText + "</li>")
                $('error-console').show()
            }
        }
    }
}

EC.registerErrorConsoleControlHandlers = function() {
    var fn = function(e) {
        Event.stop(e)
        $("error-console-hidden").toggle();
        $("error-console-shown").toggle();
    }
    Event.observe("error-console-show", "click", fn)
    Event.observe("error-console-hide", "click", fn)
    Event.observe("error-console-clear", "click", function(e) {
        Event.stop(e);
        $$("#error-console-errors li").each(function(li) { li.remove() })
        $("error-console-hidden").show();
        $("error-console-shown").hide();
        $("error-console").hide();
    })
}

Event.observe(window, "load", EC.registerErrorConsoleControlHandlers);
Ajax.Responders.register(EC.AjaxResponders)