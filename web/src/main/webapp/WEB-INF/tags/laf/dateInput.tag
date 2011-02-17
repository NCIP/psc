<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@attribute name="path"%>
<%@attribute name="local" type="java.lang.Boolean" required="false" %>
<%@attribute name="cssClass" type="java.lang.String" required="false" %>
<%@attribute name="todayDateValue" type="java.lang.String" required="false"%>
<c:choose>
    <c:when test="${!local}">
        <form:input path="${path}" cssClass="date ${cssClass}"/>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${!todayDateValue}">
                <input id="${path}" class="date ${cssClass}" name="${path}" value="${todayDateValue}"/>
            </c:when>
            <c:otherwise>
                <input id="${path}" class="date ${cssClass}" name="${path}"/>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>
<a href="#" id="${fn:replace(fn:replace(path, "[", ""), "]", "")}-calbutton">
    <img src="<laf:imageUrl name="chrome/b-calendar.gif"/>" alt="Calendar" width="17" height="16" border="0" align="absmiddle" />
</a>