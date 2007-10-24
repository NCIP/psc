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
        <c:if test="${not empty inDevelopmentStudies}">
            <h3>Templates in design</h3>
            <laf:division>
                <ul class="menu">
                    <c:forEach items="${inDevelopmentStudies}" var="study" varStatus="status">
                        <li class="autoclear ${commons:parity(status.count)}">
                            <a href="<c:url value="/pages/cal/template?study=${study.id}"/>">${study.name}</a>
                            <c:if test="${study.inAmendmentDevelopment}">(${study.developmentAmendment.name})</c:if>
                        </li>
                    </c:forEach>
                </ul>
            </laf:division>
        </c:if>
        <c:if test="${not empty assignableStudies}">
            <h3>Available templates</h3>
            <laf:division>
                <ul class="menu">
                    <c:forEach items="${assignableStudies}" var="study" varStatus="status">
                        <li class="autoclear ${commons:parity(status.count)}">
                            <a href="<c:url value="/pages/cal/template?study=${study.id}"/>" class="primary">
                                ${study.name}
                                <c:if test="${study.amended}">(${study.amendment.name})</c:if>
                            </a>
                            <ul class="controls">
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/assignSite" queryString="id=${study.id}">Assign sites</tags:restrictedListItem>
                                <c:if test="${not empty study.studySites}">
                                    <tags:restrictedListItem cssClass="control" url="/pages/dashboard/siteCoordinatorSchedule" queryString="id=${study.id}">Assign participant coordinators</tags:restrictedListItem>
                                    <tags:restrictedListItem cssClass="control" url="/pages/cal/assignParticipant" queryString="id=${study.id}">Assign participants</tags:restrictedListItem>
                                </c:if>
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/scheduleReconsent" queryString="study=${study.id}">Schedule reconsent</tags:restrictedListItem>
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/amendment" queryString="study=${study.id}">Add amendment</tags:restrictedListItem>
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