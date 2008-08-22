if (!window.SC) { window.SC = { } }
if (!SC.MP) { SC.MP = { } }

Object.extend(SC.MP, {
  postPlannedActivityAt: function(row, col, ajaxOptions) {
    var activity = SC.MP.findActivity(row)
    var day = SC.MP.findDay(col)
    var notes = SC.MP.findNotes(row)

    var plannedActivityForm = Object.extend({
      day: day,
      "activity-code": activity.code,
      "activity-source": activity.source
    }, notes)

    new Ajax.Request(SC.MP.collectionResource, Object.extend(ajaxOptions, {
      method: 'POST',
      parameters: plannedActivityForm
    }))
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