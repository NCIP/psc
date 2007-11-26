<%@page contentType="text/javascript" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
<c:if test="${changed}">
    var studySegmentItem = $("studySegment-${command.studySegment.id}-item")
    studySegmentItem.remove()
    var sib = $("studySegment-${relativeTo.id}-item")
    var container = sib.parentNode
    // replace relative to sib (${position})
    <c:choose>
        <c:when test="${position == 'before'}">container.insertBefore(studySegmentItem, sib)</c:when>
        <c:otherwise>container.appendChild(studySegmentItem)</c:otherwise>
    </c:choose>
    SC.highlight(studySegmentItem)
</c:if>
var items = $$("#epoch-${command.studySegment.epoch.id}-studySegments li")
items.each( function(elt) { Element.removeClassName(elt, "last") } )
Element.addClassName(items.last(), "last")
updateAllStudySegmentsControlVisibility(${command.studySegment.epoch.id})
<templ:updateChanges changes="${revisionChanges}" revision="${developmentRevision}" />
