<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn" %>

<html>
<head>
    <title>New User</title>
    <tags:includeScriptaculous/>

    <script type="text/javascript">
    function highlightSites(color) {
        var table = document.getElementById("gridSiteRole")
        var siteHeaders = table.getElementsByTagName("th");

        for (var j = 0; j < siteHeaders.length; j++) {

            if (siteHeaders[j].id.indexOf("siteHeader") >= 0) {
                siteHeaders[j].style.background = color;

            }
        }
    }

    function updateTable() {
        var greenColor = 'green';
        var whiteColor = '';
        var blackColor = '#ccc'

        var table = document.getElementById("gridSiteRole")

        var rows = table.getElementsByTagName("tr");
        var allRowHighlighted = false;
        var roleHeaders = rows[0].getElementsByTagName("th");
        for (var i = 0; i < roleHeaders.length; i++) {
            roleHeaders[i].style.background = whiteColor;

        }
        for (var i = 1; i < rows.length; i++) {
            var row = rows[i];

            var headers = row.getElementsByTagName("th");

            var columns = row.getElementsByTagName("td");
            var individualRowHighlighted = false;
            for (var j = 0; j < columns.length; j++) {
                var box = document.getElementById(columns[j].id + "1");
                var check = box.checked;
                if (columns[j].className != null && columns[j].className == 'notSiteSpecific') {
                    if (check) {
                        allRowHighlighted = true;
                        columns[j].style.background = greenColor;
                        roleHeaders[j + 1].style.background = blackColor;

                    } else {

                        columns[j].style.background = whiteColor;
                    }
                } else {
                    if (check) {
                        individualRowHighlighted = true;
                        columns[j].style.background = greenColor;
                        if (i == 1) {
                            roleHeaders[j+1].style.background = blackColor;
                        } else {
                            roleHeaders[j + 4].style.background = blackColor;

                        }
                    } else {
                        columns[j].style.background = whiteColor;
                    }
                }
            }
            if (individualRowHighlighted) {
                headers[0].style.background = blackColor;
            } else {
                headers[0].style.background = whiteColor;
            }
        }
        if (allRowHighlighted) {
            highlightSites(blackColor);
        }

    }

    function checkUnCheckBox(box) {
        var checkBoxVar = document.getElementById(box + '1');
        var checked = checkBoxVar.checked;
        if (checked) {
            checkBoxVar.checked = false;
        } else {
            checkBoxVar.checked = true;
        }
        updateTable()
    }

    <c:if test="${actionText=='Edit'}">
    function registerEditPasswordLink() {
        var aElement = $('changePassword')
        Event.observe(aElement, "click", function(e) {
            Event.stop(e)
            $('passwordField').enable();
            $('rePasswordField').enable();
            $('cancelChangePassword').show()
            $('changePassword').hide();
            $('passwordModified').value = true;
        })
    }

    function registerCancelEditPasswordLink() {
        var aElement = $('cancelChangePassword')
        Event.observe(aElement, "click", function(e) {
            Event.stop(e)
            $('passwordField').clear();
            $('rePasswordField').clear();
            $('passwordField').disable();
            $('rePasswordField').disable();
            $('cancelChangePassword').hide()
            $('changePassword').show();
            $('passwordModified').value = false;
        })
    }

    Event.observe(window, "load", registerEditPasswordLink);
    Event.observe(window, "load", registerCancelEditPasswordLink);
    Event.observe(window, "load", updateTable);
    </c:if>

    </script>
    <style type="text/css">
        div.label {
            width: 50%;
        }

        div.submit {
            text-align: left;
        }

        form {
            width: 40em;
        }

        h2 {
            margin-bottom: 0px;
            font-size: 15px;
        }

        table.siteRoles td, table.siteRoles th {
            text-align: center;
            padding: .5em;
        }

        table.siteRoles td.notSiteSpecific {
            vertical-align: top;
        }

        .password-edit-link {
            color:#0000cc;
            cursor:pointer;
            white-space:nowrap;
        }

    </style>
