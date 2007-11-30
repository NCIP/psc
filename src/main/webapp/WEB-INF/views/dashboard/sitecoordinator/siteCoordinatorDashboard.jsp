<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:url var="action" value="${submitUrl}" />
<c:set var="isAssignByStudy" value="${submitUrl == '/pages/dashboard/siteCoordinator/assignSubjectCoordinatorByStudy'}"/>

<html>
<head>
    <title>Site Coordinator Dashboard</title>
    <tags:stylesheetLink name="main"/>
    <script type="text/javascript">
        function registerSelector() {
            var aElement = $('selector')
            Event.observe(aElement, "change", function(e) {
                Event.stop(e)
                location.href = "${action}?selected=" + aElement.value;
            })
        }

        Event.observe(window, "load", registerSelector);
    </script>

    <style type="text/css">
        div.label {
            width: 50%;
        }

        form {
            width: 40em;
        }

        .site-coord-dash-link {
            color:#0000cc;
            cursor:pointer;
            white-space:nowrap;
        }
    </style>
</head>
<body>

<laf:box title="Site Coordinator Dashboard">
    <laf:division>
        <form:form method="post" id="assignmentForm" action="${action}">
            <form:errors path="*"/>
            <form:hidden path="selected"/>

            <div class="links-row">
                Assign By:
                <c:if test="${isAssignByStudy}">
                    Study,
                    <span id="particip-coord-view" class="site-coord-dash-link" onclick="location.href='<c:url value="/pages/dashboard/siteCoordinator/assignSubjectCoordinatorByUser"/>'">Subject Coordinator</span>
                </c:if>
                <c:if test="${not isAssignByStudy}">
                    <span id="study-view" class="site-coord-dash-link" onclick="location.href='<c:url value="/pages/dashboard/siteCoordinator/assignSubjectCoordinatorByStudy"/>'">Study</span>,
                    Subject Coordinator
                </c:if>
            </div>
            <br/>

            <c:choose>
                <c:when test="${fn:length(studies) < 1 or fn:length(sites) < 1}">
                    There are no studies assigned to your site.
                </c:when>
                <c:when test="${fn:length(users) < 1}">
                    There are no subject coordinators for your site.
                </c:when>
                <c:otherwise>
                    <div class="row">
                        <div class="label" >
                            <c:if test="${isAssignByStudy}">
                                Study:
                            </c:if>
                            <c:if test="${not isAssignByStudy}">
                                Subject Coordinator:
                            </c:if>
                        </div>
                        <div class="value">
                            <c:if test="${isAssignByStudy}">
                                <select id="selector">
                                    <c:forEach items="${studies}" var="study">
                                        <option value="${study.id}" <c:if test="${study.id == selected.id}">selected</c:if>>${study.name}</option>
                                    </c:forEach>
                                </select>
                            </c:if>
                            <c:if test="${not isAssignByStudy}">
                                <select id="selector">
                                    <c:forEach items="${users}" var="user">
                                        <option value="${user.id}" <c:if test="${user.id == selected.id}">selected</c:if>>${user.name}</option>
                                    </c:forEach>
                                </select>
                                <input type="button" value="Studies" onclick="location.href='<c:url value="/pages/dashboard/siteCoordinator/assignSubjectCoordinatorByUser"/>?selected=${selected.id}'"/>
                                <input type="button" value="Subjects" onclick="location.href='<c:url value="/pages/dashboard/siteCoordinator/assignSubjectToSubjectCoordinatorByUser"/>?selected=${selected.id}'"/>
                            </c:if>

                        </div>
                    </div>
                    <div class="row">
                        <div class="label" >
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
                                    <th></th>

                                    <c:forEach items="${sites}" var="site">
                                        <th>${site.name}</th>
                                    </c:forEach>
                                </tr>
                                <c:forEach items="${command.grid}" var="x">
                                    <tr>
                                        <th>${x.key.name}</th>

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
                    <div class="row">
                        <div class="label" ></div>
                        <div class="value">
                            <input type="submit" value="Save"/>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </form:form>
    </laf:division>
</laf:box>
</body>
</html>