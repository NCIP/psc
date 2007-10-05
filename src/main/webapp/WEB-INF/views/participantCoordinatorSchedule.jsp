<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>

<html>
<head>
    <tags:stylesheetLink name="main"/>

    <script type="text/javascript">

        function ajaxform(toDate) {
            var href = '<c:url value="/pages/cal/participantCoordinatorSchedule"/>'
            // Set up data variable
            var formdata = "";
            formdata = formdata + 'toDate='+$(toDate).value+"&";
            console.log("formdata " + formdata);
            var lastRequest = new Ajax.Request(href,
            {
                postBody: formdata
            });

            return true;
        }

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
        ul.menu li.myControl {
            padding: 0em;
            list-style-type: none;
            margin: 0.5em;
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
        ul.menu li.myclass {
            padding-left: 1em;
            list-style-type: none;
            margin: 0.5em;
        }
        ul.menu li.participant {
            padding-left: 2em;
            list-style-type: none;
            /*margin: 2em;*/
        }
       ul.menu li.controls {
            padding-left: 1em;
            list-style-type: none;
            /*margin: 2em;*/
        }
        h2 {
            margin-top: 2em;
        }
    </style>
</head>
<body>
<laf:box title="Participant Coordinator dashboard">
        <!-- Find Controller url to Set this to -->
    <h4>Welcome, ${userName}</h4>    
    <laf:division>
    <h3>Past-due activities </h3>

        <ul class="menu">
            <li class="autoclear">
                <c:forEach items="${pastDueActivities}" var="mapOfPastDueActivities" varStatus="keyStatus">
                    <c:forEach items="${mapOfPastDueActivities.key}" var="mapOfPastDueActivitiesKey" varStatus="keyStatus">
                        ${mapOfPastDueActivitiesKey.key.firstName} ${mapOfPastDueActivitiesKey.key.lastName} has <a href=
                            "<c:url value="/pages/cal/schedule?calendar=${mapOfPastDueActivities.value.id}"/>" > ${mapOfPastDueActivitiesKey.value} past-due activities </a>.  Earliest is
                            from <tags:formatDate value="${mapOfPastDueActivities.value.startDateEpoch}"/>
                        <br>
                    </c:forEach>
                </c:forEach>
            </li>
    </laf:division>
     <laf:division>
         <h3>Current activities</h3>
            <ul class="menu">

            <%--Activities for the interval: from <form:input path="fromDate" id="fromDate" size="5" onchange="ajaxform(this);" /> --%>
                    <%--to <form:input path="toDate" id="toDate" size="5" onchange="ajaxform(this);" /> days--%>
            <li class="autoclear">
                    Activities for the next <input value="7" path="toDate" id="toDate" size="5" onchange="ajaxform(this);" /> days
            </li>
            <li class="autoclear" id="participant-schedule">
                 <tags:participantCoordinatorSchedule/>
            </li>
        </ul>

    </laf:division>
    <h3>Available studies</h3>
        <ul class="menu">
            <c:forEach items="${ownedStudies}" var="study" varStatus="status">
                <li class="autoclear ${commons:parity(status.count)}">
                    <a href="<c:url value="/pages/cal/template?study=${study.id}"/>" class="primary">
                        ${study.name}
                    </a>

                    <c:forEach items="${study.studySites}" var="studySites" varStatus="studySiteStatus">
                        <li class="controls ${commons:parity(studySiteStatus.count)}">
                            ${studySites.site.name}
                            <ul class="controls">
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/assignParticipant" queryString="id=${study.id}">Assign another participants</tags:restrictedListItem>

                            </ul>
                            <c:forEach items="${studySites.studyParticipantAssignments}" var="listOfParticipants" varStatus="listOfParticipantsStatus">

                                <c:choose>
                                    <c:when test="${not empty listOfParticipants}">
                                     <li class="participant">

                                         <a href="<c:url value="/pages/cal/schedule?assignment=${listOfParticipants.participant.id}"/>" class="primary">
                                            ${listOfParticipants.participant.firstName}
                                            ${listOfParticipants.participant.lastName}
                                         </a>

                                     </li>                                                                                                                        
                                </c:when>
                                <c:otherwise>
                                    <h3>You have no participants on this study</h3>
                                </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </li>
                    </c:forEach>
                </li>
            </c:forEach>
        </ul>
</laf:box>

</body>
</html>