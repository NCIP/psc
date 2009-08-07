/*global window jQuery */
psc.namespace("subject");

psc.subject.isToday = function (date) {
  var today = new Date();
  return today.getUTCFullYear() == date.getUTCFullYear()
      && today.getUTCMonth() == date.getUTCMonth()
      && today.getUTCDate() == date.getUTCDate()
};

psc.subject.Schedule = function (scheduleApiResponse) {
  var days = [];

  function enhanceScheduledActivity(sa) {
    jQuery.extend(sa, {
      stateClasses: function () {
        var classes = ["scheduled-activity"];
        if (sa.current_state && sa.current_state.name) {
          classes.push(sa.current_state.name.toLowerCase());
        }
        this.isOpen() ? (classes.push("open")) : (classes.push("closed"));
        if (this.isOpen() && sa.currentDate() < new Date()) {
          classes.push('past-due');
        }

        return classes.join(' ');
      },

      currentDate: function () {
        return psc.tools.Dates.apiDateToUtc(this.current_state.date);
      },

      dateClasses: function () {
        var classes = ["date-" + this.current_state.date];
        if (this.isToday()) {
          classes.push("today")
        }
        return classes.join(' ');
      },

      studyClass: function () {
        return "study-" + this.study.replace(/\W/g, '_')
      },

      assignmentClass: function () {
        return "assignment-" + this.assignment.id.replace(/\W/g, '_')
      },

      isOpen: function () {
        if (sa.current_state && sa.current_state.name) {
          return jQuery.inArray(sa.current_state.name, ["scheduled", "conditional"]) >= 0;
        } else {
          return undefined;
        }
      },

      isToday: function () {
        return psc.subject.isToday(this.currentDate());
      },

      hasId: function () {
        return sa.id !== undefined;
      },

      hasAssignment: function() {
        return sa.assignment !== undefined;
      },

      hasDetails: function() {
        return sa.details !== undefined;
      },

      getDetails : function() {
        return this.details
      },

      getCondition : function() {
        return this.condition
      },

      hasCondition: function() {
        return sa.condition !== undefined;
      },

      getLabels : function() {
        return this.labels;
      },

      hasLabels: function() {
        return sa.labels !== undefined;
      },

      getPlanDay : function() {
        return this.formatted_plan_day
      }
    });
  }

  function enhanceScheduledStudySegment(ss) {
    jQuery.extend(ss, {
      assignmentName: function () {
        if (this.assignment) {
          return this.assignment.name;
        } else {
          return this.planned.study.assigned_identifier;
        }
      },
      
      startDate: function () {
        return psc.tools.Dates.apiDateToUtc(this.range.start_date);
      },
      
      stopDate: function () {
        return new Date(
          psc.tools.Dates.apiDateToUtc(this.range.stop_date).getTime() + 
          psc.tools.Dates.ONE_DAY - 1);
      }
    });
  }

  ////// init

  (function () {
    var minDayStr = null, maxDayStr = null;
    jQuery.each(scheduleApiResponse['days'], function (date, dayObject) {
      minDayStr = (minDayStr === null ? date : (minDayStr < date ? minDayStr : date));
      maxDayStr = (maxDayStr === null ? date : (maxDayStr > date ? maxDayStr : date));
      if (dayObject['activities']) {
        jQuery.each(dayObject['activities'], function(index, sa) {
          enhanceScheduledActivity(sa)
        });
      }
    });

    var minDay = psc.tools.Dates.apiDateToUtc(minDayStr);
    var maxDay = psc.tools.Dates.apiDateToUtc(maxDayStr);

    var dayCount = (maxDay.getTime() - minDay.getTime()) / psc.tools.Dates.ONE_DAY;
    for (var i = 0 ; i <= dayCount ; i += 1) {
      days.push(new Date(minDay.getTime() + i * psc.tools.Dates.ONE_DAY));
    }
    
    if (scheduleApiResponse['study_segments']) {
      jQuery.each(scheduleApiResponse['study_segments'], function (idx, ss) {
        enhanceScheduledStudySegment(ss);
      });
    }
  }());

  return jQuery.extend(scheduleApiResponse, {
    allDays: function () { return days; },
  });
};
