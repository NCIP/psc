psc.namespace("admin");

psc.admin.PurgeStudy = (function ($) {

  StudyAutocompleter = (function ($) {
      var resourceUri = "/api/v1/studies";

      var acInputIdentifier = "studies-autocompleter-input";
      var acResultsIdentifier = "studies-autocompleter-div";

      function studyAutocompleterChoiceProcessing(callback) {
          var searchString = $F(acInputIdentifier )
          if (searchString == "Search for study") {
              searchString = ""
          }

          var uri = SC.relativeUri(resourceUri)
          if (searchString.blank()) {
              return;
          }

          var params = {};
          if (!searchString.blank()) {
              params.q = searchString;
          }

          SC.asyncRequest(uri+".json", {
              method: "GET", parameters: params,
              onSuccess: function(response) {
                  callback(response.responseJSON.studies)
              }
          })
      }

      function createAutocompleter() {
         new SC.FunctionalAutocompleter(
             acInputIdentifier, acResultsIdentifier, studyAutocompleterChoices, {
                 afterUpdateElement: function(input, selected) {
                     input.value = "";
                     input.focus();
                     $('#' + acResultsIdentifier).hide()

                     alert(selected.id);
                 }
             }
         );
      }

      function studyAutocompleterChoices(str, callback) {
              studyAutocompleterChoiceProcessing(function(data) {
              var lis = data.map(function(study) {
                        var id = study.id
                        var name = study.assigned_identifier
                        var listItem = "<li id='"  + id + "'>" + name + "</li>";
                        return listItem
              }).join("\n");

              callback("<ul>\n" + lis + "\n</ul>");
            });
      }

      function populateStudyDetails(studyId) {
          
      }

      return {
          init: function() {
              createAutocompleter();
          }
      };
  })($);


  return {
      init: function() {
          $(document).ready(function() {
                StudyAutocompleter.init();
          });
      }
  };

})(jQuery);