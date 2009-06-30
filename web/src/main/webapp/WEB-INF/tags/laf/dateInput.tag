<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%--<%@taglib prefix="chrome" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>--%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@attribute name="path"%>
<%@attribute name="local" type="java.lang.Boolean" required="false" %>
<c:choose>
    <c:when test="${!local}">
        <form:input path="${path}" cssClass="date"/>
    </c:when>
    <c:otherwise>
        <input id="${path}" class="date" name="${path}"/>
    </c:otherwise>
</c:choose>
<a href="#" id="${path}-calbutton">
    <img src="<laf:imageUrl name="chrome/b-calendar.gif"/>" alt="Calendar" width="17" height="16" border="0" align="absmiddle" />
</a>