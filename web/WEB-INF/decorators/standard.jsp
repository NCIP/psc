<%-- This is the standard decorator for all study calendar pages --%>
<%@taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
  <head>
    <title>caBIG Study Calendar Module - <decorator:title/></title>
    <tags:stylesheetLink name="common"/>
    <tags:javascriptLink name="common"/>
    <decorator:head/>
  </head>
  <body>
    <div id="body">
    <decorator:body/>
    </div>
    <tags:debugInfo/>
  </body>
</html>
