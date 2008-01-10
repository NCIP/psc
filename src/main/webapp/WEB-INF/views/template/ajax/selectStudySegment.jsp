<%@page contentType="text/javascript;charset=UTF-8"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsgen:replaceHtml targetElement="selected-studySegment">
    <templ:studySegment studySegment="${studySegment}" developmentRevision="${developmentRevision}"/>
</jsgen:replaceHtml>
Element.addClassName("studySegment-${studySegment.base.id}-item", "selected")
SC.slideAndShow('selected-studySegment-content')
initializeNewStudySegment()
<jsgen:replaceHtml targetElement="errorMessages">
    <tags:replaceErrorMessagesForTemplate/>
</jsgen:replaceHtml>
<c:if test="${not empty developmentRevision}">
    hideShowReleaseTemplateButton()
</c:if>
