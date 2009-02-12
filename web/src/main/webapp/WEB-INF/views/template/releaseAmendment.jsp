<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>

<c:set var="title">Release ${study.assignedIdentifier}<c:if test="${study.inAmendmentDevelopment}">, amendment ${study.developmentAmendment.displayName}</c:if></c:set>
<html>
<head>
    <title>${title}</title>
    <script type="text/javascript">

        function displayTheCorrectDiv() {
           if ($('errors').empty()) {
                $('errorMessages').hide()
                $('releaseAmendment').show()
           } else {
                $('errorMessages').show()
                $('releaseAmendment').hide()
           }
        }
        Event.observe(window, "load", displayTheCorrectDiv)
    </script>
</head>
<body>


<laf:box title="${title}">
    <laf:division>
        <div id="errorMessages" style="display:none;">
            The template is not ready to be released due to the following :
            <p/>
            <div id="errors">
                <tags:replaceErrorMessagesForTemplate/>
            </div>
            <p>
            Please
                <a href="<c:url value="/pages/cal/template?study=${study.id}&amendment=${study.developmentAmendment.id}"/>">return to
                the template</a>.
            </p>
        </div>
        <div id="releaseAmendment" style="display:none;">
            <p>
                You are about to release
                <c:choose>
                    <c:when test="${study.inInitialDevelopment}">the template</c:when>
                    <c:otherwise>the amendment <strong>${study.developmentAmendment.displayName}</strong> for</c:otherwise>
                </c:choose>
                <strong>${study.assignedIdentifier}</strong>.
                Once you do this, it will be available for use for patient tracking and
                you will no longer be able to edit it.
            </p>
            <p>
                You cannot reverse this step.  If you're not ready to proceed, please
                <a href="<c:url value="/pages/cal/template?study=${study.id}&amendment=${study.developmentAmendment.id}"/>">return to
                the template</a>.
            </p>
            <form:form method="post">
                <div class="row">
                    <div class="value submit">
                        <input type="submit" value="${title}"/>
                    </div>
                </div>
            </form:form>
        </div>
    </laf:division>
</laf:box>
</body>
</html>