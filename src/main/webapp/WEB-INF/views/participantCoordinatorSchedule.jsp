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

        function ajaxform() {
            var href = '<c:url value="/pages/dashboard/participantCoordinatorSchedule"/>'
            // Set up data variable
            var formdata = "";
            var toDateTemp = 'toDate';
            formdata = formdata + toDateTemp+ "=" + $(toDateTemp).value+"&";

            var arrayOfCheckboxes = document.getElementsByName('activityTypes');

            for (var i = 0; i < arrayOfCheckboxes.length; i++) {
                formdata = formdata + 'activityTypes' + '[' +$(arrayOfCheckboxes[i]).value + ']'+  "=" + $(arrayOfCheckboxes[i]).checked + "&"
            }

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
        ul.menu li {
            padding: 0.5em;
            list-style-type: none;
            margin: 0.5em;
        }
        ul.menu li.participant {
            padding-left: 4em;
            list-style-type: none;
            padding-bottom: 1em;
        }
        ul.menu li.controls {
            padding-left: 2em;
            list-style-type: none;
        }
        h2 {
            margin-top: 2em;
        }
        input.checkboxes {
            margin-left:10px;
            vertical-align:middle;
        }
    </style>
</head>
<body>
    <div class="main">
        <h1>Welcome, ${userName}</h1>
    </div>
    <laf:box title="Past-due activities">
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
           </ul>
    </laf:box>
    <laf:box title="Current activities">
            <ul class="menu">
                <li class="autoclear">
                     Activities for the next <input value="7" path="toDate" id="toDate" size="5" onchange="ajaxform();" /> days
                </li>
            <li>
                Filter by Activity Type:  
                <c:forEach items="${activityTypes}" var="activityType">
                    <input TYPE=checkbox class="checkboxes" value="${activityType.id}" id="checkboxId" name="activityTypes" checked="true" onchange="ajaxform();"> ${activityType.name} </input>
                </c:forEach>
            </li>
            <li class="autoclear" id="participant-schedule">
                 <tags:participantCoordinatorSchedule/>
            </li>
        </ul>
    </laf:box>
    <laf:box title="Available studies">
        <ul class="menu">
            <c:forEach items="${ownedStudies}" var="study" varStatus="status">
                <li class="autoclear ">
                    <a href="<c:url value="/pages/cal/template?study=${study.id}"/>" class="primary">
                        ${study.name}
                    </a>

                    <c:forEach items="${study.studySites}" var="studySites" varStatus="studySiteStatus">
                        <li class="controls ">
                            ${studySites.site.name}
                            <ul class="controls">
                                <tags:restrictedListItem cssClass="control" url="/pages/cal/assignParticipant" queryString="id=${study.id}">Assign participant</tags:restrictedListItem>

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