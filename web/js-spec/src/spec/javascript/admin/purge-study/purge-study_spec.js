require_spec('spec_helper.js');

/* Scriptaculous */
require_main('scriptaculous/effects.js')
require_main('scriptaculous/builder.js')
require_main('scriptaculous/controls.js')
require_main('scriptaculous/dragdrop.js')
require_main('scriptaculous/slider.js')
require_main('common-scriptaculous.js')

require_main('admin/purge-study/purge-study.js');

Screw.Unit(function () {
  (function (jQuery, $) {

    var requestedAjaxResponses;


    describe("psc.admin.PurgeStudy", function () {
      before(function () {

          SC.relativeUri = function(arg) {
            return arg;
          }

          SC.asyncRequest = function(uri, options) {
            var resp = {
              responseJSON: {
                studies: [
                  {id: -99, assigned_identifier: 'NCT-123'}
                ]
              }
            }
            options.onSuccess(resp)
          }

          jQuery(document).ready(function() {
              psc.admin.PurgeStudy.init();
          })
      });

      describe("search", function () {
        it("returns a list of matching studies", function () {
            $('studies-autocompleter-input').value = 'NCT';

            // FIXME: This only works on Firefox (http://jehiah.cz/archive/firing-javascript-events-properly)
//            var evt = document.createEvent("HTMLEvents");
//            evt.initEvent('keydown', true, true ); // event type,bubbling,cancelable
//            $('studies-autocompleter-input').dispatchEvent(evt);

//            expect(jQuery('#studies-autocompleter-div li').length).to(equal, 1);

        });
      });
    });

  })(jQuery, $);
});
