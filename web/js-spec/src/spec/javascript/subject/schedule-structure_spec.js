/*global Screw before after describe it expect equal have_length */
/*global psc josephine equal_date */

require_spec('spec_helper.js');
require_spec('sample-data/josephine-schedule.js');
require_main('subject/schedule-structure.js');
require_main('psc-tools/misc.js');

Screw.Unit(function () {
  var j;
  
  before(function () {
    j = new psc.subject.Schedule(josephine()); // test data from josephine-schedule.js
  });

  describe("psc.subject.Schedule", function () {
    it("includes the source activities", function () {
      expect(j['days']['2009-04-25']['activities']).to(have_length, 2);
    });

    describe("source activity enhancements", function () {
      var response, sched;
      var future = ((new Date()).getFullYear() + 1) + "-05-25";
      var todayD = new Date()
      var today = todayD.getUTCFullYear() + '-' + (todayD.getUTCMonth() + 1) + '-' + todayD.getUTCDate();
      
      before(function () {
        response = { "days": { } };
        response["days"]["2008-04-25"] = { "activities": [
          {
            "current_state": {
              "name": "occurred",
              "date": "2008-04-25"
            },
            "study": "NU 1400",
            "activity": {
              "name": "Activity O",
              "type": "Other"
            },
            "study_segment": "Treatment: A",
            "id": "foo"
          },
          {
            "current_state": {
              "name": "scheduled",
              "date": "2008-04-25"
            },
            "study": "NU 1400",
            "activity": {
              "name": "Activity S",
              "type": "Other"
            },
            "study_segment": "Treatment: A"
          },
          {
            "current_state": {
              "name": "conditional",
              "date": "2008-04-25"
            },
            "study": "NU 1400",
            "activity": {
              "name": "Activity C",
              "type": "Other"
            },
            "study_segment": "Treatment: A"
          },
          {
            "current_state": {
              "name": "canceled",
              "date": "2008-04-25"
            },
            "study": "NU 1400",
            "activity": {
              "name": "Activity C",
              "type": "Other"
            },
            "study_segment": "Treatment: A"
          },
          {
            "current_state": {
              "name": "missed",
              "date": "2008-04-25"
            },
            "study": "NU 1400",
            "activity": {
              "name": "Activity M",
              "type": "Other"
            },
            "study_segment": "Treatment: A"
          },
          {
            "current_state": {
              "name": "NA",
              "date": "2008-04-25"
            },
            "study": "NU 1400",
            "activity": {
              "name": "Activity N",
              "type": "Other"
            },
            "study_segment": "Treatment: A"
          }
        ] };
        response["days"][today] = { "activities": [
          {
            "current_state": {
              "name": "scheduled",
              "date": today
            },
            "study": "NU 1400",
            "study_segment": "Treatment: A",
            "activity": {
              "name": "Activity 7",
              "type": "Other"
            }
          }
        ] };
        response["days"][future] = { "activities": [
          {
            "current_state": {
              "name": "scheduled",
              "date": future
            },
            "study": "NU 1400",
            "study_segment": "Follow up",
            "activity": {
              "name": "Activity Q",
              "type": "Other"
            }
          },
          {
            "current_state": {
              "name": "conditional",
              "date": future
            },
            "study": "NU 1400",
            "study_segment": "Follow up",
            "activity": {
              "name": "Activity Q",
              "type": "Other"
            }
          }
        ] };
        response["days"]["2009-01-01"] = { "activities": [
          {
            "current_state": { "name": "scheduled", "date": "2009-01-01" },
            "details": "Some details",
            "condition": "All the time",
            "labels": "foo bar",
            "activity": { "name": "Everything" }
          },
          {
            "current_state": { "name": "scheduled", "date": "2009-01-01" },
            "details": "Some other details",
            "activity": { "name": "Details only" }
          },
          {
            "current_state": { "name": "scheduled", "date": "2009-01-01" },
            "condition": "Once or twice",
            "activity": { "name": "Condition only" }
          },
          {
            "current_state": { "name": "scheduled", "date": "2009-01-01" },
            "labels": "foo",
            "activity": { "name": "Labels only" }
          },
          {
            "current_state": { "name": "scheduled", "date": "2009-01-01" },
            "activity": { "name": "No notes" }
          },
        ] }
        sched = new psc.subject.Schedule(response);
      });
      
      it("includes currentDate()", function () {
        expect(sched.days['2008-04-25'].activities[0].currentDate()).to(equal_utc_date, new Date(Date.UTC(2008, 3, 25)));
      });

      describe("dateClasses()", function () {
        it("includes the actual date class", function () {
          expect(sched.days['2008-04-25'].activities[0].dateClasses()).to(match, 'date-2008-04-25');
        });
        
        it("includes today for today", function () {
          expect(sched.days[today].activities[0].dateClasses()).to(match, 'today');
        });
        
        it("does not includes today for other days", function () {
          expect(sched.days['2008-04-25'].activities[0].dateClasses()).to_not(match, 'today');
        });
      });
      
      it("includes studyClass()", function () {
        expect(sched.days['2008-04-25'].activities[0].studyClass()).to(equal, 'study-NU_1400');
      });

      describe("isNull()", function () {
        before(function () {
           response["days"]={};
        });

        it("returns null when dates are null", function() {
          expect(sched.days).to(equal, {});
        });
                    
      })
      
      describe("isOpen()", function () {
        var open = [['scheduled', 1], ['conditional', 2]];
        var closed = [['occurred', 0], ['canceled', 3], ['missed', 4], ['NA', 5]];

        jQuery.each(open, function(i, pair) {
          it("is true for " + pair[0], function () {
            expect(sched.days['2008-04-25'].activities[pair[1]].isOpen()).to(equal, true);
          });
        });

        jQuery.each(closed, function(i, pair) {
          it("is false for " + pair[0], function () {
            expect(sched.days['2008-04-25'].activities[pair[1]].isOpen()).to(equal, false);
          });
        });
      });
      
      describe("isToday()", function () {
        it("is true for today", function () {
          expect(sched.days[today].activities[0].isToday()).to(equal, true)
        });

        it("is false for other days", function () {
          expect(sched.days["2008-04-25"].activities[0].isToday()).to(equal, false)
        });
      });
      
      describe("hasId()", function () {
        it("is true when it does", function () {
          expect(sched.days['2008-04-25'].activities[0].hasId()).to(be_true)
        });

        it("is false when it doesn't", function () {
          expect(sched.days['2008-04-25'].activities[1].hasId()).to(be_false)
        });
      });
      
      describe("planNotes()", function () {
        it("includes the details when present", function () {
          expect(sched.days['2009-01-01'].activities[1].planNotes()).to(
            equal, "Some other details");
        });

        it("includes the condition when present", function () {
          expect(sched.days['2009-01-01'].activities[2].planNotes()).to(
            equal, "Once or twice");
        });

        it("includes the labels when present", function () {
          expect(sched.days['2009-01-01'].activities[3].planNotes()).to(
            equal, "Labels: foo");
        });

        it("joins the elements with semi-colons when multiple are present", function () {
          expect(sched.days['2009-01-01'].activities[0].planNotes()).to(
            equal, "Some details; All the time; Labels: foo bar");
        });

        it("is null with no notes", function () {
          expect(sched.days['2009-01-01'].activities[4].planNotes()).to(
            equal, null);
        });
      });
      
      describe("stateClasses()", function () {
        var sa;
        
        function actualClasses() {
          return sa.stateClasses().split(/\s/);
        }
        
        var allClasses = "scheduled-activity scheduled occurred canceled missed conditional na past-due open closed";
        var expectations = [
          {
            describe: 'for a past scheduled activity',
            date: '2008-04-25', index: 1,
            expected: 'scheduled-activity scheduled past-due open'
          }, {
            describe: 'for a future scheduled activity',
            date: future, index: 0,
            expected: 'scheduled-activity scheduled open'
          }, {
            describe: 'for a past conditional activity',
            date: '2008-04-25', index: 2,
            expected: 'scheduled-activity conditional past-due open'
          }, {
            describe: 'for a future conditional activity',
            date: future, index: 1,
            expected: 'scheduled-activity conditional open'
          }, {
            describe: 'for an occurred activity',
            date: '2008-04-25', index: 0,
            expected: 'scheduled-activity occurred closed'
          }, {
            describe: 'for a missed activity',
            date: '2008-04-25', index: 4,
            expected: 'scheduled-activity missed closed'
          }, {
            describe: 'for a canceled activity',
            date: '2008-04-25', index: 3,
            expected: 'scheduled-activity canceled closed'
          }, {
            describe: 'for an NA activity',
            date: '2008-04-25', index: 5,
            expected: 'scheduled-activity na closed'
          }
//          ,
//          {
//            describe: 'for a null scheduled activity',
//            date: null, index: 0,
//            expected: 'null'
//          }
        ];
        
        jQuery.each(expectations, function (idx, item) {
          describe(item.describe, function () {
            before(function () {
              sa = sched['days'][item.date]['activities'][item.index];
            });
            
            var expected = item.expected.split(' ');
            var notExpected = allClasses.split(' ');
            for (var j = 0 ; j < expected.length ; j += 1) {
              notExpected.splice(jQuery.inArray(expected[j], notExpected), 1);
            }
            
            jQuery.each(expected, function (idx, a) {
              it("includes " + a, function () {
                expect(actualClasses()).to(include, a);
              });
            });
            
            jQuery.each(notExpected, function (idx, a) {
              it("does not include " + a, function () {
                expect(actualClasses()).to_not(include, a);
              });
            });
          });
        });
      });
    });

    describe("day list", function () {
      var sched;
      
      before(function () {
        sched = new psc.subject.Schedule({ 'days': { '2009-04-03': {}, '2009-04-08': {} } })
      });
      
      it("contains all days", function () {
        expect(sched.allDays()).to(have_length, 6)
      });
      
      it("is in order", function () {
        expect(sched.allDays()[0]).to(equal_utc_date, new Date(Date.UTC(2009, 3, 3)));
        expect(sched.allDays()[1]).to(equal_utc_date, new Date(Date.UTC(2009, 3, 4)));
        expect(sched.allDays()[2]).to(equal_utc_date, new Date(Date.UTC(2009, 3, 5)));
        expect(sched.allDays()[3]).to(equal_utc_date, new Date(Date.UTC(2009, 3, 6)));
        expect(sched.allDays()[4]).to(equal_utc_date, new Date(Date.UTC(2009, 3, 7)));
        expect(sched.allDays()[5]).to(equal_utc_date, new Date(Date.UTC(2009, 3, 8)));
      });
    });

    describe("segment enhancements", function () {
      var a;

      before(function () {
        a = j.study_segments[0];
      });

      describe("assignmentName()", function () {
        it("is the assignment name when present", function () {
          expect(a.assignmentName()).to(equal, "NU 1400 (1)");
        });
        
        it("is the study name otherwise", function () {
          a.assignment = null;
          expect(a.assignmentName()).to(equal, "NU 1400");
        });
      });
      
      describe("startDate()", function () {
        it("is the UTC Date version of the response start date", function () {
          expect(a.startDate()).to(equal_utc_date, new Date(Date.UTC(2009, 3, 22)));
        });

        it("returns null when converts null to a UTC date", function () {
        expect( psc.tools.Dates.apiDateToUtc(null)).to(
          equal, null);
        });
      });

      describe("stopDate()", function () {
        it("is the UTC Date version of the response stop date", function () {
          expect(a.stopDate()).to(equal_utc_time, new Date(Date.UTC(2009, 5, 5, 23, 59, 59, 999)));
        })
      });

    });
  });
});
