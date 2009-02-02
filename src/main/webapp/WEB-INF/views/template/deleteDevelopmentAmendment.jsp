<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="laf" uri="/WEB-INF/tags/laf.tld"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>


<c:set var="title">Delete ${command.study.assignedIdentifier}<c:if
        test="${command.study.inDevelopment}">, amendment ${command.study.developmentAmendment.displayName}</c:if></c:set>

<html>
<head>
    <title>${title}</title>
</head>
<body>


<laf:box title="${title}">
    <laf:division>
        <c:choose>
            <c:when test="${study.inDevelopment}">
                <div id="deleteAmendment">
                    <p>You are about to delete the amendment
                        <strong>${command.study.developmentAmendment.displayName}</strong> for
                        <strong>${command.study.assignedIdentifier}</strong>.
                    </p>

                    <p>
                        You cannot reverse this step. If you're not ready to proceed, please
                        <a href="<c:url value="/pages/cal/studyList"/>">return to the calendar list</a>.
                    </p>
                    <form:form method="post">
                        <div class="row">
                            <div class="value submit">
                                <input type="submit" value="Delete"/>
                            </div>
                        </div>
                    </form:form>
                </div>
            </c:when>
            <c:otherwise>
                <div id="errorMessages">
                    <p>
                        A released template can not be deleted.
                        Please <a href="<c:url value="/pages/cal/studyList"/>">return to the calendar list</a>.
                    </p>
                </div>
            </c:otherwise>
        </c:choose>

    </laf:division>
</laf:box>
</body>
</html>