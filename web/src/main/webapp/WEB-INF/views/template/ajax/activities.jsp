<%@page contentType="text/html" %>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<ul>
    <c:forEach items="${activities}" var="activity">
        <li id="${activity.id}">${activity.name}<c:if test="${not empty activity.code}"> (${activity.code})</c:if></li>
    </c:forEach>
</ul>