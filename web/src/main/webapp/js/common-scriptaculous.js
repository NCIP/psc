/*
 * Study calendar functions, etc., that require scriptaculous
 */

////// STUDY CALENDAR JS STYLES

SC.slideAndHide = function(element, options) {
    var e = $(element);
    if (e.style.display != "none")
    {
        new Effect.Parallel(
            [
                new Effect.BlindUp(e, {sync:true}),
                new Effect.Fade(e, {sync:true})
            ], $H(options).merge({
                duration: 1.0
            }).toObject()
        );
    }
}

SC.slideAndShow = function(element, options) {
    var e = $(element);
    if (e.style.display == "none")
    {
        new Effect.Parallel(
            [
                new Effect.BlindDown(e, {sync:true}),
                new Effect.Appear(e, {sync:true})
            ], $H(options).merge({
                duration: 1.0
            }).toObject()
        );
    }
}

SC.highlight = function(element, options) {
    var e = $(element)
    new Effect.Highlight(element, Object.extend({
        restorecolor: "#ffffff"
    }, options));
}

// creates an indicator image tag just like the one created by tags:activityIndicator
SC.activityIndicator = function(baseHref, id) {
    var attrs = {
        className: 'indicator',
        src: baseHref + 'images/indicator.white.gif',
        alt: 'activity indicator'
    }
    if (id) attrs.id = id
    return Builder.node('img', attrs)
}

////// CONTROLS

SC.InPlaceEditor = Class.create()

Object.extend(Object.extend(SC.InPlaceEditor.prototype, Ajax.InPlaceEditor.prototype), {
    initialize: function(element, url, options) {
        Ajax.InPlaceEditor.prototype.initialize.call(this,
            element, url,
            Object.extend({
                highlight: true, okText: 'OK', cancelText: 'Cancel', htmlResponse: false, ajaxOptions: {method: 'post'},
                // replace standard callbacks to implement highlight option
                onEnterHover: function(ipe) {
                    if (ipe.options.highlight) {
                        ipe.element.style.backgroundColor = ipe.options.highlightColor;
                    }
                    if (ipe._effect)
                        ipe._effect.cancel();
                },
                onLeaveHover: function(ipe) {
                    if (ipe.options.highlight) {
                        ipe._effect = new Effect.Highlight(ipe.element, {
                            startcolor: ipe.options.highlightColor, endcolor: ipe.options.highlightEndColor,
                            restorecolor: ipe._originalBackground, keepBackgroundImage: true
                        });
                    }
                }
            }, options));
    }
})

SC.inPlaceEditors = { }
SC.inPlaceEdit = function(element, url, options) {
    var e = $(element)
    SC.inPlaceEditors[e.id] = new SC.InPlaceEditor(element, url,
        Object.extend({
            evalScripts: true
        }, options)
    );
}

//Need to ovewrite the function to have the correct grid's cell identification, when scroll is involved in the page (for reference, see bug #393 in track)
SC.Draggable = Class.create(Draggable, {
    updateDrag: function($super, event, pointer) {
        $super(event, pointer);
        Position.includeScrollOffsets=true;
    }
})


Ajax.RevertableAutocompleter = Class.create();
Object.extend(Object.extend(Ajax.RevertableAutocompleter.prototype, Ajax.Autocompleter.prototype), {
    initialize: function(element, update, url, options) {
        this.baseInitialize(element, update, options);
        this.options.asynchronous  = true;
        this.options.onComplete    = this.onComplete.bind(this);
        this.options.defaultParams = this.options.parameters || null;
        this.url                   = url;
    },

    onKeyPress: function(event) {
       if(this.active)
         switch(event.keyCode) {
          case Event.KEY_TAB:
          case Event.KEY_RETURN:
              this.selectEntry();
              this.hide()
              this.active = false;
              Event.stop(event);
              return;
          case Event.KEY_ESC:
            this.hide();
            this.active = false;
            if(this.options.revertOnEsc) this.revertOnEsc()
            Event.stop(event);
            return;
          case Event.KEY_LEFT:
          case Event.KEY_RIGHT:
            return;
          case Event.KEY_UP:
            this.markPrevious();
            this.render();
            if(Prototype.Browser.WebKit) Event.stop(event);
            return;
          case Event.KEY_DOWN:
            this.markNext();
            this.render();
            if(Prototype.Browser.WebKit) Event.stop(event);
            return;
         }
        else
          if(event.keyCode==Event.KEY_TAB || event.keyCode==Event.KEY_RETURN ||
            (Prototype.Browser.WebKit > 0 && event.keyCode == 0)) return;

       this.changed = true;
       this.hasFocus = true;

       if(this.observer) clearTimeout(this.observer);
         this.observer =
           setTimeout(this.onObserverEvent.bind(this), this.options.frequency*1000);
     },

    revertOnEsc: function() {
        if(this.oldEntry) this.updateElement(this.oldEntry);
    },

    selectEntry: function() {
        this.active = false;
        this.updateElement(this.getCurrentEntry());

        this.oldEntry = this.getCurrentEntry();
    },

    onBlur: function(event) {
        // needed to make click events working
        setTimeout(this.hide.bind(this), 250);
        this.hasFocus = false;
        this.active = false;

        if(this.options.revertOnEsc) this.revertOnEsc()
    },

    reset: function() {
        this.active = false;
        this.hide();
        this.element.value = null;
        this.oldEntry = null;
    }

}) ;

Ajax.ResetableAutocompleter = Class.create();
Object.extend(Object.extend(Ajax.ResetableAutocompleter.prototype, Ajax.Autocompleter.prototype), {
    reset: function() {
        this.active = false;
        this.hide();
        this.element.value = null;
    }
}) ;

/**
 * An autocompleter which invokes a collaborating function to retrieve the
 * matching values.  The collaborating function will receive the typed text
 * as its first argument and a callback function as the second.  It must
 * invoke the callback to return the matches to the autocompleter.  Note,
 * the collaborating function's return value is ignored.
 */
SC.FunctionalAutocompleter = Class.create(Autocompleter.Base, {
  initialize: function(element, update, fn, options) {
    options.onHide = function(element, update){
        if(! this.hasFocus) {
            new Effect.Fade(update,{duration:0.15})
        }
    }.bind(this)

    this.baseInitialize(element, update, options);
    this.dataFunction = fn.bind(this);

    Event.observe(this.update, 'focus', this.keepFocus.bindAsEventListener(this));
    Event.observe(this.update, 'blur', this.onBlur.bindAsEventListener(this));
  },

  keepFocus: function() {
    this.hasFocus = true;
    this.active = true;
  },

  getUpdatedChoices: function() {
    this.startIndicator();

    this.dataFunction(this.getToken(), function(newChoices) {
      this.updateChoices(newChoices)
    }.bind(this))
  }
});
