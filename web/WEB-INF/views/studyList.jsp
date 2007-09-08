<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<html>
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
        <security:secureOperation element="/pages/cal/newStudy" operation="ACCESS">
            <p><a href="<c:url value="/pages/cal/newStudy"/>">Create a new template</a></p>
        </security:secureOperation>
     </laf:division>
        <security:secureOperation element="/pages/cal/markComplete" operation="ACCESS">
            <c:if test="${not empty incompleteStudies}">
                <h3>Templates in design</h3>
                <laf:division>
                    <ul class="menu">
                        <c:forEach items="${incompleteStudies}" var="study" varStatus="status">
                            <li class="autoclear ${commons:parity(status.count)}">
                                <a href="<c:url value="/pages/cal/template?study=${study.id}"/>">${study.name}</a>
                            </li>
                        </c:forEach>
                    </ul>
                </laf:division>
            </c:if>
        </security:secureOperation>
        <c:if test="${not empty completeStudies}">
            <h3>Completed templates</h3>
            <laf:division>
                <ul class="menu">
                    <c:forEach items="${completeStudies}" var="study" varStatus="status">
                        <li class="autoclear ${commons:parity(status.count)}">
                            <a href="<c:url value="/pages/cal/template?study=${study.id}"/>" class="primary">${study.name}</a>
                            <ul class="controls">
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/assignSite" queryString="id=${study.id}">Assign sites</tags:restrictedListItem>
                                <c:if test="${not empty study.studySites}">
                                    <tags:restrictedListItem cssClass="control" url="/pages/cal/assignParticipantCoordinator" queryString="id=${study.id}">Assign participant coordinators</tags:restrictedListItem>
                                    <tags:restrictedListItem cssClass="control" url="/pages/cal/assignParticipant" queryString="id=${study.id}">Assign participants</tags:restrictedListItem>
                                </c:if>
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/scheduleReconsent" queryString="study=${study.id}">Schedule Reconsent</tags:restrictedListItem>
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/amendment" queryString="study=${study.id}">Amend Template</tags:restrictedListItem>
                            </ul>
                        </li>
                    </c:forEach>
                </ul>
            </laf:division>
        </c:if>

    <c:if test="${not empty amendedStudies}">
        <h3>Amended templates</h3>
        <laf:division>
            <ul class="menu">
                <c:forEach items="${amendedStudies}" var="study" varStatus="status">
                    <li class="autoclear ${commons:parity(status.count)}">
                        <a href="<c:url value="/pages/cal/template?study=${study.id}"/>" class="primary">${study.name}</a>
                        <ul class="controls">
                            <tags:restrictedListItem cssClass="control" url="/pages/cal/assignSite" queryString="id=${study.id}">Amend sites</tags:restrictedListItem>
                            <c:if test="${not empty study.studySites}">
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/assignParticipantCoordinator" queryString="id=${study.id}">Assign participant coordinators</tags:restrictedListItem>
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/assignParticipant" queryString="id=${study.id}">Assign participants</tags:restrictedListItem>
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/scheduleReconsent" queryString="study=${study.id}">Schedule Reconsent</tags:restrictedListItem>
                            </c:if>
                        </ul>
                    </li>
                </c:forEach>
            </ul>
        </laf:division>
    </c:if>


        <c:if test="${not empty sites}">
            <h3>Sites</h3>
            <laf:division>
                <ul class="menu">
                    <c:forEach items="${sites}" var="site" varStatus="status">
                        <li class="autoclear ${commons:parity(status.count)}"><span class="primary">${site.name}</span>
                            <ul class="controls">
                                <tags:restrictedListItem url="/pages/cal/assignParticipantCoordinatorsToSite" queryString="id=${site.id}" cssClass="control">
                                    Assign participant coordinators
                                </tags:restrictedListItem>
                                <tags:restrictedListItem url="/pages/cal/siteParticipantCoordinatorList" queryString="id=${site.id}" cssClass="control">
                                    Assign study templates to participant coordinators
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