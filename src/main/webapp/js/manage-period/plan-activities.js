if (!window.SC) { window.SC = { } }
if (!SC.MP) { SC.MP = { } }

Object.extend(SC.MP, {
  reportError: function(message) {
    $('message').addClassName('error')
    $('message').update(message)
    new Effect.Highlight('message', {startcolor: "#FF9999"})
    console.warn(message)
  },
  
  reportInfo: function(message) {
    /* No user-visible message unless messages are improved
    $('message').addClassName('info')
    $('message').update(message)
    new Effect.Highlight('message')
    */
    console.info(message)
  },
  
  clearReport: function() {
    $('message').update('')
    $('message').className = ''
  },
  
  markerMoving: function(draggable, evt) {
    console.log("Offset: %o", draggable.offset)
    draggable.element.addClassName('moving')
    SC.MP.detach(draggable, evt)
    SC.MP.clearReport()
  },
  
  markerStopped: function(draggable, evt) {
    console.log("Offset: %o", draggable.offset)
    SC.MP.reattach(draggable, evt)
    draggable.element.removeClassName('moving')
  },
  
  detach: function(draggable, event) {
    var marker = $(draggable.element)
    SC.MP.ORIGINAL_MARKER_PARENT = marker.parentNode
    SC.MP.ORIGINAL_VIEWPORT = marker.viewportOffset()
    marker.style.position = "absolute"
    $$("body")[0].appendChild(marker)
    marker.style.left = SC.MP.ORIGINAL_VIEWPORT.left + "px"
    marker.style.top  = SC.MP.ORIGINAL_VIEWPORT.top  + "px"
    draggable.offset[0] = marker.getDimensions().width  / 2
    draggable.offset[1] = marker.getDimensions().height / 2
  },
  
  reattach: function(draggable, event) {
    var marker = draggable.element
    marker.comparableBounds = SC.MP.viewportBounds(marker)
    SC.MP.ORIGINAL_MARKER_PARENT.appendChild(marker)
    marker.style.position = "relative"
    marker.style.left = (parseInt(marker.style.left) - SC.MP.ORIGINAL_VIEWPORT.left) + "px"
    marker.style.top  = (parseInt(marker.style.top ) - SC.MP.ORIGINAL_VIEWPORT.top ) + "px"
  },
  
  setUpNewPlannedActivityDragging: function() {
    $$('.population .marker').each(SC.MP.configureNewPlannedActivityMarker)
  },
  
  configureNewPlannedActivityMarker: function(sourceMarker) {
    new Draggable(sourceMarker, {
      // scroll: "days", // TODO: make this user selectable?
      revert: function(marker) {
        return !$(marker).descendantOf($('days'))
      },
      onStart: SC.MP.markerMoving,
      onEnd: SC.MP.dropNewPlannedActivity
    })
  },
  
  dropNewPlannedActivity: function(draggable, evt) {
    SC.MP.markerStopped(draggable, evt)
    console.time('drop new')
    var marker = draggable.element
    if (SC.MP.isDroppedWithin('days', marker)) {
      // This has to go up here to preserve original source
      var markerSource = $(marker.parentNode)
      var point = SC.MP.viewportBounds(marker).center
      SC.MP.addNewPlannedActivity(point, marker, draggable, markerSource)
    }
    console.timeEnd('drop new')
  },
  
  clickNewPlannedActivity: function(mouseEvt) {
    console.log(mouseEvt)
    if (mouseEvt.type == 'mousedown') {
      SC.MP.mouseDownAt = SC.MP.findDaysRowColumn(Event.pointer(mouseEvt)) 
      console.log("Mouse down at %o", SC.MP.mouseDownAt)
    } else if (mouseEvt.type == 'mouseup') {
      if (!SC.MP.mouseDownAt) return;
      console.time('click new')
      var upPt = Event.pointer(mouseEvt)
      var upRc = SC.MP.findDaysRowColumn(upPt)
      console.log("Mouse up at %o", upRc)
      if (upRc[0] == SC.MP.mouseDownAt[0] && upRc[1] == SC.MP.mouseDownAt[1]) {
        var marker = document.createElement("div")
        marker.className = "marker"
        marker.innerHTML = "&times;"
        SC.MP.addNewPlannedActivity(upPt, marker)
      }
      console.timeEnd('click new')
      SC.MP.mouseDownAt = null;
    }
  },
  
  addNewPlannedActivity: function(pt, marker, draggable, markerSource) {
    var rc = SC.MP.findDaysRowColumn(pt)
    var row = rc[0]; var col = rc[1]; // array assignment syntax doesn't work on IE7
    var cell = SC.MP.getCell(row, col)

    if (cell) {
      if (cell.select(".marker").length > 0) {
        if (markerSource) {
          SC.MP.reportError("That cell already has a marker in it.  Remove the one that's there first if you want to change it.")
        }
      } else {
        // Move the marker immediately, but mark it pending
        if (draggable) { draggable.destroy() }
        SC.MP.replaceCellContents(cell, marker)
        SC.MP.resetMarker(marker)
        cell.addClassName("pending")

        if (markerSource) {
          // replace the moved new activity marker
          var newMarker = "<div class='" + marker.className + "' style='display: none'>" + marker.innerHTML + "</div>"
          markerSource.insert({ bottom: newMarker })
          var newMarkerElt = markerSource.select('.marker:first').first()
          SC.MP.configureNewPlannedActivityMarker(newMarkerElt);
          new Effect.Appear(newMarkerElt)
        }

        // Actually invoke the web service
        var success = function(response) {
          // prototype sends network errors to this method with status == 0
          if (response.status == 0) {
            cell.addClassName("error")
            SC.MP.reportError("Unexpected connection failure")
            return;
          }

          marker.setAttribute("resource-href", response.getHeader("Location"))
          SC.MP.configureMovingPlannedActivityMarker(marker)
          SC.MP.reportInfo("Added " + marker.innerHTML.strip() + " at " + row + ", " + col)
          SC.MP.updateUsedUnused(marker.up("tr"))
        }
        SC.MP.postPlannedActivityAt(row, col, SC.MP.plannedActivityAjaxOptions(success, cell))
      }
    }
  },
  
  configureMovingPlannedActivityMarker: function(marker) {
    new Draggable(marker, {
      // scroll: "days",
      revert: function(marker) {
        if ($(marker).hasClassName('was-moved')) {
          $(marker).removeClassName('was-moved')
          return false
        } else {
          return true
        }
      },
      onStart: SC.MP.markerMoving,
      onEnd: SC.MP.dropMovedPlannedActivity
    })
  },
  
  dropMovedPlannedActivity: function(draggable, evt) {
    SC.MP.markerStopped(draggable, evt)
    console.time('drop moved')
    var marker = $(draggable.element)
    if (SC.MP.isDroppedWithin('days', marker)) {
      var markerSource = $(marker.parentNode)
      console.log("Locating source cell")
      var sourceRC = SC.MP.findDaysRowColumn(markerSource)
      var sourceRow = sourceRC[0]; var sourceCol = sourceRC[1];
      console.log("Locating target cell")
      var targetRC = SC.MP.findDaysRowColumn(marker)
      var targetRow = targetRC[0]; var targetCol = targetRC[1];
      console.log("Source: %d, %d ; Target: %d, %d", sourceRow, sourceCol, 
        targetRow, targetCol)
      
      if (targetRow != sourceRow) {
        SC.MP.reportError("You may not move an activity marker from one row to another.  Add a new activity instead.")
      } else if (targetCol == sourceCol) {
        // do nothing
      } else {
        // actually move
        var targetCell = SC.MP.getCell(targetRow, targetCol)
        if (!targetCell) {
          SC.MP.reportError("Dragged out of range")
        } else if (targetCell.select(".marker").length > 0) {
          SC.MP.reportError("That cell already has a marker in it.  Remove the one that's there first if you want to change it.")
        } else {
          // Move the marker immediately, but mark it pending
          SC.MP.replaceCellContents(targetCell, marker)
          SC.MP.resetMarker(marker)
          marker.addClassName('was-moved')
          targetCell.addClassName("pending")

          var success = function(response) {
            // prototype sends network errors to this method with status == 0
            if (response.status == 0) {
              cell.addClassName("error")
              SC.MP.reportError("Unexpected connection failure")
              return;
            }

            SC.MP.reportInfo("Moved from " + sourceCol + " to " + targetCol)
          }
          SC.MP.putPlannedActivity(marker.getAttribute("resource-href"), targetRow, targetCol,
            SC.MP.plannedActivityAjaxOptions(success, targetCell))
        }
      }
    } else if (SC.MP.isDroppedWithin('remove-target', marker)) {
      marker.addClassName('was-moved')
      draggable.destroy()
      var cell = marker.up("td")
      var containingRow = marker.up("tr")
      SC.MP.resetMarker(marker)
      SC.MP.replaceCellContents($$("#remove-target .cell").first(), marker)
      cell.addClassName("pending")

      var success = function(response) {
        // prototype sends network errors to this method with status == 0
        if (response.status == 0) {
          cell.addClassName("error")
          SC.MP.reportError("Unexpected connection failure")
          return;
        }

        SC.MP.updateUsedUnused(containingRow)
        new Effect.Fade(marker, {
          afterFinish: function() {
            marker.remove()
            SC.MP.reportInfo("Removed")
          }
        })
      }
      SC.MP.deletePlannedActivity(marker.getAttribute("resource-href"),
        SC.MP.plannedActivityAjaxOptions(success, cell))
    }
    console.timeEnd('drop moved')
  },

  plannedActivityAjaxOptions: function(success, cell) {
    return {
      onSuccess: success,
      on1223: success, // IE returns status 1223 if the underlying call returns 204
      onFailure: function(response) {
        cell.addClassName("error")
        console.log("error from onFailure.  Status %o", response.getStatus())
        SC.MP.reportError(response.responseText)
      },
      onException: function(request, exception) {
        cell.addClassName("error")
        console.log("error from onException")
        // since IE doesn't have a reasonable toString
        var msg = exception.description ? exception.description : exception;
        SC.MP.reportError(msg)
      },
      onComplete: function() {
        cell.removeClassName("pending")
      }
    }
  },
  
  ////// Utilities
  
  cellSize: function() {
    console.time('cell size')
    var aGroup = $$('#days tbody.activity-type').first()
    if (!aGroup) return { width: 1, height: 1 }
    var groupSize = aGroup.getDimensions()
    var headerSize = aGroup.select("tr.activity-type").first().getDimensions()
    var rows = aGroup.select("tr.activity")
    var dim = { }
    if (rows.length == 0) {
      throw "Expected at least one row"
    }
    dim.height = (groupSize.height - headerSize.height) / rows.length

    var colCount = rows[0].getElementsByTagName("td").length
    if (colCount == 0) {
      throw "Expected at least one column"
    }
    dim.width = groupSize.width / colCount

    console.timeEnd('cell size')
    return dim
  },

  // Returns the logical bounds for an element
  bounds: function(elt) {
    var dim = $(elt).getDimensions()
    var offset = $(elt).cumulativeOffset()
    return SC.MP._bounds(dim, offset)
  },
  
  // Returns the view-relative bounds for an element
  viewportBounds: function(elt) {
    var e = $(elt);
    var dim = e.getDimensions()
    var offset = e.viewportOffset()

    // This is a limited workaround for scroll-related misbehavior in Element#viewportOffset.
    // It won't work in the general case, but is fine for this page.
    offset.left += e.scrollLeft
    offset.top  += e.scrollTop

    return SC.MP._bounds(dim, offset)
  },

  _bounds: function(bounds, offset) {
    bounds.top = offset.top
    bounds.left = offset.left
    bounds.bottom = bounds.top + bounds.height
    bounds.right = bounds.left + bounds.width
    bounds.center = {
      x: bounds.left + (bounds.width / 2),
      y: bounds.top + (bounds.height / 2)
    }
    bounds.toString = function() {
      return "center=(" + this.center.x + ", " + this.center.y + ")" +
             " top=" + this.top + " bottom=" + this.bottom +
             " left=" + this.left + " right=" + this.right +
             " width=" + this.width + " height=" + this.height
    }
    return bounds
  },
  
  // Returns true if the center of the marker element is within the bounds of 
  // the container
  isDroppedWithin: function(container, marker) {
    var containerBounds = SC.MP.viewportBounds(container)
    var markerBounds = marker.comparableBounds || SC.MP.viewportBounds(marker)
    console.log("container: %o | marker: %o", containerBounds, markerBounds)
    var inContainerX = containerBounds.left < markerBounds.center.x && 
      markerBounds.center.x < containerBounds.right
    var inContainerY = containerBounds.top < markerBounds.center.y && 
      markerBounds.center.y < containerBounds.bottom
    console.log("Is %o in %o? x: %s, y: %s", marker, container, inContainerX, inContainerY)
    return inContainerX && inContainerY
  },
  
  // Determines the row/column in the days grid which includes the given point.
  // If pt is an element, it uses the center of the element relative to the viewport.
  findDaysRowColumn: function(point) {
    var cellSize = SC.MP.cellSize()
    console.log("cell %d x %d", cellSize.width, cellSize.height)

    if (!point.x) {
      point = (point.comparableBounds || SC.MP.viewportBounds(point)).center
    }

    var daysBounds = SC.MP.bounds('days')
    var daysRelativeMarkerLocation = {
      x: (point.x - daysBounds.left) + $('days').scrollLeft,
      y: (point.y - daysBounds.top ) + $('days').scrollTop
    }
    var rowGroup = SC.MP.findDaysRowGroup(daysRelativeMarkerLocation.y)
    if (!rowGroup) { return [undefined, undefined] }

    var rowOffset = Math.floor(
      parseInt(daysRelativeMarkerLocation.y - rowGroup.getAttribute("drop-top")) / cellSize.height)
    var row = parseInt(rowGroup.getAttribute("drop-first-row-index")) + rowOffset
    var col = Math.floor(daysRelativeMarkerLocation.x / cellSize.width )
    console.log("point: (%d, %d) ; days origin: (%d, %d)", 
      point.x, point.y, daysBounds.left, daysBounds.top)
    console.log("relative loc: (%d, %d) ; table r: %o c: %o", 
      daysRelativeMarkerLocation.x, daysRelativeMarkerLocation.y, row, col)
    
    return [row, col]
  },
  
  // Binary search over the droppable regions in #days.  Returns null if the
  // drop was not in a droppable region
  findDaysRowGroup: function(yOffset, lo, hi, groups) {
    console.log("Search for %d in %d, %d", yOffset, lo, hi)
    if (!groups) {
      groups = $$("#days tbody.activity-type")
      if (groups.length < 1) {
        console.log("%d not in a droppable area", yOffset)
        return null
      }
      lo = 0
      hi = groups.length - 1
      console.log("Default lo %d, hi %d", lo, hi)
    } else if (hi < lo) {
      console.log("%d not in a droppable area", yOffset)
      return null
    }
    var mid = Math.round(lo + ((hi - lo) / 2))
    var candidate = groups[mid]
    console.log("Mid: %d / %o", mid, candidate)
    if (yOffset < candidate.getAttribute("drop-top")) {
      return SC.MP.findDaysRowGroup(yOffset, lo, mid - 1, groups)
    } else if (yOffset > candidate.getAttribute("drop-bottom")) {
      return SC.MP.findDaysRowGroup(yOffset, mid + 1, hi, groups)
    } else {
      return candidate
    }
  },
  
  getCell: function(row, col) {
    if ((row || row == 0) && (col || col == 0)) {
      var tr = $$('#days tr.row-' + row).first()
      return $(tr.getElementsByTagName('td')[col])
    } else {
      return null
    }
  },
  
  findTargetCell: function(marker) {
    var rc = SC.MP.findDaysRowColumn(marker)
    return SC.MP.getCell(rc[0], rc[1])
  },
  
  resetMarker: function(marker) {
    marker.style.left = ""
    marker.style.top = ""
    marker.style.opacity = ""
  },
  
  replaceCellContents: function(cell, marker) {
    // plain tc.update(marker) does not work on IE7
    while (cell.firstChild) { cell.removeChild(cell.firstChild) }
    cell.appendChild(marker)
  }
})

$(document).observe('dom:loaded', function() {
  SC.MP.setUpNewPlannedActivityDragging()
  $$("#days .marker").each(SC.MP.configureMovingPlannedActivityMarker)
  $('days').observe('mousedown', SC.MP.clickNewPlannedActivity)
  $('days').observe('mouseup', SC.MP.clickNewPlannedActivity)
  $('days').observe('click', SC.MP.unnew)
})
