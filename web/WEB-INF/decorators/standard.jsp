<%-- This is the standard decorator for all study calendar pages --%>
<%@taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
  <head>
    <title>caBIG Study Calendar Module - <decorator:title/></title>
    <tags:stylesheetLink name="debug"/>
    <tags:stylesheetLink name="lightbox"/>
    <tags:stylesheetLink name="error-console"/>
    <tags:stylesheetLink name="common"/>
    <tags:javascriptLink name="prototype"/>
    <tags:javascriptLink name="lightbox"/>
    <tags:javascriptLink name="error-console"/>
    <tags:javascriptLink name="common"/>
    <style type="text/css" xml:space="preserve">
        body { behavior: url('<c:url value="/css/csshover.htc"/>') }
    </style>
    <decorator:head/>
  </head>
  <body>
    <c:if test="${not empty breadcrumbs}"><tags:breadcrumbs anchors="${breadcrumbs}"/></c:if>
    <div id="body">
    <decorator:body/>
    </div>
    <div id="footmenu">
        <a href="http://gforge.nci.nih.gov/tracker/?func=add&group_id=31&atid=1043">Provide Feedback</a>
        | <tags:logout/>
    </div>
    <tags:errorConsole/>
    <tags:debugInfo/>
  </body>
</html>
