<%@page contentType="text/javascript" %>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>


<c:choose>
    <c:when test="${not empty error}">
        <jsgen:replaceHtml targetElement="errors"><h4>${error}</h4></jsgen:replaceHtml>
    </c:when>
    <c:otherwise>
        <jsgen:replaceHtml targetElement="errors"></jsgen:replaceHtml>
    </c:otherwise>
</c:choose>

<c:if test="${not empty source}">
    <jsgen:insertHtml targetElement="sources" position="top">
         <option value="${source.id}" selected="true">${source.name}</option>
    </jsgen:insertHtml>
</c:if>


<jsgen:replaceHtml targetElement="myTable">
<tags:activitiesTable/>

</jsgen:replaceHtml>
<tags:addNewActivityRow/>


<c:if test="${! displayCreateNewActivity}">
    <jsgen:replaceHtml targetElement="errors"><h4>Please select one of the sources to be able to create a new activity</h4></jsgen:replaceHtml>
</c:if>


<%--<jsgen:insertHtml targetElement="activities-input" position="bottom">--%>
<%--     <input id="reconcile" type="submit" name="reconcile" disabled="true" value="Reconcile" align="right" onclick="reconcileActivities()"--%>
<%--</jsgen:insertHtml>--%>

