/*global Screw expect equal wait require_spec require_main it before after describe match match_selector */
/*global psc jQuery */
require_spec("spec_helper.js");
require_main("manage-period/presentation.js");

Screw.Unit(function () {
  (function ($) {
    describe("Presentation", function () {
      before(function () {
        psc.template.mpa.Presentation.init();
      });

      function cellSelector(block, r, c) {
        return "#" + block + " tr.activity:eq(" + r + ") td.cell:eq(" + c + ")";
      }

      describe("sync scrolling", function () {
        it("does not keep days and activities in horizontal sync", function () {
          $('#days').scrollLeft(80);
          wait(function () {
            expect($('#activities').scrollLeft()).to(equal, 0);
          }, 100);
        });

        // ^^^^
        // The order between these two specs matters for some reason.
        // ____

        it("keeps days and activities in vertical sync", function () {
          $('#days').scrollTop(80);
          wait(function () {
            expect($('#activities').scrollTop()).to(equal, 80);
          }, 100);
        });

        it("keeps days and notes in vertical sync", function () {
          $('#days').scrollTop(20);
          wait(function () {
            expect($('#notes').scrollTop()).to(equal, 20);
          }, 10);
        });

        it("does not keep days and notes in horizontal sync", function () {
          $('#days').scrollLeft(34);
          wait(function () {
            expect($('#activities').scrollLeft()).to(equal, 0);
          }, 10);
        });

        it("keeps days and the day header in horizontal sync", function () {
          $('#days').scrollLeft(121);
          wait(function () {
            expect($('#days-heading').scrollLeft()).to(equal, 121);
          }, 10);
        });

        it("does not keep days and notes in vertical sync", function () {
          $('#days').scrollTop(34);
          wait(function () {
            expect($('#days-heading').scrollTop()).to(equal, 0);
          }, 10);
        });
      });

      describe("toolbar selection", function () {
        it("defaults to add", function () {
          expect($('#tool-palette .selected')).to(match_selector, '#add-tool');
        });

        $.each(['move', 'add', 'delete'], function (dc, button) {
          function click() {
            $('#tool-palette #' + button + '-tool').click()
          }

          describe("of " + button, function () {
            var actionEventReceived = null;
            before(function () {
              if (button === 'add') { $('#move-tool').click(); }

              $('#days').bind('action-changed', function (evt, data) {
                actionEventReceived = data;
              });

              click();
            });

            it("marks the button as selected", function () {
              expect($('#tool-palette .selected')).to(match_selector,
                '#' + button + '-tool');
            });

            it("marks only that button as selected", function () {
              expect($('#tool-palette .selected').length).to(equal, 1);
            });

            it("fires the action-changed event", function () {
              expect(actionEventReceived.name).to(equal, button);
            });

            it("fires the action-changed event for the start of the action", function () {
              expect(actionEventReceived.step).to(equal, 0);
            });

            it("sets the action class on the body", function () {
              expect($('body')).to(match_selector, '.action-' + button);
            });

            it("clears any other action classes from the body", function () {
              expect($('body').attr('class').replace('action-' + button, '')).
                to_not(match, /action-/);
            });

            it("sets the step class on the body", function () {
              expect($('body')).to(match_selector, '.step-0');
            });

            it("clears any other action classes from the body", function () {
              expect($('body').attr('class').replace('step-0', '')).
                to_not(match, /step-/);
            });
          });
        });
      });

      describe("displaying tool description", function () {
        $.each(['move', 'add', 'delete'], function (dc, button) {
          describe("for " + button, function () {
            function changeAction(step) {
              $('#days').trigger("action-changed", {
                name: button,
                step: step
              });
            }

            before(function () {
              changeAction(0);
            });

            it("shows the help for " + button, function () {
              wait(function () {
                expect($('#tool-palette #' + button + '-tool-detail')).
                  to(match_selector, ':visible');
              }, 400);
            });

            it("hides the other help", function () {
              wait(function () {
                expect($('#tool-palette .tool-detail:visible').length).
                  to(equal, 1);
              }, 400);
            });

            if (button === 'move') {
              describe("step 0", function () {
                it('shows the first step of the help', function () {
                  wait(function () {
                    expect($('#tool-palette #' + button + '-tool-detail .step-0')).
                      to(match_selector, ':visible');
                  }, 400);
                });

                it('does not show the second step of the help', function () {
                  wait(function () {
                    expect($('#tool-palette #' + button + '-tool-detail .step-1')).
                      to_not(match_selector, ':visible');
                  }, 400);
                });
              });

              describe("step 1", function () {
                before(function () {
                  changeAction(1);
                });

                it('shows the second step of the help', function () {
                  wait(function () {
                    expect($('#tool-palette #' + button + '-tool-detail .step-1')).
                      to(match_selector, ':visible');
                  }, 400);
                });

                it('does not show the first step of the help', function () {
                  wait(function () {
                    expect($('#tool-palette #' + button + '-tool-detail .step-0')).
                      to_not(match_selector, ':visible');
                  }, 400);
                });
              });
            }
          });
        });
      });

      describe("encountering errors", function () {
        it("shows a reported action error in the message area", function () {
          $('#days').trigger('action-error', {
            message: 'Bad, bad, bad'
          });

          expect($('#message').text()).to(equal, 'Bad, bad, bad');
        })
      });

      describe("marking up in-progress actions", function () {
        describe("add", function () {
          describe("start", function () {
            function trigger(pop) {
              $('#days').trigger('action-started', {
                action: { name: 'add', step: 0 },
                cell: $(cellSelector('days', 1, 3))[0],
                row: 1,
                column: 3,
                population: pop
              });
            }

            after(function () {
              $(cellSelector('days', 1, 3)).removeClass('in-progress').empty();
            });

            it("marks the cell in-progress", function () {
              trigger();
              expect($(cellSelector('days', 1, 3))).
                to(match_selector, ".in-progress");
            });

            it("adds the marker", function () {
              trigger();
              expect($(cellSelector('days', 1, 3) + ' .marker').length).
                to(equal, 1);
            });

            it("respects the population if specified", function () {
              trigger('Cd');
              expect($(cellSelector('days', 1, 3) + ' .marker').text()).
                to(equal, 'Cd');
            });

            it("uses a times sign if no population specified", function () {
              trigger();
              expect($(cellSelector('days', 1, 3) + ' .marker').text()).
                to(equal, "×");
            });
          });

          describe("complete", function () {
            before(function () {
              var cell = $(cellSelector('days', 1, 3))[0]
              $(cell).addClass('in-progress').
                append("<div class='marker'>&215;</div>");
              $('#days').trigger('action-completed', {
                action: { name: 'add', step: 0 },
                cell: cell,
                row: 1,
                column: 3,
                href: "/foo/1_3"
              });
            });

            after(function () {
              $(cellSelector('days', 1, 3)).removeClass('in-progress').empty();
            });

            it("makes the cell no longer in-progress", function () {
              expect($(cellSelector('days', 1, 3))).
                to_not(match_selector, '.in-progress')
            });

            it("sets the marker's resource href", function () {
              expect($(cellSelector('days', 1, 3) + ' .marker').attr('resource-href')).
                to(equal, '/foo/1_3')
            });
          })

          describe("error", function () {
            function trigger() {
              $('#days').trigger('action-error', {
                action: { name: 'add', step: 0 },
                cell: $(cellSelector('days', 1, 3))[0],
                row: 1,
                column: 3
              });
            }

            after(function () {
              $(cellSelector('days', 1, 3)).empty().
                removeClass('in-progress').
                removeClass('error');
            });

            describe("when in progress", function () {
              before(function () {
                $(cellSelector('days', 1, 3)).addClass('in-progress').
                  append("<div class='marker'>E</div>");
                trigger();
              });

              it("makes the cell no longer in-progress", function () {
                expect($(cellSelector('days', 1, 3))).
                  to_not(match_selector, '.in-progress');
              });

              it("marks the cell error", function () {
                expect($(cellSelector('days', 1, 3))).to(match_selector, '.error')
              });

              it("removes the temporary marker", function () {
                expect($(cellSelector('days', 1, 3) + ' .marker').length).
                  to(equal, 0);
              });
            });

            describe("when not in progress", function () {
              before(function () {
                trigger();
              });

              it("does not mark an error", function () {
                expect($(cellSelector('days', 1, 3))).
                  to_not(match_selector, '.error')
              });
            });
          });
        });

        describe("move", function () {
          describe("step 0", function () {
            after(function () {
              $(cellSelector('days', 1, 4)).removeClass('in-progress').
                parent('tr').removeClass('moving')
            });

            describe("on start", function () {
              before(function () {
                $('#days').trigger('action-started', {
                  action: { name: 'move', step: 0 },
                  cell: $(cellSelector('days', 1, 4))[0],
                  row: 1,
                  column: 4
                });
              });

              it("marks the source cell in-progress", function () {
                expect($(cellSelector('days', 1, 4))).
                  to(match_selector, '.in-progress')
              });

              it("marks the source cell moving", function () {
                expect($(cellSelector('days', 1, 4))).
                  to(match_selector, '.moving')
              });

              it("marks the source row 'moving'", function () {
                expect($('#days tr.activity:eq(1)')).
                  to(match_selector, '.moving')
              });
            });

            describe("on cancel", function () {
              before(function () {
                // started move at 1, 4
                $(cellSelector('days', 1, 4)).addClass('in-progress').
                  parent('tr').addClass('moving');
                // unrelated pending action on 2, 6
                $(cellSelector('days', 2, 6)).addClass('in-progress');
                // unrelated pending action on 1, 8
                $(cellSelector('days', 1, 8)).addClass('in-progress');
                $('#days').trigger('action-changed', { name: 'add', step: 0 });
              });

              after(function () {
                $(cellSelector('days', 2, 6)).addClass('in-progress');
              })

              it("clears the in-progress flag for the moving element", function () {
                expect($(cellSelector('days', 1, 4))).
                  to_not(match_selector, '.in-progress')
              });

              it("does not clear the in-progress flag for arbitrary others", function () {
                expect($(cellSelector('days', 2, 6))).
                  to(match_selector, '.in-progress')
              });

              it("does not clear the in-progress flag for others in the same row", function () {
                expect($(cellSelector('days', 1, 8))).
                  to(match_selector, '.in-progress')
              });

              it("clears the movingness of the row", function () {
                expect($('#days tr.activity:eq(1)')).
                  to_not(match_selector, '.moving')
              });

              it("clears the movingness of the cell", function () {
                expect($(cellSelector('days', 1, 4))).
                  to_not(match_selector, '.moving')
              });
            });
          });

          describe("step 1", function () {
            describe("on start", function () {
              before(function () {
                $('#days tr.activity:eq(1)').addClass('moving').
                  find('td.cell:eq(4)').addClass('moving').addClass('in-progress');
                $('#days').trigger('action-started', {
                  action: { name: 'move', step: 1 },
                  cell: $(cellSelector('days', 1, 6))[0],
                  row: 1,
                  column: 6,
                  startColumn: 4
                });
                $('#days').trigger('action-changed', { name: 'move', step: 1 });
              });

              after(function () {
                $('#days tr.activity:eq(1)').removeClass('moving').
                  find('td:eq(4), td:eq(6)').removeClass('in-progress')
                var movedMarker = $(cellSelector('days', 1, 6) + ' .marker')[0]
                if (movedMarker) {
                  $(cellSelector('days', 1, 4)).append(movedMarker);
                }
              });

              it("preserves the in-progressness of the source cell", function () {
                expect($(cellSelector('days', 1, 4))).
                  to(match_selector, '.in-progress');
              });

              it("marks the destination cell in-progress", function () {
                expect($(cellSelector('days', 1, 6))).
                  to(match_selector, '.in-progress');
              });

              it("moves the marker to the destination cell", function () {
                expect($(cellSelector('days', 1, 4) + ' .marker').length).
                  to(equal, 0);
                expect($(cellSelector('days', 1, 6) + ' .marker').length).
                  to(equal, 1);
              });

              it("clears the movingness of the row", function () {
                expect($('#days tr.activity:eq(1)')).
                  to_not(match_selector, '.moving');
              });

              it("clears the movingness of the source cell", function () {
                expect($(cellSelector('days', 1, 4))).
                  to_not(match_selector, '.moving');
              });
            });

            describe("on complete", function () {
              before(function () {
                $(cellSelector('days', 1, 4)).addClass('in-progress');
                $(cellSelector('days', 1, 2)).addClass('in-progress');
                $('#days').trigger('action-completed', {
                  action: { name: 'move', step: 1 },
                  cell: $(cellSelector('days', 1, 2))[0],
                  row: 1,
                  column: 2,
                  startColumn: 4
                });
              });

              after(function () {
                $('#days tr.activity:eq(1) td').removeClass('in-progress')
              })

              it("marks the destination cell complete", function () {
                expect($(cellSelector('days', 1, 2))).
                  to_not(match_selector, '.in-progress');
              });

              it("marks the source cell complete", function () {
                expect($(cellSelector('days', 1, 4))).
                  to_not(match_selector, '.in-progress');
              });
            });

            describe("on error", function () {
              before(function () {
                var dest = $(cellSelector('days', 1, 6))[0];

                $(cellSelector('days', 1, 4)).addClass('in-progress');
                $(dest).addClass('in-progress');
                var marker = $(cellSelector('days', 1, 4) + ' .marker')[0];
                $(dest).empty().append(marker);

                $('#days').trigger('action-error', {
                  action: { name: 'move', step: 1 },
                  cell: dest,
                  row: 1,
                  column: 6,
                  startColumn: 4
                });
              });

              after(function () {
                $('#days tr.activity:eq(1)').removeClass('moving').
                  find('td:eq(4), td:eq(6)').removeClass('in-progress').removeClass('error')
                $(cellSelector('days', 1, 4)).empty().append(
                  $(cellSelector('days', 1, 6) + ' .marker')[0]);
              });

              it("returns the marker to the source cell", function () {
                expect($(cellSelector('days', 1, 4) + ' .marker').length).
                  to(equal, 1);
                expect($(cellSelector('days', 1, 6) + ' .marker').length).
                  to(equal, 0);
              });

              it("marks the destination cell error", function () {
                expect($(cellSelector('days', 1, 6))).
                  to_not(match_selector, '.in-progress')
                expect($(cellSelector('days', 1, 6))).
                  to(match_selector, '.error')
              });

              it("marks the source cell error", function () {
                expect($(cellSelector('days', 1, 4))).
                  to_not(match_selector, '.in-progress')
                expect($(cellSelector('days', 1, 4))).
                  to(match_selector, '.error')
              });
            })
          });

        });

        describe("delete", function () {
          describe("on start", function () {
            before(function () {
              $('#days').trigger('action-started', {
                action: { name: 'delete', step: 0 },
                cell: $(cellSelector('days', 1, 4))[0],
                row: 1,
                column: 4
              });
            });

            after(function () {
              $(cellSelector('days', 1, 4)).removeClass('in-progress');
            });

            it("marks the target cell in-progress", function () {
              expect($(cellSelector('days', 1, 4))).
                to(match_selector, '.in-progress');
            })
          });

          describe("on complete", function () {
            before(function () {
              var cell = $(cellSelector('days', 1, 4))[0];
              $(cell).addClass('in-progress');
              $('#days').trigger('action-completed', {
                action: { name: 'delete', step: 0 },
                cell: cell,
                row: 1,
                column: 4
              });
            });

            after(function () {
              $(cellSelector('days', 1, 4)).removeClass('in-progress').
                empty().append("<div class='marker'>&#215;</div>");
            });

            it("clears the in-progress status", function () {
              expect($(cellSelector('days', 1, 4))).
                to_not(match_selector, '.in-progress');
            });

            it("removes the marker", function () {
              expect($(cellSelector('days', 1, 4) + ' .marker').length).
                to(equal, 0);
            });
          });

          describe("on error", function () {
            function trigger() {
              $('#days').trigger('action-error', {
                action: { name: 'delete', step: 0 },
                cell: $(cellSelector('days', 1, 4))[0],
                row: 1,
                column: 4
              });
            }

            after(function () {
              $(cellSelector('days', 1, 4)).
                removeClass('in-progress').
                removeClass('error').
                empty().append("<div class='marker'>&#215;</div>");
            });

            describe("when there is a delete in progress", function () {
              before(function () {
                $(cellSelector('days', 1, 4)).addClass('in-progress');
                trigger();
              });

              it("clears the in-progress status", function () {
                expect($(cellSelector('days', 1, 4))).
                  to_not(match_selector, '.in-progress');
              });

              it("marks the cell in error", function () {
                expect($(cellSelector('days', 1, 4))).
                  to(match_selector, '.error');
              });

              it("does not remove the marker", function () {
                expect($(cellSelector('days', 1, 4) + ' .marker').length).
                  to(equal, 1);
              });
            });

            describe("when there is not a delete in progress", function () {
              before(trigger);

              it("does not mark the cell in error", function () {
                expect($(cellSelector('days', 1, 4))).
                  to_not(match_selector, '.error');
              });
            });
          });
        });
      });

      describe("notes selection", function () {
        it("defaults to details", function () {
          expect($('.details.note')).to(match_selector, ':visible');
        });

        it("is initialized from the provided hash", function () {
          psc.template.mpa.Presentation.init("labels");
          expect($('.labels.note')).to(match_selector, ':visible');
        });

        var tabs = ['details', 'labels', 'conditions'];
        $.each(tabs, function (i, tab) {
          var otherTabs = ['details', 'labels', 'conditions'].slice(0);
          otherTabs.splice($.inArray(tab, tabs), 1);

          describe("of " + tab, function () {
            before(function () {
              $('#notes-heading li.' + tab + ' a').click();
            });

            it("marks the tab as selected", function () {
              expect($('#notes-heading li.' + tab)).to(match_selector, '.selected');
            });

            it("only selects the appropriate tab", function () {
              expect($('#notes-heading li.selected').length).to(equal, 1);
            });

            it("shows the " + tab, function () {
              expect($('#notes .note.' + tab)).to(match_selector, ':visible');
            });

            $.each(otherTabs, function (dc, otherTab) {
              it("doesn't show the " + otherTab, function () {
                expect($('#notes .note.' + otherTab + ':visible').length).to(equal, 0);
              });
            });
          });
        });

        describe("messages", function () {
          describe("error", function () {
            before(function () {
              psc.template.mpa.Presentation.error("You should not have done that");
            });

            it("sets the text in the message div", function () {
              expect($('#message').text()).to(equal, "You should not have done that");
            });

            it("sets the type style", function () {
              expect($('#message')).to(match_selector, '.error');
              expect($('#message')).to_not(match_selector, '.info');
            });
          });

          describe("info", function () {
            before(function () {
              psc.template.mpa.Presentation.info("Something is afoot");
            });

            it("sets the text in the message div", function () {
              expect($('#message').text()).to(equal, "Something is afoot");
            });

            it("sets the type style", function () {
              expect($('#message')).to(match_selector, '.info');
              expect($('#message')).to_not(match_selector, '.error');
            });
          });

          describe("clearMessage", function () {
            before(function () {
              psc.template.mpa.Presentation.info("Something is afoot");
              psc.template.mpa.Presentation.clearMessage();
            });

            it("removes the message text", function () {
              expect($('#message').text()).to(equal, "");
            });

            it("clears the type class", function () {
              expect($('#message')).to_not(match_selector, '.info');
            });
          });
        });
      });
    });
  }(jQuery));
});
