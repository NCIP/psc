psc.namespace("admin.ps");

psc.admin.ps.StudyAutocompleter = (function ($) {
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

                $('#' + acInputIdentifier).trigger("autocompleter-study-selected", {study:{assigned_identifier: selected.innerHTML, id: selected.id}});
            }
        });
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

    return {
        init: function() {
            $(document).ready(function() {
                createAutocompleter();
            });
        }
    };
})(jQuery);

psc.admin.ps.StudyDetails = (function ($) {
    var acInputIdentifier = "studies-autocompleter-input";
    var studyResourceUri = "/api/v1/studies/{assigned-identifier}/template"
    var studySitesResourceUri = "api/v1/studies/{study-identifier}/sites";  // Have to creates

    function createStudyInputListener() {
        $('#' + acInputIdentifier).bind("autocompleter-study-selected", updateStudyDetails(event))
    }

    function updateStudyDetails(event) {
        clearDetails();

        if (event == null || event.data == null || event.data.study == null) {
            return;
        }

        getStudyDetails(event.data.study, populateStudyDetails);
    }

    function getStudyDetails(study, callback) {
        if (study == null || study.assigned_identifier == null || study.assigned_identifier.blank()) {
            return;
        }

        showDetails();

        var assignedIdentifier = event.study.assigned_identifier;

        var uri = SC.relativeUri(
                studyResourceUri .replace("\{assigned-identifier\}", assignedIdentifier)
                )

        $.get(uri, function(data) {
            // callback(generateStudyJSON(data));
        })


    }

    return {
        init: function() {
            $(document).ready(function() {
                createStudyInputListener();
            });

        },

        generateStudyJSON: function(xml) {
            var studies = SC.objectifyXml("study", xml, function(elt, study) {
                var longTitles = SC.objectifyXml("long-title", xml, function(elt, holder) {
                    if (elt.childNodes[0] != null) {
                        study['long-title'] = elt.childNodes[0].nodeValue; // The text node
                    }
                });

                study['amendment'] = SC.objectifyXml("amendment", xml);
                study['development-amendment'] = SC.objectifyXml("development-amendment", xml);
            });

            return studies[0];
        },

        populateTitleAndIdentifier: function(study) {
            $('#study-assigned-identifier').html(study['assigned-identifier'])
            $('#study-long-title').html(study['long-title'])

            var amendmentCount = study['amendment'].length;
            if (amendmentCount > 0) {
                amendmentCount = amendmentCount - 1; // Don't count [Original] Amendment
            }

            $('#study-amendment-count').html(amendmentCount);
        },

        hideyDetails: function() {
            $('#study-details').hide();
        },
        showDetails: function() {
            $('#study-details').hide();
        },

        clearDetails: function() {
            $('#study-assigned-identifier').html("Loading...");
            $('#study-long-title').html("Loading...");
            $('#study-amendment-count').html("Loading...");
        }
    }
})(jQuery);