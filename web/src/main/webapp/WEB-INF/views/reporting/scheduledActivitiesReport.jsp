<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:useBean id="potentialResponsibleUsers" scope="request"
             type="java.util.Collection<edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser>"/>
<jsp:useBean id="types" scope="request"
             type="java.util.Collection<edu.northwestern.bioinformatics.studycalendar.domain.ActivityType>"/>
<jsp:useBean id="modes" scope="request"
             type="java.util.Collection<edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode>"/>

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
    <tags:javascriptLink name="jquery/jquery.query" />

    <tags:resigTemplate id="new_label_autocompleter_row">
        <li>
            <span class="label-name">[#= label #]</span>
        </li>
    </tags:resigTemplate>

    <tags:stylesheetLink name="main"/>
    <tags:sassLink name="labels"/>

    <script type="text/javascript">
        function getUri(extension) {
            return psc.tools.Uris.relative("/api/v1/reports/scheduled-activities") + extension;
        }

        function filterParam(inputElement) {
            if (inputElement.getAttribute('filter-param')) {
                return inputElement.getAttribute('filter-param');
            } else {
                return inputElement.id;
            }
        }

        function getParams() {
            var params = {};
            jQuery('.filter-value.direct').each(function (i, e) {
                if (e.value && e.value.length > 0) {
                    params[filterParam(e)] = e.value;
                }
            });
            jQuery('input.filter-value.date').each(function (i, e) {
                var dateString = e.value;
                if (dateString && dateString.length > 0) {
                    params[filterParam(e)] = psc.tools.Dates.displayDateToApiDate(dateString);
                }
            });
            return params;
        }

        function generateExport(extension) {
            location.href = getUri(extension) +
                _(getParams()).inject(jQuery.query.empty(),
                    function (qs, v, k) { return qs.SET(k, v); });
        }

        function submitFilters() {
            var uri = getUri(".json");
            var params = getParams();

            SC.asyncRequest(uri, {
                method: "GET", parameters: params,
                onSuccess: function(response) {
                    var resp = response.responseJSON;
                    var reportColumns = [
                        { key: "activity_name", label: "Activity"},
                        { key: "activity_status", label: "Activity Status"},
                        { key: "scheduled_date", label:"Scheduled Date"},
                        { key: "last_change_reason", label: "Last Change Reason"},
                        { key: "details", label: "Details"},
                        { key: "condition", label: "Condition"},
                        { key: "labels", label: "Labels",
                            formatter: function (elCell, oRecord, oColumn, oData) {
                                elCell.innerHTML = oData ? oData.join(" ") : "";
                            }
                        },
                        { key: "ideal_date", label: "Ideal Date"},
                        { key: "subject_name", label: "Subject",
                            formatter: function (elCell, oRecord, oColumn, oData) {
                                elCell.innerHTML = oRecord.getData('subject').name;
                            }
                        },
                        { key: "person_id", label: "Person ID",
                            formatter: function (elCell, oRecord, oColumn, oData) {
                                elCell.innerHTML = oRecord.getData('subject').person_id;
                            }
                        },
                        { key: "study_subject_id", label: "Study Subject Id"},
                        { key: "responsible_user", label: "Responsible User"},
                        { key: "study", label: "Study"},
                        { key: "site", label: "Site"}
                    ];
                    var reportDS = new YAHOO.util.DataSource(resp);
                    reportDS.responseType = YAHOO.util.DataSource.TYPE_JSON;

                    reportDS.responseSchema = {
                        resultsList : "rows",
                        fields : [
                            { key: "activity_name"},
                            { key: "activity_status"},
                            { key: "scheduled_date"},
                            { key: "last_change_reason"},
                            { key: "details"},
                            { key: "condition"},
                            { key: "labels" },
                            { key: "ideal_date"},
                            { key: "subject"},
                            { key: "study_subject_id"},
                            { key: "responsible_user"},
                            { key: "study"},
                            { key: "site"}
                        ]
                    };

                    var reportDT = new YAHOO.widget.DataTable("results", reportColumns, reportDS, {scrollable:false});
                    reportDT.setAttributes({width:"100%"},true);
                },
                onFailure: function(response) {
                    var fullText = response.responseText;
                    var statusCode = response.status
                    var statusText = response.statusText
                    var userFriendlyText = fullText.replace(statusCode,  "");

                    userFriendlyText = userFriendlyText.replace(statusText, "");
                    $('errors').innerHTML = userFriendlyText.trim();
                }
            })
        }

        function resetFilters() {
            jQuery(".filter-value").each(function () { $(this).value = ""; });
            jQuery("#results").hide();
        }

        //need this method to avoid form submission on the enter key press for labels autocompleter
        function checkKey(event) {
            if (event.keyCode == Event.KEY_RETURN) {
                Event.stop(event)
            }
        }

        function setInitialFilterValues() {
            jQuery("#person-id")[0].value = jQuery.query.get("personId");
            jQuery("#start-date")[0].value = jQuery.query.get("startDate");
            jQuery("#end-date")[0].value = jQuery.query.get("endDate");
        }

        jQuery(function () {
            jQuery(".filter-value").each(function () { $(this).value = ""; });
            setInitialFilterValues();
            jQuery('#search-form').submit(function (evt) {
                evt.preventDefault();
                submitFilters();
                return false;
            });
            jQuery('#reset-button').click(resetFilters);
        })
    </script>
</head>
<body>
<laf:box title="Scheduled Activities Report" cssClass="yui-skin-sam">
    <laf:division>
        <c:set var="action"><c:url value="/pages/report/scheduledActivitiesReport"/></c:set>
        <form action="#none" id="search-form">
            <div id="errors"></div>
             <div class="search_box">
                 <input id="search-button" type="submit" value="Search" class="button"/>
                 <input id="reset-button" type="button" value="Reset filters"/>
            </div>
            <div class="search-filters">
                <div class="filterGroup">
                    <label class="filterInput">
                        Study name: <input id="study" class="filter-value direct"/>
                    </label>
                </div>
                <div class="filterGroup">
                    <label class="filterInput">
                        Site name: <input id="site" class="filter-value direct"/>
                    </label>
                </div>
                <div class="filterGroup">
                    <label class="filterInput">
                        Activity status:
                        <select id="state" class="filter-value direct">
                            <option></option>
                            <c:forEach items="${modes}" var="mode">
                                <option value="${mode.name}">${mode.name}</option>
                            </c:forEach>
                        </select>
                    </label>

                    <label class="filterInput">
                        Activity type:
                        <select id="activity-type" class="filter-value direct">
                            <option></option>
                            <c:forEach items="${types}" var="type">
                                <option value="${type.name}">${type.name}</option>
                            </c:forEach>
                        </select>
                    </label>

                    <label class="filterInput">
                        Activity label:
                        <input id="labels-autocompleter-input" type="text" autocomplete="off" class="autocomplete filter-value direct" filter-param="label"/>
                        <div id="labels-autocompleter-div" class="autocomplete"></div>
                    </label>
                </div>

                <div class="filterGroup">
                    <span class="filterInput">
                        <label>
                            Activities scheduled from:
                            <laf:dateInput path="start-date" cssClass="filter-value" local="true"/>
                        </label>
                        <label>
                            to
                            <laf:dateInput path="end-date" cssClass="filter-value" local="true"/>
                        </label>
                    </span>
                </div>

                <div class="filterGroup">
                    <label class="filterInput">
                        Responsible user:
                        <select id="responsible-user" class="filter-value direct">
                            <option></option>
                            <c:forEach items="${potentialResponsibleUsers}" var="sscm">
                                <option value="${sscm.name}">${sscm.name}</option>
                            </c:forEach>
                        </select>
                    </label>
                </div>

                <div class="filterGroup">
                    <label class="filterInput">
                        Person ID:
                        <input id="person-id" class="filter-value direct"/>
                    </label>
                </div>

            </div>

            <br style="clear:both"/>
            <div id="results" class="results">
            </div>
            <div id="export">
                Export to
                <a id="xls-report" href="#" onclick="generateExport('.csv')">CSV</a> |
                <a id="csv-report" href="#" onclick="generateExport('.xls')">Excel</a> &mdash;
                <span id="authorization-disclaimer">
                    Please note: this report only shows information for subjects and studies
                    you are authorized to see.
                </span>
            </div>
          
        </form>
    </laf:division>
</laf:box>
</body>
</html>