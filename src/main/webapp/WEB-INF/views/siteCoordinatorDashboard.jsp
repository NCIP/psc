<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
    <title>Site Coordinator Dashboard</title>

    <script type="text/javascript">
        function registerStudySelector() {
            var aElement = $('studySelector')
            Event.observe(aElement, "change", function(e) {
                Event.stop(e)
                location.href = $('assignmentForm').action + "?study=" + aElement.value;
            })
        }
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
    </style>
</head>
<body>

<laf:box title="Site Coordinator Dashboard">
    <laf:division>
        <c:url value="/pages/dashboard/siteCoordinatorSchedule" var="action"/>

        <form:form method="post" id="assignmentForm" action="${action}">
            <form:errors path="*"/>
            <div class="row">
                <div class="label" >
                    Study:
                </div>
                <div class="value">
                    <select id="studySelector">
                        <c:forEach items="${studies}" var="study">
                            <option value="${study.id}" <c:if test="${study == currentStudy}">selected</c:if>>${study.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>


            <div class="row">
                <div class="label" >
                    Assign Participant Coordinators:
                </div>
                <div class="value">
                    <table cellspacing="0" cellpading="0" border="1" class="siteRoles">
                        <tr>
                            <th></th>
                            <c:forEach items="${sites}" var="site">
                                <th>${site.name}</th>
                            </c:forEach>

                        </tr>
                        <c:forEach items="${command.studyAssignmentGrid}" var="user">
                            <tr>
                                <th>${user.key.name}</th>

                                <c:forEach items="${sites}" var="site">
                                    <c:if test="${command.studyAssignmentGrid[user.key][site].siteAccessAllowed}">
                                        <td>
                                            <form:checkbox path="studyAssignmentGrid[${user.key.id}][${site.id}].selected"/>
                                        </td>
                                    </c:if>
                                    <c:if test="${not command.studyAssignmentGrid[user.key][site].siteAccessAllowed}">
                                        <td style="background-color:#999">&nbsp;</td>
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