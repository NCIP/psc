<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

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

    <%--<tags:stylesheetLink name="report" dynamic="true"/>--%>
    <script type="text/javascript">

        var bundleList;

        function submitFilters() {
            var uri = SC.relativeUri("/api/v1/reports/scheduled-activities")
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
            if (endDate != null && startDate.length > 0) {
                endDate = psc.tools.Dates.displayDateToApiDate($("actual-date-start").value)
            }

            params['end-date'] = endDate;
            params['responsible-user'] =  $("filters.subjectCoordinator").value;

            SC.asyncRequest(uri +".json", {
              method: "GET", parameters: params,
              onSuccess: function(response) {
                   var bundleListColumns = [
                        { key: "activity_name", label: "Activity Name", sortable: true},
                        { key: "activity_status", label: "Activity Status", sortable: true },
                        { key: "scheduled_date", label:"Scheduled Date", sortable:true},
                        { key: "ideal_date", label: "Ideal Date", sortable: true},
                        { key: "label", label: "Label", sortable: true},
                        { key: "subject_name", label: "Subject Name", sortable: true},
                        { key: "subject_id", label: "Subject Id", sortable: true},
                        { key: "subject_coorinator_name", label: "Subject Coordinator Name", sortable: true},
                        { key: "study", label: "Study Name", sortable: true},
                        { key: "site", label: "Site Name", sortable: true}
                   ];
                   var myDataSource = new YAHOO.util.DataSource(response.responseJSON);
                    myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;

                    myDataSource.responseSchema = {
                          resultsList : "rows",
                          fields : [
                              { key: "activity_name"},
                              { key: "activity_status"},
                              { key: "scheduled_date"},
                              { key: "ideal_date"},
                              { key: "label" },
                              { key: "subject_name"},
                              { key: "subject_id"},
                              { key: "subject_coorinator_name"},
                              { key: "study"},
                              { key: "site"}
                          ]
                      };

                      bundleList = new YAHOO.widget.DataTable("bundle-list", bundleListColumns, myDataSource, {scrollable:true});

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
           $("qurey-result-display").hide();
           $("searchResult").hide();
        }

        //need this method to avoid form submission on the enter key press for labels autocompleter
        function checkKey(event) {
            if (event.keyCode == Event.KEY_RETURN) {
                Event.stop(event)
            }
        }
    </script>
</head>
<body>
<laf:box title="Scheduled Activities Report" cssClass="yui-skin-sam">
    <laf:division>
        <c:set var="action"><c:url value="/pages/report/scheduledActivitiesReport"/></c:set>
        <form:form action="${action}"method="post" onsubmit="return false">
            <tags:errors path="*"/>
             <div class="search_box">
                 <input type="submit" value="Search" class="button" onclick="submitFilters()"/>
                    <span id="searchResult">${fn:length(results)} results</span>
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
                        <form:label path="filters.subjectCoordinator" >
                            Subject coordinator:
                        </form:label>

                        <form:select path="filters.subjectCoordinator" id="filters.subjectCoordinator" >
                            <form:option value="" label=""/>
                            <form:options items="${coordinators}" itemLabel="displayName" itemValue="id"/>
                        </form:select>

                    </span>
                </div>

            </div>

          <br style="clear:both"/>
          <div id="bundle-list" class="bundle-list">
          </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>