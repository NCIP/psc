<%@page contentType="text/javascript" language="java" %>
<%@taglib prefix="sitecoord" tagdir="/WEB-INF/tags/dashboard/sitecoordinator" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<jsgen:replaceHtml targetElement="${study.id}_${site.id}">
    <sitecoord:displaySubjects study="${study}" site="${site}" subjects="${subjects}" />    
</jsgen:replaceHtml>

hideEmptyLists(${study.id}, ${site.id})