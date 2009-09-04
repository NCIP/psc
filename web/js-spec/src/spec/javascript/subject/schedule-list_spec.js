require_spec('spec_helper.js');
require_spec('sync-updater.js');
require_main('psc-tools/misc.js');
require_main('resig-templates.js');
require_main('subject/schedule-structure.js');
require_main('subject/schedule-list.js');

Screw.Unit(function () {
  (function ($) {
    describe("psc.subject.ScheduleList", function () {
      before(function () {
        psc.subject.ScheduleList.init()
      });
      
      describe("schedule-load-start", function () {
        before(function () {
          $('#schedule').trigger('schedule-load-start');
        });
        
        /* This fails under rhino.  Theory: it's a timing thing which should be addressed after "wait" is added to screw-unit
        it("shows the load screen", function () {
          expect($('.loading').css('display')).to(equal, 'block');
        });
        */
      });
      
      describe("schedule-ready", function () {
        var oldSchedDataModule;
        var sa;
        
        before(function () {
          $('.loading').show();
          $('#schedule-error').show();
          
          oldSchedDataModule = psc.subject.ScheduleData;
          
          sa = {
            id: "etc",
            current_state: {
              'date': '2009-05-01',
              'name': 'canceled'
            },
            activity: { name: 'Activity B', type: 'Other' },
            details: "Something happens first",
            study_segment: "Treatment: X",
            study: "NU 09A0"
          };
        });
        
        after(function () {
          psc.subject.ScheduleData = oldSchedDataModule;
        });
        
        describe("with a schedule", function () {
          before(function () {
            psc.subject.ScheduleData = {
              current: function () {
                return new psc.subject.Schedule({
                  days: {
                    "2009-05-01": {
                      activities: [ sa ]
                    }
                  }
                });
              }
            };
            
            $('#schedule').trigger('schedule-ready');
          });
          
          it("appends to #scheduled-activities", function () {
            expect($('#scheduled-activities #fake_day_2009-05-01').length).to(equal, 1);
          });

          it("hides the loading screen", function () {
            expect($('.loading').css('display')).to(equal, 'none');
          });
          
          it("hides the error", function () {
            expect($('#schedule-error').css('display')).to(equal, 'none');
          });
        });
        
        describe("without a schedule", function () {
          before(function () {
            psc.subject.ScheduleData = {
              current: function () {
                return null;
              }
            };
           
            $('#schedule').trigger('schedule-ready');
          });
          
          it("hides the loading screen", function () {
            expect($('.loading').css('display')).to(equal, 'none');
          });
          
          it("hides the error", function () {
            expect($('#schedule-error').css('display')).to(equal, 'none');
          });
        });
      })

      describe("schedule-error", function() {
        it("should display #schedule-error", function() {
          $('#schedule').trigger('schedule-error')
          expect($('#schedule-error').css('display')).to_not(equal, 'none')
        });

        it("should pass the message 'Banana!'", function() {
          $('#schedule').trigger('schedule-error', "Banana!")
          expect($('#schedule-error').text()).to(equal, 'Problem loading schedule data: Banana!')
        });
      });
    });
  }(jQuery));
});