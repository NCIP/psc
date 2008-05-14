<%@page contentType="text/html" %>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<ul>
    <c:forEach items="${inDevelopmentTemplates}" var="inDevelopmentTemplate">
        <li id="${inDevelopmentTemplate.id}">${inDevelopmentTemplate.displayName}</li>
    </c:forEach>
    <c:forEach items="${releasedTemplates}" var="releasedTemplate">
        <li id="${releasedTemplate.id}">${releasedTemplate.study.name}</li>
    </c:forEach>
</ul>