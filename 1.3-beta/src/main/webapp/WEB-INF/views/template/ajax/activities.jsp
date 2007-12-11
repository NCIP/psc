<%@page contentType="text/javascript" %>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<ul>
    <c:forEach items="${activities}" var="activity">
        <li id="${activity.id}">${activity.name} (${activity.code})</li>
    </c:forEach>
</ul>