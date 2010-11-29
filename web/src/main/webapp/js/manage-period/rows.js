psc.namespace('template.mpa');

/** TODO: this module needs tests */

psc.template.mpa.ActivityRows = (function ($) {
  var Model = psc.template.mpa.Model;
  
  function addNewActivityGroup(activityType) {
    var activitiesGroup = $("#activities tbody." + psc.tools.CssSelectors.escapeComponent(activityType.selector))[0]
    if (activitiesGroup) {
      console.log("There is already a group for %o", activityType.name)
      return;
    }
    var beforeBodyN = $.grep($('#activities tbody.activity-type'), function (e) { 
      return $(e).attr('activity-type').toLowerCase() < activityType.name.toLowerCase();
    }).length;

    insertNewActivityTypeTbody("activities", "new_activity_tbody_template",
      beforeBodyN, activityType)
    insertNewActivityTypeTbody("days", "new_days_tbody_template",
      beforeBodyN, activityType)
    insertNewActivityTypeTbody("notes", "new_notes_tbody_template",
      beforeBodyN, activityType)

    return $("#activities ." + psc.tools.CssSelectors.escapeComponent(activityType.selector))[0];
  }
  
  function insertNewActivityTypeTbody(block, template, bodyN, activityType) {
    console.log("Inserting %s for %o", template, activityType)
    var newBody = resigTemplate(template, activityType)
    var container = $('#' + block);
    if (bodyN >= container.find('tbody.activity-type').length) {
      var trailer = container.find('.trailer').closest('tbody');
      if (trailer.length > 0) {
        trailer.before(newBody);
      } else {
        container.find('table').append(newBody);
      }
    } else {
      container.find('tbody.activity-type:eq(' + bodyN + ')').before(newBody);
    }
  }

  function addActivityRow(activity, activityType) {
    var activitiesGroup = $("#activities tbody." + psc.tools.CssSelectors.escapeComponent(activityType.selector))[0]
    if (!activitiesGroup) { activitiesGroup = addNewActivityGroup(activityType) }
    var beforeTypeRow = $.grep($(activitiesGroup).find('tr.activity'), function (e) {
      return $(e).find('.activity-name').text().toLowerCase() < activity.name.toLowerCase();
    }).length;
    console.log("Will insert at %s", beforeTypeRow);

    insertNewActivityRow("activities", "new_activity_row_template", 
      beforeTypeRow, activity, activityType);
    insertNewActivityRow("days", "new_days_row_template", 
      beforeTypeRow, activity, activityType);
    insertNewActivityRow("notes", "new_notes_row_template", 
      beforeTypeRow, activity, activityType);

    var tbodyIndex = $('#activities tbody.activity-type').index($(activitiesGroup));
    var rowN = $('#activities tbody.activity-type:lt(' + tbodyIndex + ')').
      map(function () { return $(this).find('tr.activity').length; }).sum() + 
      beforeTypeRow;

    console.log("Added row is now #", rowN);
    $('#days').trigger('row-added', {
      row: rowN
    });

    scrollToRow(rowN);
  }

  function scrollToRow(rowN) {
    var row = Model.findRow('days', rowN);
    var finalScroll = $(row).position().top + $(row).outerHeight() / 2 - $('#days').outerHeight() / 2;
    $('#days').animate({
      scrollTop: finalScroll
    }, { easing: 'swing', duration: 800 });
  }

  function insertNewActivityRow(block, template, typeRowN, activity, activityType) {
    console.log("Inserting %s for %o", template, activity);
    var newRow = resigTemplate(template, activity);
    var container = $('#' + block + ' tbody.' + psc.tools.CssSelectors.escapeComponent(activityType.selector));
    if (typeRowN >= container.find('tr.activity').length) {
      container.append(newRow);
    } else {
      container.find('tr.activity:eq(' + typeRowN + ')').before(newRow);
    }
  }
  
  function createActivitiesAutocompleter() {
    new SC.FunctionalAutocompleter(
      'activities-autocompleter-input', 'activities-autocompleter-div', activityAutocompleterChoices, {
        select: "activity-name",
        afterUpdateElement: function(input, selected) {
          var activity = {
            name:   selected.select(".activity-name").first().innerHTML,
            code:   selected.select(".activity-code").first().innerHTML,
            source: selected.select(".activity-source").first().innerHTML
          }
          var activityType = {
            selector: selected.getAttribute("activity-type-selector"),
            name: selected.getAttribute("activity-type-name")
          }
          addActivityRow(activity, activityType)
          input.value = ""
          input.focus()
        }
      }
    );
  }

  function activityAutocompleterChoices(str, callback) {
    findNextActivities(function(data) {
      var lis = data.map(function(activity) {
        return resigTemplate("new_activity_autocompleter_row", activity);
      }).join("\n");
      callback("<ul>\n" + lis + "\n</ul>");
    });
  }

  // Finds the set of activities implied by the settings in the add another activity boxes
  function findNextActivities(receiver) {
    var selectedSource = $F("activity-source-filter");
    var selectedType = $F("activity-type-filter");
    var searchString = $F("activities-autocompleter-input");
    if (searchString == "Search for activity") {
      searchString = "";
    }

    var uri = SC.relativeUri("/api/v1/activities");
    if (!selectedSource.blank()) {
      uri += "/" + encodeURIComponent(selectedSource);
    }

    if (searchString.blank() && selectedSource.blank()) {
      receiver([]);
      return;
    }

    var params = { };
    if (!searchString.blank()) params.q = searchString;
    if (!selectedType.blank()) params['type'] = selectedType;

    SC.asyncRequest(uri, {
      method: "GET", parameters: params,
      onSuccess: function(response) {
        var doc = response.responseXML;
        var activities = psc.template.mpa.ActivityRows.objectifyActivityXml(doc);

        receiver(activities);
      }
    });
  }

  function updateUsedOnAction(evt, data) {
    if (data.rowNumber !== null) {
      updateUsedUnused(data.row)
    };
  }
    
  function updateUsedUnused(rowN) {
    var count = psc.template.mpa.Model.findRow('days', rowN).find(".marker").length;
    $('.column').find('tr.activity:eq(' + rowN + ')').
      removeClass('unused').removeClass('used').
      addClass(count == 0 ? 'unused' : 'used');
    // IE sucks: it doesn't immediately show/hide the view/edit button after 
    // the row class is changed to used/unused.  This ugly hack reminds it.
    if ($.browser.msie) {
      Model.findRow('notes', rowN).find('.notes-edit').toggle(count != 0);
    }
  }

  return {
    init: function () {
      if (psc.template.mpa.canEdit) {
        createActivitiesAutocompleter();
        $('#days').
          bind('action-started', updateUsedOnAction).
          bind('action-error', updateUsedOnAction).
          bind('action-completed', updateUsedOnAction)
      }
      var rowCount = $('#days tr.activity').length;
      for (var i = 0; i < rowCount; i++) {
        updateUsedUnused(i);
      }
    },

    addSelectedActivityRow: function (activity, activityType) {
        addActivityRow(activity,  activityType);
    },

    // Exposed for testing

    objectifyActivityXml: function (doc) {
      return SC.objectifyXml("activity", doc, function(elt, activity) {
        activity.source = elt.parentNode.getAttribute("name")
        activity.type = {
            name: activity['type'],
            selector: "activity-type-" + activity['type'].toLowerCase().replace(/ /g, "_")
        };
        if (!activity.code) activity.code = "";
      });
    }
  }
}(jQuery))