/*global psc jQuery window Timeline */
psc.namespace("subject");

/*
 * Manages the Simile Timeline instance for the subject single schedule page.
 *
 * Known flaws:
 *   - Sync scrolling to the left in a sparse timeline doesn't update the list
 *     until a newly-visible item passes the centerline.
 *   - Timeline doesn't adapt well to schedules with lots of parallel segments
 *     (~4 or more) or events on the same day (~5).
 *   - The first time the resize event is fired, the event markers spread 
 *     apart for some reason.  (Theory: they are adapting to fit non-existent 
 *     text labels.  Possible solution: a modified painter which excludes 
 *     text.)
 *   - The descriptions for segments and activities are pretty lacking.
 *   - The hover decorator doesn't show up when you get some distance away 
 *     from "today".
 *   - Sync scrolling in the vicinity of "today" on a sparse timeline is 
 *     jumpy.
 */

(function ($) {
  psc.subject.ScheduleTimeline = (function () {
    var BAND_OVERVIEW = 0;
    var BAND_SEGMENTS = 1;
    var BAND_ACTIVITIES = 2;

    var timeline;
    var saEventSource, segmentEventSource;
    var theme, segmentTheme;
    var bandInfo;
    var resizeTimer = null;

    function initializeTimelineParameters() {
      initializeEventSources();
      initializeThemes();
      initializeBandInfo();
    }
    
    function initializeEventSources() {
       saEventSource = new Timeline.DefaultEventSource();
       segmentEventSource = new Timeline.DefaultEventSource();
    }

    function initializeThemes() {
      theme = new Timeline.ClassicTheme.create();
      theme.firstDayOfWeek = 1;
      theme.event.track.height = 10;
      theme.event.track.gap = 1;
      theme.event.track.offset = 0;
      theme.event.instant.iconHeight = 6;
      theme.event.instant.iconWidth = 10;
      
      segmentTheme = new Timeline.ClassicTheme.create();
      segmentTheme.firstDayOfWeek = 1;
      segmentTheme.event.track.height = 14;
      segmentTheme.event.tape.height = 4;
    }

    function initializeBandInfo() {
      bandInfo = [
        Timeline.createBandInfo({
          eventSource: saEventSource,
          theme: theme,
          width: "15%",
          intervalUnit: Timeline.DateTime.YEAR,
          intervalPixels: 300,
          overview: true
        }),
        Timeline.createBandInfo({
          eventSource: segmentEventSource,
          theme: segmentTheme,
          width: "35%",
          intervalUnit: Timeline.DateTime.WEEK,
          intervalPixels: 150
        }),
        Timeline.createBandInfo({
          eventSource: saEventSource,
          theme: theme,
          width: "50%",
          intervalUnit: Timeline.DateTime.WEEK,
          intervalPixels: 150,
          eventPainter: psc.subject.DivIconOnlyPainter
        })
      ];
      $.each(bandInfo, function (i, bi) {
        bi.labeller = new psc.subject.DayOnlyLabeller(Timeline.getDefaultLocale(), 0);
        if (i != BAND_ACTIVITIES) {
          bandInfo[i].syncWith = BAND_ACTIVITIES;
          bandInfo[i].highlight = true;
        }
      });

      ////// today highlight

      var todayStart = psc.tools.Dates.startOfUtcDay(new Date());

      for (var i = 0 ; i < bandInfo.length ; i += 1) {
        var params = {
          cssClass: 'today',
          startDate: todayStart,
          endDate: new Date(todayStart.getTime() + psc.tools.Dates.ONE_DAY)
        };
        if (BAND_ACTIVITIES === i) {
          params.startLabel = "Today";
        } else if (BAND_OVERVIEW === i) {
          // With the actual width, it doesn't show up on IE or Safari
          params.startDate = new Date(params.startDate.getTime() - psc.tools.Dates.ONE_DAY / 2);
          params.endDate = new Date(params.endDate.getTime() + psc.tools.Dates.ONE_DAY / 2);
        }
        bandInfo[i].decorators = [ new Timeline.SpanHighlightDecorator(params) ];
      }

      ////// hover highlight (doesn't work well)

      $.each([BAND_SEGMENTS, BAND_ACTIVITIES], function (i, band) {
        bandInfo[band].decorators.push(new Timeline.SpanHighlightDecorator({
          cssClass: 'hover-date',
          startDate: todayStart,
          endDate: new Date(todayStart.getTime() + psc.tools.Dates.ONE_DAY)
        }));
      });
    }

    function scheduleLoadStart() {
      timeline.showLoadingMessage();
    }
    
    function scheduleReady() {
      var schedule = psc.subject.ScheduleData.current();

      if (null !== schedule) {
        var saEvents = [];
        $.each(schedule['days'], function (date, dayObject) {
          $.each(dayObject['activities'], function (idx, sa) {
            saEvents.push(psc.subject.ScheduleTimeline.eventForScheduledActivity(sa));
          });
        });

        saEventSource.clear()
        saEventSource.loadJSON({
          "events": saEvents
        }, '.');

        var segEvents = [];
        $.each(schedule.study_segments, function (idx, segment) {
          segEvents.push(psc.subject.ScheduleTimeline.eventForScheduledStudySegment(segment));
        });
        
        segmentEventSource.clear()
        segmentEventSource.loadJSON({
          "events": segEvents
        }, '.');
      }

      timeline.hideLoadingMessage();
    }

    function scheduleError() {
      timeline.hideLoadingMessage();
    }

    return {
      bandInfo: function () { 
        return bandInfo; 
      },

      timeline: function () { 
        return timeline; 
      },

      eventForScheduledActivity: function (sa) {
        return {
          durationEvent: false,
          start: new Date(sa.currentDate().getTime() + psc.tools.Dates.ONE_DAY / 2),
          title: sa.activity && sa.activity.name,
          description: sa.study + " / " + sa.study_segment,
          classname: this.stateClasses(sa),
          image: psc.tools.Uris.relative("/images/" + sa.current_state.name + ".png")
        };
      },

      stateClasses: function(sa) {
        var classes = [];
        if (sa.isOpen() && sa.belongsToSubjCoord()) {
          classes.push("outstanding open mine")
        }
        if (sa.isOpen() && !sa.belongsToSubjCoord()) {
          classes.push("outstanding open others")
        }
        if (!sa.isOpen()) {
          classes.push("complete closed")
        }
        return classes;
      },

      segmentClass: function(segment) {
        if (this.assignment !== undefined) {
          if (psc.subject.ScheduleData.getSubjectCoordinator() == segment.assignment.subject_coordinator.username) {
            return "segment_mine";
          } else {
            return "segment_others";
          }
        } else {
          //preview case
          return "segment_mine";
        }
      },

      eventForScheduledStudySegment: function (segment) {
        return {
          durationEvent: true,
          start: segment.startDate(),
          end: segment.stopDate(),
          classname: this.segmentClass(segment),
          title: segment.assignmentName() + " / " + segment.name,
          description: ""
        };
      },

      create: function () {
        initializeTimelineParameters();
        timeline = Timeline.create(document.getElementById("schedule-timeline"), this.bandInfo());
        timeline.getBand(BAND_ACTIVITIES).setCenterVisibleDate(
          new Date(psc.tools.Dates.middleOfUtcDay(new Date())));

        $('#schedule').bind('schedule-load-start', scheduleLoadStart);
        $('#schedule').bind('schedule-ready', scheduleReady);
        $('#schedule').bind('schedule-error', scheduleError);
      },

      resize: function () {
        if (resizeTimer === null && timeline !== null) {
          resizeTimer = window.setTimeout(function () {
            resizeTimer = null;
            var center = timeline.getBand(BAND_ACTIVITIES).getCenterVisibleDate();
            timeline.layout();
            timeline.getBand(BAND_ACTIVITIES).setCenterVisibleDate(center);
          }, 100);
        }
      },

      FocusHandler: (function () {
        var SOURCE_NAME = "timeline";
        var lastHoverPixel = null;
        var hoverUpdating = false, 
            positionUpdatingByMouse = false, 
            positionUpdatingByKeyboard = false,
            programaticallyScrolling = false;
        
        ///// HOVERING
        
        var hoverAnimator = new psc.tools.AsyncUpdater(function (offsetPx) {
          if (offsetPx) {
            $('#schedule-timeline div.hover-date').show().css('left', offsetPx + "px");
          } else {
            $('#schedule-timeline div.hover-date').hide();
          }
        });
        
        function hoverDateByLocation(bandOffset, hoverPixel) {
          var px = hoverPixel || lastHoverPixel;
          if (px) {
            hoverUpdating = true;
            var hoverDay = psc.tools.Dates.startOfUtcDay(timeline.getBand(BAND_ACTIVITIES).getEther().pixelOffsetToDate(px));
            psc.subject.ScheduleData.hoverDate(new Date(hoverDay.getTime() + psc.tools.Dates.ONE_DAY / 2), SOURCE_NAME);
            hoverAnimator.update(timeline.getBand(BAND_ACTIVITIES).getEther().dateToPixelOffset(hoverDay) - bandOffset);
            lastHoverPixel = px;
            hoverUpdating = false;
          }
        }
        
        function mouseHoverMove(evt) {
          hoverDateByLocation(parseInt(this.style.left),
            evt.clientX - $('#schedule-timeline').offset().left);
        }
        
        function mouseHoverStop(evt) {
          hoverUpdating = true;
          stopHovering();
          psc.subject.ScheduleData.hoverDate(null, SOURCE_NAME);
          hoverUpdating = false;
        }
        
        function stopHovering() {
          lastHoverPixel = null;
          hoverAnimator.update(null);
        }
        
        function hoverBand(band) {
          // ensure that the hover continues to lie under the mouse pointer,
          // even if the band is scrolled using the wheel or keyboard
          hoverDateByLocation(band.getViewOffset())
        }
        
        function hoverEvent(evt, triggerData) {
          if (!hoverUpdating) {
            if (triggerData.date) {
              hoverDateByLocation(
                timeline.getBand(BAND_ACTIVITIES).getViewOffset(), 
                timeline.getBand(BAND_ACTIVITIES).getEther().dateToPixelOffset(psc.tools.Dates.startOfUtcDay(triggerData.date))
              );
            } else {
              stopHovering()
            }
          }
        }
        
        ////// SCROLLING
        
        var focusAnimator = new psc.tools.AsyncUpdater(function (focusDate) {
          var band = timeline.getBand(BAND_ACTIVITIES);
          if (focusDate < band.getMinVisibleDate() || focusDate > band.getMaxVisibleDate()) {
            programaticallyScrolling = true;
            band.setCenterVisibleDate(focusDate);
            programaticallyScrolling = false;
          }
        }, function (v) {
          return v === null ? null : v.getTime();
        });
        
        function internalPositionUpdating() {
          return positionUpdatingByKeyboard || positionUpdatingByMouse;
        }
        
        function startScrollingByKeyboard() {
          positionUpdatingByKeyboard = true;
        }
        
        function stopScrollingByKeyboard() {
          positionUpdatingByKeyboard = false;
        }
        
        function startScrollingByMouse() {
          positionUpdatingByMouse = true;
        }
        
        function stopScrollingByMouse() {
          positionUpdatingByMouse = false;
        }
        
        function recenter(evt, triggerData) {
          if (triggerData.source !== SOURCE_NAME) {
            focusAnimator.update(triggerData.date);
          }
        }
        
        function focusChangedByScroll() {
          if (internalPositionUpdating() && !programaticallyScrolling) {
            var band = timeline.getBand(BAND_ACTIVITIES);
            var dateFromBand = band.getCenterVisibleDate();
            var dateFromBandInUTC = new Date(Date.UTC(dateFromBand.getFullYear(), dateFromBand.getMonth(), dateFromBand.getDate(), 0,0,0))
            psc.subject.ScheduleData.focusDate(
              dateFromBandInUTC,
              SOURCE_NAME,
              new psc.tools.Range(band.getMinVisibleDate(), band.getMaxVisibleDate())
            );
          }
        }
        
        return {
          init: function () {
            var hoverBandsSelector = $.map([BAND_SEGMENTS, BAND_ACTIVITIES], function (i) {
              return "#timeline-band-" + i
            }).join(", ");
            $(hoverBandsSelector).mousemove(mouseHoverMove).mouseout(mouseHoverStop);
            timeline.getBand(BAND_ACTIVITIES).addOnScrollListener(hoverBand);

            $('#schedule-timeline').
              mouseover(startScrollingByMouse).
              mouseout(stopScrollingByMouse);
            $('div.timeline-band-input input').
              focus(startScrollingByKeyboard).
              blur(stopScrollingByKeyboard);
            timeline.getBand(BAND_ACTIVITIES).addOnScrollListener(focusChangedByScroll);

            $('#schedule').bind('hover-date-changed', hoverEvent);
            $('#schedule').bind('focus-date-changed', recenter);
          }
        }
      }())
    };
  }());
}(jQuery));