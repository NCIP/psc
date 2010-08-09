<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:useBean id="studies" scope="request"
             type="java.util.Collection<edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship>"/>
<jsp:useBean id="dashboardUser" scope="request" 
             type="edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser"/>

<html>
<head>
    <title>Dashboard for ${dashboardUser.displayName}</title>
    <tags:stylesheetLink name="main"/>
    <tags:sassLink name="dashboard"/>
</head>
<body>
<h1>Welcome, ${dashboardUser.displayName}</h1>

<%-- ////// AVAILABLE STUDIES --%>

<laf:box title="Available studies" id="available-studies">
    <laf:division>
        <p class="instructions">
            <c:choose>
                <c:when test="${empty studies}">
                    No studies are available for you to track patient schedules on.
                    Talk to your Study Team Administrator to find out why.
                </c:when>
                <c:otherwise>
                    These are the studies and sites for which you can see subjects and
                    assign new ones.
                </c:otherwise>
            </c:choose>
        </p>
    </laf:division>
    <c:forEach items="${studies}" var="utr">
        <h3>${utr.study.name}</h3>
        <laf:division>
            <ul class="sites">
                <c:forEach items="${utr.subjectAssignableStudySites}" var="ussr" varStatus="siteStatus">
                    <li class="site ${siteStatus.index % 2 == 0 ? 'even' : 'odd'}">
                        ${ussr.studySite.site.name}
                        <div class="controls">
                            <c:if test="${ussr.canAssignSubjects}">
                                <a class="control" href="<c:url value="/pages/cal/assignSubject?site=${ussr.studySite.site.id}&study=${ussr.studySite.study.id}"/>">
                                    Assign ${empty ussr.visibleAssignments ? '' : 'another'} subject
                                </a>
                            </c:if>
                        </div>
                        <ul class="assignments">
                            <c:forEach items="${ussr.visibleAssignments}" var="ussar">
                                <c:if test="${not ussar.assignment.off}">
                                    <li class="assignment ${ussar.calendarManager ? 'owned' : ''}">
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
</body>
</html>