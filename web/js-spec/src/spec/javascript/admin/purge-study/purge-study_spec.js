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
                          {id: -99, assigned_identifier: 'NCT-123'}
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
//var studySnapshotText =
//"<?xml version='1.0' encoding='UTF-8'?>\n" +
//"<study xmlns='http://bioinformatics.northwestern.edu/ns/psc' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' assigned-identifier='NCT00003641' last-modified-date='2010-02-23T15:23:50.867Z' provider='mock - NOT FOR PRODUCTION' xsi:schemaLocation='http://bioinformatics.northwestern.edu/ns/psc http://bioinformatics.northwestern.edu/ns/psc/psc.xsd'>\n" +
//"  <long-title>Phase III Randomized Study of Four Weeks of High Dose Interferon Alfa-2b in Stage TN ,TN, TN, and T, N (Microscopic) Melanoma</long-title>\n" +
//"  <secondary-identifier type='nct' value='NCT00003641'/>\n" +
//"  <secondary-identifier type='org_study' value='CDR0000066727'/>\n" +
//"  <secondary-identifier type='secondary' value='CALGB-500103'/>\n" +
//"  <secondary-identifier type='secondary' value='CAN-NCIC-ME10'/>\n" +
//"  <secondary-identifier type='secondary' value='COG-E1697'/>\n" +
//"  <secondary-identifier type='secondary' value='ECOG-1697'/>\n" +
//"  <secondary-identifier type='secondary' value='SWOG-E1697'/>\n" +
//"  <planned-calendar id='312cc213-1721-4d1d-8715-655181cfcb57'/>\n" +
//"  <amendment name='[Original]' date='2010-02-22' mandatory='true' released-date='2010-02-23T15:23:50.867Z'>\n" +
//"    <planned-calendar-delta id='5dc0779f-2917-44ab-b44b-097fc9597758' node-id='312cc213-1721-4d1d-8715-655181cfcb57'>\n" +
//"      <add id='e57779cf-bf4c-41d4-83c0-6f3af91f87f4' index='0'>\n" +
//"        <epoch id='94a698d9-73de-4acd-adda-ecfa39f16305' name='Treatment'>\n" +
//"          <study-segment id='908ca8d4-2bb6-4e79-8e1d-9df5dd06b706' name='A'>\n" +
//"            <period id='4ea1bb41-5463-46e2-b3d2-ecaeefd38081' repetitions='1' start-day='1' duration-quantity='1' duration-unit='day'>\n" +
//"              <planned-activity id='d9046094-bda8-404f-9f00-22cb22d08aba' day='1'>\n" +
//"                <activity name='Bone Marrow Aspirate' code='961' type='Disease Measure' source='Northwestern University'/>\n" +
//"              </planned-activity>\n" +
//"            </period>\n" +
//"          </study-segment>\n" +
//"        </epoch>\n" +
//"      </add>\n" +
//"    </planned-calendar-delta>\n" +
//"  </amendment>\n" +
//"</study>";

//              var studySnapshotText = "<?xml version='1.0' encoding='UTF-8'?>\n<study assigned-identifier='NCI-999' xmlns='http://bioinformatics.northwestern.edu/ns/psc' xsi:schemaLocation='http://bioinformatics.northwestern.edu/ns/psc http://bioinformatics.northwestern.edu/ns/psc/psc.xsd'></study>";
//              var studySnapshotText = "<study assigned-identifier=\"NCT00003641\" last-modified-date=\"2010-02-23T15:23:50.867Z\" provider=\"mock - NOT FOR PRODUCTION\" xsi:schemaLocation=\"http://bioinformatics.northwestern.edu/ns/psc http://bioinformatics.northwestern.edu/ns/psc/psc.xsd\"></study>";
              studySnapshot = XML.parse(studySnapshotText);
          });

          it ("generates a JSON object from a study snaphot XML", function() {
              var result = psc.admin.ps.StudyDetails.generateStudyJSON(studySnapshot);
              expect(result['provider']).to(equal, "alpha");
              expect(result['assigned-identifier']).to(equal, "NCI-999");
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
              expect(jQuery('#study-long-title').html()).to(equal, 'Loading...')
              expect(jQuery('#study-assigned-identifier').html()).to(equal, 'Loading...')
              expect(jQuery('#study-amendment-count').html()).to(equal, 'Loading...')

              psc.admin.ps.StudyDetails.populateTitleAndIdentifier({'assigned-identifier': 'NCI-999', 'long-title':'Long title.', amendment:['[Original]', 'First']});

              expect(jQuery('#study-long-title').html()).to(equal, 'Long title.')
              expect(jQuery('#study-assigned-identifier').html()).to(equal, 'NCI-999')
              expect(jQuery('#study-amendment-count').html()).to(equal, 1)
          });

          it("clears the study details fields", function() {
              jQuery('#study-assigned-identifier').html("A")
              jQuery('#study-long-title').html("B")
              jQuery('#study-amendment-count').html("C")

              psc.admin.ps.StudyDetails.clearDetails();

              expect(jQuery('#study-long-title').html()).to(equal, 'Loading...')
              expect(jQuery('#study-assigned-identifier').html()).to(equal, 'Loading...')
              expect(jQuery('#study-amendment-count').html()).to(equal, 'Loading...')
          });

          it("populates the study site details", function() {
              expect(jQuery('#associated-sites').html()).to(equal, 'Loading...');

              psc.admin.ps.StudyDetails.populateSiteAssociations(
                    { site: [
                        {'assigned-identifier': 'NU', 'subject-count': 2},
                        {'assigned-identifier': 'Mayo', 'subject-count': 0}
                    ]}
             )

              expect(jQuery('#associated-sites div:first').html()).to(equal, "NU is an associated site and has 2 subjects assigned.")
              expect(jQuery('#associated-sites div:last').html()).to(equal, "Mayo is an associated site and has 0 subjects assigned.")
          });
      });

    });

  })(jQuery, $);
});
