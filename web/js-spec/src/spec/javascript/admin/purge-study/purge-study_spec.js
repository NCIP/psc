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


    describe("psc.admin.ps", function () {
      before(function () {
          SC.relativeUri = function(arg) {
            return arg;
          }

          jQuery(document).ready(function() {
//              psc.admin.PurgeStudy.init();
          })
      });

      describe("StudyAutocompleter", function () {
        before(function () {
            SC.asyncRequest = function(uri, options) {
                var resp = {
                    responseJSON: {
                        studies: [
                          {id: -99, assigned_identifier: 'NCT-123', privileges: ["purge"]}
                        ]
                    }
                }
                options.onSuccess(resp)
            }
        });

        it("returns a list of matching studies", function () {
            $('studies-autocompleter-input').value = 'NCT';

            // FIXME: This only works on Firefox (http://jehiah.cz/archive/firing-javascript-events-properly)
//            var evt = document.createEvent("HTMLEvents");
//            evt.initEvent('keydown', true, true ); // event type,bubbling,cancelable
//            $('studies-autocompleter-input').dispatchEvent(evt);

//            expect(jQuery('#studies-autocompleter-div li').length).to(equal, 1);

        });
      });

      describe("StudyDetails", function () {
          var studySnapshot;

          before(function () {
              var studySnapshotText =  
                    "<study xmlns='http://bioinformatics.northwestern.edu/ns/psc' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' assigned-identifier='NCI-999' last-modified-date='2010-02-23T15:23:50.867Z' provider='alpha' >" +
                    "  <long-title>I am the loooong title.</long-title>\n" +
                    "  <amendment name='[Original]' date='2010-02-22' mandatory='true' released-date='2010-02-23T15:23:50.867Z'/>\n" +
                    "  <amendment name='AMEND-1' date='2010-02-23' mandatory='true' released-date='2010-02-24T15:23:50.867Z'/>\n" +
                    "  <development-amendment name='DEVO' date='2010-02-24' mandatory='true' released-date='2010-02-25T15:23:50.867Z'/>\n" +
                    "</study>";

              studySnapshot = XML.parse(studySnapshotText);
          });

          it ("generates a JSON object from a study snaphot XML", function() {
              var result = psc.admin.ps.StudyDetails.generateStudyJSON(studySnapshot);
              expect(result['long-title']).to(equal, "I am the loooong title.");

              expect(result['amendment'].length).to(equal, 2)

              expect(result['amendment'][0]['name']).to(equal, '[Original]')
              expect(result['amendment'][0]['released-date']).to(equal, '2010-02-23T15:23:50.867Z')

              expect(result['amendment'][1]['name']).to(equal, 'AMEND-1')
              expect(result['amendment'][1]['released-date']).to(equal, '2010-02-24T15:23:50.867Z')

              expect(result['development-amendment'].length).to(equal, 1)

              expect(result['development-amendment'][0]['name']).to(equal, 'DEVO')
              expect(result['development-amendment'][0]['released-date']).to(equal, '2010-02-25T15:23:50.867Z')
          });

          it("populates the study details fields", function () {
              expect(jQuery('#study-details-title').html()).to(equal, 'Loading...')
              expect(jQuery('#study-long-title').html()).to(equal, 'Loading...')
              expect(jQuery('#study-assigned-identifier').html()).to(equal, 'Loading...')
              expect(jQuery('#study-amendment-count').html()).to(equal, 'Loading...')
              expect(jQuery('#study-assigned-identifier-hidden').val()).to(equal, '')

              psc.admin.ps.StudyDetails.populateStudyDetails({'assigned-identifier': 'NCI-999', 'long-title':'Long title.', amendment:['[Original]', 'First']});

              expect(jQuery('#study-details-title').html()).to(equal, 'Displaying details for study NCI-999')
              expect(jQuery('#study-long-title').html()).to(equal, 'Long title.')
              expect(jQuery('#study-assigned-identifier').html()).to(equal, 'NCI-999')
              expect(jQuery('#study-amendment-count').html()).to(equal, 1)
              expect(jQuery('#study-assigned-identifier-hidden').val()).to(equal, 'NCI-999')
          });

          it("clears the study details fields", function() {
              jQuery('#study-assigned-identifier').html("A")
              jQuery('#study-long-title').html("B")
              jQuery('#study-amendment-count').html("C")
              jQuery('#study-assigned-identifier-hidden').val("A");

              psc.admin.ps.StudyDetails.clearDetails();

              expect(jQuery('#study-long-title').val()).to(equal, '')
              expect(jQuery('#study-long-title').html()).to(equal, 'Loading...')
              expect(jQuery('#study-assigned-identifier').html()).to(equal, 'Loading...')
              expect(jQuery('#study-amendment-count').html()).to(equal, 'Loading...')
              expect(jQuery('#study-assigned-identifier-hidden').val()).to(equal, '')

          });

          it("report a failure when populating study details", function () {
              jQuery('#study-details-title').html('Loading..');
              jQuery('#study-long-title').html("Loading..");
              jQuery('#study-assigned-identifier').html("Loading..");
              jQuery('#study-amendment-count').html("Loading..");
              jQuery('#study-assigned-identifier-hidden').val("");
              jQuery('#associated-sites').html('Loading..');

              psc.admin.ps.StudyDetails.failurePopulatingFields();

              expect(jQuery('#study-details-title').html()).to(equal, 'A problem occurred when retrieving the assigned identifier')
              expect(jQuery('#study-long-title').html()).to(equal, 'A problem occurred when retrieving the long title')
              expect(jQuery('#study-assigned-identifier').html()).to(equal, 'A problem occurred when retrieving the assigned identifier')
              expect(jQuery('#study-amendment-count').html()).to(equal, 'A problem occurred when retrieving the total amendment count')
              expect(jQuery('#study-assigned-identifier-hidden').val()).to(equal, '')
              expect(jQuery('#associated-sites').html()).to(equal, 'A problem occurred when retrieving the associated sites')
          });

          it("populates the study site details", function() {
              jQuery('#associated-sites').html('Loading..');

              psc.admin.ps.StudyDetails.setStudySites(
                    [
                        { 'study-assigned-identifier': 'NU-123', 'site-assigned-identifier': 'NU', 'subject-assignment-count': 2},
                        { 'study-assigned-identifier': 'NU-123', 'site-assigned-identifier': 'LF', 'subject-assignment-count': 1},
                        { 'study-assigned-identifier': 'XXXXXX', 'site-assigned-identifier': 'Mayo', 'subject-assignment-count': 0}
                    ]
             );

            psc.admin.ps.StudyDetails.populateSiteAssociations({'assigned-identifier': 'NU-123'});

              expect(jQuery('#associated-sites div:first').html()).to(equal, "NU is an associated site and has 2 subjects assigned.")
              expect(jQuery('#associated-sites div:last').html()).to(equal, "LF is an associated site and has 1 subjects assigned.")
          });
      });

    });

  })(jQuery, $);
});
