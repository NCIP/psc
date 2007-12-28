<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>

<html>
<head>
    <title>Assign subject</title>
    <tags:includeScriptaculous/>
    <style type="text/css">
        div.label {
            width: 35%;
        }
        div.submit {
            text-align: right;
        }
        form {
            width: 30em;
        }
        ul#population-checkboxes {
            margin: 0; padding: 0;
        }
        ul#population-checkboxes li {
            list-style-type: none
        }
    </style>
</head>
<body>
<laf:box title="Assign Subject">
    <c:if test="${not empty sites}">
        <laf:division>
            <p>
                Study: ${study.assignedIdentifier}
            </p>

            <c:url value="/pages/cal/assignSubject" var="action"/>
            <form:form method="post" action="${action}">
                <form:errors path="*"/>
                <input type="hidden" name="study" value="${study.id}"/>
                <div class="row">
                    <div class="label">
                        <form:label path="subject">Subject</form:label>
                    </div>
                    <div class="value">
                        <form:select path="subject">
                            <form:options items="${subjects}" itemLabel="fullName" itemValue="id"/>
                        </form:select>
                        <a href="<c:url value="/pages/cal/createSubject?id=${study.id}"/>">Create new subject</a>
                    </div>
                </div>
                    <div class="row">
                        <div class="label">
                            <form:label path="site">Site</form:label>
                        </div>
                        <div class="value">
                            <c:if test="${fn:length(sites) gt 1}">
                                <form:select path="site">
                                    <form:options items="${sites}"/>
                                </form:select>
                            </c:if>
                            <c:if test="${fn:length(sites) eq 1}">
                                <c:forEach items="${sites}" var="pair">
                                    ${pair.value}
                                    <input type="hidden" name="site" value="${pair.key.id}"/>
                                </c:forEach>
                            </c:if>
                        </div>
                    </div>

                <c:if test="${not empty studySegments}">
                    <div class="row">
                        <div class="label">
                            <form:label path="studySegment">Select studySegment for first epoch</form:label>
                        </div>
                        <div class="value">
                            <form:select path="studySegment">
                                <form:options items="${studySegments}" itemLabel="name" itemValue="id"/>
                            </form:select>
                        </div>
                    </div>
                </c:if>

                <div class="row">
                    <div class="label">
                        <form:label path="startDate">Start date of first epoch</form:label>
                    </div>
                    <div class="value">
                        <laf:dateInput path="startDate"/>
                    </div>
                </div>

                <c:if test="${not empty populations}">
                    <div class="row">
                        <div class="label">
                            Populations
                        </div>
                        <div class="value">
                            <ul id="population-checkboxes">
                            <c:forEach items="${populations}" var="pop">
                                <li><label>
                                    <form:checkbox path="populations" value="${pop.id}"/>
                                    ${pop.name}
                                </label></li>
                            </c:forEach>
                            </ul>
                        </div>
                    </div>
                </c:if>

                <div class="row">
                    <div class="submit">
                        <input type="submit" value="Assign"/>
                    </div>
                </div>
            </form:form>
        </laf:division>
    </c:if>
    <c:if test="${not empty unapprovedSites}">
        <h3>Unapproved sites</h3>
        <laf:division>
            <p>
                Please note:  the following sites are participating in the study, but the template
                has not yet been approved for them.  No subjects may be assigned until this happens.
                Please speak to a site coordinator if you have any questions.
            </p>
            <ul>
                <c:forEach items="${unapprovedSites}" var="site">
                    <li>${site.name}</li>
                </c:forEach>
            </ul>
        </laf:division>
    </c:if>
</laf:box>
</body>
</html>