psc.namespace("dashboard");

(function ($) {
  psc.dashboard.PastDue = {
    load: function () {
      psc.dashboard.Main.loadStarted("past-due");
      var qs = $.param({
        "responsible-user": psc.dashboard.Main.username,
        "end-date": psc.tools.Dates.utcToApiDate(new Date()),
        "state": ["scheduled", "conditional"]
      }, true);
      $.ajax({
        url: psc.tools.Uris.relative("/api/v1/reports/scheduled-activities.json"),
        type: 'GET',
        dataType: 'json',
        data: qs,
        success: updatePastDue,
        error: errorOnPastDue
      });
    }
  };

  function errorOnPastDue(xhr, status, thrown) {
    $('#past-due-subjects').
      addClass('error').
      html("<li>There was an error while querying for past-due activities: " + status + "</li>");
    $('#past-due').animate({ opacity: 'show', height: 'show' }, 250);
    psc.dashboard.Main.loadFinished("past-due");
  }

  function updatePastDue(reportResults) {
    var pastDue = buildPastDue(reportResults);
    if (_(pastDue).size() == 0) {
      $('#past-due-subjects').html("<li>No past due activities.</li>");
      $('#past-due').animate({ opacity: 'hide', height: 'hide' }, 1500, function () {
        psc.dashboard.Main.loadFinished("past-due");
      });
    } else {
      $('#past-due-subjects').html(renderPastDue(pastDue));
      $('#past-due').animate({ opacity: 'show', height: 'show' }, 750, function () {
        psc.dashboard.Main.loadFinished("past-due");
      });
    }
  }

  function buildPastDue(reportResults) {
    var pastDue = {};
    var todayApi = psc.tools.Dates.utcToApiDate(new Date());
    _(reportResults['rows']).each(function (row) {
      var key = row.subject.grid_id;
      if (!pastDue[key]) {
        pastDue[key] = {
          count: 0,
          earliestApiDate: todayApi,
          subject: row.subject
        }
      }
      pastDue[key].count += 1;
      if (row.scheduled_date < pastDue[key].earliestApiDate) {
        pastDue[key].earliestApiDate = row.scheduled_date;
      }
    });

    return pastDue;
  }

  function renderPastDue(pastDue) {
    return _(pastDue).values().sortBy(function (entry) {
      return [entry.earliestApiDate, entry.count, entry.subject.name];
    }).collect(function (entry) {
      return resigTemplate('past_due_subject', entry);
    }).join("\n");
  }
}(jQuery));