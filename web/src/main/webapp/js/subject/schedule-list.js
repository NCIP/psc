/*global jQuery psc window resigTemplate */
psc.namespace("subject");

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
        var container = $('#scheduled-activities').empty();
        jQuery.each(schedule.allDays(), function (i, day) {
          var key = dateKey(day);
          var hiddenActivities =(schedule.days && schedule.days[key] && schedule.days[key].hidden_activities);
          var activities =
            (schedule.days && schedule.days[key] && schedule.days[key].activities);
          if (psc.subject.isToday(day) && !activities) {
            container.append("<div id='schedule-today-marker' title='Today'></div>");
          }
          if (activities) {
            var items = $.map(activities, function (sa) {
              return resigTemplate('list_day_sa_entry', sa);
            }).join('\n');
            container.append(
              resigTemplate('list_day_entry', {
                dateClass: activities[0].dateClasses(),
                isToday: activities[0].isToday(),
                scheduledActivities: activities,
                hasHiddenActivities: hiddenActivities,
                displayDate: key,
                scheduledActivityListItems: items
              })
            );
            $(container.find("div.day:last-child")[0]).data("date", day);
          }
        });
        psc.subject.ScheduleList.FocusHandler.init();
      }

      psc.subject.ScheduleList.buildDayPixelRanges();
      $('#schedule-error').hide();
      $('.loading').fadeOut().hide();
    }

    function scheduleError(evt, message) {
      $('#schedule-error').empty().
        append("Problem loading schedule data: " + message).show();

      $('.loading').fadeOut();
    }

    function changeActivityVisibility(evt, data) {
      var originalVisibilityCriteria = { }
      originalVisibilityCriteria.range = psc.subject.ScheduleList.visibleDayRange();
      originalVisibilityCriteria.date = originalVisibilityCriteria.range.start;
      originalVisibilityCriteria.additionalOffset =
        $('#scheduled-activities div.date-' + psc.tools.Dates.utcToApiDate(originalVisibilityCriteria.date)).
          position().top

      var affected = $("#scheduled-activities div.day li.scheduled-activity." + data.state);

      data.show ? affected.show() : affected.hide();
      $('#scheduled-activities div.day').each(function (index, day) {
        if (data.show && $(day).find('li.scheduled-activity.' + data.state).is('*')) {
          $(day).show();
        } else if (!data.show && !$(day).find('li.scheduled-activity').is(':visible')) {
          $(day).hide();
        }
      });

      psc.subject.ScheduleList.buildDayPixelRanges();
      psc.subject.ScheduleList.FocusHandler.realign(originalVisibilityCriteria);
    }

    return {
      init: function () {
        $('#schedule').bind('schedule-load-start', function () {
          $('.loading').fadeIn();
        });
        $('#schedule').bind('schedule-ready', scheduleReady);
        $('#schedule').bind('schedule-error', scheduleError);
        $('#schedule').bind('change-activity-visibility', changeActivityVisibility)
      },

      /* Creates an index of the positions of each day block to make it easier to search for what's visible */
      buildDayPixelRanges: function() {
        var currentOffset = $('#schedule').scrollTop();
        this.dayPixelRanges = $('#scheduled-activities > div.day:visible').collect(function () {
          var header = $(this).find("h3");
          var blockTop = $(this).position().top;
          return {
            date: $(this).data('date'),
            absolutePixelRange: {
              // count as "visible" if any part of the date header is visible
              start: blockTop + header.position().top + header.height() + currentOffset,
              stop: blockTop + $(this).height() + currentOffset
            }
          };
        });
      },

      visibleDayRange: function() {
        var min, max;
        var scheduleBlockHeight = $('#schedule').height();
        var scheduleScroll = $('#schedule').scrollTop();
        $(this.dayPixelRanges).each(function (i, dayInfo) {
          if (!min && (dayInfo.absolutePixelRange.start - scheduleScroll > 0)) {
            min = dayInfo.date;
          } else if ((dayInfo.absolutePixelRange.stop - scheduleScroll) > scheduleBlockHeight) {
            max = dayInfo.date;
          }

          if (min && max) {
            return false;
          }
        });

        if (!max) {
          max = $('#scheduled-activities > .day:last-child').data('date');
        }

        if (min && max) {
          return new psc.tools.Range(min, max);
        } else {
          return null;
        }
      },

      FocusHandler: (function () {
        var SOURCE_NAME = "list";
        var hoverUpdating = false, manualScrolling = false;
        var programaticallyScrolling = false;

        var hoverAnimator = new psc.tools.AsyncUpdater(function (dateStr) {
          $('#scheduled-activities div.day.hover-date').removeClass('hover-date');
          if (dateStr) {
            $('#scheduled-activities div.date-' + dateStr).addClass('hover-date');
          }
        });

        var focusAnimator = new psc.tools.AsyncUpdater(function (data) {
          if (SOURCE_NAME == data.source) return;
          var scrollTo = determineOffsetToDate(data.date, data.range);
          if (data.additionalOffset) scrollTo -= data.additionalOffset;
          if (scrollTo) {
            programaticallyScrolling = true;
            $('#schedule').scrollTop(scrollTo);
            // allow scroll handlers to fire
            setTimeout(function () {
              programaticallyScrolling = false;
            }, 1);
          }
        }, function (v) {
          if (v === null) {
            return null;
          } else {
            return v.date.getTime() + " " + v.additionalOffset;
          }
        });

        function extractDate(classes) {
          var bits = classes.match(/date-(\d{4})-(\d{1,2})-(\d{1,2})/);
          return new Date(Date.UTC(bits[1], bits[2] - 1, bits[3], 12, 0, 0, 0));
        }

        function hoverOverDay(evt) {
          psc.subject.ScheduleData.hoverDate(
            extractDate(this.className), SOURCE_NAME);
        }

        function hoverOutDay() {
          psc.subject.ScheduleData.hoverDate(null, SOURCE_NAME);
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
            $('#scheduled-activities > div.day:visible').each(function (i, dayBlock) {
              if (date <= $(dayBlock).data('date')) {
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

        function fireFocusChanged(evt) {
          if (!programaticallyScrolling) {
            var span = psc.subject.ScheduleList.visibleDayRange();
            psc.subject.ScheduleData.focusDate(span.start, SOURCE_NAME, span);
          }
        }

        function scrollToFocusDate(evt, triggerData) {
          focusAnimator.update(triggerData);
        }

        return {
          init: function () {
            $('#scheduled-activities > .day').mouseover(hoverOverDay).mouseout(hoverOutDay);
            $('#schedule').scroll(fireFocusChanged);

            $('#schedule').bind('hover-date-changed', hoverEvent);
            $('#schedule').bind('focus-date-changed', scrollToFocusDate);
          },

          /* When the content changes, this function can shift the scroll so that the previously
             focussed date is still focussed. */
          realign: function (ref) {
            scrollToFocusDate(null, ref);
          }
        };
      }())
    };
  }());
}(jQuery));