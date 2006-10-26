<%@page contentType="text/javascript" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:if test="${changed}">
    var armItem = $("arm-${command.arm.id}-item")
    armItem.remove()
    var sib = $("arm-${relativeTo.id}-item")
    var container = sib.parentNode
    <c:choose>
        <c:when test="${position == 'before'}">container.insertBefore(armItem, sib)</c:when>
        <c:otherwise>container.appendChild(armItem)</c:otherwise>
    </c:choose>
    SC.highlight(armItem)
</c:if>
var items = $$("#epoch-${command.arm.epoch.id}-arms li")
items.each( function(elt) { Element.removeClassName(elt, "last") } )
Element.addClassName(items.last(), "last")
updateAllArmsControlVisibility(${command.arm.epoch.id})
