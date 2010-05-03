<%@tag%>
<%@attribute name="pageSpecificTitle" required="false"%>
<%@ tag import="edu.northwestern.bioinformatics.studycalendar.service.DomainContext" %>
<%@ tag import="edu.northwestern.bioinformatics.studycalendar.domain.Subject" %>
<%@ tag import="org.apache.commons.lang.StringUtils" %>
<%
    StringBuilder title = new StringBuilder("PSC");
    String separator = " - ";
    if (!StringUtils.isBlank((String) jspContext.getAttribute("pageSpecificTitle"))) {
        title.append(separator).append(jspContext.getAttribute("pageSpecificTitle"));
    }
    DomainContext breadcrumbs = (DomainContext) request.getAttribute("domainContext");
    if (breadcrumbs != null) {
        if (breadcrumbs.getSubject() != null) {
            Subject s = breadcrumbs.getSubject();
            title.append(separator).append(s.getFullName());
            if (!StringUtils.isBlank(s.getPersonId())) {
                title.append(" (").append(s.getPersonId()).append(')');
            }
        }
        if (breadcrumbs.getStudy() != null) {
            title.append(separator).append(breadcrumbs.getStudy().getAssignedIdentifier());
        }
    }
    jspContext.setAttribute("title", title.toString());
%>
${title}