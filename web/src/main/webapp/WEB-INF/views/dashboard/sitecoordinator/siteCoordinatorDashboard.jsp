<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

    <tags:javascriptLink name="resig-templates" />
<c:url var="action" value="${submitUrl}"/>
<c:set var="isAssignByStudy"
       value="${submitUrl == '/pages/dashboard/siteCoordinator/assignSubjectCoordinatorByStudy'}"/>
<tags:stylesheetLink name="yui-sam/2.7.0/datatable"/>
<%-- TODO: move common YUI parts to a tag if they are re-used --%>
<c:forEach items="${fn:split('yahoo-dom-event element-min datasource-min logger-min json-min connection-min get-min datatable-min', ' ')}" var="script">
   <tags:javascriptLink name="yui/2.7.0/${script}"/>
</c:forEach>



<html>
<head>
    <tags:includeScriptaculous/>
    <title>Site Coordinator Dashboard</title>


    <tags:stylesheetLink name="main"/>
    <style type="text/css">
        #studies-autocompleter-input {
            width: 40%
        }

    </style>
    <script type="text/javascript">
        function registerSelector() {
            var aElement = $('selector')
            Event.observe(aElement, "change", function(e) {
                Event.stop(e)
                location.href = "${action}?selected=" + aElement.value;
            })
        }

        function createAutocompleter() {
             new SC.FunctionalAutocompleter(
                 'studies-autocompleter-input', 'studies-autocompleter-div', studyAutocompleterChoices, {
                     afterUpdateElement: function(input, selected) {
                         location.href = "${action}?selected=" + selected.id
                          input.value = ""
                          input.focus()
                     }
                 }
             );
         }

        function studyAutocompleterChoices(str, callback) {
              studyAutocompleterChoiceProcessing(function(data) {
              var lis = data.map(function(study) {
                        var id = study.id
                        var name = study.assigned_identifier
                        var listItem = "<li id='"  + id + "'>" + name + "</li>";
                        return listItem
              }).join("\n");

              callback("<ul>\n" + lis + "\n</ul>");
            });
        }

       function studyAutocompleterChoiceProcessing(callback) {
            var searchString = $F("studies-autocompleter-input")
            if (searchString == "Search for study") {
                searchString = ""
            }

            var uri = SC.relativeUri("/api/v1/studies")
            if (searchString.blank()) {
                return;
            }

            var params = {};
            if (!searchString.blank()) {
                params.q = searchString;
            }

            SC.asyncRequest(uri+".json", {
                method: "GET", parameters: params,
                onSuccess: function(response) {
                    callback(response.responseJSON.studies)
                }
            })
        }


        if (${isAssignByStudy}) {
            Event.observe(window, "load", createAutocompleter)
        } else {
            Event.observe(window, "load", registerSelector);
        }

    </script>

    <style type="text/css">
        div.label {
            width: 50%;
        }

        form {
            width: 40em;
        }

        .site-coord-dash-link {
            color: #0000cc;
            cursor: pointer;
            white-space: nowrap;
        }
    </style>
</head>
<body>

<c:if test="${isAssignByStudy}">
    <c:set var="title" value="Assign study to subject coordinators"/>
</c:if>
<c:if test="${not isAssignByStudy}">
    <c:set var="title" value="Assign subject coordinator to studies"/>
</c:if>
<laf:box title="${title}">
<laf:division>
<form:form method="post" id="assignmentForm" action="${action}" onsubmit="return false">
<form:errors path="*"/>
<form:hidden path="selected"/>

<div class="links-row">
    Assign By:
    <c:if test="${isAssignByStudy}">
        Study,
        <span id="particip-coord-view" class="site-coord-dash-link"
              onclick="location.href='<c:url value="/pages/dashboard/siteCoordinator/assignSubjectCoordinatorByUser"/>'">Subject Coordinator</span>
    </c:if>
    <c:if test="${not isAssignByStudy}">
                    <span id="study-view" class="site-coord-dash-link"
                          onclick="location.href='<c:url value="/pages/dashboard/siteCoordinator/assignSubjectCoordinatorByStudy"/>'">
                        Study</span>,
        Subject Coordinator
    </c:if>
</div>
<br/>
<c:choose>
    <c:when test="${fn:length(studies) < 1 and fn:length(sites) < 1}">
        There are no studies assigned to your site.
    </c:when>
    <c:when test="${fn:length(users) < 1}">
        There are no subject coordinators for your site.
    </c:when>
    <c:otherwise>
        <div class="row">
            <div class="label">
                <c:if test="${isAssignByStudy}">
                    Study:
                </c:if>
                <c:if test="${not isAssignByStudy}">
                    Subject Coordinator:
                </c:if>
            </div>
            <div class="value">
                <c:if test="${isAssignByStudy}">
                    <input id="studies-autocompleter-input" type="text" value="${selected.assignedIdentifier}"
                           hint="Search for studies" class="autocomplete" autocomplete="off"/>
                    <div id="studies-autocompleter-div" class="autocomplete"></div>
                </c:if>
                <c:if test="${not isAssignByStudy}">
                    <select id="selector">
                        <c:forEach items="${users}" var="user">
                            <option value="${user.id}"
                                    <c:if test="${user.id == selected.id}">selected</c:if>>${user.displayName}</option>
                        </c:forEach>
                    </select>
                </c:if>

            </div>
        </div>
        <c:if test="${not empty sites}">
            <div class="row">
                <div class="label">
                    Assign Subject
                    <c:if test="${isAssignByStudy}">
                        Coordinators:
                    </c:if>
                    <c:if test="${not isAssignByStudy}">
                        Coordinator:
                    </c:if>
                </div>
                <div class="value">
                    <table class="grid">
                        <tr>
                            <c:if test="${isAssignByStudy}">
                                <th>SCs / Site</th>
                            </c:if>
                            <c:if test="${not isAssignByStudy}">
                                <th>Studies / Sites</th>
                            </c:if>


                            <c:forEach items="${sites}" var="site">
                                <th>${site.name}</th>
                            </c:forEach>
                        </tr>
                        <c:forEach items="${command.grid}" var="x">
                            <tr>
                                <c:choose>
                                    <c:when test="${fn:contains(x,'Study')}">
                                       <th>${x.key.name}</th>
                                    </c:when>
                                    <c:otherwise>
                                       <th>${x.key.displayName}</th>
                                    </c:otherwise>
                                </c:choose>
                                <c:forEach items="${sites}" var="y">
                                    <c:if test="${command.grid[x.key][y].siteAccessAllowed}">
                                        <td>
                                            <form:checkbox path="grid[${x.key.id}][${y.id}].selected"/>
                                        </td>
                                    </c:if>
                                    <c:if test="${not command.grid[x.key][y].siteAccessAllowed}">
                                        <td class="blocked">&nbsp;</td>
                                    </c:if>
                                </c:forEach>
                            </tr>
                        </c:forEach>
                    </table>
                </div>
            </div>
        </c:if>
        <c:if test="${selected ne null}">

            <div class="row">
                <div class="label"></div>
                <div class="value">
                    <input id="here" type="button" value="Save" onClick="submit()"/>
                </div>
            </div>
        </c:if>
    </c:otherwise>
</c:choose>
</form:form>
</laf:division>
</laf:box>
</body>
</html>