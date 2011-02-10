<%-- This is the decorator for pages in the PSC setup flow --%>
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
    <title>PSC - Initial setup - <decorator:title/></title>
    <tags:stylesheetLink name="debug"/>
    <tags:stylesheetLink name="common"/>
    <tags:stylesheetLink name="fields"/>
    <tags:stylesheetLink name="calendar-blue"/>
    <tags:stylesheetLink name="lightbox"/>
    <tags:stylesheetLink name="setup"/>

    <tags:javascriptLink name="prototype"/>
    <tags:javascriptLink name="common"/>
    <tags:javascriptLink name="calendar"/>
    <tags:javascriptLink name="lightbox"/>
    <tags:javascriptLink name="common"/>

    <style type="text/css" xml:space="preserve">
        body { behavior: url('<c:url value="/css/csshover.htc"/>') }
    </style>
    <script type="text/javascript">
      var INTERNAL_URI_BASE_PATH = '<c:url value="/"/>'
    </script>
    <decorator:head/>
  </head>
  <body>
  <tags:header/>
<laf:body>
    <h1>One-time PSC setup</h1>
    <laf:flashMessage/>
    <decorator:body/>
</laf:body>

<laf:footer>
    <div id="footmenu">
        <a href="http://gforge.nci.nih.gov/tracker/?func=add&group_id=31&atid=1043">Provide Feedback</a>
    </div>
</laf:footer>

<c:if test="${configuration.map.showDebugInformation}">
    <laf:debugInfo/>
</c:if>
<div id="build-name">${buildInfo.buildName}</div>
</body>
</html>  
