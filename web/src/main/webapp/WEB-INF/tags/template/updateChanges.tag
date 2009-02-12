<%@ taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator" %>
<%@ taglib prefix="templ" tagdir="/WEB-INF/tags/template" %>
<%@ attribute name="revision" type="edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision"%>
<%@ attribute name="changes" type="edu.northwestern.bioinformatics.studycalendar.web.delta.RevisionChanges"%>
if ($('revision-changes')) {
<jsgen:replaceHtml targetElement="revision-changes">
    <templ:changes changes="${changes}" revision="${revision}" />
</jsgen:replaceHtml>
}