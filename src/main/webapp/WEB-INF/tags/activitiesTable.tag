<%@tag%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>


<display:table name="activitiesPerSource" class="query-results" id="row" requestURI="activities" export="false" >
    <%--<display:setProperty name="export.xml" value="true"/>--%>
    <%--<display:setProperty name="export.xml.class" value="edu.northwestern.bioinformatics.studycalendar.web.EnhancedXmlView"/>--%>
    <display:column property="name" title="Name" sortable="true">
        <label id="Name${row.id}">${row.name}</label>
        <input id="InputName${row.id}" type="hidden" value="${row.name}"/>
    </display:column>

    <display:column property="type" title="Type" sortable="true">
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

    <display:column property="code" title="Code">
        <label id="Code${row.id}">${row.code}</label>
        <input id="InputCode${row.id}" type="hidden" value="${row.code}"/>
    </display:column>

    <display:column property="description" title="Description">
        <label id="Description${row.id}">${row.description}</label>
        <input id="InputDescription${row.id}" type="hidden" value="${row.description}"/>
    </display:column>

    <display:column property="source.name" title="Source" media="excel csv">
        <label id="Source${row.id}">${row.source.name}</label>
    </display:column>

    <display:column title="Controls" media="html">
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

