if (!window.SC) { window.SC = { } }
if (!SC.MP) { SC.MP = { } }

Object.extend(SC.MP, {
  registerActivityHover: function(tr) {
    // finding the row to highlight is too slow w/o xpath, so disable it
    if (Prototype.BrowserFeatures.XPath) {
      tr.observe("mouseover", SC.MP.hoverDataRow)
      tr.observe("mouseout", SC.MP.unhover)
    }
  },
  
  hoverDataRow: function(evt) {
    var tr = Event.findElement(evt, "tr")
    $$("." + SC.MP.findRowIndexClass(tr)).each(function(row) { row.addClassName("hover") })
  },
  
  unhover: function() {
    $$(".hover").each(function(elt) { elt.removeClassName("hover") })
  },
  
  unnew: function() {
    $$(".new-row").each(function(elt) { elt.removeClassName('new-row') })
  },

  addNewActivityGroup: function(activityType) {
    var activitiesGroup = $$("#activities tbody.activity-type-" + activityType.id).first()
    if (activitiesGroup) {
      console.log("There is already a group for %o", activityType.name)
      return;
    }
    var prefix = "activity-type-"
    var beforeBodyClass = $$("#activities tbody").map(function(group) {
      return $w(group.className).detect(function(clz) { return clz.indexOf(prefix) == 0 })
    }).detect(function(typeClass) {
      return typeClass ? activityType.id < typeClass.substring(prefix.length) : false
    })

    SC.MP.insertNewActivityTypeTbody("activities", "new_activity_tbody_template",
      beforeBodyClass, activityType)
    SC.MP.insertNewActivityTypeTbody("days", "new_days_tbody_template",
      beforeBodyClass, activityType)
    SC.MP.insertNewActivityTypeTbody("notes", "new_notes_tbody_template",
      beforeBodyClass, activityType)
    SC.MP.reindex();

    return $$("#activities .activity-type-" + activityType.id).first()
  },
  
  insertNewActivityTypeTbody: function(container, template, bodyClass, activityType) {
    console.log("Inserting %s for %o", template, activityType)
    var newBody = resigTemplate(template, activityType)
    if (bodyClass) {
      $$("#" + container + " ." + bodyClass).first().insert({ before: newBody })
    } else {
      var trailer = $$('#' + container + " tr.trailer").first()
      if (trailer) {
        trailer.up("tbody").insert({ before: newBody })
      } else {
        $$("#" + container + " table").first().insert({ bottom: newBody })
      }
    }
  },

  addActivityRow: function(activity, activityType) {
    var activitiesGroup = $$("#activities tbody.activity-type-" + activityType.id).first()
    if (!activitiesGroup) { activitiesGroup = SC.MP.addNewActivityGroup(activityType) }
    var rows = activitiesGroup.select("tr.activity")
    var beforeRowClass = SC.MP.findActivityRowClassToInsertBefore(activity.name, rows, 0, rows.length - 1)
    console.log("Will insert at %s", beforeRowClass)
    SC.MP.unnew()
    SC.MP.insertNewActivityRow("activities", "new_activity_row_template", 
      beforeRowClass, activity, activityType)
    SC.MP.insertNewActivityRow("days", "new_days_row_template", 
      beforeRowClass, activity, activityType)
    SC.MP.insertNewActivityRow("notes", "new_notes_row_template", 
      beforeRowClass, activity, activityType)
    SC.MP.reindex()
    
    var daysGroup = $$("#days .activity-type-" + activityType.id).first()
    var newRowIndex = beforeRowClass ? 
      parseInt(beforeRowClass.split('-')[1]) :
      parseInt(daysGroup.getAttribute("drop-first-row-index")) + daysGroup.select("tr.activity").length - 1
    $$(".row-" + newRowIndex).each(SC.MP.registerActivityHover)

    // event handlers
    SC.MP.registerNotePreviewHandler($$("#notes .row-" + newRowIndex + " .notes-edit").first())
    SC.MP.selectDisplayedNotes();

    SC.MP.scrollToRow(newRowIndex)
    SC.MP.clearReport()
  },

  scrollToRow: function(rowIndex) {
    var daysBounds = SC.MP.bounds('days')
    var row = $$("#days .row-" + rowIndex).first();
    var finalScroll = row.positionedOffset().top + row.getDimensions().height / 2 - daysBounds.height / 2
    new Effect.Tween('days', $('days').scrollTop, finalScroll, { transition: Effect.Transitions.sinoidal }, 'scrollTop')
  },

  findActivityRowClassToInsertBefore: function(newName, rows, lo, hi) {
    console.log("Looking for location for %s in %d, %d of %d", newName, lo, hi, rows.length)
    if (lo > hi) {
      return null;
    } else if (lo == 0 && hi == 0) {
      console.log("Should insert before %d / %o", 0, rows[0])
      return SC.MP.findRowIndexClass(rows[0])
    }
    var mid = lo + Math.floor((hi - lo) / 2)
    var midName = rows[mid].select("td").first().title
    console.log("mid %f / %s", mid, midName)
    if (newName.toLowerCase() < midName.toLowerCase()) { // new comes before mid
      console.log("Split search low mid=%d", mid)
      return SC.MP.findActivityRowClassToInsertBefore(newName, rows, lo, mid)
    } else if (mid == rows.length - 1) {
      console.log("It goes at the end")
      return null;
    }
    // new comes after mid and there is at least one thing after mid already
    var afterMid = mid + 1
    var afterMidName = rows[afterMid].select("td").first().title
    if (newName.toLowerCase() < afterMidName.toLowerCase()) { // new comes before afterMid
      console.log("Should insert before %d / %o", afterMid, rows[afterMid])
      return SC.MP.findRowIndexClass(rows[afterMid])
    } else {
      console.log("Split search high mid=%d", mid)
      return SC.MP.findActivityRowClassToInsertBefore(newName, rows, afterMid, hi)
    }
  },
  
  findRowIndexClass: function(tr) {
    return $w(tr.className).detect(function(clz) { return clz.substring(0, 4) == "row-" })
  },
  
  insertNewActivityRow: function(container, template, rowClass, activity, activityType) {
    console.log("Inserting %s for %o", template, activity)
    var newRow = resigTemplate(template, activity)
    if (rowClass) {
      $$("#" + container + " ." + rowClass).first().insert({ before: newRow })
    } else {
      $$('#' + container + " .activity-type-" + activityType.id).first().insert({ bottom: newRow })
    }
  },
  
  updateUsedUnused: function(daysRow) {
    var count = daysRow.select(".marker").length
    var rowSelector = "tr." + SC.MP.findRowIndexClass(daysRow);
    $$(rowSelector).each(function(row) {
      row.removeClassName("used")
      row.removeClassName("unused")
      if (count == 0) {
        row.addClassName("unused")
      } else {
        row.addClassName("used")
      }
    })
    // IE sucks: it doesn't immediately show the view/edit button after the row class is changed to unused
    // This ugly hack reminds it.
    if (Prototype.Browser.IE && count != 0) {
      $$("#notes " + rowSelector + " .notes-edit").first().hide().show()
    }
  },

  createActivitiesAutocompleter: function() {
    SC.MP.activitiesAutocompleter = new SC.FunctionalAutocompleter(
      'activities-autocompleter-input', 'activities-autocompleter-div', SC.MP.activityAutocompleterChoices, {
        select: "activity-name",
        afterUpdateElement: function(input, selected) {
          var activity = {
            name:   selected.select(".activity-name").first().innerHTML,
            code:   selected.select(".activity-code").first().innerHTML,
            source: selected.select(".activity-source").first().innerHTML
          }
          var activityType = {
            id: selected.getAttribute("activity-type-id"),
            name: selected.getAttribute("activity-type-name")
          }
          SC.MP.addActivityRow(activity, activityType)
          input.value = ""
          input.focus()
        }
      }
    );
  },

  activityAutocompleterChoices: function(str, callback) {
    SC.MP.findNextActivities(function(data) {
      var lis = data.map(function(activity) {
        return resigTemplate("new_activity_autocompleter_row", activity)
      }).join("\n")
      callback("<ul>\n" + lis + "\n</ul>")
    })
  }
})

$(document).observe('dom:loaded', function() {
  $$('tr.activity').each(SC.MP.registerActivityHover)
  SC.MP.createActivitiesAutocompleter()
})
