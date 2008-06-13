<%@page contentType="text/html" %>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


    <c:forEach items="${repetitions}" var="repetitionArray">
        <c:choose>
            <c:when test="${not empty repetitionArray}">
                [<c:forEach items="${repetitionArray}" var="repetition">${repetition},</c:forEach>]
            </c:when>
            <c:otherwise>
                [,]
            </c:otherwise>
        </c:choose>
    </c:forEach>
