<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<html>
<title>Calendars</title>
<head>
    <tags:stylesheetLink name="main"/>
    <style type="text/css">
        ul ul.controls {
            display: inline;
        }
        ul.controls li {
            display: inline;
        }
        ul li ul.controls li {
            margin: 0;
            padding: 3px;
        }
        ul.menu {
            margin: 0;
            padding: 0;
        }
        ul.menu li {
            padding: 0.5em;
            list-style-type: none;
            margin: 0.5em;
        }
        ul.menu li .primary {
            display: block;
            float: left;
            width: 20%;
        }
        h2 {
            margin-top: 2em;
        }
    </style>
</head>
<body>
<laf:box title="Calendars">
    <laf:division>
        <security:secureOperation element="/pages/cal/newStudy">
            <p><a href="<c:url value="/pages/cal/newStudy"/>">Create a new template</a></p>
        </security:secureOperation>
     </laf:division>
        <c:if test="${not empty inDevelopmentTemplates}">
            <h3>Templates in design</h3>
            <laf:division>
                <ul class="menu">
                    <c:forEach items="${inDevelopmentTemplates}" var="template" varStatus="status">
                        <li class="autoclear ${commons:parity(status.count)}">
                            <a href="<c:url value="/pages/cal/template?study=${template.id}&amendment=${template.developmentAmendmentId}"/>">
                                ${template.displayName}
                            </a>
                        </li>
                    </c:forEach>
                </ul>
            </laf:division>
        </c:if>
        <c:if test="${not empty releasedTemplates}">
            <h3>Released templates</h3>
            <laf:division>
                <ul class="menu">
                    <c:forEach items="${releasedTemplates}" var="template" varStatus="status">
                        <li class="autoclear ${commons:parity(status.count)}">
                            <a href="<c:url value="/pages/cal/template?study=${template.id}"/>" class="primary">
                                ${template.displayName}
                            </a>
                            <ul class="controls">
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/assignSite" queryString="id=${template.id}">
                                    Assign sites
                                </tags:restrictedListItem>
                                <c:if test="${template.canAssignSubjects}">
                                    <tags:restrictedListItem cssClass="control" url="/pages/cal/assignSubject" queryString="id=${template.id}">
                                        Assign subjects
                                    </tags:restrictedListItem>
                                </c:if>
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/scheduleReconsent" queryString="study=${template.id}">
                                    Schedule reconsent
                                </tags:restrictedListItem>
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/amendment" queryString="study=${template.id}">
                                    Add amendment
                                </tags:restrictedListItem>
                            </ul>
                        </li>
                    </c:forEach>
                </ul>
            </laf:division>
        </c:if>
</laf:box>
</body>
</html>