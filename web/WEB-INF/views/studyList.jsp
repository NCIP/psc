<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>
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
<h1>Calendar Menu</h1>

<h2>Calendar templates</h2>
<security:secureOperation element="/pages/newStudy" operation="ACCESS">
    <p><a href="<c:url value="/pages/newStudy"/>">Create a new template</a></p>
</security:secureOperation>

<security:secureOperation element="/pages/markComplete" operation="ACCESS">
<h3>Templates in design</h3>
<ul class="menu">
<c:forEach items="${incompleteStudies}" var="study" varStatus="status">
    <li class="autoclear ${commons:parity(status.count)}">
        <a href="<c:url value="/pages/template?study=${study.id}"/>">${study.name}</a>
    </li>
</c:forEach>
</ul>
</security:secureOperation>

<h3>Completed templates</h3>
<ul class="menu">
<c:forEach items="${completeStudies}" var="study" varStatus="status">
    <li class="autoclear ${commons:parity(status.count)}">
        <a href="<c:url value="/pages/template?study=${study.id}"/>" class="primary">${study.name}</a>
        <ul class="controls">
            <tags:restrictedListItem cssClass="control" url="/pages/assignSite" queryString="id=${study.id}">Assign sites</tags:restrictedListItem>
            <c:if test="${not empty study.studySites}">
                <tags:restrictedListItem cssClass="control" url="/pages/assignParticipantCoordinator" queryString="id=${study.id}">Assign participant coordinators</tags:restrictedListItem>
                <tags:restrictedListItem cssClass="control" url="/pages/assignParticipant" queryString="id=${study.id}">Assign participants</tags:restrictedListItem>
                <tags:restrictedListItem cssClass="control" url="/pages/scheduleReconsent" queryString="study=${study.id}">Schedule Reconsent</tags:restrictedListItem>
            </c:if>
        </ul>
    </li>
</c:forEach>
</ul>

<c:if test="${not empty sites}">
<h2>Sites</h2>
<ul class="menu">
    <c:forEach items="${sites}" var="site" varStatus="status">
    <li class="autoclear ${commons:parity(status.count)}"><span class="primary">${site.name}</span>
        <ul class="controls">
            <tags:restrictedListItem url="/pages/assignParticipantCoordinatorsToSite" queryString="id=${site.id}" cssClass="control">
                Assign participant coordinators
            </tags:restrictedListItem>
            <tags:restrictedListItem url="/pages/siteParticipantCoordinatorList" queryString="id=${site.id}" cssClass="control">
                Assign study templates to participant coordinators
            </tags:restrictedListItem>
        </ul>
    </li>
    </c:forEach>
</ul>
</c:if> 

<security:secureOperation element="/pages/reportBuilder" operation="ACCESS">
<h2>Reporting</h2>
<ul class="menu">
    <tags:restrictedListItem url="/pages/reportBuilder">Report Builder</tags:restrictedListItem>
</ul>
</security:secureOperation>

</body>
</html>