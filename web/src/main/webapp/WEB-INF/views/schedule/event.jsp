<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>

<c:set var="modeTitle" value="Edit"/>
<c:if test="${readOnly}">
    <c:set var="modeTitle" value="View"/>
</c:if>

<html>
<head>
    <title>${modeTitle} activity</title>
    <style type="text/css">
        #states li.previous {
            display: none;
        }

        #states li {
            list-style-type: none;
        }

        #states.expanded li {
            list-style-type: circle;
        }

        #states.expanded li.previous {
            display: list-item;
        }

        #states.expanded li.current {
            font-weight: bold;
        }

        ul#states {
            padding: 0;
            margin: 0 0 1em 0;
        }

        ul#states li {
        }

        div.label a.control {
            display: block;
            font-size: 0.9em;
            font-weight: normal;
        }

        div.row {
            padding: 4px 1em;
        }

        div.row div.label {
            width: 10em;
        }

        div.row div.value {
            margin-left: 12em;
        }
    </style>
    <script type="text/javascript">
        function registerStateHistoryControl() {
            Event.observe("show-history-control", "click", function(e) {
                Event.stop(e)
                if (Element.hasClassName("states", "expanded")) {
                    Element.removeClassName("states", "expanded")
                    $("show-history-control").innerHTML = "Show history"
                } else {
                    $("show-history-control").innerHTML = "Hide history"
                    Element.addClassName("states", "expanded")
                }
            })
        }

        Event.observe(window, "load", registerStateHistoryControl)
    </script>
</head>
<body>
<laf:box title="${modeTitle} activity for ${subject.fullName}">
    <laf:division>
        <form:form>
            <c:set var="count" value="0"/>
            <div class="row ${commons:parity(count)}">
                <div class="label">Activity</div>
                <div class="value">${scheduledActivity.activity.name}</div>
            </div>
            <c:set var="count" value="${count + 1}"/>
            <c:if test="${not empty scheduledActivity.activity.description}">
                <div class="row ${commons:parity(count)}">
                    <div class="label">Description</div>
                    <div class="value">${scheduledActivity.activity.description}</div>
                </div>
                <c:set var="count" value="${count + 1}"/>
            </c:if>
            <div class="row ${commons:parity(count)}">
                <div class="label">Code</div>
                <div class="value">${scheduledActivity.activity.code}</div>
            </div>
            <c:set var="count" value="${count + 1}"/>
            <c:if test="${not empty scheduledActivity.details}">
                <div class="row ${commons:parity(count)}">
                    <div class="label">Details</div>
                    <div class="value">${scheduledActivity.details}</div>
                </div>
                <c:set var="count" value="${count + 1}"/>
            </c:if>
            <c:if test="${not empty scheduledActivity.plannedActivity.condition}">
                <div class="row ${commons:parity(count)}">
                    <div class="label">Condition</div>
                    <div class="value">${scheduledActivity.plannedActivity.condition}</div>
                </div>
                <c:set var="count" value="${count + 1}"/>
            </c:if>
            <c:if test="${not empty scheduledActivity.labels}">
                <div class="row ${commons:parity(count)}">
                    <div class="label">Labels</div>
                    <div class="value">
                        ${commons:join(scheduledActivity.labels, " ")}
                    </div>
                </div>
                <c:set var="count" value="${count + 1}"/>
            </c:if>
            <c:if test="${not empty uriMap}">
                <div class="row ${commons:parity(count)}">
                    <div class="label">Links</div>
                    <c:forEach items="${uriMap}" var="uri" varStatus="keyStatus">
                         <div class="value"><a href="<c:url value="${uri.value}"/>">${uri.key}</a></div>
                    </c:forEach>
                </div>
            <c:set var="count" value="${count + 1}"/>
            </c:if>
            <div class="row ${commons:parity(count)}">
                <div class="label">
                    Status
                    <a href="#" class="control" id="show-history-control">Show history</a>
                </div>
                <div class="value">
                    <ul id="states">
                        <c:forEach items="${scheduledActivity.previousStates}" var="state">
                            <li class="previous">${state.textSummary}</li>
                        </c:forEach>
                        <li class="current">${scheduledActivity.currentState.textSummary}</li>
                    </ul>
                    <c:if test="${!readOnly}">
                        <label id="new-mode-selector-group">Change to
                            <form:select path="newMode" id="new-mode-selector">
                                <form:option value="" label=""/>
                                <form:option value="${scheduledActivity.currentState.mode.id}" label="leave the state same"/>
                                <form:options items="${modes}" itemValue="id" itemLabel="name"/>
                            </form:select>
                        </label>
                        <label id="new-date-input-group">Date <laf:dateInput path="newDate"/></label>
                        <label id="new-reason-input-group">Why? <form:input path="newReason"/></label>
                    </c:if>
                </div>
            </div>
            <c:set var="count" value="${count + 1}"/>

            <div class="row ${commons:parity(count)}">
                <div class="label"><form:label path="newNotes">Notes</form:label></div>
                <div class="value">
                    <c:if test="${!readOnly}">
                        <form:textarea path="newNotes" rows="6" cols="30"/>
                    </c:if>
                    <c:if test="${readOnly}">
                        ${command.newNotes}
                    </c:if>
                </div>
            </div>
            <c:set var="count" value="${count + 1}"/>

            <c:if test="${!readOnly}">
                <div class="row submit">
                    <input type="submit" value="Save"/>
                </div>
            </c:if> 
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>