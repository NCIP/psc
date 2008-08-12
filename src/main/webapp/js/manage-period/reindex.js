if (!window.SC) { window.SC = { } }
if (!SC.MP) { SC.MP = { } }

Object.extend(SC.MP, {
  reindex: function() {
    console.time("reindex")
    var rowCount = 0;
    var daysBounds = SC.MP.bounds('days')
    // store the bounds of the droppable region for each group of cells
    $$("#days tbody").each(function(groupElt) {
      var totalBounds = SC.MP.bounds(groupElt)
      var header = $(groupElt).select("tr.activity-type").first()
      var headerHeight = header.getDimensions().height
      groupElt.setAttribute("drop-top", totalBounds.top + headerHeight - daysBounds.top)
      groupElt.setAttribute("drop-bottom", totalBounds.top + totalBounds.height - daysBounds.top)
      groupElt.setAttribute("drop-first-row-index", rowCount)
      rowCount += $(groupElt).select("tr.activity").length
    })
    // renumber the activities
    $$("#activities tbody").each(function(groupElt) {
      var rowNums = groupElt.select("span.row-number")
      for (var i = 0 ; i < rowNums.length ; i++) {
        rowNums[i].innerHTML = i + 1
      }
    })
    $('days').setAttribute('day-count', $$("#days-heading td").length - 1) // -1 to remove trailer cell
    $w("activities days notes").each(SC.MP.applyActivityRowIndex)
    console.timeEnd("reindex")
  },
  
  applyActivityRowIndex: function(container) {
    var rows = $(container).select("tr.activity")
    for (var i = 0 ; i < rows.length ; i++) {
      var row = rows[i]
      // clear existing row index names
      $w(row.className).each(function(className) {
        if (className.substring(0, 4) == "row-") { row.removeClassName(className) }
      })
      row.addClassName("row-" + i)
    }
  }
})

$(document).observe('dom:loaded', function() {
  SC.MP.reindex()
  // this relies on reindex being complete
  $$('#days tr.activity').each(SC.MP.updateUsedUnused)
})