SC.Main = new Object();

// add handlers to have the entire LI for each arm act like the A was clicked
// this is so that the LI can be used as a container for other controls, but still be
// generally useful
SC.Main.registerClickForwarders = function() {
    $$('.epochs-and-arms li').each(SC.Main.registerClickForwarder)
}

SC.Main.registerClickForwarder = function(armItem) {
    Event.observe(armItem, 'click', function() {
        var armA;
        $A(armItem.getElementsByTagName("a")).each(function(a) {
            if (Element.hasClassName(a, "arm")) { armA = a; }
        });
        armA.click();
    });
}

Event.observe(window, "load", SC.Main.registerClickForwarders)
