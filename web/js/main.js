SC.Main = new Object();

// add handlers to have the entire LI for each arm act like the A was clicked
// this is so that the LI can be used as a container for other controls, but still be
// generally useful
SC.Main.registerClickForwarders = function() {
    $$('.epochs-and-arms li').each(function(li) {
        Event.observe(li, 'click', function() {
            var armA;
            $A(li.getElementsByTagName("a")).each(function(a) {
                if (Element.hasClassName(a, "arm")) { armA = a; }
            });
            armA.click();
        });
    })
}

Event.observe(window, "load", SC.Main.registerClickForwarders)
