require_spec('spec_helper.js');
require_main('manage-period/actions.js');

Screw.Unit(function () {
  (function ($) {
    describe('Actions', function () {
      var originalAjax, lastAjaxOptions, requestedAjaxResponses;
      var actionEvents;
      var COLLECTION_URI = "/studies/a/PAs";

      before(function () {
        originalAjax = $.ajax;
        requestedAjaxResponses = [];
        lastAjaxOptions = null;

        $.ajax = function (options) {
          lastAjaxOptions = options;
          var resp = requestedAjaxResponses.pop();
          function execResponse() {
            if (resp && resp.type === 'error') {
              options.error.apply(options, resp.args);
            }
            options.complete(resp && resp.xhr, (resp && resp.type) || 'success');
          }
          if (resp && resp.delay) {
            setTimeout(execResponse, resp.delay);
          } else {
            execResponse();
          }
        };

        actionEvents = [];

        $('#days').bind('action-completed', function (evt, data) {
          actionEvents.push({ type: 'completed', event: evt, data: data });
        }).bind('action-error', function (evt, data) {
          actionEvents.push({ type: 'error', event: evt, data: data });
        });

        psc.template.mpa.Actions.collectionUri = COLLECTION_URI;
        psc.template.mpa.Actions.init();
      });

      function expectAjaxError(errorText, exception, after) {
        requestedAjaxResponses.push({
          type: 'error',
          args: [null, errorText, exception],
          delay: after
        });
      }

      function expectAjaxSuccess(data, headers, after) {
        requestedAjaxResponses.push({
          type: 'success',
          xhr: pretendXhr(headers),
          args: [data, 'success'],
          delay: after
        });
      }
      
      function pretendXhr(headers) {
        return {
          getResponseHeader: function(name) {
            return (headers || {})[name];
          }
        }
      }

      after(function () {
        $.ajax = originalAjax;
        psc.template.mpa.Actions.collectionUri = null;
        $('#days').unbind('action-completed').
          unbind('action-error').unbind('action-started');
      });

      describe("add", function () {
        function trigger() {
          $('#days').trigger('action-started', {
            action: { name: 'add', step: 0 },
            activity: {
              code: '18551',
              name: 'Alcohol',
              type: 'Lab test',
              source: 'Standard'
            },
            day: 11,
            population: 'B',
            details:    'D',
            condition:  'C',
            labels: 'L1 L2',
            weight: 7,
            row: 1,
            column: 7
          });
        }

        describe("request", function () {
          before(trigger);

          it("POSTs", function () {
            expect(lastAjaxOptions.type).to(equal, 'POST');
          });

          it("uses the configured collection URI", function () {
            expect(lastAjaxOptions.url).to(equal, COLLECTION_URI);
          });

          describe("data", function () {
            it("includes the activity code", function () {
              expect(lastAjaxOptions.data['activity-code']).to(equal, '18551');
            });

            it("includes the activity source", function () {
              expect(lastAjaxOptions.data['activity-source']).to(equal, 'Standard');
            });

            it("includes the day", function () {
              expect(lastAjaxOptions.data['day']).to(equal, 11);
            });

            it("includes the details", function () {
              expect(lastAjaxOptions.data['details']).to(equal, 'D');
            });

            it("includes the condition", function () {
              expect(lastAjaxOptions.data['condition']).to(equal, 'C');
            });

            it("includes all labels", function () {
              expect(lastAjaxOptions.data['label']).to(equal, ['L1', 'L2']);
            });

            it("includes the population", function () {
              expect(lastAjaxOptions.data['population']).to(equal, 'B');
            });

            it("includes the weight", function () {
              expect(lastAjaxOptions.data['weight']).to(equal, 7);
            });
          })
        });

        describe("on error", function () {
          before(function () {
            expectAjaxError("bad news", null);
            trigger();
          });

          it("fires an error", function () {
            expect(actionEvents[0].type).to(equal, 'error');
          });

          it("includes the original data in the triggered error", function () {
            expect(actionEvents[0].data.row).to(equal, 1);
          });

          it("includes an error message in the triggered error", function () {
            expect(actionEvents[0].data.message).to(equal, "bad news");
          });
        });

        describe("on success", function () {
          before(function () {
            expectAjaxSuccess(null, { 'Location': '/new/thing' });
            trigger();
          });

          it("fires action-completed", function () {
            expect(actionEvents[0].type).to(equal, 'completed');
          });

          it("includes the original data in the triggered event", function () {
            expect(actionEvents[0].data.column).to(equal, 7);
          });

          it("includes the URI for the newly created element", function () {
            expect(actionEvents[0].data.href).to(equal, '/new/thing');
          });
        });
      });

      describe("move", function () {
        function trigger(step) {
          $('#days').trigger('action-started', {
            action: { name: 'move', step: step === 0 ? 0 : 1 },
            activity: {
              code: '18551',
              name: 'Alcohol',
              type: 'Lab test',
              source: 'Standard'
            },
            day: 2,
            population: 'B',
            details:    'D',
            condition:  'C',
            labels: 'L1 L4',
            weight: 7,
            row: 1,
            column: 7,
            startColumn: 11,
            href: '/studies/foo/315351'
          });
        }

        describe("step 0", function () {
          it("has no action", function () {
            trigger(0);
            
            expect(lastAjaxOptions).to(equal, null);
          });
        });
        
        describe("step 1", function () {
          describe("request", function () {
            before(trigger);
          
            it("is a PUT", function () {
              expect(lastAjaxOptions.type).to(equal, 'PUT');
            });

            it("targets the included href", function () {
              expect(lastAjaxOptions.url).to(equal, '/studies/foo/315351');
            });

            describe("data", function () {
              it("includes the activity code", function () {
                expect(lastAjaxOptions.data['activity-code']).to(equal, '18551');
              });

              it("includes the activity source", function () {
                expect(lastAjaxOptions.data['activity-source']).to(equal, 'Standard');
              });

              it("includes the day", function () {
                expect(lastAjaxOptions.data['day']).to(equal, 2);
              });

              it("includes the details", function () {
                expect(lastAjaxOptions.data['details']).to(equal, 'D');
              });

              it("includes the condition", function () {
                expect(lastAjaxOptions.data['condition']).to(equal, 'C');
              });

              it("includes all labels", function () {
                expect(lastAjaxOptions.data['label']).to(equal, ['L1', 'L4']);
              });

              it("includes the population", function () {
                expect(lastAjaxOptions.data['population']).to(equal, 'B');
              });

              it("includes the weight", function () {
                expect(lastAjaxOptions.data['weight']).to(equal, 7);
              });
            })
          });

          describe("on error", function () {
            before(function () {
              expectAjaxError("bad news", null);
              trigger();
            });

            it("fires an error", function () {
              expect(actionEvents[0].type).to(equal, 'error');
            });

            it("includes the original data in the triggered error", function () {
              expect(actionEvents[0].data.row).to(equal, 1);
            });

            it("includes an error message in the triggered error", function () {
              expect(actionEvents[0].data.message).to(equal, "bad news");
            });
          });

          describe("on success", function () {
            before(function () {
              expectAjaxSuccess(null);
              trigger();
            })

            it("fires action-completed", function () {
              expect(actionEvents[0].type).to(equal, 'completed');
            });

            it("includes the original data in the triggered event", function () {
              expect(actionEvents[0].data.startColumn).to(equal, 11);
            });
          });
        });
      });

      describe("delete", function () {
        function trigger() {
          $('#days').trigger('action-started', {
            action: { name: 'delete', step: 1 },
            activity: {
              code: '18551',
              name: 'Alcohol',
              type: 'Lab test',
              source: 'Standard'
            },
            day: 2,
            population: 'B',
            details:    'D',
            condition:  'C',
            labels: 'L1 L4',
            weight: 7,
            row: 1,
            column: 7,
            href: '/studies/foo/315351'
          });
        }

        describe("request", function () {
          before(trigger);
          
          it("DELETEs", function () {
            expect(lastAjaxOptions.type).to(equal, 'DELETE');
          });
          
          it("uses the included href", function () {
            expect(lastAjaxOptions.url).to(equal, '/studies/foo/315351');
          });
          
          it("has no data", function () {
            expect(lastAjaxOptions.data).to(equal, undefined);
          })
        })

        describe("on error", function () {
          before(function () {
            expectAjaxError("bad news", null);
            trigger();
          });

          it("fires an error", function () {
            expect(actionEvents[0].type).to(equal, 'error');
          });

          it("includes the original data in the triggered error", function () {
            expect(actionEvents[0].data.activity.code).to(equal, '18551');
          });

          it("includes an error message in the triggered error", function () {
            expect(actionEvents[0].data.message).to(equal, "bad news");
          });
        });

        describe("on success", function () {
          before(function () {
            expectAjaxSuccess(null);
            trigger();
          })

          it("fires action-completed", function () {
            expect(actionEvents[0].type).to(equal, 'completed');
          });

          it("includes the original data in the triggered event", function () {
            expect(actionEvents[0].data.action.name).to(equal, 'delete');
          });
        });
      });

      describe("queuing", function () {
        function trigger(day) {
          $('#days').trigger('action-started', {
            action: { name: 'add', step: 0 },
            activity: {
              code: '18551',
              name: 'Alcohol',
              type: 'Lab test',
              source: 'Standard'
            },
            day: day,
            population: 'B',
            details:    'D',
            condition:  'C',
            labels: 'L1 L2',
            weight: 7,
            row: 1,
            column: 7
          });
        }

        it("executes the actions in dispatch order", function () {
          expectAjaxSuccess(null);
          expectAjaxSuccess(null);
          expectAjaxSuccess(null);

          trigger(4);
          trigger(9);
          trigger(1);

          expect(actionEvents[0].data.day).to(equal, 4);
          expect(actionEvents[1].data.day).to(equal, 9);
          expect(actionEvents[2].data.day).to(equal, 1);
        });

        it("executes the actions in dispatch order even when some are slow", function () {
          expectAjaxSuccess(null, {}, 500);
          expectAjaxSuccess(null, {}, 10);
          expectAjaxSuccess(null, {}, 200);

          trigger(9);
          trigger(1);
          trigger(7);

          wait(function () {
            expect(actionEvents[0].data.day).to(equal, 9);
            expect(actionEvents[1].data.day).to(equal, 1);
            expect(actionEvents[2].data.day).to(equal, 7);
          }, 900);
        });
        
        it("executes everything, even when some are errors", function () {
          expectAjaxSuccess(null, 500);
          expectAjaxError("Paper jam", null, 120);
          expectAjaxSuccess(null, 120);
          
          trigger(4);
          trigger(10);
          trigger(11);
          
          wait(function () {
            expect(actionEvents[0].data.day).to(equal, 4);
            expect(actionEvents[1].data.day).to(equal, 10);
            expect(actionEvents[2].data.day).to(equal, 11);
          }, 900);
        });
      })
    });
  }(jQuery));
});
