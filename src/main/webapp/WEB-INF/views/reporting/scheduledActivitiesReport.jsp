<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
    <style type="text/css">
        .search_box {
            float:right;
            border: 1px dotted #ccc;
            width:8em;
            height:6em;
            padding:1em;
        }

        .search-filters {
            float:left;
            width:85%
        }

        input.button {
            display:block;
            margin: 0.5em 0;
        }

        table.query-results    { width: 100%; border: 0; border-spacing: 1px; }
        table.query-results td, table.query-results th { white-space: nowrap }
        table.query-results td { vertical-align: top; padding: 1px; }
        table.query-results th { text-align: center; vertical-align: bottom; padding: 2px; }
        table.query-results th a {
            display: block;
            color: white; text-decoration: none;
        }
        table.query-results th.sorted a, table.query-results th.sortable a {
            background-position: right;
            padding: 0;
            margin-right: 14px; /* for arrow image */
            width: 100%;
        }
        table.query-results tr.even td.highlighted { background-color: #7bf; }

        table.query-results ul {
            padding: 0; margin: 0;
        }
        table.query-results li {
            list-style-type: none;
        }
        table.query-results td.numeric, table.query-results td.date {
            text-align: right;
        }

        table { border: 0px; }
        th { font-weight: bold; color: white; background-color: navy; }
        th, td { text-align: left; padding: 1px 3px; }

        .even { background: lightblue; }
        .odd  { background: lavender; }

        .filterInput {
            font-size: 10pt;
            white-space: nowrap ;
            padding: 0 0.5em;
        }

        .filterGroup {
            padding: 0.5em 0.5em ;
            margin: 0.7em 0;
            background-color: #ddc ;
            color: #666 ;
            font-weight: bold ;
        }

    </style>
</head>
<body>
<laf:box title="Scheduled Activities Report">
    <laf:division>
        <form:form method="post">
            <tags:errors path="*"/>
             <div class="search_box">
                    <input type="submit" value="Search" class="button"/>
                 ${fn:length(results)} results
                 <input id="resetButton" class="button" type="reset" value="Reset Filters"/>
            </div>
            <div class="search-filters">
                <div class="filterGroup">
                    <span class="filterInput">
                        <form:label path="filters.studyAssignedIdentifier" >
                            Study name:
                        </form:label>

                        <form:input path="filters.studyAssignedIdentifier"/>
                    </span>
                </div>
                <div class="filterGroup">
                    <span class="filterInput">
                        <form:label path="filters.siteName" >
                            Site name:
                        </form:label>
                        <form:input path="filters.siteName"/>
                    </span>
                </div>
                <div class="filterGroup">
                    <span class="filterInput">
                        <form:label path="filters.currentStateMode" >
                            Activity status:
                        </form:label>

                        <form:select path="filters.currentStateMode" >
                            <form:option value="" label=""/>
                            <form:options items="${modes}" itemLabel="name" itemValue="id"/>
                        </form:select>

                    </span>
                </div>
            </div>

            <br style="clear:both"/>

            <display:table name="results" class="query-results">
                <display:column property="scheduledActivity.activity.name" title="Activity Name"/>
                <display:column property="scheduledActivity.currentState.mode.displayName" title="Activity Status"/>
                <display:column property="scheduledActivity.actualDate" title="Scheduled Date"/>
                <display:column property="scheduledActivity.actualDate" title="Ideal Date"/>
                <display:column property="subject.lastFirst" title="Subject Name"/>
                <display:column property="subject.personId" title="Subject Id"/>
                <display:column property="subjectCoordinatorName" title="Subject Coordinator Name"/>
                <display:column property="study.assignedIdentifier" title="Study Name"/>
                <display:column property="site.name" title="Site Name"/>
            </display:table>

        </form:form>
    </laf:division>
</laf:box>
</body>
</html>