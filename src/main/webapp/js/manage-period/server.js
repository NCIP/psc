if (!window.SC) { window.SC = { } }
if (!SC.MP) { SC.MP = { } }

Object.extend(SC.MP, {
  postPlannedActivityAt: function(row, col, ajaxOptions) {
    SC.asyncRequest(SC.MP.collectionResource, Object.extend(ajaxOptions, {
      method: 'POST',
      parameters: SC.MP.createPlannedActivityForm(row, col)
    }))
  },

  putPlannedActivity: function(href, toRow, toCol, ajaxOptions) {
    SC.asyncRequest(href, Object.extend(ajaxOptions, {
      method: 'PUT',
      parameters: SC.MP.createPlannedActivityForm(toRow, toCol)
    }))
  },

  deletePlannedActivity: function(href, ajaxOptions) {
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
    var params = $w("details condition").inject({}, function(notes, kind) {
      notes[kind] = content.select("." + kind).first().innerHTML.strip()
      return notes
    })
    params["label"] = content.select(".labels").first().innerHTML.strip().split(" ").compact()
    return params
  },

  findPopulation: function(row, col) {
    var marker = SC.MP.getCell(row, col).select(".marker").first()
    if (!marker) return;
    var popClass = marker.classNames().find(function(name) { return name.indexOf("population-") == 0 })
    if (popClass) {
      return popClass.substring(11);
    }
  },

  // Finds the set of activities implied by the settings in the add another activity boxes
  findNextActivities: function(receiver) {
    var selectedSource = $F("activity-source-filter")
    var selectedType = $F("activity-type-filter")
    var searchString = $F("activities-autocompleter-input")
    if (searchString == "Search for activity") {
      searchString = ""
    }

    var uri = SC.relativeUri("/api/v1/activities")
    if (!selectedSource.blank()) {
      uri += "/" + selectedSource
    }

    if (searchString.blank() && selectedSource.blank()) {
      receiver([]);
      return;
    }

    var params = { };
    if (!searchString.blank()) params.q = searchString;
    if (!selectedType.blank()) params['type-id'] = selectedType;

    SC.asyncRequest(uri, {
      method: "GET", parameters: params,
      onSuccess: function(response) {
        var doc = response.responseXML;
        var activities = SC.objectifyXml("activity", doc, function(elt, activity) {
          activity.source = elt.parentNode.getAttribute("name")
          activity.type = SC.MP.lookupActivityTypeNameById(activity['type-id'])
          if (!activity.code) activity.code = "";
        })

        receiver(activities)
      }
    })
  },

  // This is a bit of a hack, but it should work
  lookupActivityTypeNameById: function(id) {
    var opt = $A($('activity-type-filter').options).find(function(option) { return option.value == id } )
    var name = opt ? opt.innerHTML : null;
    return {
      id: id, name: name
    }
  }
})