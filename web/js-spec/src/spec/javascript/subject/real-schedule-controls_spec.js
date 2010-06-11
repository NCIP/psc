/*global jQuery before after describe it expect equal Screw psc require_spec require_main */

require_spec('spec_helper.js');
require_spec('sync-updater.js');
require_main('subject/schedule-structure.js');
require_main('psc-tools/misc.js');
require_main('resig-templates.js');
require_main('subject/schedule-list.js'); // for regenerating the list HTML
require_main('jquery/jquery.enumerable.js');
require_main('subject/real-schedule-controls.js');

psc.namespace('configuration');

Screw.Unit(function () {
  (function ($) {
    psc.configuration.calendarDateFormat = (function () {return "%m/%d/%y"; });

    describe("psc.subject.RealScheduleControls", function () {
      var realScheduleDataModule;

      before(function () {
        realScheduleDataModule = psc.subject.ScheduleData || null;
      });

      after(function () {
        psc.subject.ScheduleData = realScheduleDataModule;
      });

      function testActivity(id, dateS, currentState, study) {
        study = study || "NU 00A0";
        return {
          id: id,
          current_state: {
            name: currentState,
            date: dateS
          },
          study: study,
          study_segment: 'Treatment',
          assignment: {
            id: "GRID-" + study.replace(/\s/g, '_'),
            name: study
          },
          activity: {
            name: "Activity A",
            type: "Other"
          }
        }
      }

      describe("delay or advance", function () {
        var actualParams;

        function actual() {
          return actualParams = actualParams || psc.subject.RealScheduleControls.computeDelayParameters();
        }

        before(function () {
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
          $('#delay-assignment').val("All studies");
          $('#delay-as-of').val('');
          $('#delay-reason').val('');
          actualParams = null;
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
          print("SKIPPING assignment filter spec b/c env.js doesn't work with SELECTs");
        } else {
          it("filters the activities to delay based on the selected assignment", function () {
            $('#delay-assignment').val("GRID-NU_07A0");
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

      describe("arbitrary subsets", function () {
        function testActivityId(mode) {
          if (mode === 'conditional') {
            return 'Co';
          } else {
            return mode.substring(0, 1).toUpperCase();
          }
        }
        
        before(function () {
          var response = { "days": { } };
          response["days"]["2008-08-25"] = { "activities": [
            testActivity("S",    "2008-08-25", "scheduled"),
            testActivity("Co",   "2008-08-25", "conditional"),
            testActivity("O",    "2008-08-25", "occurred"),
            testActivity("C",    "2008-08-25", "canceled"),
            testActivity("M",    "2008-08-25", "missed"),
            testActivity("N",    "2008-08-25", "NA"),
            testActivity("07A0", "2008-08-25", "scheduled", "NU 07A0")
          ] };
          response["days"]["2009-05-01"] = { "activities": [
            testActivity("S01", "2009-05-01", "scheduled"),
            testActivity("C01", "2009-05-01", "canceled")
          ] };

          var future = new Date();
          future.setTime(future.getTime() + 2 * 86400 * 1000);
          var futureS = psc.tools.Dates.utcToApiDate(future);
          response["days"][futureS] = { "activities": [
            testActivity("Sfuture", futureS, "scheduled"),
            testActivity("Cfuture", futureS, "canceled")
          ] };

          psc.subject.ScheduleData = {
            current: function () {
              return new psc.subject.Schedule(response);
            }
          }

          $('input.event').attr('checked', '');
          psc.subject.ScheduleList.init();
          $('#schedule').trigger('schedule-ready');

          $('#mark-select-assignment').val('');
          $('#mark-activities-count').text('');

          psc.subject.RealScheduleControls.init();
        });

        describe("bulk selector links", function () {
          
          if (psc.test.envjs()) {
            print("SKIPPING bulk selector tests because env.js doesn't handle SELECTs correctly")
          } else {
          
          it("can select all activities", function () {
            $('#mark-select-all').click();
            expect($('input.event:checked').length).to(equal, 11);
          });

          it("can select no activities", function () {
            $('input.event').val(["C"]);
            expect($('input.event:checked').length).to(equal, 1);
            $('#mark-select-none').click();
            expect($('input.event:checked').length).to(equal, 0);
          });

          it("can select conditional activities", function () {
            $('#mark-select-conditional').click();
            expect($('input.event:checked').length).to(equal, 1);
            expect($('input[value=Co]')).to(match_selector, ':checked');
          });

          it("can select past-due activities", function () {
            $('#mark-select-past-due').click();
            expect($('input.event:checked').length).to(equal, 4);
            expect($('input[value=S]')).to(match_selector, ":checked");
            expect($('input[value=Co]')).to(match_selector, ":checked");
            expect($('input[value=07A0]')).to(match_selector, ":checked");
            expect($('input[value=S01]')).to(match_selector, ":checked");
            expect($('input[value=Sfuture]')).to_not(match_selector, ":checked");
          });

          it("limit selection by assignment", function () {
            $('#mark-select-assignment').val('GRID-NU_07A0');
            $('#mark-select-all').click();
            expect($('input.event:checked').length).to(equal, 1);
            expect($('input[value=07A0]')).to(match_selector, ":checked");
          });

          it("accumulate selected items", function () {
            $('#mark-select-conditional').click();
            $('#mark-select-assignment').val('GRID-NU_07A0');
            $('#mark-select-all').click();
            expect($('input.event:checked').length).to(equal, 2);
          });
          
          }
        });

        describe("activity count", function () {
          if (psc.test.envjs()) {
            print("SKIPPING activity count tests because env.js doesn't handle SELECTs or radios correctly")
          } else {

          function expectOneCheckedMessage() {
            expect($('#mark-activities-count').text()).
              to(equal, 'There is currently 1 activity checked.');
          }
          
          describe("text", function () {
            it("is singular for one selected", function () {
              $('input[value=S]').click();
              expectOneCheckedMessage();
            });

            it("is plural for zero selected", function () {
              $('input[value=S]').click().click();
              expect($('#mark-activities-count').text()).
                to(equal, 'There are currently no activities checked.');
            });

            it("is plural for multiple selected", function () {
              $('input[value=S]').click();
              $('input[value=S01]').click();
              expect($('#mark-activities-count').text()).
                to(equal, 'There are currently 2 activities checked.');
            });
          });

          it("is updated from a direct click", function () {
            $('input[value=S]').click();
            expectOneCheckedMessage();
          });

          it("is updated from a selector click", function () {
            $('#mark-select-conditional').click();
            expectOneCheckedMessage();
          });
          
          }
        });

        if (false) { 
          // disable these tests because jQuery doesn't simulate the change
          // event when val is invoked
          describe("mutable form", function () {
            before(function () {
              $('#mark-new-mode').val('move-date-only');
            });

            it("shows the shift piece for 'Leave the state the same'", function () {
              $('#mark-new-mode').val('move-date-only');
              expect($('#mark-date-group')).to(match_selector, ':visible');
            });

            it("shows the shift piece for 'Mark scheduled'", function () {
              $('#mark-new-mode').val('scheduled');
              expect($('#mark-date-group')).to(match_selector, ':visible');
            });

            it("does not show the shift piece for 'Mark occurred'", function () {
              $('#mark-new-mode').val('occurred');
              expect($('#mark-date-group')).to_not(match_selector, ':visible');
            });

            it("does not show the shift piece for 'Mark canceled'", function () {
              $('#mark-new-mode').val('canceled-or-na');
              expect($('#mark-date-group')).to_not(match_selector, ':visible');
            });

            it("does not show the shift piece for 'Mark missed'", function () {
              $('#mark-new-mode').val('missed');
              expect($('#mark-date-group')).to_not(match_selector, ':visible');
            });
          });
        }

        if (psc.test.envjs()) {
          print("SKIPPING invocation param tests because env.js doesn't handle SELECTs or radios correctly")
        } else {

        describe("invocation parameter list", function () {
          var actualParams;

          function actual() {
            return actualParams = actualParams || psc.subject.RealScheduleControls.computeMarkParameters();
          }

          before(function () {
            $('input.event').attr('checked', '');
            $('#mark-delay-or-advance').val('1');
            $('#mark-delay-amount').val(5);
            actualParams = null;
          });

          it("is null when nothing's checked", function () {
            expect(actual()).to(equal, null);
          });

          describe("date shifting only", function () {
            before(function () {
              $('#mark-new-mode').val('move-date-only');
            });

            it("delays if that's selected", function () {
              $('input[value=S]').click();
              $('#mark-delay-or-advance').val('1')
              expect(actual()['S']['date']).to(equal, '2008-08-30');
            });

            it("advances if that's selected", function () {
              $('input[value=S]').click();
              $('#mark-delay-or-advance').val('-1');
              expect(actual()['S']['date']).to(equal, '2008-08-20');
            });

            it("shifts only checked items", function () {
              $('input[value=S]').click();
              expect(actual()['S01']).to(equal, undefined);
            });

            it("shifts scheduled activities", function () {
              $('input[value=S01]').click();
              expect(actual()['S01']['state']).to(equal, 'scheduled');
            });

            it("shifts conditional activities", function () {
              $('input[value=Co]').click();
              expect(actual()['Co']['state']).to(equal, 'conditional');
            });

            $.each(['missed', 'canceled', 'occurred', 'NA'], function () {
              var mode = this;
              var id = testActivityId(mode);
              it("does not shift " + mode + " activities", function () {
                $('input[value=' + id + ']').click();
                expect(actual()).to(equal, null);
              });
            });
          });

          describe("mark scheduled", function () {
            before(function () {
              $('#mark-new-mode').val('scheduled');
            });

            it("includes the reason", function () {
              $('input[value=S01]').click();
              $('#mark-reason').val('Staycation');
              expect(actual()['S01']['reason']).to(equal, 'Staycation');
            });

            it("shifts the date", function () {
              $('input[value=S]').click();
              expect(actual()['S']['date']).to(equal, '2008-08-30');
            });

            it("sets the state to scheduled", function () {
              $('input[value=Co]').click();
              expect(actual()['Co']['state']).to(equal, 'scheduled');
            });
          });

          describe("mark canceled or NA", function () {
            before(function () {
              $('#mark-new-mode').val('canceled-or-na');
            });

            it("does not shift the date", function () {
              $('input[value=S]').click();
              expect(actual()['S']['date']).to(equal, '2008-08-25');
            });

            $.each(['missed', 'occurred', 'scheduled', 'canceled'], function (i, v) {
              var mode = v;
              var id = testActivityId(mode);
              it("converts " + mode + " activities to canceled", function () {
                $('input[value=' + id + ']').click();
                expect(actual()[id]['state']).to(equal, 'canceled');
              });
            });

            $.each(['conditional', 'NA'], function (i, v) {
              var mode = v;
              var id = testActivityId(mode);
              it("converts " + mode + " activities to NA", function () {
                $('input[value=' + id + ']').click();
                expect(actual()[id]['state']).to(equal, 'NA');
              });
            });
          });

          $.each(['occurred', 'missed'], function (i, v) {
            var targetMode = v;
            describe("mark " + targetMode, function () {
              before(function () {
                $('#mark-new-mode').val(targetMode);
              });

              it("does not shift the date", function () {
                $('input[value=S]').click();
                expect(actual()['S']['date']).to(equal, '2008-08-25');
              });

              $.each(['scheduled', 'occurred', 'canceled', 'missed', 'conditional', 'NA'], function (i, v) {
                var mode = v;
                var id = testActivityId(mode);
                it("converts " + mode + " activities to " + targetMode, function () {
                  $('input[value=' + id + ']').click();
                  expect(actual()[id]['state']).to(equal, targetMode);
                });
              })
            });
          });
        });

        }
      });
    });
  }(jQuery));
});