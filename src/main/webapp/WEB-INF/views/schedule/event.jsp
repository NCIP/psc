<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<html>
<head>
    <%--<title>Edit event for ${subject.fullName}</title>--%>
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
    < tags:javascriptLink name="scheduled-activity"/>
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
<%--<h1>Edit event for ${subject.fullName}</h1>--%>
<laf:box title="Edit activity for ${subject.fullName}">
    <laf:division>
        <form:form>
            <div class="row even">
                <div class="label">Activity</div>
                <div class="value">${scheduledActivity.activity.name}</div>
            </div>
            <div class="row odd">
                <div class="label">Details</div>
                <div class="value">${scheduledActivity.details}</div>
            </div>
            <c:if test="${not empty scheduledActivity.plannedActivity.condition}">
                <div class="row even">
                    <div class="label">Condition</div>
                    <div class="value">${scheduledActivity.plannedActivity.condition}</div>
                </div>
            </c:if>
            <c:choose>
                <c:when test="${not empty scheduledActivity.plannedActivity.condition}">
                    <div class="row odd">
                </c:when>
                <c:otherwise>
                    <div class="row even">
                </c:otherwise>
            </c:choose>
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
                    <label id="new-mode-selector-group">Change to
                        <form:select path="newMode" id="new-mode-selector">
                            <form:option value="" label=""/>
                            <form:options items="${modes}" itemValue="id" itemLabel="name"/>
                        </form:select>
                    </label>
                    <label id="new-date-input-group">Date <form:input path="newDate"/></label>
                    <label id="move_date_by_new-date-input-group"/>
                    <label id="new-reason-input-group">Why? <form:input path="newReason"/></label>
                </div>
            </div>

            <c:choose>
                <c:when test="${not empty scheduledActivity.plannedActivity.condition}">
                    <div class="row even">
                </c:when>
                <c:otherwise>
                    <div class="row odd">
                </c:otherwise>
            </c:choose>
                <div class="label"><form:label path="newNotes">Notes</form:label></div>
                <div class="value">
                    <form:textarea path="newNotes" rows="6" cols="30"/>
                </div>
            </div>

            <div class="row submit">
                <input type="submit" value="Save"/>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>