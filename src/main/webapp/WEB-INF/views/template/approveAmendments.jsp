<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<c:set var="title">Approve amendments for ${study.name}</c:set>
<html>
<head>
    <title>${title}</title>
    <style type="text/css">
        #approvals .approval {
            text-align: right;
        }
    </style>
</head>
<body>
<laf:box title="${title}">
    <laf:division>
        <p>
            You are approving the template<c:if test="${study.amended}"> and amendments</c:if>
            of <strong>${study.name}</strong> for use at <strong>${site.name}</strong>.
        </p>
        <p>
            You cannot reverse any changes you make here.  In the case of mandatory amendments,
            changes will be immediately propagated to subject schedules.  Please be sure that you
            have all necessary regulatory approvals before proceeding.
        </p>
        <form:form method="post">
            <table id="approvals" class="grid">
                <tr>
                    <th>Approve</th>
                    <th>Amendment</th>
                    <th>Approval date</th>
                </tr>
                <c:forEach items="${command.approvals}" var="approval" varStatus="status">
                    <tr>
                        <td class="approve">
                            <c:choose>
                                <c:when test="${approval.alreadyApproved}">
                                    <input disabled="disabled" type="checkbox" checked="checked" />
                                </c:when>
                                <c:otherwise>
                                    <form:checkbox path="approvals[${status.index}].justApproved"/>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="amendment-name">
                            <!-- TODO: link to page describing the amendment -->
                            ${approval.amendment.displayName}
                        </td>
                        <td class="date">
                            <c:choose>
                                <c:when test="${approval.alreadyApproved}">
                                    <tags:formatDate value="${approval.date}"/>
                                </c:when>
                                <c:otherwise>
                                    <laf:dateInput path="approvals[${status.index}].date"/>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
            </table>
            <div class="row">
                <div class="value submit">
                    <input type="submit" value="Approve selected amendments"/>
                </div>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>