<%@tag%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<%@attribute name="configEntry" type="edu.northwestern.bioinformatics.studycalendar.web.admin.BindableConfigurationEntry" %>
<%@attribute name="configEntryPath" type="java.lang.String" %>
<c:choose>
    <c:when test="${configEntry.property.controlType == 'boolean'}">
        <div>
            <label><form:radiobutton path="${configEntryPath}" value="true"/> Yes</label>
            <label><form:radiobutton path="${configEntryPath}" value="false"/> No</label>
        </div>
    </c:when>
    <c:when test="${configEntry.property.controlType == 'text'}">
        <div><form:input path="${configEntryPath}"/></div>
    </c:when>
    <c:otherwise>
        <div>Unimplemented control type ${configEntry.property.controlType} for ${configEntryPath}</div>
    </c:otherwise>
</c:choose>
<p class="description">${configEntry.property.description}</p>
<c:if test="${not empty configEntry.default}"><p class="description">(Default: ${configEntry.default})</p></c:if>
