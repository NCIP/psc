<%@page contentType="text/html" %>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<ul>
    <c:forEach items="${searchStudies}" var="searchStudy">
        <li id="${searchStudy.id}">${searchStudy.assignedIdentifier}</li>
    </c:forEach>
</ul>