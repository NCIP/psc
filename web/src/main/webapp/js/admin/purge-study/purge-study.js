psc.namespace("admin.ps");

psc.admin.ps.Warning = (function ($) {
    return {
        init: function() {
            $(document).ready(function() {
                $('#continue-to-purge-study').click(function() {
                    $('#study-search-container').show();
                    $('#purge-study-warning').hide();
                });
            });
        }
    };
})(jQuery);

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
        params.privilege = "purge"

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

                $('#' + acInputIdentifier).trigger("autocompleter-study-selected", {assigned_identifier: selected.innerHTML});
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
    var studySites = [];

    function createStudyInputListener() {
        $('#' + acInputIdentifier).bind("autocompleter-study-selected", updateStudyDetails)
    }

    function updateStudyDetails(event, study) {
        psc.admin.ps.StudyDetails.clearDetails();

        if (study == null || study.assigned_identifier == null || study.assigned_identifier.blank()) {
            return;
        }

        psc.admin.ps.StudyDetails.showDetails();

        getStudyDetails(study.assigned_identifier, [psc.admin.ps.StudyDetails.populateStudyDetails, psc.admin.ps.StudyDetails.populateSiteAssociations], psc.admin.ps.StudyDetails.failurePopulatingFields);
    }

    function getStudyDetails(assignedIdentifier, callbacks, failureCallback) {
        var uri = SC.relativeUri(
                studyResourceUri .replace("\{assigned-identifier\}", encodeURIComponent(assignedIdentifier))
                )

        $.ajax({url: uri,
            success: function(data) {
                var study = psc.admin.ps.StudyDetails.generateStudyJSON(data);
                study['assigned-identifier'] =  assignedIdentifier;
                for (var i=0; i < callbacks.length; i++) {
                    callbacks[i](study);
                }
            },
            error: failureCallback
        })
    }

    function getStudySites() {
        return studySites;
    }

    return {
        setStudySites: function(studySitesData) {
            studySites = studySitesData;
        },

        generateStudyJSON: function(xml) {
            var study= {};
            SC.objectifyXml("long-title", xml, function(elt, holder) {
                if (elt.childNodes[0] != null) {
                    study['long-title'] = elt.childNodes[0].nodeValue; // The text node
                }
            });

            study['amendment'] = SC.objectifyXml("amendment", xml);
            study['development-amendment'] = SC.objectifyXml("development-amendment", xml);

            return study;
        },

        populateStudyDetails: function(study) {
            $('#study-details-title').html("Displaying details for study " + study['assigned-identifier']);
            $('#study-assigned-identifier').html(study['assigned-identifier']);
            $('#study-assigned-identifier-hidden').val(study['assigned-identifier']);

            var lTitle = "<span class='none-specified'>None Specified</span>";
            if (study['long-title'] != null && !study['long-title'].blank()) {
                lTitle = study['long-title'];
            }
            $('#study-long-title').html(lTitle);

            var amendmentCount = study['amendment'].length;
            if (amendmentCount > 0) {
                amendmentCount = amendmentCount - 1; // Don't count [Original] Amendment
            }

            $('#study-amendment-count').html(amendmentCount);
        },

        populateSiteAssociations: function(study) {
            $('#associated-sites').html('');

            for(var i = 0; i < getStudySites().length; i++) {
                var studySite = getStudySites()[i];
                if (studySite['study-assigned-identifier'] === study['assigned-identifier']) {
                    var msg = studySite['site-assigned-identifier'] + " is an associated site and has " + studySite['subject-assignment-count'] + " subjects assigned.";
                    $('#associated-sites').append("<div>" + msg + "</div>");
                }
            }
        },

        failurePopulatingFields: function() {
            $('#study-details-title').html('A problem occurred when retrieving the assigned identifier');
            $('#study-assigned-identifier').html('A problem occurred when retrieving the assigned identifier');
            $('#study-long-title').html('A problem occurred when retrieving the long title');
            $('#study-amendment-count').html('A problem occurred when retrieving the total amendment count');
            $('#study-assigned-identifier-hidden').val('');
            $('#associated-sites').html('A problem occurred when retrieving the associated sites');
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
            $('#study-assigned-identifier-hidden').val('');
            $('#associated-sites').html(loading);
        },

        init: function(studySitesData) {
            $(document).ready(function() {
                psc.admin.ps.StudyDetails.setStudySites(studySitesData);
                createStudyInputListener();
            });
        }
    }
})(jQuery);