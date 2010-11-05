<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@attribute name="property"%>
<%@attribute name="subjectAssignment" type="edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment" %>
<%@ tag import="org.restlet.util.Template" %>
<%@ tag import="edu.northwestern.bioinformatics.studycalendar.configuration.Configuration" %>
<%@ tag import="edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment" %>
<%@ tag import="edu.northwestern.bioinformatics.studycalendar.service.DomainContext" %>
<%@ tag import="edu.northwestern.bioinformatics.studycalendar.service.GeneratedUriTemplateVariable" %>

<%
    StudySubjectAssignment studySubjectAssignment = (StudySubjectAssignment)subjectAssignment;
    DomainContext context = (DomainContext) request.getAttribute("domainContext");
    Template uriTemplate = (Template) ((Configuration) request.getAttribute("configuration")).getMap().get(property);
    request.setAttribute("uriTemplate",uriTemplate);
%>

<c:if test="${not empty uriTemplate}">
    <c:url value="<%= uriTemplate.format(GeneratedUriTemplateVariable.getAllTemplateValues(context, studySubjectAssignment)) %>"/>
</c:if>
