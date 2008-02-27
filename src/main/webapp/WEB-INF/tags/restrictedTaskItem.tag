<%@tag%>
<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@attribute name="task" required="true" type="gov.nih.nci.cabig.ctms.web.chrome.Task" %>
<security:secureOperation element="${task.url}">
    <laf:taskLink task="${task}"/>
</security:secureOperation>