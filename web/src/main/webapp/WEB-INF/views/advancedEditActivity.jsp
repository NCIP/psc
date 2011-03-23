<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>

<html>
<head>
    <tags:javascriptLink name="scriptaculous/scriptaculous"/>
    <tags:javascriptLink name="activity-property/activity-property" />
    <style type="text/css">
        div.label {
            width: 35%;
        }
        div.submit {
            text-align: right;
        }
        form {
            width: 35em;
        }
        a.property-edit {
            float:right;
        }
        a.property-edit:hover, a.newUriBtn:hover {
            background-color:#DDDDDD;
            border-color:#444444;
        }
        a.property-edit, a.newUriBtn {
            background-color:#CCCCCC;
            border:1px solid #999999;
            color:#444444 !important;
            margin:0 2px;
            padding:2px;
            font-weight:bold;
            text-decoration:none;
        }
        #uri-tips {
            float: right;
            width: 47%;
        }

        #activity-input {
            float: left;
            width: 50%;
        }
    </style>
    <script type="text/javascript">
        function updateActivityError() {
            if (isCorrectInput()){
               $('activityError').update("");
            }
        }
        function isCorrectInput() {
            var activityName = $('activity.name').value;
            var activityCode = $('activity.code').value;
            var isCorrectData = true;
            if (activityCode == "" || activityName =="") {
               isCorrectData  = false;
               $('activityError').update("Activity name or code can not be empty.");
            }
            return isCorrectData;
        }
        <c:if test="${not empty activity}">
            $(document).observe("dom:loaded", function() {
                $('activity-form').observe("submit", function(fn) {
                    if (!isCorrectInput()) {
                        Event.stop(fn);
                    }
                })
            })
        </c:if>
    </script>
  </head>
<body>
<laf:box title="Available URI Template Variables" autopad="true" id="uri-tips">
    <c:forEach items="${variables}" var="variable">
        <div class="row">
            <div class="label">${variable.attributeName}</div>
            <div class="value">${variable.description}</div>
        </div>
    </c:forEach>
</laf:box>
<laf:box title="${action} Activity" id="activity-input">
    <laf:division>
           <form:form id="activity-form">
            <div style="height:10px;padding-bottom:20px; width:40em; color:red;">
                <form:errors path="*"/>
                <h5 id="activityError"></h5>
            </div>
            <div class="row">
                <div class="label"><form:label path="activity.name">Activity Name</form:label></div>
                <div class="value">
                    <form:input path="activity.name" size="30"/>
                </div>
            </div>
            <div class="row">
                <div class="label"><form:label path="activity.code">Activity Code</form:label></div>
                <div class="value">
                    <form:input path="activity.code" size="30"/>
                </div>
            </div>
            <c:if test="${not empty source}">
                <div class="row">
                    <div class="label">Activity Source</div>
                    <div class="value">${source.name}</div>
                </div>
            </c:if>
            <div class="row">
                <div class="label"><form:label path="activity.type">Activity Type</form:label></div>
                <div class="value">
                    <form:select path="activity.type">
                       <c:forEach items="${activityTypes}" var="activityType">
                       <c:if test="${activityType.name == activityDefaultType}">
                           <option value="${activityType.id}" selected="selected">${activityType.name}</option>
                       </c:if>
                       <c:if test="${activityType.name != activityDefaultType}">
                           <option value="${activityType.id}">${activityType.name}</option>
                       </c:if>
                    </c:forEach>
                    </form:select>
                </div>
            </div>
            <div class="row">
                <div class="label"><form:label path="activity.description">Activity Description</form:label></div>
                <div class="value">
                    <form:input path="activity.description" size="30"/>
                </div>
            </div>
            <div class="row">
                <div class="label">URI</div>
                <div class="value" id="uriProperties">
                    <table width="100%" id="propertyTable" class="propertyTable">
                        <c:forEach items="${existingList}" var="list" varStatus="status">
                            <tr class="${commons:parity(status.count)} property" id="oldUri list-${status.count-1}" width="100%">
                                <td>
                                    <input name="listKey" class="listKey" id="listKey" type="hidden" value="${list.key}" />
                                    <span class="textName">Text: </span>
                                    <span class="textValue">${list.value.textValue}</span>
                                    <br />
                                    <span class="templateName">Template: </span>
                                    <span class="templateValue">${list.value.templateValue}</span>
                                    <a class="property-edit" id="property-edit" href="#property-edit">Edit</a>
                                 </td>
                            </tr>
                        </c:forEach>
                    </table>
                </div>
                <div class="value"><a class="newUriBtn" id="newUriBtn" href='#newUriBtn'>New URI</a></div>
            </div>
            <div class="row submit">
                <input type="submit" value="Save"/>
            </div>
        </form:form>
    </laf:division>
</laf:box>
<div id="lightbox">
    <div id="edit-property-lightbox">
        <h1>Editing Activity Uri</h1>
        <div class="row">
            <div class="label">
                <label for="edit-property-textValue">Text</label>
            </div>
            <div class="value">
                <input type="text" class="text" id="edit-property-textValue" hint="External URI" size="40"/>
            </div>
        </div>
        <div class="row">
            <div class="label">
                <label for="edit-property-templateValue">Template</label>
            </div>
            <div class="value">
                <input type="text" class="text" id="edit-property-templateValue" hint="https://external?subject={subject-identifier}." size="40" />
            </div>
        </div>
        <div class="row">
            <div class="submit">
                <input type="button" value="Done" id="edit-property-done"/>
            </div>
        </div>
    </div>
</div>
</body>
</html>