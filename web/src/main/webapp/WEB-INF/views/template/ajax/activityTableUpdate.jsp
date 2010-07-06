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

<c:if test="${empty error && !displayCreateNewActivity}">
    <jsgen:replaceHtml targetElement="errors"><h4>Please select one of the sources to be able to create a new activity</h4></jsgen:replaceHtml>
</c:if>
$('myIndicator').conceal()

<jsgen:replaceHtml targetElement="bottomsTop">
    <c:if test="${showPrev}">
        <input type="button" id="bottomsTopPrevActivityPageButton" value="< Previous" onclick="displayNext(${-index})"/>
    </c:if>
    <c:if test="${numberOfPages != null}">
         <c:forEach begin="1" end="${numberOfPages}" var="pageNumber" varStatus="pageNumberStatus">
            <c:choose>
                <c:when test="${selectedPage == (pageNumber *100 -100)}">
                    <input type="button" id="bottomsTopPageNumber${pageNumber *100 -100}" class="pageNumberSelected" value="${pageNumber}" onclick="displayNext(${pageNumber *100 -100})"/>
                </c:when>
                <c:otherwise>
                    <input type="button" id="bottomsTopPageNumber${pageNumber *100 -100}" class="pageNumberDefault" value="${pageNumber}" onclick="displayNext(${pageNumber *100 -100})"/>
                </c:otherwise>
            </c:choose>
        </c:forEach>
    </c:if>
    <c:if test="${showNext}">
        <input type="button" id="bottomsTopNextActivityPageButton" value="Next >" onclick="displayNext(${index})"/>
    </c:if>
</jsgen:replaceHtml>

<jsgen:replaceHtml targetElement="bottomsBottom">
    <c:if test="${showPrev}">
        <input type="button" id="bottomsBottomPrevActivityPageButton" value="< Previous" onclick="displayNext(${-index})"/>
    </c:if>
    <c:if test="${numberOfPages != null}">
        <c:forEach begin="1" end="${numberOfPages}" var="pageNumber" varStatus="pageNumberStatus">
            <c:choose>
                <c:when test="${selectedPage == (pageNumber *100 -100)}">
                    <input type="button" id="bottomsBottomPageNumber${pageNumber *100 -100}" class="pageNumberSelected" value="${pageNumber}" onclick="displayNext(${pageNumber *100 -100})"/>
                </c:when>
                <c:otherwise>
                    <input type="button" id="bottomsBottomPageNumber${pageNumber *100 -100}" class="pageNumberDefault" name="pageNumber" value="${pageNumber}" onclick="displayNext(${pageNumber *100 -100})"/>
                </c:otherwise>
            </c:choose>
        </c:forEach>
    </c:if>
    <c:if test="${showNext}">
        <input type="button" id="bottomsBottomNextActivityPageButton" value="Next >" onclick="displayNext(${index})"/>
    </c:if>
</jsgen:replaceHtml>
