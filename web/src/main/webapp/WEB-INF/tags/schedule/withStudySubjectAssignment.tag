<%@tag import="edu.northwestern.bioinformatics.studycalendar.service.DomainContext" %>
<%--
  Temporarily replaces the DomainContext in the request scope with a copy containing the
  specified assignment.
--%>
<%@attribute name="value"
             type="edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment" %>
<%
    String DOMAIN_CONTEXT_KEY = "domainContext";

    DomainContext original = (DomainContext) request.getAttribute(DOMAIN_CONTEXT_KEY);
    DomainContext augmented = original.clone();
    augmented.setStudySubjectAssignment(value);
    request.setAttribute(DOMAIN_CONTEXT_KEY, augmented);
%>

<jsp:doBody/>

<%
    request.setAttribute(DOMAIN_CONTEXT_KEY, original);
%>