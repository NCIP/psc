<%--<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>--%>
<%@taglib prefix="laf"  tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="title"%>
<%@attribute name="id"%>
<%@attribute name="cssClass"%>
<%@attribute name="style"%>
<div class="division ${cssClass}"
    <laf:attribute name="id" value="${id}"/> <laf:attribute name="style" value="${style}"/>>
    <c:if test="${not empty title}">
        <h3>${title}</h3>
    </c:if>
    <div class="content">
        <jsp:doBody/>
    </div>
</div>
