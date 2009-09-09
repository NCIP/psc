/*global jQuery before after describe it expect equal Screw psc require_spec require_main */

require_spec('spec_helper.js');
require_spec('sync-updater.js');
require_main('psc-tools/misc.js');
require_main('psc-tools/range.js');
require_main('subject/schedule-structure.js');
require_main('subject/schedule-data.js');

Screw.Unit(function () {
  (function ($) {
    describe("psc.subject.ScheduleData", function () {
      var originalAjax;
      var lastAjaxOptions;
      var desiredErrorText = null;
      var desiredAjaxSetupException = null;
      var desiredXmlHttpRequestStatus = null;
      var desiredXmlHttpRequestStatusText = null;
      var desiredAjaxSendExceptionText  = null;

      before(function () {
        psc.subject.ScheduleData.uriGenerator(function () {
          return "spec";
        });

        // Stub out the ajax calls to track invocations
        originalAjax = $.ajax;
        $.ajax = function (opts) {
          lastAjaxOptions = opts;

          var errorParams =
            [ desiredErrorText,
              desiredXmlHttpRequestStatus,
              desiredXmlHttpRequestStatusText,
              desiredAjaxSendExceptionText
            ].any(function(p) {return p !== null;});

          if (errorParams) {
            var xmlHttpRequest = null
            if (desiredXmlHttpRequestStatus || desiredXmlHttpRequestStatusText) {
              xmlHttpRequest = {
                status: desiredXmlHttpRequestStatus,
                statusText: desiredXmlHttpRequestStatusText
              }
            }

            opts.error(
              xmlHttpRequest,
              desiredErrorText,
              new Error(desiredAjaxSendExceptionText)
            );
          } else if (desiredAjaxSetupException !== null) {
            throw desiredAjaxSetupException;
          } else {
            opts.success({
              "days": {
                "2009-01-01": { activities: [
                  {
                    activity: { name: 'foo', type: 'Other' },
                    current_state: {
                      'date': "2009-01-01"
                    }
                  }
                ] }
              }
            }, "complete");
          }
        };
      });

      after(function () {
        $.ajax = originalAjax;
        psc.subject.ScheduleData.clear();
        lastAjaxOptions = null;
        desiredErrorText = null;
        desiredAjaxSetupException = null;
        desiredXmlHttpRequestStatus = null;
        desiredXmlHttpRequestStatusText = null;
        desiredAjaxSendExceptionText  = null;
      });

      it("triggers 'schedule-load-start' on refresh()", function () {
        var triggered = false;
        $('#schedule').bind('schedule-load-start', function () { triggered = true; });

        psc.subject.ScheduleData.refresh();

        expect(triggered).to(equal, true);
      });

      it("loads JSON from the server", function () {
        psc.subject.ScheduleData.refresh();
        expect(lastAjaxOptions.dataType).to(equal, "json");
      });

      describe("URI generator", function () {
        var originalGenerator;
        before(function () {
          try {
            originalGenerator = psc.subject.ScheduleData.uriGenerator();
          } catch (e) {
            originalGenerator = null;
          }
          psc.subject.ScheduleData.uriGenerator(null);
        });

        after(function () {
          psc.subject.ScheduleData.uriGenerator(originalGenerator);
        });

        it("uses the URI from the provided generator", function () {
          psc.subject.ScheduleData.uriGenerator(function () {
            return 'foo';
          });
          psc.subject.ScheduleData.refresh();
          expect(lastAjaxOptions.url).to(equal, "foo");
        });

        it("falls down without a generator", function () {
          expect(function () { psc.subject.ScheduleData.uriGenerator() }).
            to(raise, "psc.subject.ScheduleData.uriGenerator not set.  Don't know which resource to load from.")
        });
      });

      it("replaces the schedule on a successful load", function () {
        expect(psc.subject.ScheduleData.current()).to(equal, null);
        psc.subject.ScheduleData.refresh();

        expect(psc.subject.ScheduleData.current()).to_not(equal, null);
      });

      describe("schedule-ready", function () {
        var scheduleReady = false;
        before(function () {
          $('#schedule').bind('schedule-ready', function () {
            scheduleReady = true;
          });
        });

        after(function () {
          scheduleReady = false;
        });

        it("is triggered on success", function () {
          desiredErrorText = null;
          psc.subject.ScheduleData.refresh();
          expect(scheduleReady).to(equal, true);
        });

        it("is triggered when unchanged", function () {
          desiredErrorText = "notmodified";
          psc.subject.ScheduleData.refresh();
          expect(scheduleReady).to(equal, true);
        });

        it("is not triggered for errors", function () {
          desiredErrorText = "timeout";
          psc.subject.ScheduleData.refresh();
          expect(scheduleReady).to(equal, false);
        });
      });

      describe("schedule-error", function () {
        var scheduleError = false;
        var lastText = null;
        before(function () {
          $('#schedule').bind('schedule-error', function (evt, textStatus) {
            scheduleError = true;
            lastText = textStatus;
          });
        });

        after(function () {
          scheduleError = false;
          lastText = null;
        });

        it("is not triggered on success", function () {
          desiredErrorText = null;
          psc.subject.ScheduleData.refresh();
          expect(scheduleError).to(equal, false);
        });

        it("is not triggered when unchanged", function () {
          desiredErrorText = "notmodified";
          psc.subject.ScheduleData.refresh();
          expect(scheduleError).to(equal, false);
        });

        it("is triggered on error", function () {
          desiredErrorText = "error";
          psc.subject.ScheduleData.refresh();
          expect(scheduleError).to(equal, true);
        });

        it("is triggered with details of the error on timeout", function () {
          desiredErrorText = "timeout";
          psc.subject.ScheduleData.refresh();
          expect(lastText).to(equal, "timeout");
        });

        it("is triggered with a message including response code and text on error", function () {
          desiredErrorText = "error";
          desiredXmlHttpRequestStatus = 400;
          desiredXmlHttpRequestStatusText = "Bad Request: Missing Activities";
          psc.subject.ScheduleData.refresh();
          expect(lastText).to(equal, "Bad Request: Missing Activities (400)");
        });

        it("is triggered with exception message from the exception", function () {
          desiredAjaxSendExceptionText = "Parsing Error";
          psc.subject.ScheduleData.refresh();
          expect(lastText).to(equal, "Problem Requesting Schedule Data (Parsing Error)");
        });

        it("is triggered with general error message", function () {
          $.ajax = function (opts) {
            opts.error(null, null,null);
          }
          psc.subject.ScheduleData.refresh();
          expect(lastText).to(equal, "Problem Requesting Schedule Data");
        });

        /* TODO: reenable when there's support for testing async calls
        it("is triggered 300ms after there's an error setting up the ajax call", function () {
          desiredAjaxSetupException = "Configuration error of some kind";
          psc.subject.ScheduleData.refresh();
          expect(lastText).to(equal, desiredAjaxSetupException);
        });
        */
      });
    });
  }(jQuery));
});
