<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
    <title>Site Coordinator Dashboard</title>
    <tags:stylesheetLink name="main"/>
    <script type="text/javascript">
        function registerUserSelector() {
            var aElement = $('userSelector')
            Event.observe(aElement, "change", function(e) {
                Event.stop(e)
                location.href = $('assignmentForm').action + "?user=" + aElement.value;
            })
        }
        Event.observe(window, "load", registerUserSelector);
        Event.observe(window, "load", registerStudySelector);

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

        .site-coord-dash-link {
            color:#0000cc;
            cursor:pointer;
            white-space:nowrap;
        }


        table.grid td, table.grid th {
            text-align: center;
            padding: .5em;
        }

        table.grid td.blocked {
            background-color:#999;
        }

        table.grid th {
            background-color:#ddf
        }
    </style>
</head>
<body>

<laf:box title="Site Coordinator Dashboard">
    <laf:division>
        <c:url value="/pages/dashboard/siteCoordinatorScheduleByUser" var="action"/>

        <form:form method="post" id="assignmentForm" action="${action}">
            <form:errors path="*"/>

            <div class="links-row">
                Assign By:
                <span id="study-view" class="site-coord-dash-link" onclick="location.href='<c:url value="/pages/dashboard/siteCoordinatorScheduleByStudy"/>'">Study</span>,
                Participant Coordinator
            </div>
            <br/>
            <form:hidden path="user"/>

            <div class="row">
                <div class="label" >
                    User:
                </div>
                <div class="value">
                    <select id="userSelector">
                        <c:forEach items="${users}" var="user">
                            <option value="${user.id}" <c:if test="${user == currentUser}">selected</c:if>>${user.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>


            <div class="row">
                <div class="label" >
                    Assign Participant Coordinators:
                </div>
                <div class="value">
                    <table cellspacing="0" cellpading="0" border="1" class="grid">
                        <tr>
                            <th></th>

                            <c:forEach items="${sites}" var="site">
                                <th>${site.name}</th>
                            </c:forEach>
                        </tr>
                        <c:forEach items="${command.studyAssignmentGrid}" var="study">
                            <tr>
                                <th>${study.key.name}</th>

                                <c:forEach items="${sites}" var="site">
                                    <c:if test="${command.studyAssignmentGrid[study.key][site].siteAccessAllowed}">
                                        <td>
                                            <form:checkbox path="studyAssignmentGrid[${study.key.id}][${site.id}].selected"/>
                                        </td>
                                    </c:if>
                                    <c:if test="${not command.studyAssignmentGrid[study.key][site].siteAccessAllowed}">
                                        <td class="blocked">&nbsp;</td>
                                    </c:if>
                                </c:forEach>
                            </tr>
                        </c:forEach>
                    </table>
                </div>
            </div>


            <div class="row">
                <div class="label" ></div>
                <div class="value">
                    <input type="submit" value="Save"/>
                </div>
            </div>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>