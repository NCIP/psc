<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>

<jsp:useBean id="command" scope="request" type="edu.northwestern.bioinformatics.studycalendar.web.admin.AuthenticationSystemSelectorCommand"/>
<jsp:useBean id="authenticationSystemKey" scope="request" type="java.lang.String"/>

<c:if test="${fn:length(command.conf) > 1}">
    <h3>Configuration options for the selected system</h3>
    <c:forEach items="${command.conf}" var="entry" varStatus="status">
        <c:if test="${entry.key != authenticationSystemKey}">
            <div class="row ${commons:parity(status.count)}">
                <div class="label">
                    <form:label path="conf[${entry.key}].value">${entry.value.property.name}</form:label>
                </div>
                <div class="value">
                    <admin:configurationInput configEntry="${entry.value}" configEntryPath="conf[${entry.key}].value"/>
                </div>
            </div>
        </c:if>
    </c:forEach>
</c:if>
