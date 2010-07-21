<%@page contentType="text/html" %>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="inDevelopmentTemplates" scope="request" type="java.util.List<edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship>"/>
<jsp:useBean id="releasedTemplates" scope="request" type="java.util.List<edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship>"/>

<%-- TODO: why is this split into released vs. development? --%>
<ul>
    <c:forEach items="${inDevelopmentTemplates}" var="template">
        <li id="dev${template.study.id}">${template.study.developmentDisplayName}</li>
    </c:forEach>
    <c:forEach items="${releasedTemplates}" var="template">
        <li id="rel${template.study.id}">${template.study.releasedDisplayName}</li>
    </c:forEach>
</ul>