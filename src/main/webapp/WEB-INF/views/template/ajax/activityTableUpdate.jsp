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
$('myIndicator').conceal()

<jsgen:replaceHtml targetElement="bottomsBottom">
    <c:if test="${showPrev}">
        <input type="button" id="prevActivityPageButton" name="prevActivityPageButton" value="< Previous" onclick="displayNext(${-index})"/>
    </c:if>
    <c:if test="${numberOfPages != null}">
         <c:forEach begin="1" end="${numberOfPages}" var="pageNumber" varStatus="pageNumberStatus">
            <input type="button" id="pageNumber" name="pageNumber" value="${pageNumber}" onclick="displayNext(${pageNumber *100 -100})"/>
        </c:forEach>
    </c:if>
    <c:if test="${showNext}">
        <input type="button" id="nextActivityPageButton" name="nextActivityPageButton" value="Next >" onclick="displayNext(${index})"/>
    </c:if>
</jsgen:replaceHtml>


<jsgen:replaceHtml targetElement="bottomsTop">
    <c:if test="${showPrev}">
        <input type="button" id="prevActivityPageButton" name="prevActivityPageButton" value="< Previous" onclick="displayNext(${-index})"/>
    </c:if>
    <c:if test="${numberOfPages != null}">
         <c:forEach begin="1" end="${numberOfPages}" var="pageNumber" varStatus="pageNumberStatus">
            <input type="button" id="pageNumber" name="pageNumber" value="${pageNumber}" onclick="displayNext(${pageNumber *100 -100})"/>
        </c:forEach>
    </c:if>
    <c:if test="${showNext}">
        <input type="button" id="nextActivityPageButton" name="nextActivityPageButton" value="Next >" onclick="displayNext(${index})"/>
    </c:if>
</jsgen:replaceHtml>