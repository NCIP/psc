require_spec('spec_helper.js');
require_main('manage-period/model.js');
require_main('manage-period/grid-controls.js');

Screw.Unit(function () {
  (function ($) {
    describe("Grid controls", function () {
      var actionStarted, actionError, actionChanged;

      before(function () {
        psc.template.mpa.GridControls.init();

        $('#days').bind('action-started', function (evt, data) {
          actionStarted = data;
        });
        $('#days').bind('action-error', function (evt, data) {
          actionError = data;
        });
        $('#days').bind('action-changed', function (evt, data) {
          actionChanged = data;
        });
      });

      after(function () {
        actionStarted = null;
        actionError = null;
      });

      function click(r, c) {
        $('#days tbody tr.activity:eq(' + r + ') td.cell:eq(' + c + ')').click();
      }

      describe("when action is add", function () {
        before(function () {
          $('#days').trigger('action-changed', {
            name: 'add',
            step: 0
          });
        });

        describe("when an empty cell is clicked", function () {
          before(function () {
            click(1, 3);
          });

          it("fires action-started", function () {
            expect(actionStarted).to_not(equal, undefined);
          });

          it("does not fire an error", function () {
            expect(actionError).to(equal, undefined);
          });

          describe("the start event", function () {
            it("includes the action information", function () {
              expect(actionStarted.action).to(equal, {
                name: 'add', step: 0
              });
            });

            it("includes the correct PA day", function () {
              expect(actionStarted.day).to(equal, 22);
            });

            describe("population data", function () {
              it("matches the selector", function () {
                $('#population-selector').val('Cd');
                click(1, 3);
                expect(actionStarted.population).to(equal, 'Cd');
              });

              if (psc.test.envjs()) {
                print("SKIPPING no population spec because SELECT behavior on env.js is broken");
              } else {
                it("is null when no population is selected", function () {
                  $('#population-selector').val('');
                  click(1, 3);
                  expect(actionStarted.population).to(equal, null);
                });
              }
            });

            it("includes the activity info", function () {
              expect(actionStarted.activity).to(equal, {
                name: 'EZ',
                code: 'ze',
                type: 'Disease measure',
                source: 'Arb'
              });
            });

            it("includes the correct details", function () {
              expect(actionStarted.details).to(equal, "");
            });

            it("includes the correct condition", function () {
              expect(actionStarted.condition).to(equal, "");
            });

            it("includes the correct labels", function () {
              expect(actionStarted.labels).to(equal, "foo research-billing");
            });
          });
        });

        describe("when an occupied cell is clicked", function () {
          before(function () {
            click(1, 4);
          });

          it("does not fire action-started", function () {
            expect(actionStarted).to(equal, undefined);
          });

          it("fires an error", function () {
            expect(actionError).to_not(equal, undefined);
          });

          describe("the error event", function () {
            it("includes the action", function () {
              expect(actionError.action).to(equal, {
                name: 'add', step: 0
              });
            });

            it("includes the message", function () {
              expect(actionError.message).to(equal,
                "There is already an activity in that row and column.");
            });
          });
        });

        describe("when an in-progress cell is clicked", function () {
          before(function () {
            $('#days tbody tr.activity:eq(1) td.cell:eq(3)').addClass('in-progress');
            click(1, 3);
          });

          it("does not fire an error", function () {
            expect(actionError).to(equal, null);
          });

          it("does not fire start", function () {
            expect(actionStarted).to(equal, null);
          });
        });
      });

      describe("when action is delete", function () {
        before(function () {
          $('#days').trigger('action-changed', {
            name: 'delete',
            step: 0
          });
        });

        describe("when an empty cell is clicked", function () {
          before(function () {
            click(2, 4);
          });

          it("does not fire action-started", function () {
            expect(actionStarted).to(equal, undefined);
          });

          it("fires an error", function () {
            expect(actionError).to_not(equal, undefined);
          });

          describe("the error event", function () {
            it("includes the action", function () {
              expect(actionError.action).to(equal, {
                name: 'delete', step: 0
              });
            });

            it("includes the message", function () {
              expect(actionError.message).to(equal,
                "There is no activity in that row and column.");
            });
          });
        });

        describe("when an occupied cell is clicked", function () {
          before(function () {
            $('#days tbody tr.activity:eq(3) td.cell:eq(0)').click();
          });

          it("fires action-started", function () {
            expect(actionStarted).to_not(equal, undefined);
          });

          it("does not fire an error", function () {
            expect(actionError).to(equal, undefined);
          });

          describe("the start event", function () {
            it("includes the action", function () {
              expect(actionStarted.action).to(equal, {
                name: 'delete', step: 0
              });
            });

            it("includes the row number", function () {
              expect(actionStarted.row).to(equal, 3);
            });

            it("includes the column number", function () {
              expect(actionStarted.column).to(equal, 0);
            });

            it("includes the planned activity URI", function () {
              expect(actionStarted.href).to(equal, "/etc/PAs/3_0");
            });
          });
        });
      });

      describe("when action is move", function () {
        describe("when the first click", function () {
          before(function () {
            $('#days').trigger('action-changed', {
              name: 'move',
              step: 0
            });
            // only capture events caused by the next action
            actionStarted = actionChanged = actionError = null;
          });

          describe("is in an empty cell", function () {
            before(function () {
              click(2, 4);
            });

            it("does not fire action-started", function () {
              expect(actionStarted).to(equal, undefined);
            });

            it("fires an error", function () {
              expect(actionError).to_not(equal, undefined);
            });

            describe("the error event", function () {
              it("includes the action", function () {
                expect(actionError.action).to(equal, {
                  name: 'move', step: 0
                });
              });

              it("includes the message", function () {
                expect(actionError.message).to(equal,
                  "Click on the activity you'd like to move, first.");
              });
            });
          });

          describe("is in an occupied cell", function () {
            before(function () {
              click(1, 4);
            });

            it("fires action-started", function () {
              expect(actionStarted).to_not(equal, undefined);
            });

            describe("the action start", function () {
              it("includes the row", function () {
                expect(actionStarted.row).to(equal, 1);
              });

              it("includes the column", function () {
                expect(actionStarted.column).to(equal, 4);
              });

              it("includes the resource href", function () {
                expect(actionStarted.href).to(equal, "/etc/PAs/1_4");
              });
            });

            it("fires action-changed", function () {
              expect(actionChanged).to(equal, {
                name: 'move',
                step: 1
              });
            });

            it("does not fire an error", function () {
              expect(actionError).to(equal, undefined);
            });
          });
        });

        describe("when the second click", function () {
          before(function () {
            // simulate successful first click
            $('#days').trigger('action-changed', { name: 'move', step: 0 });
            click(1, 4);

            // only capture events caused by the next action
            actionStarted = actionChanged = actionError = null;
          });

          describe("is in the same cell", function () {
            before(function () {
              click(1, 4);
            });

            it("fires an error", function () {
              expect(actionError).to_not(equal, null);
            });

            it("gives a useful error message", function () {
              expect(actionError.message).to(equal,
                "To move, click in an empty cell elsewhere in the row.  To cancel, click outside the grid.");
            });

            it("stays in the move mode", function () {
              expect(actionChanged).to(equal, null);
            });

            it("does not fire start", function () {
              expect(actionStarted).to(equal, null);
            });
          });

          describe("is in an occupied cell in the same row", function () {
            before(function () {
              click(1, 8);
            });

            it("fires an error", function () {
              expect(actionError).to_not(equal, null);
            });

            it("gives a useful error message", function () {
              expect(actionError.message).to(equal,
                "To move, click in an empty cell elsewhere in the row.  To cancel, click outside the grid.");
            });

            it("stays in the move mode", function () {
              expect(actionChanged).to(equal, null);
            });

            it("does not fire start", function () {
              expect(actionStarted).to(equal, null);
            });
          });

          describe("is in an empty cell in the same row", function () {
            before(function () {
              click(1, 6);
            });

            it("fires no error", function () {
              expect(actionError).to(equal, null);
            });

            it("resets to move step 0", function () {
              expect(actionChanged).to(equal, {
                name: 'move', step: 0
              });
            });

            it("fires start", function () {
              expect(actionStarted).to_not(equal, null);
            });

            describe("the fired start event", function () {
              it("includes the row", function () {
                expect(actionStarted.row).to(equal, 1);
              });

              it("includes the column", function () {
                expect(actionStarted.column).to(equal, 6);
              });

              it("includes the target day", function () {
                expect(actionStarted.day).to(equal, 43);
              });

              it("includes the start column", function () {
                expect(actionStarted.startColumn).to(equal, 4);
              });
              
              it("includes the href", function () {
                expect(actionStarted.href).to(equal, "/etc/PAs/1_4")
              });
            });
          });

          describe("is in a different row", function () {
            before(function () {
              click(2, 3);
            });

            it("fires an error", function () {
              expect(actionError).to_not(equal, null);
            });

            it("gives a useful error message", function () {
              expect(actionError.message).to(equal,
                "You can only move activities within the same row.");
            });

            it("resets to the first move step", function () {
              expect(actionChanged).to(equal, {
                name: 'move', step: 0
              });
            });

            it("does not fire start", function () {
              expect(actionStarted).to(equal, null);
            });
          });

          $(['#activities', '#notes', '#tool-details', 'body']).each(function (dc, sel) {
            describe("is in " + sel, function () {
              before(function () {
                $(sel).click()
              });

              it("does not fire an error", function () {
                expect(actionError).to(equal, null);
              });

              it("resets to the first move step", function () {
                expect(actionChanged).to(equal, {
                  name: 'move', step: 0
                });
              });

              it("does not fire start", function () {
                expect(actionStarted).to(equal, null);
              });
            });
          });

        });

      });

    });
  }(jQuery));
});
