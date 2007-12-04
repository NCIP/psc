<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@attribute name="view" required="true" type="edu.northwestern.bioinformatics.studycalendar.web.delta.AmendmentView"%>
<%@attribute name="style" required="false"%>
<laf:box id="amendment-${view.amendment.id}" cssClass="amendment" title="${view.amendment.displayName}" style="${style}">
    <laf:division>
        <c:forEach items="${view.changes.flattened}" var="c">
            <p class="change" id="change-${c.id}-para">${c.sentence}</p>
        </c:forEach>
    </laf:division>
</laf:box>