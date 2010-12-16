<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<laf:header>
    <jsp:attribute name="logoText">${configuration.map.logoAltText}</jsp:attribute>
    <jsp:attribute name="logoImageUrl"><c:url value="${configuration.map.logoImageUrl}"/></jsp:attribute>
    <jsp:attribute name="logoDimensionsWidth"><c:url value="${configuration.map.logoDimensionsWidth}"/></jsp:attribute>
    <jsp:attribute name="logoDimensionsHeight"><c:url value="${configuration.map.logoDimensionsHeight}"/></jsp:attribute>
    <jsp:attribute name="tagline">Patient Study Calendar</jsp:attribute>
    <jsp:attribute name="taglineImageUrl"><c:url value="/images/tagline.png"/></jsp:attribute>
    <jsp:attribute name="logoutUrl"><c:url value="/auth/logout"/></jsp:attribute>
    <jsp:attribute name="preLogoutHtml">
        <c:if test="${configuration.ctmsConfigured}">
            <c:set var="ctmsName" value="${configuration.map.ctmsName}"/>
            <c:set var="ctmsUrl" value="${configuration.map.ctmsUrl}"/>
            <c:if test="${not empty ctmsUrl}">
                return to <tags:externalLink url="${ctmsUrl}" appShortName="ctms">${ctmsName}</tags:externalLink> |
                <%--return to <a href="<c:url value="${ctmsUrl}"/>">${ctmsName}</a> |--%>
            </c:if>
        </c:if>
    </jsp:attribute>
    <jsp:attribute name="renderSection">
        <c:if test="${not empty section.tasks}">
            <laf:sectionTab section="${section}" currentSection="${currentSection}"/>
        </c:if>
    </jsp:attribute>
    <jsp:attribute name="renderTask">
        <tags:restrictedTaskItem task="${task}"/>
    </jsp:attribute>
</laf:header>
