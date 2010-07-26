<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="dash" tagdir="/WEB-INF/tags/dashboard/subjectcoordinator" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>

<html>
<head>
    <tags:stylesheetLink name="main"/>
    <tags:javascriptLink name="dashboards"/>

    <tags:includeScriptaculous/>
    <tags:javascriptLink name="resig-templates" />
    <tags:javascriptLink name="jquery/jquery.cookie"/>
    <tags:javascriptLink name="jquery/jquery-ui-1.7.2.custom.min"/>
    <tags:javascriptLink name="jquery/jquery.enumerable"/>
    <tags:stylesheetLink name="jquery/jquery-ui-1.7.2.custom"/>
    <tags:icsInstructions/>
    <title>Subject Coordinator Dashboard</title>
    <script type="text/javascript">
        SC.registerCurrentActivitiesUpdaters('<c:url value="/pages/dashboard/subjectCoordinatorSchedule"/>')
    </script>

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

        ul.menu li.noMargin {
            padding-left: 2em;
            list-style-type: none;
            margin: 0em;
        }

        ul.menu li.noMarginAtAllNotifications {
            list-style-type: none;
            margin: 0em;
            padding: 0.3em;
        }

        ul.menu li.noMarginForNotifications {
            list-style-type: none;
            /*margin: 0em;*/
            margin-left:1em;
            margin-right:0em;
            margin-bottom:0em;
            margin-top:0em;
            padding: 0.3em;
        }

        ul.menu li.subject {
            padding-left: 4em;
            list-style-type: none;
            padding-bottom: 0em;
            margin:0;
        }
        ul.menu li.controls {
            padding-left: 2em;
            list-style-type: none;
        }
        h2 {
            margin-top: 2em;
        }

        input.activity-type {
            margin-left:10px;
            vertical-align:middle;
        }

        .day h3{
            width:10%;
        }

        .site h3{
            margin-right:1em;
            width:8%;
        }

        tr, td {
            vertical-align:top;
        }

        .day ul.noMargin {
            margin:0em;
        }

        .h2Inline {
            width:20%;
            display:inline;
            cursor:pointer;
            /*font-size:0.6em;*/
        }

        .dismissNotification {
            margin-left:10px;
        }

        .currentActivity select#activityTypesList{
          vertical-align: top;
          margin: 0; padding: 0;
        }
 
    </style>
    <script type="text/javascript">

        function dismissNotification(notificationId){
            var data = "notificationId="+notificationId;
            var href = '<c:url value="/pages/dashboard/subjectCoordinatorSchedule/dismissNotification"/>'
            href= href+"?"+data
            var lastRequest = new Ajax.Request(href,
            {
                method: 'post'
            });
        }

        Event.observe(window, "load", registerHeaderCollapse);
    </script>    
</head>
<body>
    <div class="main">
        <h1>Welcome, ${userName.displayName}</h1>
    </div>
    <dash:pastDueActivities activities="${pastDueActivities}"/>
    <dash:notifications notifications="${notifications}" />

    <dash:currentActivities activityTypes="${activityTypes}" numberOfDays="7"/>
    <laf:box title="Available studies">
        <ul class="menu">
            <c:forEach items="${ownedStudies}" var="study" varStatus="status">

                <li class="day autoclear ${commons:parity(status.index)}">

                    <div class="section autoclear collapsible">
                        <a href="<c:url value="/pages/cal/template?study=${study.id}"/>" class="primary myclasstoo">
                            ${study.assignedIdentifier}
                        </a>
                        <h2 class="h2Inline"></h2>

                        <div class="content" style="display: none">
                            <laf:division>
                                <ul class="noMargin" >
                                    <c:forEach items="${ownedStudySites}" var="studySites" varStatus="ssStatus">
                                        <c:if test="${studySites.study.id == study.id}">
                                            <li class="noMargin ">
                                                 <h3 class="site">${studySites.site.name} </h3>
                                                 <c:if test="${configuration.map.enableAssigningSubject}">
                                                     <ul class="controls">
                                                         <c:choose>
                                                             <c:when test="${empty studySites.unapprovedAmendments}">
                                                                <tags:restrictedListItem cssClass="control" url="/pages/cal/assignSubject" queryString="study=${study.id}&site=${studySites.site.id}">Assign subject</tags:restrictedListItem>
                                                             </c:when>
                                                             <c:otherwise>
                                                                 This template has not been approved yet
                                                             </c:otherwise>
                                                         </c:choose>
                                                    </ul>
                                                </c:if>
                                                <c:forEach items="${studySites.studySubjectAssignments}" var="listOfSubjects" varStatus="listOfSubjectsStatus">
                                                    <c:choose>
                                                        <c:when test="${not empty listOfSubjects}">
                                                         <li class="subject">

                                                             <%--should display only subjects that are on the study, for that using all those checkings (if subject has any scheduled or conditional activities)--%>
                                                             <c:set var="isAnyActivityAssigned" value="false"/>
                                                             <c:forEach items="${listOfSubjects.scheduledCalendar.scheduledStudySegments}" var="studySegmentsAssignment">
                                                                <c:forEach items="${studySegmentsAssignment.activities}" var="activity">
                                                                    <c:if test="${activity.currentState.mode eq 'scheduled' or activity.currentState.mode eq 'conditional'}">
                                                                        <c:set var="isAnyActivityAssigned" value="true"/>
                                                                    </c:if>
                                                                 </c:forEach>
                                                               </c:forEach>
                                                             <c:if test="${isAnyActivityAssigned}">
                                                                 <%--<a href="<c:url value="/pages/cal/schedule?calendar=${listOfSubjects.scheduledCalendar.id}"/>" class="primary">--%>
                                                                 <a href="<c:url value="/pages/subject?subject=${listOfSubjects.subject.id}"/>" class="primary">
                                                                    ${listOfSubjects.subject.firstName}
                                                                    ${listOfSubjects.subject.lastName}
                                                                 </a>
                                                             </c:if>
                                                         </li>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <h3>You have no subjects on this study</h3>
                                                    </c:otherwise>
                                                    </c:choose>
                                                </c:forEach>
                                            </li>
                                        </c:if>
                                    </c:forEach>
                                </ul>
                            </laf:division>
                        </div>
                    </div>
                </li>
            </c:forEach>
        </ul>
    </laf:box>
    <c:if test="${not empty colleguesStudies}">
        <laf:box title="Colleagues - Subject Coordinators">
            <ul class="menu">
                <c:forEach items="${colleguesStudies}" var="mapOfUsersAndStudies" varStatus="status">
                    <li class="autoclear ">
                        <a href="<c:url value="/pages/dashboard/colleagueSubjectCoordinator?id=${mapOfUsersAndStudies.key.id}"/>"> ${mapOfUsersAndStudies.key.displayName} </a>
                    </li>
                </c:forEach>
            </ul>
        </laf:box>
    </c:if>
    <c:if test="${not empty userName.studySubjectAssignments}">
        <laf:box title="Export ICS">
            <div class="row even">
                <div class="value">
                    <a class="control ics-subscribe"
                       href="<c:url value="/api/v1/users/${userName.username}/roles/subject-coordinator/schedules.ics"/>"
                       title="Subscribe as ICS for iCal, Outlook and other calendar applications">Subscribe</a>
                    <a class="control"
                       href="<c:url value="/api/v1/users/${userName.username}/roles/subject-coordinator/schedules.ics"/>"
                       title="Download as ICS for one-time import into iCal, Outlook and other calendar applications">Export ICS</a>
                </div>
            </div>
        </laf:box>
    </c:if>
</body>
</html>