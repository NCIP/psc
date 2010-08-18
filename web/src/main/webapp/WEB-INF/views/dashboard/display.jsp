<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:useBean id="command" scope="request"
             type="edu.northwestern.bioinformatics.studycalendar.web.dashboard.DashboardCommand"/>
<jsp:useBean id="currentUser" scope="request"
             type="edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser"/>
<jsp:useBean id="configuration" scope="request"
             type="edu.northwestern.bioinformatics.studycalendar.configuration.Configuration"/>

<html>
<head>
    <title>Dashboard for ${command.user.displayName}</title>
    <tags:stylesheetLink name="main"/>
    <tags:sassLink name="dashboard"/>
</head>
<body>
<c:choose>
    <c:when test="${!command.colleagueDashboard}">
        <h1>Welcome, ${command.user.displayName}</h1>
    </c:when>
    <c:otherwise>
        <h1>The dashboard for your colleague ${command.user.displayName}</h1>
    </c:otherwise>
</c:choose>
<c:if test="${command.hasHiddenInformation}">
    <p>
        <em>Please note</em>:  This colleague has access to one or more studies or study subjects
        which you do not. Any information about those studies or subjects will not be displayed on
        this page.
    </p>
</c:if>

<%-- ////// AVAILABLE STUDIES --%>

<laf:box title="Available studies" id="available-studies">
    <laf:division>
        <p class="instructions">
            <c:choose>
                <c:when test="${empty command.assignableStudies}">
                    No studies are available for
                    ${command.colleagueDashboard ? command.user.displayName : 'you'} to track
                    patient schedules on. Talk to the Study Team Administrator to find out why.
                </c:when>
                <c:otherwise>
                    These are the studies and sites for which
                    ${command.colleagueDashboard ? command.user.displayName : 'you'} can see
                    subjects and assign new ones.
                </c:otherwise>
            </c:choose>
        </p>
    </laf:division>
    <c:forEach items="${command.assignableStudies}" var="entry">
        <h3><a href="<c:url value="/pages/cal/template?study=${entry.key.id}"/>">${entry.key.assignedIdentifier}</a></h3>
        <laf:division>
            <ul class="sites">
                <c:forEach items="${entry.value}" var="ussr" varStatus="siteStatus">
                    <li class="site ${siteStatus.index % 2 == 0 ? 'even' : 'odd'}">
                        ${ussr.studySite.site.name}
                        <div class="controls">
                            <c:if test="${ussr.canAssignSubjects && configuration.map.enableAssigningSubject}">
                                <a class="control" href="<c:url value="/pages/cal/assignSubject?site=${ussr.studySite.site.id}&study=${ussr.studySite.study.id}"/>">
                                    Assign ${empty ussr.visibleAssignments ? '' : 'another'} subject
                                </a>
                            </c:if>
                        </div>
                        <ul class="assignments">
                            <c:forEach items="${ussr.visibleAssignments}" var="ussar">
                                <c:if test="${not ussar.assignment.off}">
                                    <li class="assignment ${ussar.assignment.managerCsmUserId == command.user.csmUser.userId ? 'owned' : ''}">
                                        <a href="<c:url value="/pages/subject?subject=${ussar.assignment.subject.id}"/>">
                                            ${ussar.assignment.subject.fullName}
                                        </a>
                                    </li>
                                </c:if>
                            </c:forEach>
                        </ul>
                    </li>
                </c:forEach>
            </ul>
        </laf:division>
    </c:forEach>
</laf:box>

<%-- ////// COLLEAGUES --%>

<c:if test="${not empty command.colleagues}">
    <laf:box title="Colleagues" id="colleagues" autopad="true">
        <c:if test="${command.colleagueDashboard}">
            <p class="instructions">
                Please note, ${currentUser.displayName}: these are your colleagues, not those
                of ${command.user.displayName}.
            </p>
        </c:if>
        <ul>
            <c:forEach items="${command.colleagues}" var="colleague" varStatus="cStatus">
                <li class="colleague ${cStatus.index % 2 == 0 ? 'even' : 'odd'}">
                    <a href="<c:url value="/pages/dashboard?user=${colleague.username}"/>"
                       title="View the dashboard for ${colleague.displayName} (${colleague.username})">
                        ${colleague.displayName}
                    </a>
                </li>
            </c:forEach>
        </ul>
    </laf:box>
</c:if>
</body>
</html>