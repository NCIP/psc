<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
    <tags:stylesheetLink name="report"/>
    <%--<tags:stylesheetLink name="report" dynamic="true"/>--%>
    <style type="text/css">
        table.query-results th.sortable a { background-image: url(<c:url value="/images/arrow_off.png"/>) }
        table.query-results th.order1 a { background-image: url(<c:url value="/images/arrow_down.png"/>) }
        table.query-results th.order2 a { background-image: url(<c:url value="/images/arrow_up.png"/>) }
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

                    <span class="filterInput">
                        <form:label path="filters.activityType" >
                            Activity type:
                        </form:label>

                        <form:select path="filters.activityType" >
                            <form:option value="" label=""/>
                            <form:options items="${types}" itemLabel="name" itemValue="id"/>
                        </form:select>
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

                        <form:select path="filters.subjectCoordinator" >
                            <form:option value="" label=""/>
                            <form:options items="${coordinators}" itemLabel="displayName" itemValue="id"/>
                        </form:select>

                    </span>
                </div>

            </div>



            <br style="clear:both"/>

            <display:table name="results" class="query-results" id="row" requestURI="scheduledActivitiesReport" export="true">
                <display:column property="scheduledActivity.activity.name" title="Activity Name" sortable="true" media="csv, excel"/>
                <display:column property="scheduledActivity.currentState.mode.displayName" title="Activity Status"  sortable="true"/>
                <display:column title="Scheduled Date"  sortable="true">
                    <c:if test="${row.scheduledActivity.currentState.mode.name == 'canceled'}">
                        -
                    </c:if>
                    <c:if test="${row.scheduledActivity.currentState.mode.name != 'canceled'}">
                        <tags:formatDate value="${row.scheduledActivity.actualDate}"/>
                    </c:if>
                </display:column>
                <display:column title="Ideal Date"  sortable="true">
                    <tags:formatDate value="${row.scheduledActivity.idealDate}"/>
                </display:column>
                <display:column property="subject.lastFirst" title="Subject Name"  sortable="true"/>
                <display:column property="subject.personId" title="Subject Id"  sortable="true"/>
                <display:column property="subjectCoordinatorName" title="Subject Coordinator Name"  sortable="true"/>
                <display:column property="study.assignedIdentifier" title="Study Name" sortable="true"/>
                <display:column property="site.name" title="Site Name"  sortable="true"/>
            </display:table>

        </form:form>
    </laf:division>
</laf:box>
</body>
</html>