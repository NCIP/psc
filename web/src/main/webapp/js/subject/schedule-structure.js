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

      mineClass: function () {
        var classes = [];
        if (this.isOpen() && this.belongsToSubjCoord()) {
          classes.push("mine");
        }
        if (this.isOpen() && !this.belongsToSubjCoord()) {
          classes.push("others");
        }
        return classes;
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

      belongsToSubjCoord: function() {
        if (this.assignment !== undefined) {
          if (psc.subject.ScheduleData.getSubjectCoordinator() == this.assignment.subject_coordinator.username) {
            return true;
          } else {
            return false;
          }
        } else {
          //preview case
          return true;
        }
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

      hasStudySubjectId: function() {
        return (sa.study_subject_id !== undefined && sa.study_subject_id.length>0);
      },

      canUpdateSchedule: function() {
        return this.hasAssignment() && jQuery.inArray("update", sa.assignment.privileges) >= 0
      },

      planNotes: function () {
        var notes = [];
        if (this.details) notes.push(this.details);
        if (this.condition) notes.push(this.condition);
        if (this.labels) notes.push("Labels: " + this.labels);
        if (notes.length > 0) {
          return notes.join("; ")
        } else {
          return null;
        }
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

  function enhanceAssignment(ssa, index) {
    jQuery.extend(ssa, {
      counterClass: function () {
        return index%2==0 ? "even" : "odd";
      },

      canUpdateSchedule: function () {
        return(ssa.privileges != undefined  && _.include(ssa.privileges, 'update-schedule'));
      },

      hasNotifications: function () {
        return (ssa['notifications'] != undefined  && ssa['notifications'].length > 0);
      },

      hasPopulations: function () {
          return ssa['populations'] != undefined && ssa['populations'].length > 0;
      }
    });
  }

  function enhanceNotification(n, index, canUpdateSchedule) {
    jQuery.extend(n, {
      notificationClass: function () {
        return index%2==0 ? "even" : "odd";
      },

      canUpdateSchedule: function() {
        return canUpdateSchedule;
      },

      hasMessageWithLink: function() {
        return (n['message'] != undefined && n['message'].indexOf('pages/cal') >=0);
      },

      displayMessage: function() {
        if (n['message'] != undefined) {
          if (n['message'].indexOf('optional amendment') >=0) {
            return n['message'] + " using amendment section";
          } else if(n['message'].indexOf('pages/cal') >=0) {
            return psc.tools.Uris.relative(n['message']);
          }
        }
        return n['message'];
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
      if (dayObject['hidden_activities']) {
        return dayObject['hidden_activities'];
      }
    });

    var minDay = psc.tools.Dates.apiDateToUtc(minDayStr);
    var maxDay = psc.tools.Dates.apiDateToUtc(maxDayStr);
    if (maxDay != null && minDay != null){
      var dayCount = (maxDay.getTime() - minDay.getTime()) / psc.tools.Dates.ONE_DAY;
      for (var i = 0 ; i <= dayCount ; i += 1) {
        days.push(new Date(minDay.getTime() + i * psc.tools.Dates.ONE_DAY));
      }
    }
    
    if (scheduleApiResponse['study_segments']) {
      jQuery.each(scheduleApiResponse['study_segments'], function (idx, ss) {
        enhanceScheduledStudySegment(ss);
      });
    }

    if (scheduleApiResponse['assignments']) {
       jQuery.each(scheduleApiResponse['assignments'], function (index, ssa) {
       enhanceAssignment(ssa, index);
       if (ssa['notifications']) {
          jQuery.each(ssa['notifications'], function(index,notification) {
          enhanceNotification(notification, index, ssa.canUpdateSchedule())
        });
      }
      });
    }
  }());

  return jQuery.extend(scheduleApiResponse, {
    allDays: function () { return days; }
  });
};
