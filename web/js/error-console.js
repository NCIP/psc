var EC = { }

EC.AjaxResponders = {
    onException: function(request, exception) {
        new Insertion.Top('error-console-errors', "<li>" + exception + "</li>")
        $('error-console').show()
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
}

Event.observe(window, "load", EC.registerErrorConsoleControlHandlers);
Ajax.Responders.register(EC.AjaxResponders)