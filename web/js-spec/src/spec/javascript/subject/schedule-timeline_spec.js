/*global Screw before after describe it expect equal equal_date match psc */

Timeline_ajax_url = "/main/simile-timeline/2.3.0/timeline_ajax/simile-ajax-api.js?bundle=true";
Timeline_urlPrefix = "/main/simile-timeline/2.3.0/timeline_js/";
Timeline_parameters = "bundle=true";

require_spec("spec_helper.js");
require_spec('sync-updater.js');
// TODO: this isn't working
// require_main("simile-timeline/2.3.0/timeline_js/timeline-api.js");
// require_main("simile-timeline/2.3.0/timeline_js/timeline-bundle.js");
require_main("psc-tools/misc.js");
require_main("subject/schedule-structure.js");
require_main("subject/schedule-timeline.js");

Screw.Unit(function () {
  describe("Event conversion", function () {
    describe("For scheduled activities", function () {
      var sa, actualEvent;

      before(function () {
        sa = {
          current_state: {
            'date': '2009-03-01',
            'name': 'canceled'
          },
          activity: { name: 'Activity B', type: 'Other' },
          details: "Something happens first",
          study_segment: "Treatment: X",
          study: "NU 09A0"
        };

        actualEvent = function () {
          var sched = new psc.subject.Schedule({
            "days": {
              "2009-03-01": {
                "activities": [ sa ]
              }
            }
          })
          return psc.subject.ScheduleTimeline.eventForScheduledActivity(
            sched['days']['2009-03-01']['activities'][0]);
        };
      });

      it("generates an instant event", function () {
        expect(actualEvent().durationEvent).to(equal, false);
      });

      it("has the right start", function () {
        expect(actualEvent().start).to(equal_date, new Date(2009, 2, 1));
      });

      it("has a title", function () {
        expect(actualEvent().title).to(equal, "Activity B");
      });

      it("has the segment in the description", function () {
        expect(actualEvent().description).to(match, "Treatment: X");
      });

      it("has the study in the description", function () {
        expect(actualEvent().description).to(match, "NU 09A0");
      });

      it("has an image based on the state", function () {
        expect(actualEvent().image).to(equal, "/images/canceled.png");
      });

      it("the image path is app relative", function () {
        psc.tools.Uris.INTERNAL_URI_BASE_PATH = "/foo";
        expect(actualEvent().image).to(equal, "/foo/images/canceled.png");
      });
    });
    
    describe("For segments", function () {
      var segment, actualEvent;
      
      before(function () {
        segment = new psc.subject.ScheduledStudySegment("NU 1402", "Treatment: B");
        segment.addActivity({
          current_state: { 'date': '2009-04-06' }
        });
        
        actualEvent = function () {
          return psc.subject.ScheduleTimeline.eventForScheduledStudySegment(segment);
        };
      });
      
      it("is a duration event", function () {
        expect(actualEvent().durationEvent).to(equal, true);
      });
      
      it("has the right start", function () {
        expect(actualEvent().start).to(equal_utc_date, new Date(Date.UTC(2009, 3, 6)));
      });
      
      it("has the right end", function () {
        expect(actualEvent().end).to(equal_utc_date, new Date(Date.UTC(2009, 3, 6, 23, 59, 59, 999)));
      });
      
      it("has a title reflecting the segment name", function () {
        expect(actualEvent().title).to(match, "Treatment: B");
      });
      
      it("has a title reflecting the study name", function () {
        expect(actualEvent().title).to(match, "NU 1402");
      });
    });
  });
});