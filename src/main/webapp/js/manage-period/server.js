if (!window.SC) { window.SC = { } }
if (!SC.MP) { SC.MP = { } }

Object.extend(SC.MP, {
  postPlannedActivityAt: function(row, col, ajaxOptions) {
    console.log("POST %d, %d %s", row, col, SC.MP.collectionResource)
    SC.asyncRequest(SC.MP.collectionResource, Object.extend(ajaxOptions, {
      method: 'POST',
      parameters: SC.MP.createPlannedActivityForm(row, col)
    }))
  },

  putPlannedActivity: function(href, row, col, ajaxOptions) {
    console.log("PUT %d, %d %s", row, col, href)
    SC.asyncRequest(href, Object.extend(ajaxOptions, {
      method: 'PUT',
      parameters: SC.MP.createPlannedActivityForm(row, col)
    }))
  },

  deletePlannedActivity: function(href, ajaxOptions) {
    console.log("DELETE %s", href)
    SC.asyncRequest(href, Object.extend(ajaxOptions, { method: 'DELETE' }))
  },

  createPlannedActivityForm: function(row, col) {
    var activity = SC.MP.findActivity(row)
    var notes = SC.MP.findNotes(row)

    return Object.extend({
      day: SC.MP.findDay(col),
      population: SC.MP.findPopulation(row, col),
      "activity-code": activity.code,
      "activity-source": activity.source
    }, notes)
  },

  findActivity: function(row) {
    var cell = $$("#activities .row-" + row + " td").first()
    return {
      name: cell.title,
      code: cell.getAttribute("activity-code"),
      source: cell.getAttribute("activity-source")
    }
  },

  findDay: function(col) {
    var dayHeader = $$("#days-heading .day")[col]
    return dayHeader.getAttribute("day-number")
  },

  findNotes: function(row) {
    var content = $$("#notes .row-" + row + " .notes-content").first()
    return $w("details condition labels").inject({}, function(notes, kind) {
      notes[kind] = content.select("." + kind).first().innerHTML.strip()
      return notes
    })
  },

  findPopulation: function(row, col) {
    var marker = SC.MP.getCell(row, col).select(".marker").first()
    if (!marker) return;
    var popClass = marker.classNames().find(function(name) { return name.indexOf("population-") == 0 })
    if (popClass) {
      return popClass.substring(11);
    }
  }
})