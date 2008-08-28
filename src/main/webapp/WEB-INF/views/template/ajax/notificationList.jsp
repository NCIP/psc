<%@page contentType="text/javascript;charset=UTF-8"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="tag" tagdir="/WEB-INF/tags/dashboard/subjectcoordinator" %>

<jsgen:replaceHtml targetElement="notification-table">
    <tag:notificationsList/>
</jsgen:replaceHtml>