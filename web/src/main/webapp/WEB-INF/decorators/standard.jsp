<%-- This is the standard decorator for all study calendar pages --%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>

<jsp:useBean id="configuration" scope="request"
             type="edu.northwestern.bioinformatics.studycalendar.configuration.Configuration"/>
<jsp:useBean id="buildInfo" scope="request"
             type="gov.nih.nci.cabig.ctms.tools.BuildInfo"/>

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

    <tags:stylesheetLink name="lightbox"/>
    <tags:stylesheetLink name="calendar-blue"/>

    <tags:stylesheetLink name="debug"/>
    <tags:stylesheetLink name="common"/>
    <tags:stylesheetLink name="fields"/>
    <tags:stylesheetLink name="error-console"/>

    <tags:javascriptLink name="jquery/jquery"/>
    <script type="text/javascript">
      jQuery.noConflict();
    </script>
    <tags:javascriptLink name="prototype"/>
    <tags:javascriptLink name="underscore-min"/>

    <tags:javascriptLink name="common"/>
    <tags:javascriptLink name="calendar"/>
    <tags:javascriptLink name="ccts-hotlinks"/>
    <tags:javascriptLink name="lightbox"/>
    <tags:javascriptLink name="error-console"/>

    <script type="text/javascript" src="<c:url value="/public/configuration.js"/>"></script>
    
    <c:choose>
      <c:when test="${configuration.map.showDebugInformation}">
        <tags:javascriptLink name="firebug/firebug"/>
      </c:when>
      <c:otherwise>
        <tags:javascriptLink name="firebug/firebugx"/>
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
