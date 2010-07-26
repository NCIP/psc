<%@page contentType="text/javascript;charset=UTF-8"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="studySegment" scope="request" type="edu.northwestern.bioinformatics.studycalendar.web.template.StudySegmentTemplate"/>
<jsp:useBean id="canEdit" scope="request" type="java.lang.Boolean"/>
<c:if test="${not empty requestScope['developmentRevision']}">
    <jsp:useBean scope="request" id="developmentRevision" type="edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision"/>
</c:if>

<jsgen:replaceHtml targetElement="selected-studySegment">
    <templ:studySegment studySegment="${studySegment}" developmentRevision="${developmentRevision}" visible="true" canEdit="${canEdit}"/>
</jsgen:replaceHtml>
Element.addClassName("studySegment-${studySegment.base.id}-item", "selected");
SC.slideAndShow('selected-studySegment-content');
initializeNewStudySegment();
<jsgen:replaceHtml targetElement="errorMessages">
    <tags:replaceErrorMessagesForTemplate/>
</jsgen:replaceHtml>
<c:if test="${canEdit}">
    hideShowReleaseTemplateButton()
    epochControlls()
</c:if>
 