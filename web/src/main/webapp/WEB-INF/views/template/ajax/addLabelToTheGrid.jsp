<%@ page contentType="text/javascript;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
    <c:set var="arrayOfDays" value="" />
    <c:set var="arrayOfActivityIndices" value="" />
    <c:forEach items="${plannedActivities}" var="activity" varStatus="activityIndex">
        <c:set var="arrayOfDays" value="${arrayOfDays},${activity.day}" />
        <c:set var="arrayOfActivityIndices" value="${arrayOfActivityIndices},${activity.id}" />
    </c:forEach>

    var arrayOfDays = '<c:out value="${arrayOfDays}"/>'
    var arrayOfActivityIndices = '<c:out value="${arrayOfActivityIndices}"/>'
    var element = Builder.node("a", {
        href: '#',
        onclick:'new LabelDisplayLogic(\'${activity.name}\', \'${details}\', \'${conditionalDetails}\', \'${label.name}\', \'${label.id}\', \'${period.duration.days}\', \'${rowNumber}\', \'${arrayOfDays}\', \'${arrayOfActivityIndices}\', \'<c:url value=""/>\' )'
    })
    element.innerHTML='${label.name}' + ', '
    $('grid[${rowNumber}].label').appendChild(element)