</head>
<body>
<laf:box title="${actionText} User">
    <laf:division>

        <form:form method="post">
            <form:hidden path="user.id"/>
            <form:hidden path="passwordModified"/>
            <tags:errors path="user.*"/>
            <tags:errors path="emailAddress*"/>
            <tags:errors path="password*"/>
            <c:set var="passwordError" value="false"/>
            <spring:bind path="command.password">
                <c:set var="passwordError" value="${status.errors.errorCount > 0}"/>
            </spring:bind>
            <div class="row">
                <div class="label" >
                    <form:label path="user.name" >User Name:</form:label>
                </div>
                <div class="value">
                    <c:if test="${actionText=='Create'}">
                        <form:input path="user.name"/>
                    </c:if>
                    <c:if test="${actionText=='Edit'}">
                        ${command.user.name}
                    </c:if>
                </div>
            </div>
            <div class="row">
                <div class="label">
                    <form:label path="emailAddress">Email Address:</form:label>
                </div>
                <div class="value">
                    <c:if test="${actionText=='Create'}">
                        <form:input path="emailAddress"/>
                    </c:if>
                    <c:if test="${actionText=='Edit'}">
                        ${command.emailAddress}
                    </c:if>
                </div>
            </div>

            <c:if test="${usingLocalAuthenticationSystem}">
                <div class="row">
                    <div class="label" >
                        <form:label path="password">Password:</form:label>
                    </div>
                    <div class="value">
                        <form:password path="password" id="passwordField" disabled="${actionText=='Edit' and not passwordError}"/>

                        <c:if test="${actionText == 'Edit'}">
                            <span id="changePassword" class="password-edit-link" style="<c:if test="${passwordError}">display:none</c:if>">Change Password</span>
                            <span id="cancelChangePassword" class="password-edit-link" style="<c:if test="${not passwordError}">display:none</c:if>">Undo Password Changes</span>
                        </c:if>
                    </div>
                </div>
                <div class="row">
                    <div class="label" >
                        <form:label path="rePassword">Re-Enter Password:</form:label>
                    </div>
                    <div class="value">
                        <form:password path="rePassword" id="rePasswordField" disabled="${actionText=='Edit' and not passwordError}"/>
                    </div>
                </div>
            </c:if>
            <div class="row">
                <div class="label" >
                    <form:label path="userActiveFlag">Enable User:</form:label>
                </div>
                <div class="value">
                    <%--<input type="checkbox" name="command.userActiveFlag" <c:if test="${command.user.activeFlag}">checked="true"</c:if>/>--%>
                    <input name="userActiveFlag" type="checkbox" <c:if test="${command.user.activeFlag}">checked="true"</c:if>/>
                </div>
            </div>
            <div class="row">
                <div class="label">
                    <h2>Please select roles</h2>
                </div>
                <div class="value">
                    <tags:errors path="rolesGrid*"/>
                    <table class="grid siteRoles" id="gridSiteRole">
                        <tr>
                            <th></th>
                            <c:forEach items="${roles}" var="role">
                                <th id="roleHeader${site.key.id}">${role.displayName}</th>
                            </c:forEach>

                        </tr>
                        <c:forEach items="${command.rolesGrid}" var="site" varStatus="index">
                            <tr id="siteRow${site.key.id}">
                                <th id="siteHeader${site.key.id}">${site.key.name}</th>

                                <c:forEach items="${roles}" var="role">
                                    <c:if test="${not role.siteSpecific && index.first}">
                                        <td id="rolesGrid[${site.key.id}][${role}].selected"
                                            <c:if test="${not role.siteSpecific && index.first}">rowspan="${fn:length(command.rolesGrid)}"</c:if>
                                            class="notSiteSpecific" onclick="checkUnCheckBox(
                                                           'rolesGrid[${site.key.id}][${role}].selected')">
                                            <form:checkbox path="rolesGrid[${site.key.id}][${role}].selected" onclick="checkUnCheckBox(
                                                           'rolesGrid[${site.key.id}][${role}].selected')"/>
                                        </td>
                                    </c:if>
                                    <c:if test="${role.siteSpecific}">
                                        <td id="rolesGrid[${site.key.id}][${role}].selected" onclick="checkUnCheckBox(
                                                           'rolesGrid[${site.key.id}][${role}].selected')">
                                            <form:checkbox path="rolesGrid[${site.key.id}][${role}].selected" onclick="checkUnCheckBox(
                                                           'rolesGrid[${site.key.id}][${role}].selected')"/>
                                        </td>
                                    </c:if>
                                </c:forEach>
                            </tr>
                        </c:forEach>
                    </table>
                </div>

            </div>


            <div class="row">
                <div class="label">&nbsp;</div>
                <div class="submit">
                    <input type="submit"
                           value="<c:if test="${actionText=='Create'}">Create</c:if><c:if test="${actionText=='Edit'}">Save</c:if>"/>
                    <input type="submit"
                           name="_cancel"
                           value="Cancel"/>
                </div>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>