<%@tag%>
<%@tag import="gov.nih.nci.cabig.ctms.web.WebTools" %>
<%@taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
 <!--?????-->
<%@taglib prefix="ctmsfn" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/functions" %>

<div id="debug">
    <h1>Submitted info</h1>
    <h2>Request parameters</h2>
    <dl>
    <c:forEach items="<%= new java.util.TreeMap<String, String>(request.getParameterMap()) %>" var="item">
        <dt class="var">${item.key}</dt><dd>${ctmsfn:nl2br(fn:join(item.value, ', '))}</dd>
    </c:forEach>
    </dl>
    <h2>Request headers</h2>
    <dl>
    <c:forEach items="<%= WebTools.headersToMap(request) %>" var="item">
        <dt class="var">${item.key}</dt><dd>${ctmsfn:nl2br(fn:join(item.value, ', '))}</dd>
    </c:forEach>
    </dl>
    <h2>Cookies</h2>
    <dl>
    <c:forEach items="${cookie}" var="item">
        <dt class="var">${item.key}</dt><dd>${ctmsfn:nl2br(item.value.value)}</dd>
    </c:forEach>
    </dl>

    <h1>Context info</h1>
    <h2>Request attributes</h2>
    <dl>
    <c:forEach items="<%= WebTools.requestAttributesToMap(request) %>" var="item">
        <dt class="var">${item.key}</dt><dd>${ctmsfn:nl2br(item.value)} <em class="clazz">${ctmsfn:classname(item.value)}</em></dd>
    </c:forEach>
    </dl>
    <h2>Request properties</h2>
    <dl>
    <c:forEach items="<%= WebTools.requestPropertiesToMap(request) %>" var="item">
        <dt class="var">${item.key}</dt><dd>${ctmsfn:nl2br(item.value)} <em class="clazz">${ctmsfn:classname(item.value)}</em></dd>
    </c:forEach>
    </dl>
    <h2>Session attributes</h2>
    <dl>
    <c:forEach items="<%= WebTools.sessionAttributesToMap(request.getSession(false)) %>" var="item">
        <dt class="var">${item.key}</dt><dd>${ctmsfn:nl2br(item.value)} <em class="clazz">${ctmsfn:classname(item.value)}</em></dd>
    </c:forEach>
    </dl>
    <h2>Context initialization parameters</h2>
    <dl>
    <c:forEach items="${initParam}" var="item">
        <dt class="var">${item.key}</dt><dd>${ctmsfn:nl2br(item.value)} <em class="clazz">${ctmsfn:classname(item.value)}</em></dd>
    </c:forEach>
    </dl>
    <%-- Jetty has an NPE in the toString in one of the values exposed by this block.
    <h2>Application attributes</h2>
    <dl>
    <c:forEach items="<%= WebTools.servletContextAttributesToMap(application) %>" var="item">
        <dt class="var">${item.key}</dt>
        <dd>${ctmsfn:nl2br(item.value)} <em class="clazz">${ctmsfn:classname(item.value)}</em></dd>
    </c:forEach>
    </dl>
    --%>
</div>
