/*global window jQuery */
psc.namespace("subject");

psc.subject.isToday = function (date) {
  var today = new Date();
  return today.getUTCFullYear() == date.getUTCFullYear()
      && today.getUTCMonth() == date.getUTCMonth()
      && today.getUTCDate() == date.getUTCDate()
};

psc.subject.Schedule = function (scheduleApiResponse) {
  var segments = [];
  var segmentMap = {};
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
      }
    });
  }
  
  var segmentKey = function (sa) {
    return sa.study + '#' + sa['study_segment'];
  };
  
  var newSegmentFor = function (sa) {
    var newSegment = new psc.subject.ScheduledStudySegment(
      sa.study, sa.study_segment);
    segments.push(newSegment);
    segmentMap[segmentKey(sa)] = newSegment;
  };
  
  // Ensure that segments are still in order
  var updateSegments = function () {
    segments.sort(function(a, b) {
      return a.startDate() - b.startDate();
    });
  };
  
  (function () {
    var minDayStr = null, maxDayStr = null;
    jQuery.each(scheduleApiResponse['days'], function (date, dayObject) {
      minDayStr = (minDayStr === null ? date : (minDayStr < date ? minDayStr : date));
      maxDayStr = (maxDayStr === null ? date : (maxDayStr > date ? maxDayStr : date));
      if (dayObject['activities']) {
        jQuery.each(dayObject['activities'], function(index, sa) {
          enhanceScheduledActivity(sa)
          if (!segmentMap[segmentKey(sa)]) {
            newSegmentFor(sa);
          }
          segmentMap[segmentKey(sa)].addActivity(sa);
          updateSegments();
        });
      }
    });

    var minDay = psc.tools.Dates.apiDateToUtc(minDayStr);
    var maxDay = psc.tools.Dates.apiDateToUtc(maxDayStr);
    
    var dayCount = (maxDay.getTime() - minDay.getTime()) / psc.tools.Dates.ONE_DAY;
    for (var i = 0 ; i <= dayCount ; i += 1) {
      days.push(new Date(minDay.getTime() + i * psc.tools.Dates.ONE_DAY));
    }
  }());
  
  return jQuery.extend(scheduleApiResponse, {
    allDays: function () { return days; },
    studySegments: function () { return segments; }
  });
};

psc.subject.ScheduledStudySegment = function(study, name) {
  var activities = [];
  var startDate; 
  var stopDate;
  
  function updateRange(dateStr) {
    var date = psc.tools.Dates.apiDateToUtc(dateStr);
    if (!startDate || startDate > date) {
      startDate = date;
    }
    if (!stopDate || stopDate < date) {
      stopDate = new Date(date.getTime() + 86399999);
    }
  }
  
  return {
    name: function () { return name; },
    study: function () { return study; },
    
    startDate: function () { return startDate; },
    stopDate: function () { return stopDate; },
    
    scheduledActivities: function () { return activities; },
    
    addActivity: function (scheduledActivity) {
      activities.push(scheduledActivity);
      updateRange(scheduledActivity.current_state.date);
    }
  };
};
