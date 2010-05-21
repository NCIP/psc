<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<html>
<head>
<title>Purge Study</title>

    <title>Site Coordinator Dashboard</title>


    <tags:stylesheetLink name="main"/>
    <style type="text/css">

        #status-row {
            text-align:center;
        }

        #status-message {
            background-color: #FFFFA1;
        }
        
        #purge-study-warning {
            /*width: 55em;*/
            text-align:center;
        }
        .warning-message {
            font-size: 1.5em;
        }

        .warning {
            color: #f00;
            text-decoration: blink;
        }

        #study-search-container {
            display: none;
        }
        
        #study-search-container div.label {
            width: 4em;
        }

        #study-search-container div.value {
            margin-left: 6em;
        }

        #studies-autocompleter-input {
            width: 25em
        }

        #study-details {
            display:none;
            margin-top: 1em;
            margin-left: 1em;
            width: 40em;
            border: 1px solid #ccc;
        }

        #study-label {
            width: 3%;
            margin-right: 0.4em
        }

        #study-details div.label {
            width: 25%;
            margin-right: 0.4em
        }

        #study-details h1 {
            padding: 0.4em;
            border-bottom: 1px solid #ccc;
            background-color: #DEEBFC;
            color: #000;
        }

        /*form {*/
            /*width: 45em;*/
        /*}*/

        .none-specified {
            color: #999;
            font-style: italic;
        }
    </style>

    <tags:includeScriptaculous/>
    <tags:javascriptLink name="admin/purge-study/purge-study"/>
    <script type="text/javascript">
        var studySiteJSON = ${studySiteJSON};

        jQuery(document).ready(function() {
            psc.admin.ps.StudyDetails.init(studySiteJSON);
            psc.admin.ps.StudyAutocompleter.init();
            psc.admin.ps.Warning.init();

            jQuery("form").submit( function () {
                return confirm("Are you sure you want to purge this study?  This action will permanently delete the study template and all associated subject schedules.");
            });

            jQuery("#acknowledge-status-message").click( function(event) {
                event.stopPropagation();
                jQuery("#status-row").hide();
            })
        })
    </script>

</head>

  <body>
    <laf:box title="Purge Study">
        <laf:division>
            <c:if test="${not empty param.status}">
                <div class="row" id="status-row">
                    <h2 id="status-message">${param.status} <a href="#" id="acknowledge-status-message">OK</a></h2>
                </div>
            </c:if>
            <div class="row" id="purge-study-warning">
                <p class="warning-message"><span class="warning">Warning:</span> This page allows purging of studies and all associated study information, including subject schedules.</p>
                <p>Are you sure you want to continue? <button id="continue-to-purge-study">Yes</button></p>
            </div>
            <div class="row" id="study-search-container">
                <div class="label">
                    Study:
                </div>
                <div class="value">
                    <input id="studies-autocompleter-input" type="text" 
                           hint="Search for studies" class="autocomplete" autocomplete="off"/>
                    <div id="studies-autocompleter-div" class="autocomplete"></div>
                </div>
            </div>
            <form:form method="post">
                <div class="row">
                    <div id="study-details">
                        <form:hidden path="studyAssignedIdentifier" id="study-assigned-identifier-hidden"/>
                        <h1 id="study-details-title">Loading...</h1>
                        <div class="row">
                            <div class="label">Assigned Identifier:</div>
                            <div id="study-assigned-identifier" class="value">Loading...</div>
                        </div>
                        <div class="row">
                            <div class="label">Long Title:</div>
                            <div id="study-long-title" class="value">Loading...</div>
                        </div>
                        <div class="row">
                            <div class="label">Total Amendments:</div>
                            <div id="study-amendment-count" class="value">Loading...</div>
                        </div>
                        <div class="row">
                            <div class="label">Associated Sites:</div>
                            <div id="associated-sites" class="value">Loading...</div>
                        </div>
                        <div class="row">
                            <div class="label">&nbsp;</div>
                            <div class="value"><input type="submit" id="purge-study-button" value="Purge Study"/></div>
                        </div>
                    </div>
                </div>
            </form:form>
        </laf:division>
    </laf:box>
  </body>
</html>