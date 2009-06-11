psc.namespace("subject");

psc.subject.DivIconOnlyPainter = function (params) {
  Timeline.OriginalEventPainter.call(this, params);
}

psc.subject.DivIconOnlyPainter.prototype = new Timeline.OriginalEventPainter();
psc.subject.DivIconOnlyPainter.prototype.constructor = psc.subject.DivIconOnlyPainter;

psc.subject.DivIconOnlyPainter.prototype.getType = function() {
    return 'div-icon-only';
};

psc.subject.DivIconOnlyPainter.prototype._computeMetrics = function() {
     var eventTheme = this._params.theme.event;
     var trackHeight = Math.max(eventTheme.track.height, eventTheme.tape.height);
     var metrics = {
            trackOffset: eventTheme.track.offset,
            trackHeight: trackHeight,
               trackGap: eventTheme.track.gap,
         trackIncrement: trackHeight + eventTheme.track.gap,
              iconWidth: eventTheme.instant.iconWidth,
             iconHeight: eventTheme.instant.iconHeight,
    impreciseIconMargin: eventTheme.instant.impreciseIconMargin
     };
     
     return metrics;
};

psc.subject.DivIconOnlyPainter.prototype.paintPreciseInstantEvent = function(evt, metrics, theme, highlightIndex) {
    var doc = this._timeline.getDocument();
    var text = evt.getText();
    
    var startDate = evt.getStart();
    var startPixel = Math.round(this._band.dateToPixelOffset(startDate));
    var iconRightEdge = Math.round(startPixel + metrics.iconWidth / 2);
    var iconLeftEdge = Math.round(startPixel - metrics.iconWidth / 2);

    var rightEdge = iconRightEdge;
    var track = this._findFreeTrack(evt, rightEdge);
    
    var iconElmtData = this._paintEventDivIcon(evt, track, iconLeftEdge, metrics, theme, 0);
    var els = [iconElmtData.elmt];

    var self = this;
    var clickHandler = function(elmt, domEvt, target) {
        return self._onClickInstantEvent(iconElmtData.elmt, domEvt, evt);
    };
    SimileAjax.DOM.registerEvent(iconElmtData.elmt, "mousedown", clickHandler);
    
    var hDiv = this._createHighlightDiv(highlightIndex, iconElmtData, theme, evt);
    if (hDiv != null) {els.push(hDiv);}
    this._fireEventPaintListeners('paintedEvent', evt, els);

    this._eventIdToElmt[evt.getID()] = iconElmtData.elmt;
    this._tracks[track] = iconLeftEdge;
};

psc.subject.DivIconOnlyPainter.prototype._paintEventDivIcon = function(evt, iconTrack, left, metrics, theme, tapeHeight) {
    var top; // top of the icon
    if (tapeHeight > 0) {
        top = metrics.trackOffset + iconTrack * metrics.trackIncrement + 
              tapeHeight + metrics.impreciseIconMargin;
    } else {
        var middle = metrics.trackOffset + iconTrack * metrics.trackIncrement +
                     metrics.trackHeight / 2;
        top = Math.round(middle - metrics.iconHeight / 2);
    }
    var iconDiv = this._timeline.getDocument().createElement("div");
    iconDiv.className = this._getElClassName('timeline-event-icon', evt, 'icon');
    iconDiv.id = this._encodeEventElID('icon', evt);
    iconDiv.style.left = left + "px";
    iconDiv.style.top = top + "px";
    iconDiv.style.width  = metrics.iconWidth + "px";
    iconDiv.style.height = metrics.iconHeight + "px";

    if(evt._title != null)
        iconDiv.title = evt._title;

    this._eventLayer.appendChild(iconDiv);
    
    return {
        left:   left,
        top:    top,
        width:  metrics.iconWidth,
        height: metrics.iconHeight,
        elmt:   iconDiv
    };
};

//////

psc.subject.DayOnlyLabeller = function (locale, timeZone) {
  Timeline.GregorianDateLabeller.call(this, locale, timeZone);
}

psc.subject.DayOnlyLabeller.prototype = new Timeline.GregorianDateLabeller();
psc.subject.DayOnlyLabeller.prototype.constructor = psc.subject.DayOnlyLabeller;

psc.subject.DayOnlyLabeller.prototype.labelPrecise = function(date) {
    return (date.getUTCMonth() + 1) + "/" + date.getUTCDate() + "/" + date.getUTCFullYear();
};
