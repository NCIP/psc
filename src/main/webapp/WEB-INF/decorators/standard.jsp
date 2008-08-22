<%-- This is the standard decorator for all study calendar pages --%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
  <head>
    <c:set var="specificTitle"><decorator:title/></c:set>
    <title><tags:pageTitle pageSpecificTitle="${specificTitle}"/></title>

    <script type="text/javascript">
      var INTERNAL_URI_BASE_PATH = '<c:url value="/"/>'
    </script>

    <laf:stylesheetLink name="debug"/>
    <laf:stylesheetLink name="common"/>
    <laf:stylesheetLink name="fields"/>
    <laf:stylesheetLink name="calendar-blue"/>
    <tags:stylesheetLink name="lightbox"/>
    <tags:stylesheetLink name="error-console"/>
    <tags:stylesheetLink name="common"/>

    <laf:javascriptLink name="prototype"/>
    <laf:javascriptLink name="common"/>
    <laf:javascriptLink name="calendar"/>
    <laf:javascriptLink name="ccts-hotlinks"/>
    <tags:javascriptLink name="lightbox"/>
    <tags:javascriptLink name="error-console"/>
    <tags:javascriptLink name="common"/>
    <c:choose>
      <c:when test="${configuration.map.showDebugInformation}">
        <tags:javascriptLink name="firebug/firebug"/>
      </c:when>
      <c:otherwise>
        <tags:javascriptLink name="firebugx"/>
      </c:otherwise>
    </c:choose>

    <style type="text/css" xml:space="preserve">
        body { behavior: url('<c:url value="/css/csshover.htc"/>') }
    </style>

    <decorator:head/>

    <tags:sessionTimeout/>
  </head>
  <body>
<tags:header/>
<tags:breadcrumbs anchors="${breadcrumbs}"/>
<laf:body>
    <laf:flashMessage/>
    <decorator:body/>
</laf:body>

<laf:footer>
    <div id="footmenu">
        <a href="http://gforge.nci.nih.gov/tracker/?func=add&group_id=31&atid=1043">Provide Feedback</a>
    </div>
</laf:footer>

<tags:errorConsole/>
<c:if test="${configuration.map.showDebugInformation}">
    <laf:debugInfo/>
</c:if>
<div id="build-name">${buildInfo.buildName}</div>
</body>
</html>  
