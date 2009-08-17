<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@attribute name="property"%>
<%@attribute name="subjectAssignment" type="edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment" %>
<%@ tag import="edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext" %>
<%@ tag import="org.restlet.util.Template" %>
<%@ tag import="edu.northwestern.bioinformatics.studycalendar.web.GeneratedUriTemplateVariable" %>
<%@ tag import="edu.northwestern.bioinformatics.studycalendar.configuration.Configuration" %>
<%@ tag import="edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment" %>
<%
    StudySubjectAssignment studySubjectAssignment = (StudySubjectAssignment)subjectAssignment;
    BreadcrumbContext context = (BreadcrumbContext) request.getAttribute("breadcrumbContext");
    Template uriTemplate = (Template) ((Configuration) request.getAttribute("configuration")).getMap().get(property);
%>

<c:url value="<%= uriTemplate.format(GeneratedUriTemplateVariable.getAllTemplateValues(context, studySubjectAssignment)) %>"/>
