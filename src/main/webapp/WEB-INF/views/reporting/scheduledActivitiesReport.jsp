<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<html>
<head>
    <style type="text/css">
        .search_box {
            float:right;
            border: 1px dotted #ccc;
            width:8em;
            height:8em;
            padding:1em
        }
        .results th {
            font-weight:bold
        }
    </style>
</head>
<body>
<laf:box title="Administration">
    <laf:division>
        <form:form method="post">
            <tags:errors path="*"/>
             <div class="search_box">
                    <input type="submit"
                           value="Search"/>
            </div>
            <div class="row">
                <div class="label" >
                    <%--<form:label path="command.filter.activityMode" >--%>
                    Activity Mode:
                    <%--</form:label>--%>
                </div>
                <div class="value">
                    <select name="filter_modes">
                        <option value=""></option>
                        <c:forEach items="${modes}" var="mode">
                            <option value="${mode.id}">${mode.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="row">
                <div class="label" >
                    <form:label path="filters.studyAssignedIdentifier" >
                    Study name:
                    </form:label>
                </div>
                <div class="value">
                    <!--StudyIdentifierName-->
                    <form:input path="filters.studyAssignedIdentifier"/>
                </div>
            </div>

            <div>
                <!--TODO: Remove temporary output table, and use some sort of table rendering tag-->
                <table border="1" class="results">
                    <tr>
                        <th>Schd. Act. Id</th>
                        <th>Activity Name</th>
                        <th>Activity Status</th>
                        <th>Subject First Name</th>
                        <th>Subject Last Name</th>
                        <th>Subject Patient Id</th>
                        <th>Study Name</th>
                        <th>Site Name</th>
                    </tr>
                    <c:forEach items="${results}" var="row">
                        <tr>
                            <td>${row.id}</td>
                            <td>${row.scheduledActivity.activity.name}</td>
                            <td>${row.scheduledActivity.currentState.textSummary}</td>
                            <td>${row.subject.firstName}</td>
                            <td>${row.subject.lastName}</td>
                            <td>${row.subject.personId}</td>
                            <td>${row.study.assignedIdentifier}</td>
                            <td>${row.site.assignedIdentifier}</td>
                        </tr>
                    </c:forEach>
                </table>
            </div>

        </form:form>
    </laf:division>
</laf:box>
</body>
</html>