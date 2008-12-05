if (!window.SC) { window.SC = { } }
if (!SC.SS) { SC.SS = { } }

Object.extend(SC.SS, {
  // Determines the relative position of the month/year/today markers in the
  // detail timeline, then applies those positions to the total timeline. 
  addTotalTimelineTicks: function() {
    var parent = $$('#detail-timeline table').first()
    var parentWidth = parent.getWidth()
    var parentLeft = parent.positionedOffset().left
    $w('year month today').each(function(kind) {
      var tdClass = kind == 'today' ? kind : kind + '-start'
      $$('#detail-timeline tr.activity-boxes .' + tdClass).each(function(elt) {
        var percent = 100.0 * (elt.positionedOffset().left - parentLeft) / parentWidth
        var div = document.createElement("DIV")
        div.className = "tick " + kind + "-tick"
        div.style.left = percent.toFixed(2) + "%"
        $('total-timeline').appendChild(div)
      })
    })
  },
  
  participationStartDate: function() {
    return SC.SS.extractDateFromClass($$('#total-timeline .start-date').first())
  },
  
  participationEndDate: function() {
    return SC.SS.extractDateFromClass($$('#total-timeline .end-date').first())
  },
  
  extractDateFromClass: function(elt, prefix) {
    if (!prefix) { prefix = 'date' }
    var dateClass = SC.SS.extractValueClass(elt, prefix)
    if (!dateClass) return null
    return SC.SS.extractDateFromClassValue(dateClass)
  },
  
  // Creates a JS date from a string like pfx-YYYY-MM-DD
  extractDateFromClassValue: function(dateClass) {
    parts = dateClass.split('-')
    return new Date(parts[1], parts[2] - 1, parts[3])
  },
  
  convertDateIntoClassValue: function(date, prefix) {
    if (!prefix) prefix = "date"
    return prefix + '-' + (date.getFullYear()) + "-" + SC.zeropad(date.getMonth() + 1, 2) + "-" + SC.zeropad(date.getDate(), 2);
  },
  
  extractValueClass: function(elt, valuePrefix) {
    var valueClass = $w(elt.className).detect(function(clazz) { return clazz.startsWith(valuePrefix + '-') })
    if (valueClass) {
      return valueClass
    } else {
      console.log("No %s-* class in %s", valuePrefix, elt.className)
      return null
    }
  },
  
  // Sizes the refbox for the total timeline based on the relative sizes
  // of the detail timeline and the total timeline, and the detail timeline
  // study cells based on the sizes of the cells in the detail timeline
  sizeTimelineElements: function() {
    var reffraction = ( 1.0 * $('detail-timeline').getWidth() / $$('#detail-timeline table').first().getWidth() )
    var refboxWidth;
    if (reffraction > 1) {
      refboxWidth = ($('total-timeline').getWidth() - 4) + "px"
      $('detail-timeline').style.width = ($$('#detail-timeline table').first().getWidth()) + (Prototype.Browser.IE ? 6 : 0) + 'px'
    } else {
      refboxWidth = ($('total-timeline').getWidth() * reffraction) + "px"
    }
    $('total-timeline-refbox').style.width = refboxWidth
    
    $$('#detail-timeline-studies tr.activity-boxes td').first().style.height = 
      ($$('#detail-timeline tr.activity-boxes td').first().getHeight()) + "px"
  },
  
  repositionTotalTimelineRefbox: function() {
    var frac = 1.0 * $('detail-timeline').scrollLeft / $$('#detail-timeline table').first().getWidth()
    var totalWidth = $('total-timeline').getWidth()
    $('total-timeline-refbox').style.left = [
      totalWidth * frac, totalWidth - $('total-timeline-refbox').getWidth() - 2
    ].min() + "px"
  },
  
  todayCell: function() {
    return $$('#detail-timeline td.today').first()
  },
  
  repositionTodayLabel: function() {
    var todayCell = SC.SS.todayCell()
    if (todayCell) {
      SC.SS.positionDateRelativeToDetailTimelineCell($('detail-timeline-date-today'), todayCell)
    }
  },
  
  // Takes the unpositioned .segment-box DIVs and overlays them on the
  // .segment-group TRs
  positionSegmentBoxes: function() {
    $$('#detail-timeline .segment-box').each(function(segmentBox) {
      var targetRowSelector = "#detail-timeline tr.segment-group." + SC.SS.extractValueClass(segmentBox, "row")
      var dateClasses = $w('start end').collect(function(s) {
        return SC.SS.extractValueClass(segmentBox, s + "_date").replace(s + '_', '')
      })
      var tds = dateClasses.collect(function(dateClass) {
        return $$(targetRowSelector + " td." + dateClass).first()
      })
      var topOffsets = tds.collect(function(td) { return td.positionedOffset()['top'] })
      var tableLeftShift = $$('#detail-timeline table').first().positionedOffset()['left']
      var leftOffsets = tds.collect(function(td) { return td.positionedOffset()['left'] - tableLeftShift })
      segmentBox.style.width = (leftOffsets.last() + tds.last().getWidth() - leftOffsets.first() - 6) + "px"
      segmentBox.style.height = (tds.first().getHeight() - 6) + "px"
      segmentBox.style.top = (topOffsets.first() + 2) + "px"
      segmentBox.style.left = (leftOffsets.first() + 2) + "px"
      segmentBox.show()
    })
  },
  
  // Determines the day the mouse is hovering over and highlights it,
  // both in the detail timeline and in the schedule.
  overDayCell: function(evt) {
    var src = Event.findElement(evt, ".day")
    var dateClass = SC.SS.extractValueClass(src, "date")
    if (!dateClass) return null;
    $$('.day.' +  dateClass).invoke('addClassName', 'hover')
    // update & position the date display
    var display = $('detail-timeline-date-hover')
    var date = SC.SS.extractDateFromClassValue(dateClass)
    // TODO: factor this out into a common function in PSC, if it isn't already there
    display.title = (date.getMonth() + 1) + '/' + date.getDate() + '/' + (date.getFullYear())
    display.innerHTML = display.title
    display.show()
    var relativeTo = $$('#detail-timeline tr.activity-boxes td.' + dateClass).first()
    SC.SS.positionDateRelativeToDetailTimelineCell(display, relativeTo)
  },
  
  positionDateRelativeToDetailTimelineCell: function(dateElt, relativeTo) {
    var relativeToBounds = Object.extend(relativeTo.getDimensions(), relativeTo.viewportOffset())
    var relativeToMidpoint = relativeToBounds.left + relativeToBounds.width / 2
    var visible = 0;
    var detailLeft = $('detail-timeline').cumulativeOffset().left
    var detailRight = detailLeft + $('detail-timeline').getWidth()
    if (relativeToMidpoint < detailLeft) {
      visible = -1
    } else if (relativeToMidpoint > detailRight) {
      visible = 1
    }
    
    // add pointer if at edge
    var newInner;
    if (visible < 0) {
      newInner = "&lt;&nbsp;" + dateElt.title
    } else if (visible > 0) {
      newInner = dateElt.title + "&nbsp;&gt;"
    } else {
      newInner = dateElt.title
    }
    if (dateElt.innerHTML != newInner) dateElt.innerHTML = newInner;

    var minLeft = detailLeft
    var maxLeft = detailRight - dateElt.getWidth()
    var idealLeft = relativeToMidpoint - dateElt.getWidth() / 2 + 6
    var selectedPx = [[minLeft, idealLeft].max(), maxLeft].min()
    dateElt.style.left = selectedPx + "px"
    dateElt.style.top = ($('detail-timeline-block').cumulativeOffset().top - dateElt.getHeight()) + "px"
    
    dateElt.show()
  },
  
  offDayCell: function(evt) {
    $$('.day.hover').invoke('removeClassName', 'hover')
    $('detail-timeline-date-hover').hide()
  },
  
  clickDayCell: function(evt) {
    var src = Event.findElement(evt, '.day')
    var date = SC.SS.extractDateFromClass(src, 'date')
    SC.SS.scrollScheduleToDate(date)
  },
  
  // Smoothly scrolls the schedule list to the given date.
  // Only works with dates that actually have entries (for the moment).
  scrollScheduleToDate: function(date) {
    var now = new Date()
    var scrollToElt;
    if (now.getFullYear() == date.getFullYear() && now.getMonth() == date.getMonth() && now.getDate() == date.getDate()) {
      scrollToElt = $('schedule-today-marker')
    } else {
      var dateClass = SC.SS.convertDateIntoClassValue(date)
      scrollToElt = $$('#scheduled-activities .' + dateClass).first()
    }
    if (!scrollToElt) return;
    new Effect.Tween('scheduled-activities', 
      $('scheduled-activities').scrollTop, 
      scrollToElt.positionedOffset().top, 
      { transition: Effect.Transitions.sinoidal }, 
      'scrollTop')
  }
})

