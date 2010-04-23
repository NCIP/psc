<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<html>
<head>
<title>Purge Study</title>

    <title>Site Coordinator Dashboard</title>


    <tags:stylesheetLink name="main"/>
    <style type="text/css">

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

        form {
            width: 45em;
        }

        .none-specified {
            color: #999;
            font-style: italic;
        }
    </style>

    <tags:includeScriptaculous/>
    <tags:javascriptLink name="admin/purge-study/purge-study"/>
    <script type="text/javascript">
        jQuery(document).ready(function() {
            psc.admin.ps.StudyDetails.init();
            psc.admin.ps.StudyAutocompleter.init();
        })
    </script>

</head>

  <body>
    <laf:box title="Purge Study">
        <laf:division>
            <%--<div class="row" id="purge-study-warning">--%>
                <%--<p>Warning: This </p>--%>
            <%--</div>--%>
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
            <div class="row">
                <div id="study-details">
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
                </div>
            </div>
        </laf:division>
    </laf:box>
  </body>
</html>