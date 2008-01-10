<%@ page contentType="text/javascript;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<c:if test="${not empty command.studySegment}">
    <jsgen:replaceHtml targetElement="studySegment-${command.studySegment.id}">${command.revisedStudySegment.name}</jsgen:replaceHtml>
</c:if>
<c:if test="${not empty command.epoch}">
    <jsgen:replaceHtml targetElement="epoch-${command.epoch.id}-name">${command.revisedEpoch.name}</jsgen:replaceHtml>
</c:if>
<c:if test="${not empty command.study}">
    <jsgen:replaceHtml targetElement="study-name">${command.study.assignedIdentifier}</jsgen:replaceHtml>
    <jsgen:replaceHtml targetElement="breadcrumbs">
        <a href="/studycalendar/pages/cal/studyList">Studies</a> /
        <a href="/studycalendar/pages/cal/template?study=${command.study.id}&amendment=${command.study.developmentAmendment.id}">${command.study.name}</a>
    </jsgen:replaceHtml>
</c:if>
<templ:updateChanges changes="${revisionChanges}" revision="${developmentRevision}" />

    <jsgen:replaceHtml targetElement="errorMessages">
        <tags:replaceErrorMessagesForTemplate/>
    </jsgen:replaceHtml>


