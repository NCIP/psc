/*global jQuery psc window */
if (!window.psc) { var psc = { }; }
if (!psc.subject) { psc.subject = { }; }

(function ($) {
  psc.subject.ScheduleList = (function () {
    function zeropad(i) {
      if (i < 10) {
        return "0" + i;
      } else {
        return "" + i;
      }
    }
    
    function dateKey(date) {
      return date.getUTCFullYear() + "-" +
        zeropad(date.getUTCMonth() + 1) + "-" +
        zeropad(date.getUTCDate());
    }
    
    function scheduleReady() {
      var schedule = psc.subject.ScheduleData.current();
      
      if (null !== schedule) {
        var container = $('#scheduled-activities').empty()
        jQuery.each(schedule.allDays(), function (i, day) {
          var key = dateKey(day);
          var activities = 
            (schedule.days && schedule.days[key] && schedule.days[key].activities)
          if (psc.subject.isToday(day) && !activities) {
            container.append("<div id='schedule-today-marker' title='Today'></div>");
          }
          if (activities) {
            var items = $.map(activities, function (sa) {
              return resigTemplate('list_day_sa_entry', sa)
            }).join('\n');
            container.append(
              resigTemplate('list_day_entry', {
                dateClass: activities[0].dateClasses(),
                isToday: activities[0].isToday(),
                scheduledActivities: activities,
                displayDate: key,
                scheduledActivityListItems: items
              })
            )
            $(container.find("div.day:last-child")[0]).data("date", day)
          }
        })
        psc.subject.ScheduleList.FocusHandler.init()
      }

      $('#schedule-error').hide()
      $('.loading').fadeOut().hide();
    }

    function scheduleError(evt, message) {
      $('#schedule-error').empty().
        append("Problem loading schedule data: " + message).show();
      $('.loading').fadeOut();
    }

    return {
      init: function () {
        $('#schedule').bind('schedule-load-start', function () {
          $('.loading').fadeIn();
        });
        $('#schedule').bind('schedule-ready', scheduleReady);
        $('#schedule').bind('schedule-error', scheduleError);
      },
      
      FocusHandler: (function () {
        var SOURCE_NAME = "list";
        var hoverUpdating = false, manualScrolling = false;
        var programaticallyScrolling = false;
        
        var hoverAnimator = new psc.tools.AsyncUpdater(function (dateStr) {
          $('#scheduled-activities .day').removeClass('hover-date');
          if (dateStr) {
            $('#scheduled-activities .date-' + dateStr).addClass('hover-date');
          }
        });
        
        var focusAnimator = new psc.tools.AsyncUpdater(function (data) {
          var scrollTo = determineOffsetToDate(data.date, data.range);
          if (scrollTo) {
            programaticallyScrolling = true;
            $('#schedule').scrollTop(scrollTo);
            programaticallyScrolling = false;
          }
        }, function (v) {
          return v === null ? null : v.date.getTime();
        });
        
        function extractDate(classes) {
          var bits = classes.match(/date-(\d{4})-(\d{1,2})-(\d{1,2})/);
          return new Date(Date.UTC(bits[1], bits[2] - 1, bits[3], 12, 0, 0, 0));
        }
        
        function hoverOverDay(evt) {
          psc.subject.ScheduleData.hoverDate(extractDate(this.className), SOURCE_NAME)
        }
        
        function hoverOutDay() {
          psc.subject.ScheduleData.hoverDate(null, SOURCE_NAME)
        }
        
        function hoverEvent(evt, triggerData) {
          if (triggerData.date) {
            hoverAnimator.update(dateKey(triggerData.date));
          } else {
            hoverAnimator.update(null);
          }
        }
        
        function determineOffsetToDate(date, range) {
          var offset;
          if (psc.subject.isToday(date)) {
            var todayElts = $('#schedule-today-marker, #scheduled-activities .today');
            if (todayElts.length > 0) {
              offset = todayElts.position().top;
            }
          } else {
            var nextVisibleBlock;
            $('#scheduled-activities > .day').each(function (i, dayBlock) {
              if (date < $(dayBlock).data('date')) {
                nextVisibleBlock = $(dayBlock);
                return false;
              }
            });
            // only shift the list if the next visible activity in the list
            // is actually visible on the timeline (avoids nasty feedback)
            if (nextVisibleBlock && range.includes(nextVisibleBlock.data('date'))) {
              offset = nextVisibleBlock.position().top;
            } else {
              offset = 0;
            }
          }

          if (offset !== undefined) {
            return offset + $('#schedule').scrollTop();
          } else {
            return $('#scheduled-activities').height();
          }
        }
        
        function visibleRange() {
          var min, max;
          var scheduleBlockHeight = $('#schedule').height();
          $('#scheduled-activities > .day').each(function (i, dayBlock) {
            var header = $(dayBlock).find("h3");
            if (!min && (header.position().top + header.height() > 0)) {
              min = $(dayBlock).data('date');
            } else if ($(dayBlock).position().top + $(dayBlock).height() > scheduleBlockHeight) {
              max = $(dayBlock).data('date');
            }
            if (min && max) {
              return false;
            }
          })
          if (!max) {
            max = $('#scheduled-activities > .day:last-child').data('date');
          }
          if (min && max) {
            return new psc.tools.Range(min, max);
          } else {
            return null;
          }
        }
        
        function fireFocusChanged(evt) {
          if (!programaticallyScrolling) {
            var span = visibleRange();
            psc.subject.ScheduleData.focusDate(span.start, SOURCE_NAME, span);
          }
        }
        
        function scrollToFocusDate(evt, triggerData) {
          if (triggerData.source !== SOURCE_NAME) {
            focusAnimator.update(triggerData);
          }
        }
        
        return {
          init: function () {
            $('#scheduled-activities > .day').mouseover(hoverOverDay).mouseout(hoverOutDay);
            $('#schedule').scroll(fireFocusChanged)
            
            $('#schedule').bind('hover-date-changed', hoverEvent)
            $('#schedule').bind('focus-date-changed', scrollToFocusDate)
          }
        }
      }())
    };
  }());
}(jQuery));