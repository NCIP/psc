<%@taglib prefix="laf" uri="/WEB-INF/tags/laf.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="revision" type="edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision"%>
<%@ attribute name="changes" type="edu.northwestern.bioinformatics.studycalendar.web.delta.RevisionChanges"%>
<laf:box title="Changes for ${revision.displayName}" autopad="true">
    <c:forEach items="${changes.flattened}" var="c">
        <p class="change" id="change-${c.id}-para">${c.sentence}</p>
    </c:forEach>
</laf:box>
