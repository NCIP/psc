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

                $('#' + acInputIdentifier).trigger("autocompleter-study-selected", {assigned_identifier: selected.innerHTML, id: selected.id});
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
    var sites;

    function createStudyInputListener() {
        $('#' + acInputIdentifier).bind("autocompleter-study-selected", updateStudyDetails)
    }

    function updateStudyDetails(event, study) {
        psc.admin.ps.StudyDetails.clearDetails();

        if (study == null) {
            return;
        }

        getStudyDetails(study, psc.admin.ps.StudyDetails.populateStudyDetails);
    }

    function getStudyDetails(study, callback) {

        if (study == null || study.assigned_identifier == null || study.assigned_identifier.blank()) {
            return;
        }

        psc.admin.ps.StudyDetails.showDetails();

        var assignedIdentifier = study.assigned_identifier;

        var uri = SC.relativeUri(
                studyResourceUri .replace("\{assigned-identifier\}", assignedIdentifier)
                )

        $.get(uri, function(data) {
            callback(psc.admin.ps.StudyDetails.generateStudyJSON(data));

        })


    }

    return {
        init: function(sitesData) {
            $(document).ready(function() {
                sites = sitesData;
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

            if (studies.length == 0) {
                return;
            }

            return studies[0];
        },

        populateStudyDetails: function(study) {
            $('#study-details-title').html("Displaying details for study " + study['assigned-identifier']);
            $('#study-assigned-identifier').html(study['assigned-identifier']);
            
            var long = study['long-title'] || "<span class='none-specified'>None Specified</span>"
            $('#study-long-title').html(long);

            var amendmentCount = study['amendment'].length;
            if (amendmentCount > 0) {
                amendmentCount = amendmentCount - 1; // Don't count [Original] Amendment
            }

            $('#study-amendment-count').html(amendmentCount);
        },
        populateSiteAssociations: function(sites) {
            if (sites == null) {
                return;
            }

            $('#associated-sites').html('');
            for(var i = 0; i < sites.site.length; i++) {
                var site = sites.site[i];
                var msg = site['assigned-identifier'] + " is an associated site and has " + site['subject-count'] + " subjects assigned.";
                $('#associated-sites').append("<div>" + msg + "</div>");
            }
        },
        hideyDetails: function() {
            $('#study-details').hide();
        },
        showDetails: function() {
            $('#study-details').show();
        },

        clearDetails: function() {
            var loading = 'Loading...';
            $('#study-details-title').html(loading);
            $('#study-assigned-identifier').html(loading);
            $('#study-long-title').html(loading);
            $('#study-amendment-count').html(loading);
            $('#associated-sites').html(loading);
        }
    }
})(jQuery);