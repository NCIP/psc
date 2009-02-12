<%@tag%>
<%@taglib prefix="security" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@attribute name="task" required="true" type="gov.nih.nci.cabig.ctms.web.chrome.Task" %>
<security:secureOperation element="${task.url}">
    <laf:taskLink task="${task}"/>
</security:secureOperation>