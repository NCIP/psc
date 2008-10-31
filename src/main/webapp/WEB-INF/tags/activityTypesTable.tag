<%@tag%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>


<display:table name="activityTypes" class="query-results" id="row" requestURI="activityTypes" >
    <display:column title="Type" sortable="true">
        <label id="Type${row.id}">${row.name}</label>
        <input id="TypeName${row.id}" type="hidden" value="${row.name}"/>
    </display:column>

    <display:column title="Controls" media="html">
        <input id="Edit${row.id}" type="button" name="EditButton" value="Edit" onclick="editActivityType(${row.id})"/>
        <input id="Save${row.id}" type="button" name="SaveButton" value="Save" style="display:none" onclick="saveActivityType(${row.id})"/>
        <c:forEach items="${enableDeletes}" var="enableDelete">
           <c:if test="${row.id == enableDelete.key}">
                <c:if test="${enableDelete.value==true}">
                    <input id="Delete${row.id}" type="button" id="DeleteButton" name="DeleteButton" value="Delete" onclick="deleteActivityType(${row.id})"/>
                </c:if>
           </c:if>
        </c:forEach>
    </display:column>
</display:table>