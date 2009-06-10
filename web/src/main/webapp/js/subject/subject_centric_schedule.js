if (!window.psc) { window.psc = { } }
if (!psc.subject) { psc.subject = { } }

psc.subject.SegmentRow = function(start, stop) {
  var range = new psc.tools.Range(start, stop);
  var segments = [];
  
  return {
    segments: segments,
    
    willFit: function(segment) {
      if (!range.includes(segment.dateRange)) return false;
      
      for (var i = 0 ; i < this.segments.length ; i++) {
        if (this.segments[i].dateRange.intersects(segment.dateRange)) {
          return false
        }
      }
      
      return true
    },
    
    add: function(segment) {
      if (this.willFit(segment)) {
        segments[segments.length] = segment
      } else {
        throw segment.name + " will not fit in this row.  Use willFit to check first."
      }
    }
  }
}

psc.subject.ScheduleDay = function(day) {
  var activities = [];
  
  function dateClass(date) {
    function zeropad(i) {
      return i > 9 ? "" + i : "0" + i;
    }
    
    return 'date-' +
      date.getFullYear()           + '-' +
      zeropad(date.getMonth() + 1) + '-' +
      zeropad(date.getDate())
  }
  
  return {
    date: day,
    
    today: function() {
      var today = new Date()
      return this.date.getYear() == today.getYear() &&
        this.date.getMonth() == today.getMonth() &&
        this.date.getDate() == today.getDate()
    },
    
    empty: function() {
      return this.activities.length == 0
    },
    
    activities: activities,
    
    addActivity: function(newOne) {
      this.activities[this.activities.length] = newOne
    },
    
    detailTimelineClasses: function() {
      var classes = ['day', dateClass(this.date)];
      if (this.date.getDate() == 1) {
        classes[classes.length] = 'month-start'
        if (this.date.getMonth() == 0) {
          classes[classes.length] = 'year-start'
        }
      }
      if (this.today()) {
        classes[classes.length] = 'today'
      }
      if (this.activities.length != 0) {
        classes[classes.length] = 'has-activities'
      }
      return classes;
    }
  }
}