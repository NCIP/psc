<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@attribute name="logoImageUrl" required="true"%>
<%@attribute name="logoText" required="true"%>
<%@attribute name="logoDimensionsWidth" required="true"%>
<%@attribute name="logoDimensionsHeight" required="true"%>

<%@attribute name="tagline" required="true"%>
<%@attribute name="taglineImageUrl" required="true"%>
<%@attribute name="logoutUrl" required="true"%>
<%@attribute name="preLogoutHtml" required="false"%>

<c:if test="${not empty requestScope['currentUser']}">
    <jsp:useBean id="currentUser" scope="request"
                 type="edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser"/>
</c:if>

<%-- The fragment provided for this attribute will be invoked for each section --%>
<%@attribute name="renderSection" fragment="true" required="true"%>
<%@variable name-given="section" %>
<%@variable name-given="currentSection" %>

<%-- The fragment provided for this attribute will be invoked for each task in the current section --%>
<%@attribute name="renderTask" fragment="true" required="true"%>
<%@variable name-given="task"%>

<div id="header">
    <div class="background-R">
        <img src="${logoImageUrl}" alt="${logoText}" id="logo" width="${logoDimensionsWidth}" height="${logoDimensionsHeight}">
        <img src="${taglineImageUrl}" alt="${tagline}" id="tagline" width="268" height="22">
    </div>

    <div id="login-action">
        ${preLogoutHtml}
        <c:choose>
            <c:when test="${not empty currentUser}"><a href="${logoutUrl}">Log out</a></c:when>
            <c:otherwise><a href="<c:url value="/public/login"/>">Log in</a></c:otherwise>
        </c:choose>
    </div>

    <ul id="sections" class="tabs">
    <c:forEach items="${sections}" var="section">
        <jsp:invoke fragment="renderSection"/>
    </c:forEach>
    </ul>

    <div id="taskbar">
        <c:if test="${not empty currentSection.tasks}">
            Tasks:
            <c:forEach items="${currentSection.tasks}" var="task">
                <jsp:invoke fragment="renderTask"/>
            </c:forEach>
        </c:if>
    </div>
</div>