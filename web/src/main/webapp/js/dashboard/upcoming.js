psc.namespace('dashboard');

(function ($) {
  function extractActivityTypes() {
    return _($('#upcoming *[name="activity-types"]').serializeArray()).
      map(function (e) { return e.value; });
  }

  function loadUpcoming(days, activityTypes) {
    psc.dashboard.Main.loadStarted("upcoming");
    $('#upcoming-activity-days').slideUp(250);

    if (activityTypes.length == 0) {
      updateUpcoming({ rows: [] });
      return;
    }

    var startDate = new Date();
    var endDate = new Date();

    //since we count today as the day 1, we need to subtract  1 from the endDate to have the correct boundaries
    endDate.setDate(endDate.getDate() + (days-1));
    var qs = $.param({
      "responsible-user": psc.dashboard.Main.username,
      "start-date": psc.tools.Dates.nonUtcToApiDate(startDate),
      "end-date": psc.tools.Dates.nonUtcToApiDate(endDate),
      "state": ["scheduled", "conditional"],
      "activity-type": activityTypes
    }, true);
    $.ajax({
      url: psc.tools.Uris.relative("/api/v1/reports/scheduled-activities.json"),
      type: 'GET',
      dataType: 'json',
      data: qs,
      success: updateUpcoming,
      error: errorOnUpcoming
    });
  }

  function buildUpcoming(reportResult) {
    var upcoming = {};

    _(reportResult['rows']).each(function (row) {
      var dateKey = row['scheduled_date'];
      if (!upcoming[dateKey]) {
        upcoming[dateKey] = {
          day: row.scheduled_date,
          subjects: []
        };
      }
      var subjectRecord = _(upcoming[dateKey]['subjects']).detect(function (s) {
        return s.subject_grid_id == row.subject.grid_id;
      });
      if (!subjectRecord) {
        subjectRecord = {
          subject_name: row.subject.name,
          subject_grid_id: row.subject.grid_id,
          activities: []
        };
        upcoming[dateKey]['subjects'].unshift(subjectRecord);
      }
      subjectRecord['activities'].unshift({
        activity_name: row.activity_name,
        activity_type: row.activity_type,
        id: row.grid_id
      });
    });

    // sort subjects within days ...
    _(upcoming).each(function (day) {
      day.subjects = _.sortBy(day.subjects, function (s) { return s.subject_name; });
      // ... and activities for subjects
      _(day.subjects).each(function (s) {
        s.activities = _.sortBy(s.activities, function (a) {
          return [a.activity_type, a.activity_name]
        });
      });
    });

    return upcoming;
  }

  function updateUpcoming(reportResults)  {
    var upcoming = buildUpcoming(reportResults);
    if (_(upcoming).size() == 0) {
      $('#upcoming-activity-days').removeClass("error").
        html(resigTemplate('none_upcoming', {})).
        animate({ opacity: 'show', height: 'show' }, 250);
    } else {
      var content = _(upcoming).chain().keys().sort().map(function (dateKey) {
        return resigTemplate('upcoming_day', upcoming[dateKey])
      }).value().join("\n");

      $('#upcoming-activity-days').hide().removeClass("error").
        html(content).
        animate({ opacity: 'show', height: 'show' }, 250);
    }
    psc.dashboard.Main.loadFinished("upcoming");
  }

  function errorOnUpcoming(xhr, status, thrown) {
    console.log("error args", arguments);
    $('#upcoming-activity-days').addClass("error").
      html(resigTemplate('error_upcoming', { error: xhr.statusText })).
      animate({ opacity: 'show', height: 'show' }, 250);
    psc.dashboard.Main.loadFinished("upcoming");
  }

  psc.dashboard.Upcoming = {
    refresh: function () {
      var activityTypes = extractActivityTypes();
      var days = parseInt($('#upcoming-days').val());
      if (_.isNaN(days) || days < 1) days = 7;
      loadUpcoming(days, activityTypes);
    },

    init: function () {
      var self = this;
      $('#upcoming input, #upcoming select').change(this.refresh);
      $('#upcoming-all').click(function () {
        console.log('all clicked');
        $('#upcoming option').each(function (i, e) { e.selected = true });
        self.refresh();
        return false;
      });
      $('#upcoming-none').click(function () {
        console.log('none clicked', $('#upcoming option'));
        $('#upcoming option').each(function (i, e) { e.selected = false });
        self.refresh();
        return false;
      });
      this.refresh();
    }
  };
}(jQuery));