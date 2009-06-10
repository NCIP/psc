/*global jQuery psc window */
if (!window.psc) { var psc = { }; }
if (!psc.subject) { psc.subject = { }; }

(function ($) {
  psc.subject.ScheduleData = (function () {
    var schedule = null, focusDate = new Date();
    
    function triggeredDateComparableFn(triggerData) {
      return triggerData && triggerData.date && triggerData.date.getTime();
    }
    
    var focusDateTriggerer = new psc.tools.AsyncUpdater(function (triggerData) {
      focusDate = triggerData.date;
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
        $('#schedule').trigger('schedule-error', textStatus);
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
          $('#schedule').trigger('schedule-error', e);
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
