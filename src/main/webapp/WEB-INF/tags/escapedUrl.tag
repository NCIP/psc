<%-- Note that this doesn't account for the query string.  It could be modified to do so if necessary. --%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@tag import="org.apache.commons.lang.StringUtils" %>
<%@tag import="org.restlet.data.Reference" %>
<%@attribute name="var" required="true" type="java.lang.String" %>
<%@attribute name="value" required="true" type="java.lang.String" %>
<%
    String[] components = ((String) jspContext.getAttribute("value")).split("/");
    for (int i = 0; i < components.length; i++) components[i] = Reference.encode(components[i]);
    jspContext.setAttribute("escapedValue", StringUtils.join(components, '/'));
%>
<c:url value="${escapedValue}" var="escapedUrl"/>
<% request.setAttribute((String) jspContext.getAttribute("var"), jspContext.getAttribute("escapedUrl")); %>