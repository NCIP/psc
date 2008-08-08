<%@tag%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

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
