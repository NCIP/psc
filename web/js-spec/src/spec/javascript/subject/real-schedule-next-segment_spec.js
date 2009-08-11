require_spec('spec_helper.js');
require_spec('sync-updater.js');
require_main('subject/schedule-structure.js');
require_main('psc-tools/misc.js');
require_main('subject/real-schedule-next-segment.js');

Screw.Unit(function () {
   (function ($) {
    describe("psc.subject.RealScheduleNextSegment", function () {
      var realScheduleDataModule;

      before(function () {
        realScheduleDataModule = psc.subject.ScheduleData || null;
      });

      after(function () {
        psc.subject.ScheduleData = realScheduleDataModule;
      });

      function createTestSegment(startDate, stopDate, studyIdentifier) {
        return {
          range: {
            start_date: startDate,
            stop_date: stopDate
          },
          planned: {
            study: {
              assigned_identifier : studyIdentifier
            }
          }
        }
      }

      describe("Next segment dates", function () {
        before(function () {
          var response;

          response = {
            "days": { "2008-04-25": { } },
            "study_segments": [
              createTestSegment("2008-04-25", "2008-05-05", "NU_011"),
              createTestSegment("2008-05-06", "2008-05-11", "NU_011"),
              createTestSegment("2008-04-15", "2008-04-24", "NU_011"),
              createTestSegment("2008-04-10", "2008-04-18", "NU_012"),
              createTestSegment("2008-04-19", "2008-04-28", "NU_012")
            ]
          };

          psc.subject.ScheduleData = {
            current: function () {
              return new psc.subject.Schedule(response);
            }
          };
          $('#studySegmentSelector').val("1")
          psc.subject.RealScheduleNextSegment.init();
        });

        it("displays immediate date when immediate mode selected", function () {
          $('#mode-radio-immediate').click()
          expect($('#start-date-input').val()).to(equal, psc.tools.Dates.utcToDisplayDate(new Date()));
        });

        it("displays per protocol date when per protocol mode selected", function () {
          $('#mode-radio-per-protocol').click()
          expect($('#start-date-input').val()).to(equal, '05/12/2008');
        });

        it("updates per protocol date as seleced segment change according to correct per protocol date ", function() {
          $('#studySegmentSelector').val(2)
          $('#studySegmentSelector').click()
          expect($('#start-date-input').val()).to(equal, '04/29/2008');
        });

      })
   })
 }(jQuery));
})
