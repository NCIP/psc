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
    var day = SC.MP.findDay(col)
    var notes = SC.MP.findNotes(row)

    return Object.extend({
      day: day,
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
  }
})