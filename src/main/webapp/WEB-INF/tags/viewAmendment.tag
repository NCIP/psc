<%@taglib prefix="laf" uri="/WEB-INF/tags/laf.tld"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@attribute name="view" required="true" type="edu.northwestern.bioinformatics.studycalendar.web.delta.AmendmentView"%>
<%@attribute name="style" required="false"%>
<laf:box id="amendment-${view.amendment.id}" cssClass="amendment" title="${view.amendment.displayName}" style="${style}">
    <laf:division>
        <p>
            <a href="<c:url value="/pages/cal/template?study=${view.study.id}&amendment=${view.amendment.id}"/>">
                View the template as of this amendment
            </a>
        </p>
    </laf:division>
    <h3>Changes</h3>
    <laf:division>
        <c:forEach items="${view.changes.flattened}" var="c">
            <p class="change" id="change-${c.id}-para">${c.sentence}</p>
        </c:forEach>
    </laf:division>
    <c:if test="${not empty view.approvals}">
        <h3>Approvals</h3>
        <laf:division>
            <ul class="amendment-approvals">
               <c:forEach items="${view.approvals}" var="approvalEntry">
                   <li>
                       <c:if test="${    empty approvalEntry.value.date}"><em>Not approved</em></c:if>
                       <c:if test="${not empty approvalEntry.value.date}">Approved <tags:formatDate value="${approvalEntry.value.date}"/></c:if>
                       for
                       <c:if test="${approvalEntry.value.approvable}"><a href="<c:url value="/pages/cal/template/approve?studySite=${approvalEntry.key.id}"/>"></c:if>
                       ${approvalEntry.key.site.name}
                       <c:if test="${approvalEntry.value.approvable}"></a></c:if>
                   </li>
               </c:forEach>
            </ul>
        </laf:division>
    </c:if>
</laf:box>