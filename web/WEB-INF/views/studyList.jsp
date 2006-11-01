<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
<title>Patient Study Calendar Menu</title>
</head>
<body>
<h1>Calendar Menu</h1>

<strong> Calendar Templates </strong>
<security:secureOperation element="/studycalendar/pages/newStudy" operation="ACCESS">
<p><a href="<c:url value="/pages/newStudy"/>">Create New Study Template</a></p>
</security:secureOperation>
<br>
<security:secureOperation element="/studycalendar/pages/markComplete" operation="ACCESS">
<br>
<strong> Templates In Design </strong>
<table cellpadding="5">
    <c:forEach items="${studies}" var="study">
    	<c:if test="${not study.plannedCalendar.complete}">
    	<tr>
    		<td><a href="<c:url value="/pages/template?study=${study.id}"/>">${study.name}</a></td>
    	</tr>
    	</c:if>
    </c:forEach>
</table>
<br>
</security:secureOperation>
<br>
<strong> Completed Templates </strong>
<table cellpadding="5">
    <c:forEach items="${studies}" var="study">
    	<c:if test="${study.plannedCalendar.complete}">
        <tr>
            <td><a href="<c:url value="/pages/template?study=${study.id}"/>">${study.name}</a></td>
        	<td>
        	<security:secureOperation element="/studycalendar/pages/assignSite" operation="ACCESS">
        		<a href="<c:url value="/pages/assignSite?id=${study.id}"/>">Assign Sites</a>
        	</security:secureOperation>
			<security:secureOperation element="/studycalendar/pages/assignParticipantCoordinator" operation="ACCESS">        		
        		<a href="<c:url value="/pages/assignParticipantCoordinator?id=${study.id}"/>">Assign Participant Coordinators</a>
        	</security:secureOperation>	
        	<security:secureOperation element="/studycalendar/pages/assignParticipant" operation="ACCESS">
        		<a href="<c:url value="/pages/assignParticipant?id=${study.id}"/>">Assign Participants</a>
        	</security:secureOperation>	
        	</td>
        </tr>
        </c:if>
    </c:forEach>
</table>
<br>
<br>
<strong> Administration </strong>
	<security:secureOperation element="/studycalendar/pages/manageSites" operation="ACCESS">
            <div><a href="<c:url value="/pages/manageSites"/>">Manage Sites</a></div>
    </security:secureOperation>        
    <security:secureOperation element="/studycalendar/pages/assignParticipantCoordinatorsToSite" operation="ACCESS">        
			<div><a href="<c:url value="/pages/sitesForAssignParticipantCoordinators"/>">Assign Participant Coordinators to Site</a></div>
    </security:secureOperation>
	<security:secureOperation element="/studycalendar/pages/assignParticipantCoordinator" operation="ACCESS">
			<div><a href="<c:url value="/pages/sitesForAssignParticipantCoordinators"/>">Assign Study Templates to Participant Coordinators</a></div>
	</security:secureOperation>		
</body>
</html>