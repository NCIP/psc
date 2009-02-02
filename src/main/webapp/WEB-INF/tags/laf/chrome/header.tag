<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@attribute name="logoImageUrl" required="true"%>
<%@attribute name="logoText" required="true"%>
<%@attribute name="tagline" required="true"%>
<%@attribute name="taglineImageUrl" required="true"%>
<%@attribute name="logoutUrl" required="true"%>
<%@attribute name="preLogoutHtml" required="false"%>

<%-- The fragment provided for this attribute will be invoked for each section --%>
<%@attribute name="renderSection" fragment="true" required="true"%>
<%@variable name-given="section" %>
<%@variable name-given="currentSection" %>

<%-- The fragment provided for this attribute will be invoked for each task in the current section --%>
<%@attribute name="renderTask" fragment="true" required="true"%>
<%@variable name-given="task"%>

<div id="header">
    <div class="background-R">
        <img src="${logoImageUrl}" alt="${logoText}" id="logo" width="129" height="40">
        <img src="${taglineImageUrl}" alt="${tagline}" id="tagline" width="268" height="22">
    </div>

    <div id="login-action">
        ${preLogoutHtml}<a href="${logoutUrl}">Log out</a>
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