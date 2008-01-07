<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="dash" tagdir="/WEB-INF/tags/dashboard/subjectcoordinator" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>

<html>
<head>
    <tags:stylesheetLink name="main"/>
    <tags:javascriptLink name="dashboards"/>

    <tags:includeScriptaculous/>

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
            width:30%;
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

 
    </style>
    <script type="text/javascript">
        Event.observe(window, "load", registerHeaderCollapse);
    </script>    
</head>
<body>
    <div class="main">
        <h1>Welcome, ${userName.name}</h1>
    </div>
    <dash:pastDueActivities activities="${pastDueActivities}"/>
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
                                    <c:forEach items="${study.studySites}" var="studySites" varStatus="studySiteStatus">

                                        <li class="noMargin ">
                                             <h3 class="site">${studySites.site.name} </h3>

                                             <ul class="controls">
                                                <tags:restrictedListItem cssClass="control" url="/pages/cal/assignSubject" queryString="study=${study.id}&site=${studySites.site.id}">Assign subject</tags:restrictedListItem>
                                            </ul>

                                            <c:forEach items="${studySites.studySubjectAssignments}" var="listOfSubjects" varStatus="listOfSubjectsStatus">
                                                <c:choose>
                                                    <c:when test="${not empty listOfSubjects}">
                                                     <li class="subject">
                                                         <a href="<c:url value="/pages/cal/schedule?calendar=${listOfSubjects.scheduledCalendar.id}"/>" class="primary">
                                                            ${listOfSubjects.subject.firstName}
                                                            ${listOfSubjects.subject.lastName}
                                                         </a>

                                                     </li>
                                                </c:when>
                                                <c:otherwise>
                                                    <h3>You have no subjects on this study</h3>
                                                </c:otherwise>
                                                </c:choose>
                                            </c:forEach>
                                        </li>
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
                        <a href="<c:url value="/pages/dashboard/colleagueSubjectCoordinator?id=${mapOfUsersAndStudies.key.id}"/>"> ${mapOfUsersAndStudies.key.name} </a>
                    </li>
                </c:forEach>
            </ul>
        </laf:box>
    </c:if>
</body>
</html>