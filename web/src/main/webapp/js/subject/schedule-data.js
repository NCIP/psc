/*global jQuery psc window */
psc.namespace("subject");

(function ($) {
  psc.subject.ScheduleData = (function () {
    var schedule, readOnly = false, subjectCoordinator = null, focusDate = new Date();

    
    function triggeredDateComparableFn(triggerData) {
      return triggerData && triggerData.date && triggerData.date.getTime();
    }
    
    var focusDateTriggerer = new psc.tools.AsyncUpdater(function (triggerData) {
      $('#schedule').trigger('focus-date-changed', triggerData);
    }, triggeredDateComparableFn);
    
    var hoverDateTriggerer = new psc.tools.AsyncUpdater(function (triggerData) {
      $('#schedule').trigger('hover-date-changed', triggerData);
    }, triggeredDateComparableFn);
    
    var uriGenerator = null;
    
    function error(XMLHttpRequest, textStatus, errorThrown) {
      if (textStatus === "notmodified") {
        triggerScheduleReady();
      } else {
        var msg = psc.subject.ScheduleData.errorMessage(XMLHttpRequest, textStatus, errorThrown);

        $('#schedule').trigger('schedule-error', msg);
      }
    }
    
    function replaceSchedule(newData) {
      schedule = new psc.subject.Schedule(newData);
      triggerScheduleReady();
    }
    
    function triggerScheduleReady() {
      $('#schedule').trigger('schedule-ready');
      $('#schedule').trigger('focus-date-changed', { 
        date: focusDate,
        source: "load",
        range: new psc.tools.Range(focusDate, focusDate)
      });
    }
    
    function doLoad() {
      try {
        $.ajax({
          dataType: 'json',
          url: (psc.subject.ScheduleData.uriGenerator())(),
          success: replaceSchedule,
          error: error
        });
      } catch (e) {
        // Since this method is called from a jQuery event handler,
        // trigger the error from a different thread
        setTimeout(function () {
          $('#schedule').trigger('schedule-error', 'Problem Requesting Schedule Data (' + e.message + ')');
        }, 300)
      }
    }
    
    return {
      current: function () {
        return schedule; 
      },
      
      focusDate: function (newFocusDate, source, range) {
        if (arguments.length > 0) {
          focusDateTriggerer.update({ 
            date: newFocusDate, 
            source: source, 
            range: range
          });
          return newFocusDate;
        } else {
          return focusDate;
        }
      },
      
      hoverDate: function (newHoverDate, source) {
        if (arguments.length > 0) {
          hoverDateTriggerer.update({ 
            date: newHoverDate, 
            source: source
          });
          return newHoverDate;
        } else {
          return hoverDate;
        }
      },
      
      uriGenerator: function (newGen) {
        if (arguments.length > 0) {
          uriGenerator = newGen;
        } else {
          if (uriGenerator) {
            return uriGenerator;
          } else {
            throw "psc.subject.ScheduleData.uriGenerator not set.  Don't know which resource to load from."
          }
        }
      },

      setSubjectCoordinator: function(subjCoord) {
        subjectCoordinator = subjCoord;
      },

      getSubjectCoordinator: function() {
        return subjectCoordinator;
      },

      setReadOnly: function(v) {
          readOnly = v;
      },

      isReadOnly: function() {
          return readOnly;
      },

      errorMessage: function(XMLHttpRequest, textStatus, errorThrown) {
        var msg = textStatus;
        if (msg === null || msg === "error") {
          if (XMLHttpRequest !== null) {
            msg =  XMLHttpRequest.statusText + " (" + XMLHttpRequest.status + ")";
          } else if (errorThrown !== null) {
            msg = "Problem Requesting Schedule Data (" + errorThrown.message + ")";
          } else {
            msg = "Problem Requesting Schedule Data";
          }
        }

        return msg;
      },

      clear: function () {
        schedule = null; 
      },
      
      refresh: function () {
        $('#schedule').trigger('schedule-load-start');
      },
      
      init: function () {
        $('#schedule').bind('schedule-load-start', doLoad);
      }
    };
  }());

  $(document).ready(psc.subject.ScheduleData.init);

}(jQuery));
