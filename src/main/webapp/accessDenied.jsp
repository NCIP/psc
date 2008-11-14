<%@ page import="java.io.PrintWriter" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>Authorization failed</title></head>
  <body>
    <h1>Access denied</h1>
    <c:if test="${configuration.map.showDebugInformation}">
        <h2>ACEGI_SECURITY_LAST_EXCEPTION</h2>
        <pre>
        <%
            Exception last = (Exception) session.getAttribute("ACEGI_SECURITY_LAST_EXCEPTION");
            if (last != null) last.printStackTrace(new PrintWriter(out));
        %>
        </pre>
    </c:if>
  </body>
</html>