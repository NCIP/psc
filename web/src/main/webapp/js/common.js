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

/** Like new Ajax.Request, except it does method tunneling
 *  that's compatible with Restlet's TunnelFilter */
SC.asyncRequest = function(href, options) {
  console.log("async: %s %s", (options.method || 'get').toUpperCase(), href)
  if (options && options.method) {
    // the tunnel filter in Restlet requires that the _method parameter be in the query string
    if (!['get', 'post'].include(options.method.toLowerCase())) {
      var method = options.method.toLowerCase()
      delete options['method']
      var delim = href.indexOf("?") < 0 ? '?' : '&'
      href = href + delim + "method=" + method
    }
  }
  return new Ajax.Request(href, options)
}

/** Provides a context-path-sensitive relative URI within PSC -- similar to the JSTL tag c:url. */
SC.relativeUri = function(path) {
  var relpath = path;
  if (path.substring(0,1) == '/') relpath = path.substring(1);
  // URI_BASE_PATH is generated in decorators/standard.jsp
  return INTERNAL_URI_BASE_PATH + relpath;
}

SC.NS_BINDINGS = {
  psc: 'http://bioinformatics.northwestern.edu/ns/psc'
}

SC.nsResolver = function(prefix) {
  return SC.NS_BINDINGS[prefix] || null
}

/**
 * Executes the given XPath against the target document (usually the responseXML from
 * an async request).  Converts every node returned by the XPath into a simple javascript
 * Object with properties matching the attributes of the XML element.
 *
 * If "initializer" is specified, it must be a function which takes two parameters.  This
 * function will be passed each node along with the new object corresponding to it.
 *
 * Returns a the list of objects corresponding to the results of the XPath query, or
 * an empty list if there are no matches.
 */
SC.objectifyXml = function(elementName, xmlDoc, initializer) {
  var wrap = function(elt) {
    var obj = $A(elt.attributes).inject({}, function(o, attr) {
      if (attr && !attr.value.blank()) o[attr.nodeName] = attr.value;
      return o
    });
    if (initializer) initializer(elt, obj)
    return obj
  }

  if (Prototype.BrowserFeatures.XPath) {
    var xmlIterator = xmlDoc.evaluate("//psc:" + elementName, xmlDoc, SC.nsResolver, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null)
    var list = []
    var elt;
    while (elt = xmlIterator.iterateNext()) {
      list.unshift(wrap(elt));
    }
    return list;
  } else {
    // IE sucks
    var recurse = function(elt) {
      if (elt.nodeName == elementName) {
        return elt
      } else {
        return $A(elt.childNodes).map(function(e) { return recurse(e) })
      }
    }
    return recurse(xmlDoc.documentElement).flatten().compact().map(wrap)
  }
}

SC.zeropad = function(number, digits) {
    var s = "" + number;
    while (s.length < digits) {
        s = "0" + s
    }
    return s;
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

SC.addInputHintBehavior = function(input) {
  console.log("adding hint behavior to %o", input)
  if (input.getAttribute("hint") && input.type == 'text') {
    $(input).observe('focus', SC.inputHintFocus)
    $(input).observe('blur', SC.inputHintBlur)
    SC.applyInputHint(input)
  } else {
    console.log("Can't apply an input hint to %o -- it isn't a text input with the hint attribute.", input)
  }
}

SC.inputHintFocus = function(evt) {
  var input = Event.element(evt)
  if (input.hasClassName("input-hint")) {
    input.removeClassName("input-hint")
    input.value = ""
  }
}

SC.inputHintBlur = function(evt) {
  var input = Event.element(evt)
  SC.applyInputHint(input)
}

SC.applyInputHint = function(input) {
  if (input.value.blank()) {
    input.addClassName("input-hint")
    input.value = input.getAttribute("hint")
  } else {
    input.removeClassName("input-hint")
  }
}

$(document).observe("dom:loaded", function() {
  $$("input[hint]").each(SC.addInputHintBehavior)
})
