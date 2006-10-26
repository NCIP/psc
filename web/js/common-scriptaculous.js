/*
 * Study calendar functions, etc., that require scriptaculous
 */

////// STUDY CALENDAR JS STYLES

SC.slideAndHide = function(element, options) {
    var e = $(element);
    new Effect.Parallel(
        [
            new Effect.BlindUp(e, {sync:true}),
            new Effect.Fade(e, {sync:true})
        ], $H(options).merge({
            duration: 1.0
        })
    );
}

SC.slideAndShow = function(element, options) {
    var e = $(element);
    new Effect.Parallel(
        [
            new Effect.BlindDown(e, {sync:true}),
            new Effect.Appear(e, {sync:true})
        ], $H(options).merge({
            duration: 1.0
        })
    );
}

SC.highlight = function(element, options) {
    var e = $(element)
    new Effect.Highlight(element, $H(options))
}

////// CONTROLS

SC.InPlaceEditor = Class.create()

Object.extend(Object.extend(SC.InPlaceEditor.prototype, Ajax.InPlaceEditor.prototype), {
    initialize: function(element, url, options) {
        Ajax.InPlaceEditor.prototype.initialize.call(this,
            element, url,
            Object.extend({
                highlight: true, okText: 'OK', cancelText: 'Cancel' 
            }, options));
        // remove handlers from element, if requested
        if (options.externalControlOnly) {
            Event.stopObserving(this.element, 'click', this.onclickListener);
            Event.stopObserving(this.element, 'mouseover', this.mouseoverListener);
            Event.stopObserving(this.element, 'mouseout', this.mouseoutListener);
        }
    },

    // unfortunately, these are straight copies of the methods from Ajax.InPlaceEditor,
    // required to implement highlight option
    enterHover: function() {
        if (this.saving) return;
        if (this.options.highlight) this.element.style.backgroundColor = this.options.highlightcolor;
        if (this.effect) {
            this.effect.cancel();
        }
        Element.addClassName(this.element, this.options.hoverClassName)
    },
    leaveHover: function() {
        if (this.options.backgroundColor) {
          this.element.style.backgroundColor = this.oldBackground;
        }
        Element.removeClassName(this.element, this.options.hoverClassName)
        if (this.saving) return;
        if (this.options.highlight) {
            this.effect = new Effect.Highlight(this.element, {
              startcolor: this.options.highlightcolor,
              endcolor: this.options.highlightendcolor,
              restorecolor: this.originalBackground
            });
        }
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

