/*global Screw before after describe it expect equal have_length */

require_spec('spec_helper.js');

if (psc.test.envjs()) {
  print("SKIPPING controls_spec until I can figure out why jquery.query won't load in Rhino");
} else {

require_main('jquery/jquery.query.js');
require_main('resig-templates.js');
require_main('psc-tools/misc.js');

require_main('schedule-preview/parameters.js');
require_main('schedule-preview/controls.js');

Screw.Unit(function () {
  (function ($) {
    describe("initialization", function () {
      describe("with a known segment", function () {
        before(function () {
          psc.schedule.preview.Controls.init("segment[a]=GRID-A&start_date[a]=2009-03-04");
        });

        it("creates entries in the list", function () {
          expect($('#preview-segments li.preview-segment').length).to(equal, 1);
        });

        it("inserts the entries before the control block", function () {
          expect($('#preview-segments li:first')).to(match_selector, '.preview-segment-GRID-A');
        });

        it("hides the pending message", function () {
          expect($('#refresh-preview-control .notice')).to(match_selector, ':hidden');
        });

        describe("of a list element", function () {
          var li;

          before(function () {
            li = $('#preview-segments li.preview-segment:first-child');
          });

          it("displays the name", function () {
            expect(li.find('.segment-name').text()).to(equal, 'Treatment: A');
          });

          it("displays the date", function () {
            expect(li.find('.segment-date .date').text()).to(equal, "03/04/2009");
          });

          it("knows the id", function () {
            expect(li.data('id')).to(equal, 'GRID-A');
          });
          
          it("is not newly-added", function () {
            expect(li).to_not(match_selector, '.newly-added');
          })
        });
      });

      describe("with an unknown segment", function () {
        before(function () {
          psc.schedule.preview.Controls.init("segment[a]=FAIL&start_date[a]=2009-03-04");
        });

        it("still creates a list entry", function () {
          expect($('#preview-segments li.preview-segment').length).to(equal, 1);
        });

        it("does not know the name", function () {
          expect($('#preview-segments li.preview-segment:first-child .segment-name').text()).
            to(equal, "Unknown");
        });

        it("notes that it does not know the name", function () {
          expect($('#preview-segments li.preview-segment:first-child')).
            to(match_selector, ".unknown");
        });
      });
    });

    describe("add", function () {
      before(function () {
        psc.schedule.preview.Controls.init("segment[a]=GRID-B&start_date[a]=2009-03-08");

        $('#next-segment input.date').val("7/12/2009");
        $('#next-segment select option[value=GRID-FU]').attr('selected', true);
        $('#next-segment #add-button').click();
      })

      it("adds a new entry", function () {
        expect($('#preview-segments li.preview-segment').length).to(equal, 2);
      });

      it("updates the parameters", function () {
        expect(psc.schedule.preview.Parameters.requestedSegments()).to(equal, [
          { segment: "GRID-B", start_date: "2009-03-08" },
          { segment: "GRID-FU", start_date: "2009-07-12" }
        ]);
      });

      it("shows the pending message", function () {
        expect($('#refresh-preview-control .notice')).to(match_selector, ':visible');
      });

      describe("the new list entry", function() {
        var newLi;

        before(function () {
          newLi = $('#preview-segments li.preview-segment:last')
        });

        it("has the name", function () {
          expect(newLi.find('.segment-name').text()).to(equal, "Follow up");
        });

        it("has the date", function () {
          expect(newLi.find('.segment-date .date').text()).to(equal, "07/12/2009");
        });

        it("has the id", function () {
          expect(newLi.data('id')).to(equal, "GRID-FU");
        });

        it("is before the controls", function () {
          expect(newLi.next().attr('id')).to(equal, 'next-segment')
        });

        it("is flagged as new", function () {
          expect(newLi).to(match_selector, '.newly-added');
        });
      });
    });

    describe("remove", function () {
      describe("cannot happen to the last segment", function () {
        before(function () {
          psc.schedule.preview.Controls.init("segment[a]=GRID-B&start_date[a]=2009-03-08");
        });

        it("disables the control", function () {
          expect($('#preview-segments li .remove.control :button')).to(match_selector, ':disabled');
        });
      });

      describe("of an element", function() {
        before(function () {
          psc.schedule.preview.Controls.init("segment[a]=GRID-B&start_date[a]=2009-03-08&segment[4]=GRID-A&start_date[4]=2009-02-03");

          $('li.preview-segment-GRID-A .remove.control :button').click()
        });

        it("marks the block as removed", function () {
          expect($('.preview-segment-GRID-A')).to(match_selector, '.removed');
        });

        it("changes the remove button into a restore button", function () {
          expect($('.preview-segment-GRID-A :button').val()).to(equal, 'Restore');
        });

        it("shows the you-need-to-update message", function () {
          expect($('#refresh-preview-control .notice')).to(match_selector, ':visible');
        });

        it("removes the element from the parameters", function () {
          expect(psc.schedule.preview.Parameters.requestedSegments()).to(equal, [
            { segment: 'GRID-B', start_date: '2009-03-08' }
          ]);
        });
        
        it("prevents the other from being removed", function () {
          expect($('.preview-segment-GRID-B .remove :button')).to(match_selector, ':disabled');
        });

        describe("being restored", function () {
          before(function () {
            $('li.preview-segment-GRID-A .remove.control :button').click()
          });

          it("changes the button text back to a remove button", function () {
            expect($('.preview-segment-GRID-A :button').val()).to(equal, "Remove");
          });

          it("removes the .removed tag", function () {
            expect($('.preview-segment-GRID-A')).to_not(match_selector, '.removed');
          });

          /* Pending: this requires changes in psc.s.p.Parameters.
          it("hides the you-need-to-update message if that was the only remove", function () {
            expect($('#refresh-preview-control .notice')).to(match_selector, ':hidden');
          });
          */

          it("restores with the correct date", function () {
            expect(psc.schedule.preview.Parameters.requestedSegments()).to(equal, [
              { segment: 'GRID-A', start_date: '2009-02-03' },
              { segment: 'GRID-B', start_date: '2009-03-08' }
            ]);
          });
        });
      });
    });

    describe("refresh", function () {
      before(function () {
        psc.schedule.preview.Controls.init("segment[a]=GRID-B&start_date[a]=2009-03-08&segment[4]=GRID-A&start_date[4]=2009-02-03");
        $('.preview-segment-GRID-B .remove :button').click();
        
        $('#next-segment input.date').val("7/12/2009");
        $('#next-segment select option[value=GRID-FU]').attr('selected', true);
        $('#next-segment #add-button').click();
      });
      
      it("should trigger ScheduleData.refresh", function () {
        var triggered = false;
        $('#schedule').bind('schedule-load-start', function () {
          triggered = true;
        });
        
        $('#refresh-preview-control :button').click();
        
        expect(triggered).to(be_true);
      });
      
      describe("on success", function () {
        before(function () {
          $('#schedule').trigger('schedule-ready');
        });
        
        it("clears all pending removes", function () {
          expect($('.preview-segment-GRID-B').length).to(equal, 0);
        });

        it("clears all pending adds", function () {
          expect($('#preview-segments .newly-added').length).to(equal, 0);
        });
        
        it("hides the refresh prompt", function () {
          expect($('#refresh-preview-control .notice')).to(match_selector, ':hidden');
        });
        
        // Note that the bracket escaping behavior is different on FF vs. IE & Safari
        describe("URL hash updates", function () {
          it("retains retained segments", function () {
            expect(window.location.hash).to(match, /segment(\[|%5B)4(\]|%5D)=GRID-A/);
          });
          
          it("adds the newly added", function () {
            expect(window.location.hash).to(match, /segment(\[|%5B).(\]|%5D)=GRID-FU/);
          });
          
          it("removes the removed", function () {
            expect(window.location.hash).to_not(match, /segment(\[|%5B).(\]|%5D)=GRID-B/);
          })
        })
      });
      
      describe("on error", function () {
        before(function () {
          $('#schedule').trigger('schedule-error');
        });
        
        it("retains the pending removes", function () {
          expect($('#preview-segments .removed').length).to(equal, 1);
        });
        
        it("retains the pending adds", function () {
          expect($('#preview-segments .newly-added').length).to(equal, 1);
        });
      });
    });
  }(jQuery));
});

} // end env detect