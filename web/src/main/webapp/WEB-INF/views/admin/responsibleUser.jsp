<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="commons"
           uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/functions" %>

<jsp:useBean id="command" scope="request"
             type="edu.northwestern.bioinformatics.studycalendar.web.admin.ResponsibleUserForSubjectAssignmentCommand"/>
<jsp:useBean id="initialShow" scope="request"
             type="java.lang.Boolean"/>

<html>
<head>
    <title>Reassign subjects managed by ${not empty command.responsible ? command.responsible.displayName : 'no one'}</title>
    <style type="text/css">
        div.target-assignments {
            float: left;
            width: 20em;
        }
        div.target-assignments label {
            display: block;
        }
        div.target-assignments label.all {
            font-style: italic;
        }
        div.new-responsible {
            float: left;
            width: 30em;
        }
    </style>
    <script type="text/javascript">
        (function ($) {
            function selectOrDeselectAll(selected, id) {
                $('#study-site-' + id + " input.target-assignment").each(function (i, e) {
                    e.checked = selected;
                });
            }

            function syncAllCheckbox(id) {
                var base = '#study-site-' + id + " input.target-assignment";
                $('#all-assignments-' + id)[0].checked =
                    ($(base + ":checked").length == $(base).length);
            }

            $(function () {
                $('input.all').click(function (evt) {
                    var id = evt.target.id.substring("all-assignments-".length);
                    selectOrDeselectAll(evt.target.checked, id);
                });
                $('input.target-assignment').click(function (evt) {
                    var id = $(evt.target).parents('form')[0].id.substring('study-site-'.length);
                    syncAllCheckbox(id);
                })
            });
        }(jQuery))
    </script>
</head>
<body>
<laf:box title="Reassign subjects managed by ${not empty command.responsible ? command.responsible.displayName : 'no one'}">
    <laf:division>
        <c:choose>
            <c:when test="${empty command.reassignables}">
                <p class="instructions">
                    <c:choose>
                        <c:when test="${empty command.responsible}">
                            All subjects have a responsible coordinator designated.
                        </c:when>
                        <c:otherwise>
                            This user does not have any schedules you may reassign.
                        </c:otherwise>
                    </c:choose>
                </p>
            </c:when>
            <c:otherwise>
                <p class="instructions">
                    Find the site and study for which you'd like to reassign subject schedules.
                    Then select the subjects you'd like to reassign and the coordinator you'd like
                    to assign them to.  N.b.: you can only change one site/study's worth at a time.
                </p>
            </c:otherwise>
        </c:choose>
        <c:if test="${not initialShow}">
            <tags:noform>
                <tags:errors/>
            </tags:noform>
        </c:if>
    </laf:division>
    <c:forEach items="${command.reassignables}" var="siteEntry">
        <laf:division title="${siteEntry.key.name}">
            <c:forEach items="${siteEntry.value}" var="studyEntry" varStatus="studyStatus">
                <form:form id="study-site-${studyEntry.value.studySite.id}"
                           cssClass="${commons:parity(studyStatus.index)} autoclear">
                    <div class="target-assignments" id="target-assignments-${studyEntry.value.studySite.id}">
                        <label class="all">
                            <input type="checkbox" class="all" id="all-assignments-${studyEntry.value.studySite.id}"/>
                            All for ${studyEntry.value.studySite.study.assignedIdentifier}
                        </label>
                        <c:forEach items="${studyEntry.value.assignments}" var="a">
                            <label>
                                <form:checkbox path="targetAssignments" value="${a.id}"
                                               cssClass="target-assignment"/>
                                ${a.subject.fullName}
                            </label>
                        </c:forEach>
                    </div>
                    <div class="new-responsible">
                        <c:choose>
                            <c:when test="${empty studyEntry.value.eligibleUsers}">
                                There are no other Study Subject Calendar Managers for
                                this site and study.
                            </c:when>
                            <c:otherwise>
                                <form:select path="newResponsible">
                                    <option></option>
                                    <form:options items="${studyEntry.value.eligibleUsers}"
                                                  itemValue="username" itemLabel="displayName"/>
                                </form:select>
                                <input type="submit" value="Change responsible user"/>
                                <c:if test="${not studyEntry.value.stillManageable}">
                                    <p class="instructions">
                                        Note: the current manager (${command.responsible.displayName})
                                        no longer has access to these subjects.  Please change them
                                        as soon as you can.
                                    </p>
                                </c:if>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </form:form>
            </c:forEach>
        </laf:division>
    </c:forEach>
</laf:box>
</body>
</html>