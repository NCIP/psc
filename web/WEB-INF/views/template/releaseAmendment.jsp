<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<c:set var="title">Release ${study.name}<c:if test="${study.inAmendmentDevelopment}">, amendment ${study.developmentAmendment.name} (${study.developmentAmendment.date})</c:if></c:set>
<html>
<head>
    <title>${title}</title>
</head>
<body>
<laf:box title="${title}">
    <laf:division>
        <p>
            You are about to release
            <c:choose>
                <c:when test="${study.inInitialDevelopment}">the template</c:when>
                <c:otherwise>the amendment <strong>${study.developmentAmendment.name} (${study.developmentAmendment.date})</strong> for</c:otherwise>
            </c:choose>
            <strong>${study.name}</strong>.
            Once you do this, it will be available for use for patient tracking and
            you will no longer be able to edit it.
        </p>
        <p>
            You cannot reverse this step.  If you're not ready to proceed, please
            <a href="<c:url value="/pages/cal/template?study=${study.id}"/>">return to
            the template</a>.
        </p>
        <form:form method="post">
            <div class="row">
                <div class="value submit">
                    <input type="submit" value="${title}"/>
                </div>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>