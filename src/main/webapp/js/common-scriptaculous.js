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
        // remove handlers from element, if requested
        if (options.externalControlOnly) {
            Event.stopObserving(this.element, 'click', this.onclickListener);
            Event.stopObserving(this.element, 'mouseover', this.mouseoverListener);
            Event.stopObserving(this.element, 'mouseout', this.mouseoutListener);
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