Event.observe(window, 'load', function() {
  SC.SS.sizeTimelineElements()
  
  $('detail-timeline').observe('scroll', SC.SS.repositionTotalTimelineRefbox)
  SC.SS.repositionTotalTimelineRefbox()
  if (SC.SS.todayCell()) {
    $('detail-timeline').observe('scroll', SC.SS.repositionTodayLabel)
    SC.SS.repositionTodayLabel()
  }
  
  // This is too slow w/o XPath (i.e., on IE), so disable it.
  if (Prototype.BrowserFeatures.XPath) {
    $$('.day').each(function(e) { 
      e.observe('mouseover', SC.SS.overDayCell)
      e.observe('mouseout', SC.SS.offDayCell)
    })
  }
  
  $$('#detail-timeline .day').each(function(e) {
    e.observe('click', SC.SS.clickDayCell)
  })
  
  // IE (the bastard) overlays the scroll bar on the bottom of #detail-timeline
  // instead of making space for it, so we have to do this silliness
  if (Prototype.Browser.IE) {
    $('detail-timeline').style.height = ($('detail-timeline').getHeight() + $$('#detail-timeline table tr.segment-group td').first().getHeight()) + "px"
  }
  
  SC.SS.addTotalTimelineTicks()
  SC.SS.positionSegmentBoxes()
})
