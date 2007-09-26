<%@page contentType="text/javascript" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="templ" tagdir="/WEB-INF/tags/template"%>
<c:if test="${changed}">
    var epoch = $("epoch-${command.epoch.id}")
    epoch.remove()
    var sib = $("epoch-${relativeTo.id}")
    var container = sib.parentNode
    <c:choose>
        <c:when test="${position == 'before'}">container.insertBefore(epoch, sib)</c:when>
        <c:otherwise>container.appendChild(epoch)</c:otherwise>
    </c:choose>
    SC.highlight(epoch)
</c:if>
var epochDivs = $$("div.epoch")
epochDivs.each( function(elt) { Element.removeClassName(elt, "last") } )
Element.addClassName(epochDivs.last(), "last")
updateAllEpochsControlVisibility()
<templ:updateChanges changes="${revisionChanges}" revision="${developmentRevision}" />
