<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="commons" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/functions" %>

<jsp:useBean id="command" scope="request"
             type="edu.northwestern.bioinformatics.studycalendar.web.dashboard.DashboardCommand"/>
<jsp:useBean id="activityTypes" scope="request"
             type="java.util.List<edu.northwestern.bioinformatics.studycalendar.domain.ActivityType>"/>
<jsp:useBean id="currentUser" scope="request"
             type="edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser"/>
<jsp:useBean id="configuration" scope="request"
             type="edu.northwestern.bioinformatics.studycalendar.configuration.Configuration"/>

<html>
<head>
    <title>Dashboard for ${command.user.displayName}</title>
    <tags:stylesheetLink name="main"/>
    <tags:sassLink name="dashboard"/>

    <tags:javascriptLink name="jquery/jquery.query" />
    <tags:javascriptLink name="psc-tools/misc"/>
    <tags:javascriptLink name="resig-templates"/>
    <tags:javascriptLink name="dashboard/main"/>
    <tags:javascriptLink name="dashboard/past-due"/>
    <tags:javascriptLink name="dashboard/upcoming"/>
    <tags:javascriptLink name="dashboard/notifications"/>

    <tags:resigTemplate id="past_due_subject">
        <li class="past-due-subject">
            <a href="<c:url value="/pages/subject?subject=[#= subject.grid_id #]"/>">[#= subject.name #]</a>
            has [#= count #] past due activit[#= count == 1 ? 'y' : 'ies' #].
            [#= (count == 1) ? 'It is from' : 'The earliest is from' #]
            [#= psc.tools.Dates.apiDateToDisplayDate(earliestApiDate) #].
        </li>
    </tags:resigTemplate>

    <tags:resigTemplate id="upcoming_day">
        <h3>
            [#= psc.tools.Dates.weekdayName(psc.tools.Dates.apiDateToUtc(day)) #]
            [#= psc.tools.Dates.apiDateToDisplayDate(day) #]
        </h3>
        <laf:division>
            <ul class="upcoming-day">
                [# _(subjects).each(function (subject, i) { #]
                    <li class="subject autoclear [#= (i % 2 == 0 ? 'even' : 'odd') #]">
                        <a href="<c:url value="/pages/subject?subject=[#= subject.subject_grid_id #]"/>">
                            [#= subject.subject_name #]
                        </a>
                        <ul class="subject-day-activities">
                            [# _(subject.activities).each(function (activity) { #]
                                <li>
                                    <a href="<c:url value=""/>">
                                        <a href="<c:url value="/pages/cal/scheduleActivity?event=[#= activity.id #]"/>">[#= activity.activity_name #]</a>
                                    </a>
                                </li>
                            [# }); #]
                        </ul>
                    </li>
                [# }); #]
            </ul>
        </laf:division>
    </tags:resigTemplate>

    <tags:resigTemplate id="none_upcoming">
        <laf:division>
            There are no pending scheduled activities matching the given criteria.
        </laf:division>
    </tags:resigTemplate>

    <tags:resigTemplate id="error_upcoming">
        <laf:division>
            <p class="error">Error retrieving upcoming activities: [#= error #]</p>
        </laf:division>
    </tags:resigTemplate>

    <script type="text/javascript">
        jQuery(function () {
            psc.dashboard.Main.init('${command.user.username}');
            psc.dashboard.PastDue.load();
            psc.dashboard.Upcoming.init();
            psc.dashboard.Notifications.init();
        });
    </script>
</head>
<body>
<div id="loading">
    <tags:activityIndicator/> Loading
</div>
<c:choose>
    <c:when test="${!command.colleagueDashboard}">
        <h1 class="autoclear">Welcome, ${command.user.displayName}</h1>
    </c:when>
    <c:otherwise>
        <h1 class="autoclear">The dashboard for your colleague ${command.user.displayName}</h1>
    </c:otherwise>
</c:choose>
<c:if test="${command.hasHiddenInformation}">
    <p>
        <em>Please note</em>:  This colleague has access to one or more studies or study subjects
        which you do not. Any information about those studies or subjects will not be displayed on
        this page.
    </p>
</c:if>

<%-- ////// PAST DUE --%>

<laf:box title="Past due activities" id="past-due" autopad="true">
    <ul id="past-due-subjects"></ul>
</laf:box>

<%-- ////// NOTIFICATIONS --%>

<c:if test="${not empty command.pendingNotifications}">
    <laf:box title="Unaddressed notifications" id="notifications" autopad="true">
        <div id="notification-error" class="error"></div>
        <ul id="notification-subjects">
            <c:forEach items="${command.pendingNotifications}" var="entry" varStatus="noteStatus">
                <li class="notification-subject ${commons:parity(noteStatus.index)}">
                    <a href="<c:url value="/pages/subject?subject=${entry.key.gridId}"/>">${entry.key.fullName}</a>
                    <ul class="subject-notifications">
                        <c:forEach items="${entry.value}" var="notification">
                            <li id="notification-${notification.id}" class="notification">
                                <%-- TODO: the notification system really needs refactoring.  This is gross. --%>
                                <c:choose>
                                    <c:when test="${fn:contains(notification.message,'pages/cal')}">
                                        <a href= "<c:url value="${notification.message}"/>">
                                            ${notification.title}
                                        </a>
                                    </c:when>
                                    <c:otherwise>${notification.message}</c:otherwise>
                                </c:choose>
                                <span class="controls">
                                    <a class="control dismiss" href="#">Dismiss</a>
                                </span>
                            </li>
                        </c:forEach>
                    </ul>
                </li>
            </c:forEach>
        </ul>
    </laf:box>
</c:if>

<%-- ////// UPCOMING --%>

<laf:box title="Upcoming activities" id="upcoming">
    <laf:division>
        <p><label>
            Show activities for the next <input id="upcoming-days" size="3" value="7"/> days.
        </label></p>
        <p>
            <c:choose>
                <c:when test="${fn:length(activityTypes) >= 8 || param['long-types']}">
                    <label id="activity-type-multiple">
                        From these activity types:
                        <select name="activity-types" multiple size="4">
                            <c:forEach items="${activityTypes}" var="type">
                                <option selected>${type.name}</option>
                            </c:forEach>
                        </select>
                    </label>
                    <a href="#" class="control" id="upcoming-all">all</a>
                    <a href="#" class="control" id="upcoming-none">none</a>
                </c:when>
                <c:otherwise>
                    From these activity types:
                    <c:forEach items="${activityTypes}" var="type">
                        <label>
                            <input type="checkbox" name="activity-types" value="${type.name}" checked/>
                            ${type.name}
                        </label>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </p>
    </laf:division>
    <div id="upcoming-activity-days"></div>
</laf:box>

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
                                <li class="assignment ${ussar.assignment.managerCsmUserId == command.user.csmUser.userId ? 'owned' : 'unowned'} ${ussar.assignment.off ? 'off-study' : 'on-study'}">
                                    <a href="<c:url value="/pages/subject?subject=${ussar.assignment.subject.id}"/>">
                                        ${ussar.assignment.subject.fullName}
                                    </a>
                                    <c:if test="${ussar.assignment.off}">(off study)</c:if>
                                </li>
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

<%-- ////// EXPORT --%>

<c:if test="${not empty command.managedAssignments}">
    <laf:box title="Export to Calendar Applications" autopad="true">
        <a class="control ics-subscribe"
           href="<c:url value="/api/v1/users/${command.user.username}/roles/subject-coordinator/schedules.ics"/>"
           title="Subscribe as ICS for iCal, Outlook and other calendar applications">Subscribe</a>
        <a class="control"
           href="<c:url value="/api/v1/users/${command.user.username}/roles/subject-coordinator/schedules.ics"/>"
           title="Download as ICS for one-time import into iCal, Outlook and other calendar applications">Download ICS</a>
    </laf:box>
</c:if>

</body>
</html>