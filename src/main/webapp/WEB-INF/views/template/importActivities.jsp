<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>

<html>
<head><title>Import Activities</title></head>
<body>
<laf:box>
    <laf:division>
        <form:form method="post" enctype="multipart/form-data">
            <form:errors path="*"/>
            
            Activities Xml File: <input type="file" name="activitiesFile"/><br/>
            <input type="submit" value="Submit"/>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>