<%@page contentType="text/javascript" language="java" %>
<%@taglib prefix="sitecoord" tagdir="/WEB-INF/tags/dashboard/sitecoordinator" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<jsgen:replaceHtml targetElement="${study.id}_${site.id}">
    <sitecoord:displayParticipants study="${study}" site="${site}" participants="${participants}" />    
</jsgen:replaceHtml>