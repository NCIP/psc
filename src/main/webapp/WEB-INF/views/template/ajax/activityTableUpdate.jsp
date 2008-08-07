<%@page contentType="text/javascript" %>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>


<c:choose>
    <c:when test="${not empty error}">
        <jsgen:replaceHtml targetElement="errors"><h3>${error}</h3></jsgen:replaceHtml>
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
            <display:table name="activitiesPerSource" class="query-results" id="row" requestURI="activity" export="false" >
                <%--<display:column title="Reconsile">
//                   <input id="${row.id}" class="checkbox${row.id}" type="checkbox" value="${row.id}" name="checkbox"/>
                <%--</display:column>--%>
                <display:column title="Activity Name">
                    <label id="Name${row.id}">${row.name}</label>
                    <input id="InputName${row.id}" type="hidden" value="${row.name}"/>
                </display:column>

                <display:column title="Type">
                    <label id="Type${row.id}">${row.type}</label>
                    <div id="DivType${row.id}" style="display:none">
                        <select id="SourceTypes${row.id}">
                            <c:out value="${activityTypes}"/>
                            <c:forEach items="${activityTypes}" var="activityType">
                                <c:if test="${activityType.name == row.type}">
                                    <option value="${activityType.id}" selected="selected">${activityType.name}</option>
                                </c:if>
                                <c:if test="${activityType.name != row.type}">
                                    <option value="${activityType.id}">${activityType.name}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </display:column>

                <display:column title="Code">
                    <label id="Code${row.id}">${row.code}</label>
                    <input id="InputCode${row.id}" type="hidden" value="${row.code}"/>
                </display:column>

                <display:column title="Description">
                    <label id="Description${row.id}">${row.description}</label>
                    <input id="InputDescription${row.id}" type="hidden" value="${row.description}"/>
                </display:column>

                <display:column title="Source">
                    <label id="Source${row.id}">${row.source.name}</label>
                    <%--<input id="InputCode${row.id}" type="hidden" value="${row.code}"/>--%>
                </display:column>

                <display:column title="Controls">
                    <%--<c:out value="${row.id}"/>--%>
                    <%--<c:out value="${row_rowNum}"/>--%>
                    <input id="Edit${row.id}" type="button" name="EditButton" value="Edit" onclick="editActivity(${row.id})"/>
                    <input id="Save${row.id}" type="button" name="SaveButton" value="Save" style="display:none" onclick="saveActivity(${row.id})"/>
                    <c:forEach items="${enableDeletes}" var="enableDelete">
                       <c:if test="${row.id == enableDelete.key}">
                                <c:if test="${enableDelete.value==true}">
                                    <input id="Delete${row.id}" type="button" id="DeleteButton" name="DeleteButton" value="Delete" onclick="deleteActivity(${row.id})"/>
                                </c:if>
                       </c:if>
                    </c:forEach>
                </display:column>
            </display:table>
</jsgen:replaceHtml>


<c:if test="${displayCreateNewActivity == true}">
    <jsgen:insertHtml targetElement="row" position="top">
        <tr>
            <td>
                <input id="addActivityName" type="text" class="addActivityName"/>
            </td>
            <td>
                <select id="types" name="types" >
                    <c:forEach items="${activityTypes}" var="type">
                        <option class="type" id="type" value="${type.id}">${type.name}</option>
                    </c:forEach>
                </select>
            </td>
            <td>
                <input id="addActivityCode" type="text" class="addActivityCode" value=""/>
            </td>
            <td>
                <input id="addActivityDescription" type="text" class="addActivityDescription" value=""/>
            </td>
            <td>
                <label id="Source${row.id}">${row.source.name}</label>
            </td>

            <td>
                <input type="submit" id="addActivity" name="addActivity" value="Create" onclick="addNewActivity()"/>
            </td>
        </tr>

    </jsgen:insertHtml>

</c:if>


<%--<jsgen:insertHtml targetElement="activities-input" position="bottom">--%>
<%--     <input id="reconcile" type="submit" name="reconcile" disabled="true" value="Reconcile" align="right" onclick="reconcileActivities()"--%>
<%--</jsgen:insertHtml>--%>

