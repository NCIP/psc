/**global psc jQuery require_spec require_main */

require_spec('spec_helper.js');
require_main('lightbox.js');
require_main('manage-period/model.js');
require_main('manage-period/activity-notes.js');

if (psc.test.envjs()) {
  print("SKIPPING activity-notes.js because prototype observers don't work consistently with env.js");
} else {

Screw.Unit(function () {
  (function ($) {
    describe("psc.template.mpa.ActivityNotes", function () {
      describe('preview', function () {
        before(function () {
          psc.template.mpa.ActivityNotes.updateNotePreview(1);
        });

        it("includes the details from the row", function () {
          expect($('#notes-preview #details-preview').text()).to(equal, 'Row 1 details');
        });

        it("includes the condition from the row", function () {
          expect($('#notes-preview #condition-preview').text()).to(equal, 'Row 1 condition');
        });

        it("includes the labels from the row", function () {
          expect($('#notes-preview #labels-preview').text()).to(equal, 'r1 label');
        });

        it("includes the weight from the row", function () {
          expect($('#notes-preview #weight-preview').text()).to(equal, '1');
        });
      });

      describe("editing", function () {
        before(function () {
          psc.template.mpa.ActivityNotes.init();
          $('.notes-edit:eq(0)').click();
        });

        describe("start", function () {
          it("shows the lightbox", function () {
            expect($('#lightbox')).to(match_selector, ':visible');
          });

          it("copies the details from the row", function () {
            expect($('#edit-notes-lightbox #edit-notes-details').val()).to(equal, "Row 0 details");
          });

          it("copies the condition from the row", function () {
            expect($('#edit-notes-lightbox #edit-notes-condition').val()).to(equal, "Row 0 condition");
          });

          it("copies the labels from the row", function () {
            expect($('#edit-notes-lightbox #edit-notes-labels').val()).to(equal, "r0 label");
          });

          it("copies the weight from the row", function () {
            expect($('#edit-notes-lightbox #edit-notes-weight').val()).to(equal, "0");
          });
        });

        describe("while editing", function () {
          it("updates the details in the row as the input is changed", function() {
            $('#edit-notes-details').val("I'd prefer these details");
            wait(function () {
              expect($('#notes tr.activity:eq(0) .details').text()).
                to(equal, "I'd prefer these details");
            }, 600);
          });

          it("updates the condition in the row as the input is changed", function() {
            $('#edit-notes-condition').val("4 gt 8");
            wait(function () {
              expect($('#notes tr.activity:eq(0) .condition').text()).
                to(equal, "4 gt 8");
            }, 600);
          });

          it("updates the labels in the row as the input is changed", function() {
            $('#edit-notes-labels').val("some other labels");
            wait(function () {
              expect($('#notes tr.activity:eq(0) .labels').text()).
                to(equal, "some other labels");
            }, 600);
          });

          it("updates the weight in the row as the input is changed", function() {
            $('#edit-notes-weight').val("4");
            wait(function () {
              expect($('#notes tr.activity:eq(0) .weight').text()).to(equal, "4");
            }, 600);
          });
        });

        describe("on completion", function () {
          var actionStarted;

          before(function () {
            $('#days').bind('action-started', function (evt, data) {
              actionStarted = data;
            });
            $('#notes tr.activity:eq(0)').
              find('.details').text('new details').end().
              find('.condition').text('new condition').end().
              find('.labels').text('nlabel').end().
              find('.weight').text('-4').end();

            print()

            $('#edit-notes-done').click();
          });

          it("hides the lightbox", function () {
            expect($('#lightbox')).to_not(match_selector, ':visible');
          });

          describe("the update-notes action", function () {
            it("is started", function () {
              expect(actionStarted.action.name).to(equal, 'update-notes');
            });

            it("includes the details in the data", function () {
              expect(actionStarted.details).to(equal, 'new details');
            });

            it("includes the condition in the data", function () {
              expect(actionStarted.condition).to(equal, 'new condition');
            });

            it("includes the details in the data", function () {
              expect(actionStarted.labels).to(equal, ['nlabel']);
            });

            it("includes the details in the data", function () {
              expect(actionStarted.weight).to(equal, '-4');
            });
          });
        })
      });
    });
  }(jQuery));
});

}