var SC = new Object();

SC.asyncSubmit = function(form, options) {
    var f = $(form);
    new Ajax.Request(f.action, $H(options).merge({
        asynchronous: true,
        parameters: Form.serialize(f)
    }).toObject())
}

SC.asyncLink = function(anchor, options, indicator) {
    var a = $(anchor)
    Event.observe(a, "click", function(e) {
        Event.stop(e);
        SC.doAsyncLink(anchor, options, indicator)
    })
}

SC.doAsyncLink = function(anchor, options, indicator) {
    var a = $(anchor)
    if (indicator) { $(indicator).reveal() }
    new Ajax.Request(a.href, Object.extend({
        asynchronous: true,
        onComplete: function() {
            if (indicator) { $(indicator).conceal() }
        }
    }, options));
}

SC.SessionExpiredLogic = Class.create( {
    initialize: function(url) {
        this.url = url
        this.content =
            Builder.node('div', {id:'lightbox-content'}, [
                Builder.node('h1', 'Session Timed Out'),
                Builder.node('p', 'Your session has expired.  To continue, please ', [
                        Builder.node('a', {href:this.url}, 'log in again.')
                        ])
                ])
    },
    execute: function() {
        $('lightbox').update(this.content)
        LB.Lightbox.activate()
    }
})

SC.UserActiveChallenge = Class.create( {
    initialize: function(url) {
        this.url = url
        this.button = Builder.node('input', {type:'button', value:'Confirm Active', id:'user-active-button'});
        this.add_submit_listener()
    },
    execute: function() {
        $('lightbox').update(
                Builder.node('div', {id:'lightbox-content'}, [
                    Builder.node('h1', 'Your Session is About to Expire'),
                    Builder.node('p', 'Please confirm that you are still active by clicking the button below.'),
                    this.button
                    ]))

        LB.Lightbox.activate()
    },
    add_submit_listener: function() {
        Event.observe(this.button, 'click', function(e) {
            new Ajax.Request(
                    this.url, {
                method:'get',
                onSuccess: function(transport){
                    LB.Lightbox.deactivate()
                }
            })
        }.bindAsEventListener(this))
    }
})

SC.SessionTimer = Class.create(PeriodicalExecuter, {
    initialize: function($super, expire_logic, time_in_seconds) {
        $super(this.expire, time_in_seconds)
        this.expire_logic = expire_logic
    },
    reset: function() {
        this.stop()
        this.registerCallback()
    },
    expire: function() {
        this.expire_logic.execute()
        this.stop()
    }
})

SC.HttpSessionExpirationManager = Class.create({
    initialize: function(session_warning_in_seconds, session_timeout_in_seconds, session_alive_url, login_url) {
        this.timers = []
        
        if (session_warning_in_seconds > 0) {
            this.userChallengeTimer =
               new SC.SessionTimer(new SC.UserActiveChallenge(session_alive_url), session_warning_in_seconds)

            this.timers.push(this.userChallengeTimer)
        }

        if (session_timeout_in_seconds > 0) {
            this.httpSessionTimer =
                new SC.SessionTimer(new SC.SessionExpiredLogic(login_url), session_timeout_in_seconds)

            this.timers.push(this.httpSessionTimer)
        }

        Ajax.Responders.register({
            timers:this.timers,
            onComplete: function() {
                this.timers.each(function(timer) {
                    timer.reset()
                })
            }
        })
    }
})

////// COOKIES

/** Main fns based on http://www.quirksmode.org/js/cookies.html */
var Cookies = {
    add: function(name, value, days) {
        if (days) {
            var date = new Date();
            date.setTime(date.getTime() + (days*24*60*60*1000));
            var expires = "; expires=" + date.toGMTString();
        }
        else var expires = "";
        document.cookie = name + "=" + value + expires + "; path=/";
    },

    get: function(name) {
        var nameEQ = name + "=";
        var ca = document.cookie.split(';');
        for(var i=0;i < ca.length;i++) {
            var c = ca[i];
            while (c.charAt(0) == ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
        }
        return null;
    },

    clear: function(name) {
        Cookies.add(name, "", -1);
    },

    set: function(name, value, days) {
        Cookies.clear(name)
        Cookies.add(name, value, days)
    }
}

////// PROTOTYPE EXTENSIONS

Element.addMethods( {
    // Like prototype's hide(), but uses the visibility CSS prop instead of display
    conceal: function() {
        for (var i = 0; i < arguments.length; i++) {
          var element = $(arguments[i]);
          element.style.visibility = 'hidden';
        }
    },

    // Like prototype's show(), but uses the visibility CSS prop instead of display
    reveal: function() {
        for (var i = 0; i < arguments.length; i++) {
          var element = $(arguments[i]);
          element.style.visibility = 'visible';
        }
    }
} );

// Extend Prototype Form object to have toggle checkboxes method
Form.Methods.checkCheckboxes = function(form, checkboxName) {
    Form.Methods.setCheckboxesChecked(form, checkboxName, true)
}
Form.Methods.uncheckCheckboxes = function(form, checkboxName) {
    Form.Methods.setCheckboxesChecked(form, checkboxName, false)
}
//Form.Methods.setCheckboxesChecked = function(form, checkboxName, isChecked) {
//    var events = $(form).getInputs('checkbox',checkboxName)
//    events.each(function(event) {
//        event.checked = isChecked;
//    })
//    return form
//}
Form.Methods.setCheckboxesChecked = function(form, checkboxName, isChecked) {
    var events = $$("#" + form.id + " input." + checkboxName)
    events.each(function(event) {
        event.checked = isChecked;
    })
    return form
}

Element.addMethods();


////// DOM EXTENSIONS

// Adds an IE-like click() fn for other browsers
if (!document.all && HTMLElement && !HTMLElement.prototype.click) {
    HTMLElement.prototype.click = function() {
        var evt = this.ownerDocument.createEvent('MouseEvents');
        // evt.initMouseEvent('click', true, true, this.ownerDocument.defaultView, 1, 0, 0, 0, 0, false, false, false, false, 0, null);
        evt.initEvent("click", true, true);
        this.dispatchEvent(evt);
    }
}

////// CCTS

if (!window.CCTS) { window.CCTS = { } }

CCTS.appShortName = 'psc'

///////

function registerHeaderCollapse() {
    $$(".collapsible").each(function(section) {
        var header = section.getElementsByTagName("H2")[0]
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

/////  autocompleter search fields
function initSearchField() {

    $$("input[type=text].autocomplete").each(function(theInput)
    {
        /* Add event handlers */
        Event.observe(theInput, 'focus', clearDefaultText);
        Event.observe(theInput, 'blur', replaceDefaultText);
        /* Save the current value */
        if (theInput.value != '') {
            theInput.defaultText = theInput.value;
            theInput.className = 'pending-search';

        }

    });
}
function clearDefaultText(e) {
    var target = window.event ? window.event.srcElement : e ? e.target : null;
    if (!target) return;

    if (target.value == target.defaultText) {
        target.value = '';
        target.className = 'search';

    }

}

function replaceDefaultText(e) {
    var target = window.event ? window.event.srcElement : e ? e.target : null;
    if (!target) return;

    if (target.value == '' && target.defaultText) {
        target.value = target.defaultText;
        target.className = 'pending-search';
    }

}
