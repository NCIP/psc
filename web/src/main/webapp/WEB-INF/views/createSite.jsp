<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<tags:stylesheetLink name="yui-sam/2.7.0/datatable"/>
<%-- TODO: move common YUI parts to a tag if they are re-used --%>
<c:forEach items="${fn:split('yahoo-dom-event element-min datasource-min logger-min json-min connection-min get-min datatable-min', ' ')}" var="script">
   <tags:javascriptLink name="yui/2.7.0/${script}"/>
</c:forEach>


<html>
<head>
    <tags:includeScriptaculous/>
    <tags:stylesheetLink name="provided-sites"/>
    <style type="text/css">
        div.label {
            width: 35%;
        }
        div.submit {
            text-align: center;
        }
        form {
            width: 40em;
        }
    </style>
    <tags:javascriptLink name="sites/provided-sites" />
    <tags:javascriptLink name="resig-templates" />
    <tags:resigTemplate id="new_site_data_row">
        <tr class="site row [#= identifier #]">
            <td title="[#= name #]" id="site-name-[#= identifier #]" >
                [#= name #]
            </td>
            <td title="[#= identifier #]" id="assigned-identifier-[#= identifier #]">
                [#= identifier #]
            </td>
            <td title="[#= provider #]" id="provider-[#= identifier #]">
                [#= provider #]
            </td>
        </tr>
    </tags:resigTemplate>
    <tags:resigTemplate id="new_site_control">
            <td id="control [#= identifier #]">
                <input id="add-[#= identifier #]" type="button" name="create-button" value="Create" onclick="SC.PS.addNewSite('[#= identifier #]')"/>
            </td>
    </tags:resigTemplate>
</head>
<body>

<laf:box title="${action} site" cssClass="yui-skin-sam"  autopad="true">
    <laf:division>
        <form:form method="post">
            <form:errors path="*"/>
            <input name="provider" id="provider" type="hidden"/>
            <div class="row">
                <div class="label"><form:label path="site.name">Site name</form:label></div>
                <div class="value"><form:input path="site.name" size="40" id="site-name"/></div>
            </div>
            <div class="row">
                <div Class="label"><form:label path="site.assignedIdentifier">Assigned Identifier</form:label></div>
                <div class="value"><form:input path="site.assignedIdentifier" size="40" id="assigned-identifier"/></div>
            </div>
            <div class="row submit">
                    <input type="submit" value="Save"/>
            </div>
            <tags:activityIndicator id="provided-site-search-indicator"/>
        </form:form>
        <c:forEach items="${sites}" var="site">
             <input id="existing-site-name-${fn:replace(site.name, " ", "_")}" type="hidden"/>
             <input id="existing-assigned-identifier-${site.assignedIdentifier}" type="hidden"/>
        </c:forEach>
        <div class="site-response" id="site-response"/>
    </laf:division>
</laf:box>
</body>
</html>