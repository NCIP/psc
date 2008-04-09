<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
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
                    <form:label path="filters.currentStateMode" >
                    Activity Status:
                    </form:label>
                </div>
                <div class="value">
                    <form:select path="filters.currentStateMode" >
                        <form:option value="" label=""/>
                        <form:options items="${modes}" itemLabel="name" itemValue="id"/>
                    </form:select>
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

            <display:table name="results">
                <display:column property="id" title="ID" />
                <display:column property="scheduledActivity.activity.name" title="Activity Name"/>
                <display:column property="scheduledActivity.currentState.textSummary" title="Summary"/>
                <display:column property="subject.firstName" title="First Name"/>
                <display:column property="subject.lastName" title="Last Name"/>
                <display:column property="subject.personId" title="Person Id"/>
                <display:column property="study.assignedIdentifier" title="Study Name"/>
                <display:column property="site.name" title="Site Name"/>
            </display:table>

        </form:form>
    </laf:division>
</laf:box>
</body>
</html>