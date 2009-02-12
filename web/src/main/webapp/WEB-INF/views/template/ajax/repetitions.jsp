<%@page contentType="text/html" %>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<ul>
    <c:forEach items="${labels}" var="label">
        <li id="${label.id}">${label.name}</li>
    </c:forEach>
</ul>