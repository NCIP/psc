/*global jQuery before after describe it expect equal Screw psc require_spec require_main */

require_spec('spec_helper.js');
require_main('subject/schedule-structure.js');
require_main('psc-tools/misc.js');
require_main('subject/real-schedule-controls.js')

Screw.Unit(function () {
  (function ($) {
    describe("psc.subject.RealScheduleControls", function () {
      describe("delay or advance", function () {
        var realScheduleDataModule, actualParams;

        function testActivity(id, dateS, currentState, study) {
          return {
            id: id,
            current_state: {
              name: currentState,
              date: dateS
            },
            study: study || "NU 00A0",
            activity: {
              name: "Activity A",
              type: "Other"
            }
          }
        }

        function actual() {
          return actualParams = actualParams || psc.subject.RealScheduleControls.computeDelayParameters();
        }

        before(function () {
          realScheduleDataModule = psc.subject.ScheduleData;

          var response = { "days": { } };
          response["days"]["2008-04-25"] = { "activities": [
            testActivity("S", "2008-04-25", "scheduled"),
            testActivity("Co", "2008-04-25", "conditional"),
            testActivity("O", "2008-04-25", "occurred"),
            testActivity("C", "2008-04-25", "canceled"),
            testActivity("M", "2008-04-25", "missed"),
            testActivity("N", "2008-04-25", "NA"),
            testActivity("07A0", "2008-04-25", "scheduled", "NU 07A0")
          ] };
          response["days"]["2008-05-01"] = { "activities": [
            testActivity("S01", "2008-05-01", "scheduled"),
            testActivity("C01", "2008-05-01", "canceled")
          ] };

          psc.subject.ScheduleData = {
            current: function () {
              return new psc.subject.Schedule(response);
            }
          };

          $('#delay-or-advance').val('1')
          $('#delay-amount').val('3');
          $('#delay-study').val("All studies");
          $('#delay-as-of').val('');
          $('#delay-reason').val('');
          actualParams = null;
        });

        after(function () {
          psc.subject.ScheduleData = realScheduleDataModule;
        });

        it("delays based on the activity's current date", function () {
          $('#delay-amount').val('8');
          expect(actual()['S']['date']).to(equal, '2008-05-03');
        });

        it("advances based on the activity's current date", function () {
          $('#delay-or-advance').val('-1')
          $('#delay-amount').val('3');
          expect(actual()['S01']['date']).to(equal, '2008-04-28');
        });

        it("filters the activities to delay based on the as-of date", function () {
          $('#delay-as-of').val("05/01/2008");
          expect(actual()['S']).to(equal, undefined);
          expect(actual()['S01']['date']).to(equal, '2008-05-04');
        });

        if (psc.test.envjs()) {
          print("SKIPPING study filter spec b/c env.js doesn't work with SELECTs");
        } else {
          it("filters the activities to delay based on the selected study", function () {
            $('#delay-study').val("NU 07A0");
            expect(actual()['S']).to(equal, undefined);
            expect(actual()['07A0']['date']).to(equal, '2008-04-28');
          });
        }

        it("delays scheduled activities", function () {
          expect(actual()['S']['state']).to(equal, 'scheduled');
        });

        it("delays conditional activities", function () {
          expect(actual()['Co']['state']).to(equal, 'conditional');
        });

        $.each(['missed', 'canceled', 'occurred', 'NA'], function () {
          var mode = this;
          var id = mode.substring(0, 1).toUpperCase();
          it("does not delay " + mode + " activities", function () {
            expect(actual()[id]).to(equal, undefined);
          });
        });

        it("includes the reason", function () {
          $('#delay-reason').val('Timing');
          expect(actual()['Co']['reason']).to(equal, 'Timing');
        });
      });
    });
  }(jQuery));
});