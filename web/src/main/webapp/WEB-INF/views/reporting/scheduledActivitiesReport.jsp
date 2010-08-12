<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:useBean id="command" scope="request"
             type="edu.northwestern.bioinformatics.studycalendar.web.reporting.ScheduledActivitiesReportCommand"/>
<jsp:useBean id="coordinators" scope="request"
             type="java.util.List<edu.northwestern.bioinformatics.studycalendar.domain.User>"/>

<tags:javascriptLink name="psc-tools/misc"/>
<tags:stylesheetLink name="yui-sam/2.7.0/datatable"/>
<%-- TODO: move common YUI parts to a tag if they are re-used --%>
<c:forEach items="${fn:split('yahoo-dom-event element-min datasource-min logger-min json-min connection-min get-min datatable-min', ' ')}" var="script">
   <tags:javascriptLink name="yui/2.7.0/${script}"/>
</c:forEach>

<html>
<title>Report</title>
<head>
    <tags:stylesheetLink name="report"/>
    <tags:javascriptLink name="labels/manage-label" />
    <tags:javascriptLink name="labels/labelServer" />
    <tags:javascriptLink name="resig-templates" />

    <tags:resigTemplate id="new_label_autocompleter_row">
        <li>
            <span class="label-name">[#= label #]</span>
        </li>
    </tags:resigTemplate>

    <tags:stylesheetLink name="main"/>
    <tags:sassLink name="labels"/>

    <script type="text/javascript">

        var bundleList;

        function getUri(extention) {
            return SC.relativeUri("/api/v1/reports/scheduled-activities") + extention
        }

        function getParams() {
            var params = {};
            params['study'] = $("filters.studyAssignedIdentifier").value;
            params['site'] = $("filters.siteName").value;
            params['state'] = $("filters.currentStateMode").value;
            params['activity-type'] = $("filters.activityType").value;
            params['label'] = $F("labels-autocompleter-input");
            var startDate = $("actual-date-start").value
            if (startDate != null && startDate.length > 0) {
                startDate = psc.tools.Dates.displayDateToApiDate($("actual-date-start").value)
            }
            params['start-date'] = startDate;
            var endDate = $("actual-date-stop").value
            if (endDate != null && endDate.length > 0) {
                endDate = psc.tools.Dates.displayDateToApiDate($("actual-date-stop").value)
            }

            params['end-date'] = endDate;
            params['responsible-user'] =  $("filters.subjectCoordinator").value;
            params['person-id'] =  $("filters.personId").value;
            return params;
        }

        function generateExport(extention) {
            var uri = getUri(extention);
            var params = getParams();
            location.href = uri + '?study='+ params['study'] + '&site=' + params['site'] +
                            '&state=' + params['state'] + '&activity-type='+ params['activity-type'] +
                            '&label=' + params['label'] + '&start-date='+params['start-date'] +
                            '&end-date='+ params['end-date'] + '&responsible-user=' + params['responsible-user'] +
                            '&person-id='+ params['person-id'];
        }

        function submitFilters() {
            var uri = getUri(".json");
            var params = getParams();

            SC.asyncRequest(uri, {
              method: "GET", parameters: params,
              onSuccess: function(response) {
                   var resp = response.responseJSON
                   var bundleListColumns = [
                        { key: "activity_name", label: "Activity", sortable: true},
                        { key: "activity_status", label: "Activity Status", sortable: true },
                        { key: "scheduled_date", label:"Scheduled Date", sortable:true},
                        { key: "details", label: "Details", sortable: true, width: 200},
                        { key: "condition", label: "Condition", sortable: true, width: 200},
                        { key: "labels", label: "Labels", sortable: false,
                            formatter: function (elCell, oRecord, oColumn, oData) {
                                elCell.innerHTML = oData.join(" ")
                            }
                        },
                        { key: "ideal_date", label: "Ideal Date", sortable: true},
                        { key: "subject_name", label: "Subject", sortable: true},
                        { key: "person_id", label: "Person ID", sortable: true},
                        { key: "study_subject_id", label: "Study Subject Id", sortable: true},
                        { key: "subject_coordinator_name", label: "Subject Coordinator", sortable: true},
                        { key: "study", label: "Study", sortable: true},
                        { key: "site", label: "Site", sortable: true}
                   ];
                   var myDataSource = new YAHOO.util.DataSource(resp);
                    myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;

                    myDataSource.responseSchema = {
                        resultsList : "rows",
                        fields : [
                            { key: "activity_name"},
                            { key: "activity_status"},
                            { key: "scheduled_date"},
                            { key: "details"},
                            { key: "condition"},
                            { key: "labels" },
                            { key: "ideal_date"},
                            { key: "subject_name"},
                            { key: "person_id"},
                            { key: "study_subject_id"},
                            { key: "subject_coorinator_name"},
                            { key: "study"},
                            { key: "site"}
                        ]
                    };

                    bundleList = new YAHOO.widget.DataTable("bundle-list", bundleListColumns, myDataSource, {scrollable:true});
                },
                onFailure: function(response) {
                    $('errors').innerHTML = "Please correct date format";
                }
            })
        }

        function resetFilters() {
           $("filters.studyAssignedIdentifier").value = "";
           $("filters.siteName").value = "";
           $("filters.currentStateMode").value = "";
           $("filters.activityType").value = "";
           $("actual-date-start").value = "";
           $("actual-date-stop").value = "";
           $("filters.subjectCoordinator").value = "";
           $("filters.personId").value ="";
           $("bundle-list").hide();
           $('messages').innerHTML = "";
           $("labels-autocompleter-input").value = "";
        }

        //need this method to avoid form submission on the enter key press for labels autocompleter
        function checkKey(event) {
            if (event.keyCode == Event.KEY_RETURN) {
                Event.stop(event)
            }
        }

        function setUpFiltersWithValues() {
            $("filters.personId").value = '${command.personId}';
            $("actual-date-start").value = '${command.startDate}';
            $("actual-date-stop").value = '${command.endDate}';
        }

        Event.observe(window, "load", setUpFiltersWithValues)
    </script>
</head>
<body>
<laf:box title="Scheduled Activities Report" cssClass="yui-skin-sam">
    <laf:division>
        <c:set var="action"><c:url value="/pages/report/scheduledActivitiesReport"/></c:set>
        <form:form action="${action}"method="post" onsubmit="return false">
            <div id="errors"></div>
             <div class="search_box">
                 <input type="submit" value="Search" class="button" onclick="submitFilters()"/>
                 <input id="resetButton" type="submit" value="Reset filters" onclick="resetFilters()"/>
            </div>
            <div class="search-filters">
                <div class="filterGroup">
                    <span class="filterInput">
                        <form:label path="filters.studyAssignedIdentifier" >
                            Study name:
                        </form:label>
                        <form:input id="filters.studyAssignedIdentifier" path="filters.studyAssignedIdentifier"/>
                    </span>
                </div>
                <div class="filterGroup">
                    <span class="filterInput">
                        <form:label path="filters.siteName" >
                            Site name:
                        </form:label>
                        <form:input id="filters.siteName" path="filters.siteName"/>
                    </span>
                </div>
                <div class="filterGroup">
                    <span class="filterInput">
                        <form:label path="filters.currentStateMode" >
                            Activity status:
                        </form:label>

                        <form:select path="filters.currentStateMode" id="filters.currentStateMode" >
                            <form:option value="" label=""/>
                            <form:options items="${modes}" itemLabel="name" itemValue="id"/>
                        </form:select>

                    </span>

                    <span class="filterInput">
                        <form:label path="filters.activityType" >
                            Activity type:
                        </form:label>

                        <form:select path="filters.activityType" id="filters.activityType" >
                            <form:option value="" label=""/>
                            <form:options items="${types}" itemLabel="name" itemValue="id"/>
                        </form:select>
                    </span>

                    <span class="filterInput">
                        <label>Activity label: </label>
                        <input id="labels-autocompleter-input" type="text" autocomplete="off" class="autocomplete"/>
                        <div id="labels-autocompleter-div" class="autocomplete"></div>
                    </span>
                </div>

                <div class="filterGroup">
                    <span class="filterInput">
                        <form:label path="filters.actualActivityDate" >
                            Activities scheduled from:
                        </form:label>

                        <form:input path="filters.actualActivityDate.start" id="actual-date-start" cssClass="date"/>

                        <a href="#" id="actual-date-start-calbutton">
                            <img src="<laf:imageUrl name='chrome/b-calendar.gif'/>" alt="Calendar" width="17"
                                 height="16" border="0" align="absmiddle"/>
                        </a>

                        to

                        <form:input path="filters.actualActivityDate.stop" id="actual-date-stop" cssClass="date"/>

                        <a href="#" id="actual-date-stop-calbutton">
                            <img src="<laf:imageUrl name='chrome/b-calendar.gif'/>" alt="Calendar" width="17"
                                 height="16" border="0" align="absmiddle"/>
                        </a>
                    </span>
                </div>

                <div class="filterGroup">
                    <span class="filterInput">
<%--
                        <form:label path="filters.subjectCoordinator" >
                            Subject coordinator:
                        </form:label>

                        <form:select path="filters.subjectCoordinator" id="filters.subjectCoordinator" >
                            <form:option value="" label=""/>
                            <form:options items="${coordinators}" itemLabel="displayName" itemValue="id"/>
                        </form:select>
--%>
                    </span>
                </div>

                <div class="filterGroup">
                    <span class="filterInput">
                        <form:label path="filters.personId" >
                            Person ID:
                        </form:label>
                        <form:input id="filters.personId" path="filters.personId"/>
                    </span>
                </div>

            </div>

            <br style="clear:both"/>
            <div id="bundle-list" class="bundle-list">
            </div>
            <div class="export">
                Export to
                <a id="xls-report" href="#" onclick="generateExport('.csv')">CSV</a> |
                <a id="csv-report" href="#" onclick="generateExport('.xls')">Excel</a> &mdash;
                <span id="authorization-disclaimer">
                    Please note: this report only shows information for subjects and studies
                    you are authorized to see.
                </span>
            </div>
          
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>