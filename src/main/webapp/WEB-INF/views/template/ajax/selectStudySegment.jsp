<%@page contentType="text/javascript;charset=UTF-8"%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
<jsgen:replaceHtml targetElement="selected-studySegment">
    <templ:studySegment studySegment="${studySegment}" developmentRevision="${developmentRevision}"/>
</jsgen:replaceHtml>
Element.addClassName("studySegment-${studySegment.base.id}-item", "selected")
SC.slideAndShow('selected-studySegment-content')
initializeNewStudySegment()
